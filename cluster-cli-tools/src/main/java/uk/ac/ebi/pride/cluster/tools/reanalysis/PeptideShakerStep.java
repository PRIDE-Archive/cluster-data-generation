package uk.ac.ebi.pride.cluster.tools.reanalysis;

import com.compomics.software.autoupdater.HeadlessFileDAO;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.control.util.JarLookupService;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.enums.AllowedPeptideShakerFollowUpParams;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.enums.AllowedPeptideShakerParams;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.exception.ProcessingException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.exception.UnspecifiedException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.model.processing.ProcessingStep;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.control.memory.MemoryWarningSystem;
import uk.ac.ebi.pride.cluster.tools.reanalysis.reanalysis.control.util.Utilities;
import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerStep extends ProcessingStep {
    @Override
    public boolean process() throws UnspecifiedException, ProcessingException, ClusterDataImporterException {
        return false;
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
