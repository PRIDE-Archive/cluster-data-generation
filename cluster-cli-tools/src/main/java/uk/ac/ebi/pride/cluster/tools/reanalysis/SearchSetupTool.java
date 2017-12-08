package uk.ac.ebi.pride.cluster.tools.reanalysis;

import com.compomics.software.autoupdater.HeadlessFileDAO;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.IdentificationParameters;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.control.memory.MemoryWarningSystem;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.control.util.JarLookupService;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.control.util.ZipUtils;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.enums.AllowedSearchGUIParams;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.exception.ProcessingException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.exception.UnspecifiedException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.processing.ProcessingStep;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.control.util.PipelineFileLocalDownloadingService;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.GlobalProcessingProperties;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.processing.processsteps.SearchGUIStep;
import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;

import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;

/**
 *
 * @author Kenneth Verheggen
 * @author Yasset Perez-Riverol
 */
public class SearchSetupTool extends ProcessingStep implements ICommandTool{

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(SearchSetupTool.class);

    /**
     * the temp folder for the entire processing
     */
    private File tempResources;

    /**
     * The Fasta Databases that would be used for search
     */
    private List<Path> fastaDatabases = new ArrayList<>();

    /**
     * Search Parameters File.
     */
    private Path paramFile = null;

    /**
     * The mgf Files that would be use to perform the search.
     */
    private List<Path> mgfFilesPaths = null;


