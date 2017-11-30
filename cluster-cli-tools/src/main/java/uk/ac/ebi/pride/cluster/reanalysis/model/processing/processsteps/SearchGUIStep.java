package uk.ac.ebi.pride.cluster.reanalysis.model.processing.processsteps;

import com.compomics.software.autoupdater.HeadlessFileDAO;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.reanalysis.control.util.JarLookupService;
import uk.ac.ebi.pride.cluster.reanalysis.model.enums.AllowedSearchGUIParams;
import uk.ac.ebi.pride.cluster.reanalysis.model.exception.ProcessingException;
import uk.ac.ebi.pride.cluster.reanalysis.model.exception.UnspecifiedException;
import uk.ac.ebi.pride.cluster.reanalysis.model.processing.ProcessingStep;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;

import uk.ac.ebi.pride.cluster.reanalysis.control.memory.MemoryWarningSystem;
import uk.ac.ebi.pride.cluster.reanalysis.model.GlobalProcessingProperties;

/**
 *
 * @author Kenneth Verheggen
 */
public class SearchGUIStep extends ProcessingStep {

    /**
     * The logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(SearchGUIStep.class);

    /**
     * Temporary output for processing
     */
    public SearchGUIStep() {

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
    public boolean doAction() throws ProcessingException, UnspecifiedException {
        try {
            File parameterFile = new File(parameters.get("id_params"));
            File fastaFile = new File(parameters.get("fasta_file"));
            File real_outputFolder = new File(parameters.get("output_folder"));

            //update the fasta here if the search setup step was not run before
            if (!parameters.containsKey("search_setup_done")) {
                SearchSetupStep searchSetupStep = new SearchSetupStep();
                searchSetupStep.setParameters(parameters);
                searchSetupStep.LoadFasta(fastaFile.getAbsolutePath());
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

    public File getJar() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
        //check if this is possible in another way...
        File toolFolder = GlobalProcessingProperties.TOOL_FOLDER;
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder, "SearchGUI");
        if (!temp.exists()) {
            LOGGER.info("Downloading latest SearchGUI version...");
            URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
            downloadLatestZipFromRepo(temp, "SearchGUI", "eu.isas.searchgui", "SearchGUI", null, null, jarRepository, false, false, new HeadlessFileDAO(), new WaitingHandlerCLIImpl());
        }
        return JarLookupService.lookupFile("SearchGUI-.*.jar", temp);
    }

    @Override
    public String getDescription() {
        return "Running SearchGUI";
    }

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }
}
