package uk.ac.eb.pride.cluster.reanalysis.processsteps;


import org.apache.commons.io.FileUtils;
import uk.ac.eb.pride.cluster.reanalysis.checkpoints.PeptideShakerReportCheckPoints;
import uk.ac.eb.pride.cluster.reanalysis.control.engine.callback.CallbackNotifier;
import uk.ac.eb.pride.cluster.reanalysis.control.util.ZipUtils;
import uk.ac.eb.pride.cluster.reanalysis.model.enums.AllowedPeptideShakerFollowUpParams;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.PladipusProcessingException;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.UnspecifiedPladipusException;
import uk.ac.eb.pride.cluster.reanalysis.model.feedback.Checkpoint;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerFollowUpStep extends PeptideShakerStep {

    private File real_output_folder;
    private static final File temp_peptideshaker_output = new File(System.getProperty("user.home") + "/pladipus/temp/search/PeptideShaker/mgf");

    public PeptideShakerFollowUpStep() {

    }

    private List<String> constructArguments() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedPladipusException {
        if (temp_peptideshaker_output.exists()) {
            FileUtils.deleteDirectory(temp_peptideshaker_output);
        }
        temp_peptideshaker_output.mkdirs();
        real_output_folder = new File(parameters.get("output_folder"));
        File peptideShakerJar = getJar();
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideShakerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.FollowUpCLI");
        //construct the cmd
        for (AllowedPeptideShakerFollowUpParams aParameter : AllowedPeptideShakerFollowUpParams.values()) {
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
    public boolean doAction() throws PladipusProcessingException, UnspecifiedPladipusException {
        try {
            List<String> constructArguments = constructArguments();
            File peptideShakerJar = getJar();
            //add callback notifier for more detailed printouts of the processing
            CallbackNotifier callbackNotifier = getCallbackNotifier();
            for (PeptideShakerReportCheckPoints aCheckPoint : PeptideShakerReportCheckPoints.values()) {
                callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(), aCheckPoint.getFeedback()));
            }
            startProcess(peptideShakerJar, constructArguments);
            //run peptideShaker with the existing files
            cleanupAndSave();
            return true;
        } catch (IOException | XMLStreamException | URISyntaxException ex) {
            throw new PladipusProcessingException(ex);
        }
    }

    private void cleanupAndSave() throws IOException {
        //parameters.put("out",real_output_file.getAbsolutePath());
        //copy as a stream?
        if (!real_output_folder.exists()) {
            real_output_folder.mkdirs();
        }
        for (File anMGF : temp_peptideshaker_output.listFiles()) {
            File zipMGF = new File(anMGF.getAbsolutePath() + ".zip");
            ZipUtils.zipFile(anMGF, zipMGF);
            File real_output_file = new File(real_output_folder, zipMGF.getName());
            real_output_file.createNewFile();
            System.out.println("Copying " + zipMGF.getAbsolutePath() + " to " + real_output_file.getAbsolutePath());
            try (FileChannel source = new FileInputStream(zipMGF).getChannel();
                    FileChannel destination = new FileOutputStream(real_output_file).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }
            //delete the local one
            anMGF.delete();
            zipMGF.delete();
        }
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker FollowUpCLI";
    }
}