    public IdentificationParameters updateAlgorithmSettings(SearchParameters searchParameters, File fasta) throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
        searchParameters.setFastaFile(fasta);
        SpeciesFactory.getInstance().initiate(new SearchGUIStep().getJar().getParentFile().getAbsolutePath());
        IdentificationParameters temp = new IdentificationParameters(searchParameters);
        return temp;
    }



    /**
     * This project run a set of mgf for a set of fasta files and get the corresponding results.
     * The input of the tool are the following:
     *  spectrum-files: comma separated mgf files
     *  fasta-files: comma separated fasta protein databases
     *  temp-directory: temporary directory where all the computations will be performed.
     *  search-parameters: Search parameter to research the data in PRIDE.
     * @param args
     */
    public static void main(String[] args) {
        SearchSetupTool tool = new SearchSetupTool();
        Options options = tool.initOptions();
        try {
            tool.runCommand(options, args);
        } catch (ClusterDataImporterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Init the options for the commandline.
     */
    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption("s", "spectrum-files", true, "Spectrum Files to be analyzed during the run, they should be coma separated");
        options.addOption("f", "fasta-files", true, "Databases to run the identification, they should be comma separated");
        options.addOption("t", "temp-directory", true, "The user can specified the temp folder, this parameters is mandatory. ");
        options.addOption("p", "search-parameters", true, "Search parameters provided by the user to perform the analysis");
        return options;

    }

    @Override
    public void runCommand(Options options, String[] args) throws ClusterDataImporterException {

        //parse the command
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if(!cmd.hasOption("s") || !cmd.hasOption("f") || !cmd.hasOption("t") || !cmd.hasOption("p")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("ant", options);
            }

            String[]  mgfFiles = cmd.getOptionValues("s");
            String[]  fastaFiles = cmd.getOptionValues("f");
            String    tempDirectory= cmd.getOptionValue("t");
            String parametersFile = cmd.getOptionValue("p");

            // This method clean the temporary folder
            LOGGER.info("Cleaning previous results for the same project before copying the files in the temp Directory -- " + tempDirectory);
            cleanUp(tempDirectory);

            /**
             *   Copy a set of fasta files to temp folder. This is important because the tool in the search does
             *   indexing of those files and if we have multiple tool accessing to the same fasta, they can be corrupted.
             */
            LOGGER.info("Copying the Fasta File into a Temp Directory -- " + tempDirectory);
            copyFastaFilesToTempFolder(fastaFiles, parametersFile);

            /**
             * Init parameters for searching, this means that we need to inject into the parameters file
             * the corresponding path of the fasta file.
             */
            LOGGER.info("Modified the Param file with the corresponding Fasta File -- " + tempDirectory);
            modifiedParamFileWithFasta();

            /**
             * Copy the mgf files to temp folder. This is important because every tool change the original files
             * for example the mgfs are splited using specific sizes.
             */
            LOGGER.info("Copy the mgf to the temp Directory -- " + tempDirectory);
            copyMGFFilesToTempFolder(mgfFiles);


        }catch(ParseException | ClusterDataImporterException e){
            LOGGER.error("The reanalysis pipeline has fail to reanalyze the dataset.. " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
        }
    }

    /**
     * Copy the mgf to the temp folder. This is important because the tools that works with those files
     * change the mgf, then they needs to be written in the temp folder.
     * @param mgfFiles mgfFiles
     * @throws ClusterDataImporterException Cluster data import error.
     */
    private void copyMGFFilesToTempFolder(String[] mgfFiles) throws ClusterDataImporterException {
        try {
            for (String mgfFileName : mgfFiles) {
                File mgfFile = new File(mgfFileName);
                if (tempResources != null) {
                    Path mgfPath = Files.copy(mgfFile.toPath(), tempResources.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    mgfFilesPaths.add(mgfPath);
                    LOGGER.info("The mgf file -- " + mgfFileName + " has been copy to the temp folder -- " + tempResources);
                } else {
                    throw new ClusterDataImporterException("Error copying the fasta files the the temp directory -- ", new IOException());
                }
            }
        }catch (IOException ex){
            throw new ClusterDataImporterException("Error copying the mgf files the the temp directory -- ", new IOException());
        }
    }

    /**
     * This method used the current List of the fasta Files and modified the parameters file.
     */
    private void modifiedParamFileWithFasta() throws ClusterDataImporterException {

        SearchParameters sparameters;
        try {
            sparameters = SearchParameters.getIdentificationParameters(paramFile.toFile());
            IdentificationParameters updatedIdentificationParameters = null;
            for(Path databasePath: fastaDatabases){
               updatedIdentificationParameters  = updateAlgorithmSettings(sparameters, databasePath.toFile());
            }
            IdentificationParameters.saveIdentificationParameters(updatedIdentificationParameters, paramFile.toFile());

        } catch (IOException | ClassNotFoundException | URISyntaxException | XMLStreamException | UnspecifiedException e) {
            throw new ClusterDataImporterException("The parameters file was not found in the temp folder, then it can not be modfied -- " + paramFile, e);
        }
    }

    /**
     * Copy fasta files from original path into temp folder.
     * @param fastaFiles fasta files
     * @throws ClusterDataImporterException Error copying the files
     */
    private void copyFastaFilesToTempFolder(String[] fastaFiles, String parametersFile) throws ClusterDataImporterException {
        try {
            for (String fastaName : fastaFiles) {
                File fastaFile = new File(fastaName);
                if (tempResources != null) {
                    Path database = Files.copy(fastaFile.toPath(), tempResources.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    fastaDatabases.add(database);
                    LOGGER.info("The fasta fille -- " + fastaName + " has been copy to the temp folder -- " + tempResources);
                } else {
                    throw new ClusterDataImporterException("Error copying the fasta files the the temp directory -- ", new IOException());
                }
            }
        }catch (IOException ex){
            throw new ClusterDataImporterException("Error copying the fasta files the the temp directory -- ", new IOException());
        }

        // The parametersFile is also copy into temp folder because they will be updated by the pipeline.
        try{
            File paramFile = new File(parametersFile);
            this.paramFile = Files.copy(paramFile.toPath(), tempResources.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException ex){
            throw new ClusterDataImporterException("Error copying the parameters file into the temp folder" , ex);
        }
    }


    /**
     * This method create or clean an existing temp folder before the reanalysis.
     * @param tempDirectoryName temporary folder
     * @throws ClusterDataImporterException Exception if the folder can't be deleted.
     */
    private void cleanUp(String tempDirectoryName) throws ClusterDataImporterException {
        tempResources  = new File(tempDirectoryName);
        if (tempResources.exists()) {
            LOGGER.info("The temp folder -- " + tempDirectoryName + " already exists -- cleaning process will start");
            for (File aFile : tempResources.listFiles()) {
                if (aFile.exists()) {
                    LOGGER.info("Deleting the following file -- " + aFile.getAbsoluteFile());
                    if (aFile.isFile()) {
                        aFile.delete();
                    } else {
                        try {
                            FileUtils.deleteDirectory(aFile);
                        } catch (IOException ex) {
                            throw new ClusterDataImporterException("Error creating/cleaning the information from temp folder --" + tempDirectoryName, ex);
                        }
                    }
                }
            }
        } else {
            tempResources.mkdirs();
            LOGGER.info("The temp folder -- " + tempDirectoryName + " do not exist -- a new folder has been created");
        }
    }


    private List<String> constructArguments() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
        //finds the searchGUI jar in the specified folder, if it's not there, download it
        File searchGuiJar = getJar();
        //create a searchGUI commandline using the provided parameters
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        cmdArgs.add("-cp");
        cmdArgs.add(searchGuiJar.getAbsolutePath());
        cmdArgs.add("eu.isas.searchgui.cmd.SearchCLI");
        if (!parameters.containsKey("output_data")) {

        }
        //checks if we are not missing mandatory parameters
        for (AllowedSearchGUIParams aParameter : AllowedSearchGUIParams.values()) {
            if (parameters.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(parameters.get(aParameter.getId()));
            } else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            }
        }
        return cmdArgs;
    }

    @Override
    public boolean process() throws ProcessingException, UnspecifiedException {
        try {
            File parameterFile = new File(parameters.get("id_params"));
            File fastaFile = new File(parameters.get("fasta_file"));
            File real_outputFolder = new File(parameters.get("output_folder"));

            //update the fasta here if the search setup step was not run before
            if (!parameters.containsKey("search_setup_done")) {
                SearchSetupTool searchSetupTool = new SearchSetupTool();
                searchSetupTool.setParameters(parameters);
                searchSetupTool.LoadFasta(fastaFile.getAbsolutePath());
                parameters.put("search_setup_done", "true");
            }

            if (GlobalProcessingProperties.TEMP_FOLDER_SEARCHGUI.exists()) {
                GlobalProcessingProperties.TEMP_FOLDER_SEARCHGUI.delete();
            }
            GlobalProcessingProperties.TEMP_FOLDER_SEARCHGUI.mkdirs();

            LOGGER.info("Starting SearchGUI...");

            parameters.put("output_folder", GlobalProcessingProperties.TEMP_FOLDER_SEARCHGUI.getAbsolutePath());
            startProcess(getJar(), constructArguments());
            //storing intermediate results
            LOGGER.debug("Storing results in " + real_outputFolder);
            real_outputFolder.mkdirs();
            File outputFile = new File(real_outputFolder, "searchgui_out.zip");
            File tempOutput = new File(GlobalProcessingProperties.TEMP_FOLDER_SEARCHGUI, "searchgui_out.zip");
            //copy as a stream?
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            try (FileChannel source = new FileInputStream(tempOutput).getChannel();
                 FileChannel destination = new FileOutputStream(outputFile).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }
            //  FileUtils.copyDirectory(temp_searchGUI_output, real_outputFolder);
            //in case of future peptideShaker searches :
            parameters.put("identification_files", GlobalProcessingProperties.TEMP_FOLDER_SEARCHGUI.getAbsolutePath());
            parameters.put("out", real_outputFolder.getAbsolutePath() + "/" + parameterFile.getName() + ".cps");
            parameters.put("output_folder", real_outputFolder.getAbsolutePath());
        } catch (IOException |
                ClassNotFoundException |
                XMLStreamException |
                URISyntaxException |
                UnspecifiedException ex) {
            throw new ProcessingException(ex);
        }
        return true;
    }



}
