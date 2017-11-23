/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.eb.pride.cluster.reanalysis.util;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class CLIRunner {

    private static final Logger LOGGER = Logger.getLogger(CLIRunner.class);


    public static void BuildIntegrationCommandLine(String[] args) {

        DefaultParser parser = new DefaultParser();

        //parse the command line
        try {
            CommandLine line = parser.parse(GetOptions(), args);
            String fasta = null;
            File outputFolder = null;
            boolean isValidCLI = true;
            boolean runPeptideShaker = line.hasOption("peptideshaker");
            if (line.hasOption("fasta")) {
                fasta = line.getOptionValue("fasta");
            } else {
                LOGGER.error("Fasta is a mandatory parameter.");
                isValidCLI = false;
            }

            if (line.hasOption("out")) {
                outputFolder = new File(line.getOptionValue("out"));
                if (!outputFolder.exists()) {
                    outputFolder.mkdirs();
                } else if (!outputFolder.isDirectory()) {
                    LOGGER.error("Output folder exists but is not a directory !");
                    isValidCLI = false;
                }
            }

            String spectrum_files = null;
            String id_params = null;
            if (line.hasOption("spectrum_files")) {
                spectrum_files = line.getOptionValue("spectrum_files");
            } else {
                LOGGER.error("spectrum_files is a mandatory parameter in case an assay identifier is not provided.");
                isValidCLI = false;
            }
            if (line.hasOption("id_params")) {
                id_params = line.getOptionValue("id_params");
            } else {
                LOGGER.error("id_params is a mandatory parameter in case an assay identifier is not provided.");
                isValidCLI = false;
            }
            if (isValidCLI) {
                IntegrationFromFile.run(spectrum_files, id_params, fasta, outputFolder, runPeptideShaker);
            }

        } catch (ParseException ex) {
            LOGGER.error(ex);
        }
    }

    public static void main(String[] args) {
        BuildIntegrationCommandLine(args);
    }

    private static Options GetOptions() {
        Options options = new Options();
        options.addOption("help", false, "Help message on the usage of the commandline");
        options.addOption("fasta", true, "The sequence database to be searched");
        options.addOption("out", true, "The output folder for the results");
        options.addOption("peptideshaker", false, "Indicating if PeptideShaker needs to be run");
        options.addOption("id_params", true, "The compomics Identification Parameters file");
        options.addOption("spectrum_files", true, "The input spectrum files (MGF)");
        return options;
    }


}
