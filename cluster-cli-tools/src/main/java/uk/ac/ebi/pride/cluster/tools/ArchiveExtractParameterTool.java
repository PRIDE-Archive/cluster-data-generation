package uk.ac.ebi.pride.cluster.tools;


import com.compomics.pridesearchparameterextractor.cmd.PrideSearchparameterExtractor;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.data.exception.SubmissionFileException;
import uk.ac.ebi.pride.data.io.SubmissionFileParser;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.data.util.MassSpecFileFormat;
import uk.ac.ebi.pride.spectracluster.utilities.FileTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 21/11/2017.
 */
public class ArchiveExtractParameterTool {

    private static final Logger LOGGER = Logger.getLogger(PrideSearchparameterExtractor.class);

    public static void main(String[] args) {
        //init log4J
        //   BasicConfigurator.configure();
        // create Options object
        Options options = initOptions();
        //parse the command
        CommandLineParser parser = new BasicParser();

        try {
            if (args.length == 0) {
                help(options);
            } else {
                CommandLine cmd = parser.parse(options, args);
                handleCommand(cmd, options);
            }
        } catch (FileNotFoundException | ParseException | SubmissionFileException ex) {
            LOGGER.error(ex);
        }
    }

    private static Options initOptions() {
        Options options = new Options();
        options.addOption("i", "input-folder",true, "Project Folder in PRIDE, (e.g /nfs/pride/prod/archive/2017/11/PXD007710)  ");
        options.addOption("o", "output-folder", true, "The output folder");
        options.addOption("s", "split-assay", false, "Split the output into Project Folders, <PXD00XXXXX>/PXD-AssayID");
        return options;
    }

    private static void handleCommand(CommandLine cmd, Options options) throws FileNotFoundException, SubmissionFileException {

        String type = "";
        File input = null;
        File output = null;
        List<File> peakFiles = null;
        boolean saveMGF = false;

        if(!(cmd.hasOption("i") || !cmd.hasOption("o"))){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "ant", options );
        }

        String inputProjectFolder = cmd.getOptionValue("i");
        String inputOutputFolder  = cmd.getOptionValue("o");

        File projectInternalPath = new File(inputProjectFolder, FileTypes.INTERNAL_DIRECTORY);

        Submission submission = SubmissionFileParser.parse(new File(projectInternalPath, FileTypes.SUBMISSION_FILE));

        submission.getDataFiles().stream().filter( file -> file.getFileFormat() == MassSpecFileFormat.MZIDENTML).forEach(
                file -> {
                    System.out.println();
                }
        );
//
//            if (!cmd.hasOption("type")) {
//                LOGGER.error("The type needs to be specified in the command !");
//                help(options);
//            } else if (cmd.getOptionValue("type").equalsIgnoreCase("mzid") && !cmd.hasOption("peak_files")) {
//                throw new IllegalArgumentException("peak_files argument is mandatory in the case of mzid extraction");
//            } else {
//                type = cmd.getOptionValue("type");
//                //Get the input files required
//                input = new File(cmd.getOptionValue("in"));
//                if (!input.exists()) {
//                    throw new FileNotFoundException(input.getAbsolutePath() + " does not exist !");
//                } else if (type.equalsIgnoreCase("mzid")) {
//                    //if the type is mzID we need to also get the peakfiles...
//                    peakFiles = new ArrayList<>();
//                    String[] peakFilesArg = cmd.getOptionValue("peak_files").split(",");
//                    for (String peakFileArg : peakFilesArg) {
//                        File peakFile = new File(peakFileArg.trim());
//                        if (!peakFile.exists()) {
//                            throw new FileNotFoundException(peakFile.getAbsolutePath() + " does not exist !");
//                        } else {
//                            peakFiles.add(peakFile);
//                        }
//                    }
//                }
//                //Get the output location and options
//                output = new File(cmd.getOptionValue("out"));
//                if (!output.exists()) {
//                    output.mkdirs();
//                }
//                saveMGF = cmd.hasOption("save_mgf");
//            }
//        }
//
//        //EXECUTE THE EXTRACTION
//        try {
//            //get the correct extractor
//            PrideParameterExtractor extractor;
//            switch (type) {
//                case "pridexml":
//                    extractor = new PrideXMLParameterExtractor(input, output, saveMGF);
//                    break;
//                case "mzid":
//                    extractor = new PrideMzIDParameterExtractor(input, peakFiles, output, saveMGF);
//                    break;
//                default:
//                    throw new IllegalArgumentException("Type needs to be either pridexml or mzid");
//            }
//
//            //run the extractor
//            if (extractor.analyze()) {
//                LOGGER.info("The analysis was completed succesfully");
//            }
//        } catch (ParameterExtractionException ex) {
//            LOGGER.error(ex);
//        }
    }

    private static void help(Options options) {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("Main", options);
        System.exit(0);
    }
}
