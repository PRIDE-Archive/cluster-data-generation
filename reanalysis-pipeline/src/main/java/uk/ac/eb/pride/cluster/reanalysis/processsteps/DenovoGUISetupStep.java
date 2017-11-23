
package uk.ac.eb.pride.cluster.reanalysis.processsteps;


import org.apache.commons.io.FileUtils;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.UnspecifiedException;
import uk.ac.eb.pride.cluster.reanalysis.model.processing.ProcessingStep;
import uk.ac.eb.pride.cluster.reanalysis.util.PladipusFileDownloadingService;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Kenneth Verheggen
 */
public class DenovoGUISetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;

    public DenovoGUISetupStep() {
        tempResources = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/SearchGUI/resources");
    }

    @Override
    public boolean doAction() throws UnspecifiedException {
        System.out.println("Running " + this.getClass().getName());
        try {
        if (tempResources.exists()) {
            for (File aFile : tempResources.listFiles()) {
                if (aFile.exists()) {
                    if (aFile.isFile()) {
                        aFile.delete();
                    } else {
                        FileUtils.deleteDirectory(aFile);
                    }
                }
            }
        } else {
            tempResources.mkdirs();
        }
        initialiseInputFiles();
    } catch (IOException e){
           throw new UnspecifiedException(e);
        }
        return true;
    }

    private void initialiseInputFiles() throws IOException {
        //original
        String inputPath = parameters.get("spectrum_files");
        String paramPath = parameters.get("id_params");

        if (inputPath.toLowerCase().endsWith(".mgf")) {
            parameters.put("spectrum_files", PladipusFileDownloadingService.downloadFile(inputPath, tempResources).getAbsolutePath());
        } else {
            parameters.put("spectrum_files", PladipusFileDownloadingService.downloadFile(inputPath, tempResources).getAbsolutePath());
        }

        parameters.put("id_params", PladipusFileDownloadingService.downloadFile(paramPath, tempResources).getAbsolutePath());

        //output
        File outputFolder = new File(parameters.get("output_folder") + "/" + parameters.get("title"));
        outputFolder.mkdirs();
        parameters.put("output_folder", outputFolder.getAbsolutePath());
    }

    @Override
    public String getDescription() {
        return "Initialisation of the search process";
    }

}
