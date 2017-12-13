package uk.ac.ebi.pride.cluster.tools.reanalysis;

import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.preferences.IdentificationParameters;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.enums.AllowedPeptideShakerMzIdConversionParams;
import uk.ac.ebi.pride.cluster.tools.reanalysis.enums.AllowedPeptideShakerParams;
import uk.ac.ebi.pride.cluster.tools.reanalysis.enums.AllowedSearchGUIParams;
import uk.ac.ebi.pride.cluster.tools.reanalysis.exception.UnspecifiedException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.memory.MemoryWarningSystem;
import uk.ac.ebi.pride.cluster.tools.reanalysis.processing.ProcessingStep;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerStep extends ProcessingStep implements ICommandTool{

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(PeptideShakerStep.class);

    /**
     * the temp folder for the entire processing
     */
    private File searchGUIResultsFile;

    /**
     * Search Parameters File.
     */
    private File paramFile = null;

    /**
     * Property Tools define some of the static parameters about who to run the parameters
     */
    private Properties toolProperties = new Properties();

    /**
     * PeptideShaker Tool path. This path is used globally to
     */
    private File peptideSheckerJar;

    /**
     * The output of the mzId files
     */
    private File mzIDFile;


    /**
     * Default constructor initialize the tool properties file.
     */
    private PeptideShakerStep(){
        try {
            InputStream propertyFile = this.getClass().getClassLoader().getResourceAsStream("tool.properties");
            toolProperties.load(propertyFile);
            parameters = new HashMap<>();
        } catch (IOException e) {
            LOGGER.info("Error reading the Default property parameters for this tool -- " + PeptideShakerStep.class);
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
        PeptideShakerStep tool = new PeptideShakerStep();
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
        options.addOption("i", "identification-results", true,    "The user can specified full path to searchgui results ");
        options.addOption("p", "search-parameters", true, "Search parameters provided by the user to perform the analysis");
        options.addOption("o","mzid-output", true, "Absolute path to the mzid output file");
        return options;

    }

    @Override
    public void runCommand(Options options, String[] args) throws ClusterDataImporterException {

        //parse the command
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if(!cmd.hasOption("i") || !cmd.hasOption("p") || !cmd.hasOption("o")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("ant", options);
            }

            this.searchGUIResultsFile = new File(cmd.getOptionValue("i"));
            this.paramFile     = new File(cmd.getOptionValue("p"));
            this.mzIDFile = new File(cmd.getOptionValue("o"));

            if(!searchGUIResultsFile.exists() || !this.paramFile.exists()){
                LOGGER.info("The param files or SearchGUI results do not exists in the folloing paths -- " + searchGUIResultsFile.getAbsolutePath() + " -- " + paramFile.getAbsolutePath());
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("ant", options);
            }

            LOGGER.info("PeptideShaker starts the processing of the SearchGUI results -- " + paramFile.toString());
            process();

            LOGGER.info("Star the conversion to mzIdentML of the PeptideShaker results -- " + paramFile.toString());
            posProcessMZIdConversion();

        }catch(ParseException | ClusterDataImporterException e){
            LOGGER.error("The reanalysis pipeline has fail to reanalyze the dataset.. " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
        }
    }

    /**
     * This method convert the results from PeptideShaker to mzId
     */
    private boolean posProcessMZIdConversion() throws ClusterDataImporterException {

        try {
            LOGGER.info("starting the process of data research with SearchGUI Tool in temp folder -- " + searchGUIResultsFile);
            List<String> parameters = constructMzIdArguments();
            Process process = startProcess(searchGUIResultsFile.getParentFile().getAbsoluteFile(), parameters);
            process.waitFor();

            LOGGER.debug("Converting the PeptideShaker results to mzID File --- " + searchGUIResultsFile);
            if (!mzIDFile.exists()) {
                throw new ClusterDataImporterException("The Conversion to mzID hasn't success -- " + searchGUIResultsFile, new Exception());
            }
        } catch (IOException | XMLStreamException | URISyntaxException |  UnspecifiedException ex) {
            throw new ClusterDataImporterException("Error performing the Search with SeachGUI -- ", ex);
        } catch (InterruptedException ex) {
            throw new ClusterDataImporterException("Execution of the SearchGUI subprocess has fail -- ", ex);
        }
        return true;

    }

    private List<String> constructMzIdArguments() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
        // This is a little bit hard code but it should be seen better in the future for deploy.

        this.peptideSheckerJar = new File(toolProperties.getProperty("peptideshaker.path"), toolProperties.getProperty("peptideshaker.tool") + "-" + toolProperties.getProperty("peptideshaker.version") + ".jar");
        //create a searchGUI commandline using the provided parameters
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideSheckerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.MzidCLI");
        //checks if we are not missing mandatory parameters
        for (AllowedPeptideShakerMzIdConversionParams aParameter : AllowedPeptideShakerMzIdConversionParams.values()) {
            // Set the parameters for the Search Tool using default parameters.
            if (toolProperties.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(toolProperties.getProperty(aParameter.getId()));
            } else if(aParameter.equals(AllowedPeptideShakerMzIdConversionParams.INPUT_FILE)) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(searchGUIResultsFile.getParentFile().getAbsolutePath() + "/" + toolProperties.getProperty("peptideshaker_output_file"));
            } else if(aParameter.equals(AllowedPeptideShakerMzIdConversionParams.OUTPUT_FILE)){
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(this.mzIDFile.getAbsolutePath());
            } else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            }
        }

        return cmdArgs;
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

        this.peptideSheckerJar = new File(toolProperties.getProperty("peptideshaker.path"), toolProperties.getProperty("peptideshaker.tool") + "-" + toolProperties.getProperty("peptideshaker.version") + ".jar");
        //create a searchGUI commandline using the provided parameters
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideSheckerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.PeptideShakerCLI");
        //checks if we are not missing mandatory parameters
        for (AllowedPeptideShakerParams aParameter : AllowedPeptideShakerParams.values()) {
            // Set the parameters for the Search Tool using default parameters.
            if (toolProperties.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(toolProperties.getProperty(aParameter.getId()));
            } else if(aParameter.equals(AllowedPeptideShakerParams.IDENTIFICATION_PARAMETERS)) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(paramFile.getAbsolutePath());
            } else if(aParameter.equals(AllowedPeptideShakerParams.IDENTIFICATION_FILES)){
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(this.searchGUIResultsFile.getAbsolutePath());
            } else if(aParameter.equals(AllowedPeptideShakerParams.PEPTIDESHAKER_OUTPUT)){
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(searchGUIResultsFile.getParentFile().getAbsolutePath() + "/" + toolProperties.getProperty("peptideshaker_output_file"));
            }else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            }
        }

        return cmdArgs;
    }

    @Override
    public boolean process() throws ClusterDataImporterException {

        try {
            LOGGER.info("starting the process of data research with SearchGUI Tool in temp folder -- " + searchGUIResultsFile);
            List<String> cmdParams = constructArguments();
            Process process = startProcess(searchGUIResultsFile.getParentFile().getAbsoluteFile(), cmdParams);
            process.waitFor();

            LOGGER.debug("Storing results in temp directory --- " + searchGUIResultsFile);
            File outputFile = new File(searchGUIResultsFile.getParentFile().getAbsolutePath() + "/" + toolProperties.getProperty("peptideshaker_output_file"));
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


//    private static final Logger LOGGER = Logger.getLogger(PeptideShakerStep.class);
//
//    private File temp_peptideshaker_cps;
//
//    public PeptideShakerStep() {
//
//    }
//
//    private List<String> constructArguments() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
//        File peptideShakerJar = getJar();
//        ArrayList<String> cmdArgs = new ArrayList<>();
//        cmdArgs.add("java");
//        cmdArgs.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
//        cmdArgs.add("-cp");
//        cmdArgs.add(peptideShakerJar.getAbsolutePath());
//        cmdArgs.add("eu.isas.peptideshaker.cmd.PeptideShakerCLI");
//        //check if folder exists
//        File outputFolder = new File(parameters.get("output_folder"));
//        outputFolder.mkdirs();
////check if reports are requested
//        if (parameters.containsKey("reports")) {
//            File outputReportFolder = new File(outputFolder, "reports");
//            outputReportFolder.mkdirs();
//            parameters.put("out_reports", outputReportFolder.getAbsolutePath());
//
//        }
//
//        for (AllowedPeptideShakerParams aParameter : AllowedPeptideShakerParams.values()) {
//            if (parameters.containsKey(aParameter.getId())) {
//                cmdArgs.add("-" + aParameter.getId());
//                cmdArgs.add(parameters.get(aParameter.getId()));
//            } else if (aParameter.isMandatory()) {
//                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
//            }
//        }
//        //check if spectra need to be exported
//        if (parameters.containsKey("spectrum_folder")) {
//            File exportFolder = new File(parameters.get("spectrum_folder"));
//            exportFolder.mkdirs();
//        }
//        //also add these for other possible CLI's?
//        for (AllowedPeptideShakerFollowUpParams aParameter : AllowedPeptideShakerFollowUpParams.values()) {
//            if (parameters.containsKey(aParameter.getId())) {
//                cmdArgs.add("-" + aParameter.getId());
//                cmdArgs.add(parameters.get(aParameter.getId()));
//            }
//        }
//        //temp solution
//        // if (parameters.containsKey("out_reports") && parameters.containsKey("reports")) {
//        System.out.println("Adding reports to command line...");
//        cmdArgs.add("-out_reports");
//        cmdArgs.add(parameters.get("out_reports"));
//        cmdArgs.add("-reports");
//        cmdArgs.add(parameters.get("reports"));
//        //   }
//        /*
//        for (Map.Entry<String,String> aParameter:parameters.entrySet()){
//            cmdArgs.add("-" + aParameter.getKey());
//            cmdArgs.add(parameters.get(aParameter.getValue()));
//
//        }*/
//
//        return cmdArgs;
//    }
//
//    @Override
//    public boolean process() throws ProcessingException, UnspecifiedException {
//        try {
//            LOGGER.info("Running Peptide Shaker");
//            //aqcuire the peptide shaker jar
//            File peptideShakerJar = getJar();
//            //temporary store the end output location
//            File realOutput = new File(parameters.get("output_folder"));
//            //check if we are not missing any
//            cleanUp();
//            String experiment = "output";
//
//            if (parameters.containsKey("experiment")) {
//                experiment = parameters.get("experiment");
//            }
//
//            String sample = "respin";
//            if (parameters.containsKey("sample")) {
//                sample = parameters.get("sample");
//            }
//
//            String replicate = "0";
//            if (parameters.containsKey("replicate")) {
//                replicate = parameters.get("replicate");
//            }
//
//            File temporaryOutput = new File(GlobalProcessingProperties.TEMP_FOLDER_PEPTIDESHAKER, realOutput.getName());
//            temporaryOutput.mkdirs();
//            if (parameters.containsKey("output_folder")) {
//                temp_peptideshaker_cps = new File(temporaryOutput, experiment + "_" + sample + "_" + replicate + ".cpsx");
//                parameters.put("out", temp_peptideshaker_cps.getAbsolutePath());
//            }
//            //generate parameters
//            List<String> constructArguments = constructArguments();
//            //start processing with peptideshaker
//            startProcess(peptideShakerJar, constructArguments);
//
//
//            //if it is required, move the cps file to the "real" output folder
//            if (parameters.containsKey("save_cps")) {
//                Utilities.copyFile(temp_peptideshaker_cps, new File(realOutput, temp_peptideshaker_cps.getName()));
//            }
//            //delete the local temporary output
//            FileUtils.deleteDirectory(temporaryOutput);
//            return true;
//        } catch (IOException | XMLStreamException | URISyntaxException ex) {
//            throw new ProcessingException(ex);
//        } catch (Exception ex) {
//            throw new UnspecifiedException(ex);
//        }
//    }
//
//    public File getJar() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
//        File peptideShakerToolFolder = GlobalProcessingProperties.TOOL_FOLDER_PEPTIDESHAKER;
//        if (!peptideShakerToolFolder.exists()) {
//            LOGGER.info("Downloading latest PeptideShaker version...");
//            URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
//            downloadLatestZipFromRepo(peptideShakerToolFolder, "PeptideShaker", "eu.isas.peptideshaker", "PeptideShaker", null, null, jarRepository, false, false, new HeadlessFileDAO(), new WaitingHandlerCLIImpl());
//        }
//        return JarLookupService.lookupFile("PeptideShaker-.*.jar", peptideShakerToolFolder);
//    }
//
//    @Override
//    public String getDescription() {
//        return "Running PeptideShaker";
//    }
//
//    void cleanUp() {
//        if (GlobalProcessingProperties.TEMP_FOLDER_PEPTIDESHAKER.exists()) {
//            for (File aFile : GlobalProcessingProperties.TEMP_FOLDER_PEPTIDESHAKER.listFiles()) {
//                try {
//                    if (!aFile.isDirectory()) {
//                        aFile.delete();
//                    } else {
//                        FileUtils.deleteDirectory(aFile);
//                    }
//                } catch (Exception e) {
//                    LOGGER.warn(e);
//                }
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        ProcessingStep.main(args);
//    }
}
