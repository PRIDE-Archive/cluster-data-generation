/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.eb.pride.cluster.reanalysis.util;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import uk.ac.eb.pride.cluster.reanalysis.control.util.ZipUtils;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.ProcessingException;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.UnspecifiedException;
import uk.ac.eb.pride.cluster.reanalysis.processsteps.PeptideShakerStep;
import uk.ac.eb.pride.cluster.reanalysis.processsteps.SearchGUIStep;
import uk.ac.eb.pride.cluster.reanalysis.processsteps.SearchSetupStep;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class IntegrationFromFile {

    public static final Logger LOGGER = Logger.getLogger(IntegrationFromFile.class);

    public static boolean cleanTempDirectory = true;
    public static boolean experimentFastaName = false;

    public static void run(String spectrum_files, String id_params, String fasta, File outputFolder, boolean peptideShaker) {
        run(spectrum_files, id_params, fasta, outputFolder, peptideShaker, true);
    }

    public static void runNext(String spectrum_files, String id_params, String fasta, File outputFolder, boolean peptideShaker) {
        run(spectrum_files, id_params, fasta, outputFolder, peptideShaker, false);
    }

    public static void run(String spectrum_files, String id_params, String fasta, File outputFolder, boolean peptideShaker, boolean performSetupStep) {

        HashMap<String, String> parameters = SearchProperties.getInstance().getParameters();
        try {
            if (!cleanTempDirectory) {
                parameters.put("skip_cleaning", "true");
            }
            if (experimentFastaName) {
                parameters.put("experiment", new File(fasta).getName().substring(0, 20));
            }
            parameters.put("spectrum_files", spectrum_files);
            parameters.put("id_params", id_params);
            parameters.put("fasta_file", fasta);
            parameters.put("output_folder", outputFolder.getAbsolutePath());
            File reports = new File(outputFolder, "reports");
            reports.mkdirs();
            parameters.put("out_reports", reports.getAbsolutePath());

            //setup
            if (performSetupStep) {
                SearchSetupStep setupStep = new SearchSetupStep();
                setupStep.setParameters(parameters);
                setupStep.doAction();
            }
            //search
            SearchGUIStep searchStep = new SearchGUIStep();
            searchStep.setParameters(parameters);
            searchStep.doAction();
            //peptideShaker
            if (peptideShaker) {
                PeptideShakerStep pepStep = new PeptideShakerStep();
                parameters.put("output_folder", outputFolder.getAbsolutePath());
                pepStep.setParameters(parameters);
                pepStep.doAction();
            }
            //do the steps in tandem?

        } catch (ProcessingException | UnspecifiedException ex) {
            ex.printStackTrace();
        } finally {

            File reportFolder = new File(parameters.get("out_reports"));
            if (reportFolder.exists()) {
                LOGGER.info("Zipping peptideshaker reports...");
                try {
                    File zippedFolder = new File(reportFolder.getAbsolutePath() + ".zip");
                    ZipUtils.zipFolder(reportFolder, zippedFolder);
                    for (File aFile : reportFolder.listFiles()) {
                        aFile.deleteOnExit();
                    }
                    if (zippedFolder.exists()) {
                        FileUtils.deleteQuietly(reportFolder);
                    }
                } catch (UnspecifiedException | IOException ex) {
                    LOGGER.error(ex);
                }
            }
        }

    }

}
