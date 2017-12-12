package uk.ac.ebi.pride.cluster.tools.reanalysis;

import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.preferences.IdentificationParameters;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.tools.reanalysis.memory.MemoryWarningSystem;
import uk.ac.ebi.pride.cluster.tools.reanalysis.enums.AllowedSearchGUIParams;
import uk.ac.ebi.pride.cluster.tools.reanalysis.exception.UnspecifiedException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.processing.ProcessingStep;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumAppenderIntCharge;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;


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
    private List<Path> mgfFilesPaths = new ArrayList<>();

    /**
     * Property Tools define some of the static parameters about who to run the parameters
     */
    private Properties toolProperties = new Properties();

    /**
     * Search GUI Tool path. This path is used globally to
     */
    private File searchGuiJar;



    /**
     * Default constructor initialize the tool properties file.
     */
    private SearchSetupTool(){
        try {
            InputStream propertyFile = this.getClass().getClassLoader().getResourceAsStream("tool.properties");
            toolProperties.load(propertyFile);
            parameters = new HashMap<>();
        } catch (IOException e) {
            LOGGER.info("Error reading the Default property parameters for this tool -- " + SearchSetupTool.class);
            e.printStackTrace();
        }
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

            String[]  mgfFiles      = cmd.getOptionValue("s").split(",");
            String[]  fastaFiles    = cmd.getOptionValue("f").split(",");
            String    tempDirectory = cmd.getOptionValue("t");
            String parametersFile   = cmd.getOptionValue("p");

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

            LOGGER.info("Set Parameters name of the Search parameters File -- " + paramFile.toString());
            setParametersName();

            process();

        }catch(ParseException | ClusterDataImporterException e){
            LOGGER.error("The reanalysis pipeline has fail to reanalyze the dataset.. " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
        }
    }

    private void setParametersName() throws ClusterDataImporterException {
        SearchParameters sParameters;
        try {
            sParameters = SearchParameters.getIdentificationParameters(paramFile.toFile());
            IdentificationParameters idParameters = new IdentificationParameters(sParameters);
            idParameters.setName("Default Parameters from PRIDE Predictor");
            IdentificationParameters.saveIdentificationParameters(idParameters, paramFile.toFile());
        } catch (IOException | ClassNotFoundException  e) {
            throw new ClusterDataImporterException("The parameters file was not found in the temp folder, then it can not be modfied -- " + paramFile, e);
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
                File outputFile = new File(tempResources.getAbsolutePath(), mgfFile.getName());
                if (copyMGFWithCorrectChargeSearchGUI(mgfFile, outputFile)) {
                    mgfFilesPaths.add(outputFile.toPath());
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
     * Copy the spectra from the exported files into the spectra to be search. Here we are adding
     * some corrections to the spectra needed for the reprocessing by the search engines.
     * @param mgfFile mgfFile
     * @param outputFile Outpuf MGF
     * @return
     * @throws IOException
     */
    private boolean copyMGFWithCorrectChargeSearchGUI(File mgfFile, File outputFile) throws IOException {

        ISpectrum[] spectra = ParserUtilities.readMGFScans(mgfFile);
        FileWriter outputFileAppend = new FileWriter(outputFile, true);
        for(ISpectrum spectrum: spectra)
            MGFSpectrumAppenderIntCharge.INSTANCE.appendSpectrum(outputFileAppend, spectrum);

        return true;
    }

    /**
     * This method used the current List of the fasta Files and modified the parameters file.
     */
    private void modifiedParamFileWithFasta() throws ClusterDataImporterException {

        SearchParameters sParameters;
        try {
            sParameters = SearchParameters.getIdentificationParameters(paramFile.toFile());
            IdentificationParameters updatedIdentificationParameters = null;
            for(Path databasePath: fastaDatabases){
               updatedIdentificationParameters  = updateAlgorithmSettingsWithFasta(sParameters, databasePath.toFile());
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
                String fastaFileName = fastaFile.getName();
                if (tempResources != null) {
                    File outpufFile = new File(tempResources.getAbsolutePath(), fastaFileName);
                    Path database = Files.copy(fastaFile.toPath(), outpufFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    fastaDatabases.add(database);
                    LOGGER.info("The fasta filled -- " + fastaName + " has been copy to the temp folder -- " + tempResources);
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
            File outputParam = new File(tempResources.getAbsolutePath(), paramFile.getName());
            this.paramFile = Files.copy(paramFile.toPath(), outputParam.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
        if (tempResources.exists() && tempResources.isDirectory()) {
            LOGGER.info("The temp folder -- " + tempDirectoryName + " already exists -- cleaning process will start");
            if(tempResources.listFiles() != null && tempResources.list().length > 0){
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
            }
        } else if(!tempResources.isDirectory()){
            tempResources.deleteOnExit();
            tempResources.mkdirs();
        }else {
            tempResources.mkdirs();
            LOGGER.info("The temp folder -- " + tempDirectoryName + " do not exist -- a new folder has been created");
        }
    }

    /**
     * Construct the Arguments for the tool, this method returns the tool and the corresponding parameters for
     * the search tool.
     * @return
     * @throws IOException
     * @throws XMLStreamException
     * @throws URISyntaxException
     * @throws UnspecifiedException
     */
    private List<String> constructArguments() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
        // This is a little bit hard code but it should be seen better in the future for deploy.

        this.searchGuiJar = new File(toolProperties.getProperty("searchgui.path"), toolProperties.getProperty("searchgui.tool") + "-" + toolProperties.getProperty("searchgui.version") + ".jar");
        //create a searchGUI commandline using the provided parameters
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        cmdArgs.add("-cp");
        cmdArgs.add(searchGuiJar.getAbsolutePath());
        cmdArgs.add("eu.isas.searchgui.cmd.SearchCLI");
        //checks if we are not missing mandatory parameters
        for (AllowedSearchGUIParams aParameter : AllowedSearchGUIParams.values()) {
            // Set the parameters for the Search Tool using default parameters.
            if (toolProperties.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(toolProperties.getProperty(aParameter.getId()));
            } else if(aParameter.equals(AllowedSearchGUIParams.SPECTRUM_FILES)) {
                StringBuilder mgfBuild = new StringBuilder("");
                for (Path filePath : mgfFilesPaths) {
                    mgfBuild.append(filePath.toAbsolutePath().toString()).append(", ");
                }
                String mgfFiles = mgfBuild.toString().substring(0, mgfBuild.toString().length() - 2);
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(mgfFiles);
            } else if(aParameter.equals(AllowedSearchGUIParams.IDENTIFICATION_PARAMETERS)){
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(paramFile.toAbsolutePath().toString());
            } else if(aParameter.equals(AllowedSearchGUIParams.OUTPUT_FOLDER)){
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(tempResources.getAbsolutePath());
            }else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            }
        }

        return cmdArgs;
    }

    @Override
    public boolean process() throws ClusterDataImporterException {

        try {
            LOGGER.info("starting the process of data research with SearchGUI Tool in temp folder -- " + tempResources);
            List<String> cmdParams = constructArguments();
            Process process = startProcess(tempResources, cmdParams);
            process.waitFor();

            LOGGER.debug("Storing results in temp directory --- " + tempResources);
            File outputFile = new File(tempResources, "searchgui_out.zip");
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
        } catch (IOException | XMLStreamException | URISyntaxException |  UnspecifiedException ex) {
            throw new ClusterDataImporterException("Error performing the Search with SeachGUI -- ", ex);
        } catch (InterruptedException ex) {
            throw new ClusterDataImporterException("Execution of the SearchGUI subprocess has fail -- ", ex);
        }
        return true;
    }

    /**
     * Update the parameters file with the new Fasta File
     * @param searchParameters
     * @param fasta
     * @return
     * @throws IOException
     * @throws XMLStreamException
     * @throws URISyntaxException
     * @throws UnspecifiedException
     */
    public IdentificationParameters updateAlgorithmSettingsWithFasta(SearchParameters searchParameters, File fasta) throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
        searchParameters.setFastaFile(fasta);
        IdentificationParameters newParameters = new IdentificationParameters(searchParameters);
        return newParameters;
    }




}
