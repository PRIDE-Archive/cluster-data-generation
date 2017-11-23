package uk.ac.ebi.pride.cluster.tools;


import com.compomics.pridesearchparameterextractor.cmd.PrideSearchparameterExtractor;
import com.compomics.pridesearchparameterextractor.extraction.impl.PrideMzIDParameterExtractor;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.data.exception.SubmissionFileException;
import uk.ac.ebi.pride.data.io.SubmissionFileParser;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.spectracluster.utilities.FileTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        CommandLineParser parser = new DefaultParser();

        try{
            CommandLine cmd = parser.parse(options, args);
            handleCommand(cmd, options);
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


        if(!(cmd.hasOption("i") || !cmd.hasOption("o"))){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "ant", options );
        }

        String inputProjectFolder = cmd.getOptionValue("i");
        String outputFolder  = cmd.getOptionValue("o");

        File projectInternalPath = new File(inputProjectFolder, FileTypes.INTERNAL_DIRECTORY);

        Submission submission = SubmissionFileParser.parse(new File(projectInternalPath, FileTypes.SUBMISSION_FILE));

        submission.getDataFiles().stream()
                .filter( file -> file.getFileType() == ProjectFileType.RESULT &&
                        (FileTypes.isTypeFile(file.getFileName(), FileTypes.COMPRESS_MZIDENTML) || (FileTypes.isTypeFile(file.getFileName(), FileTypes.COMPRESS_PRIDE))))
                .forEach(
                        file -> {

                            // Processing File
                            File inputFile = new File(projectInternalPath, FileTypes.removeGzip(file.getFileName()));
                            String assayNumber = file.getAssayAccession();

                            LOGGER.info("Processing Assay -- " + assayNumber + " -- Following file -- " + inputFile.getAbsolutePath());


                            // Process an mzIdentml
                            if(FileTypes.isTypeFile(file.getFileName(), FileTypes.COMPRESS_MZIDENTML)){

                                List<File> peakFiles = new ArrayList<>();

                                // List of files associated with the mzIdentML
                                retrieveListPeakFileNames(file).stream().forEach( fileName -> {
                                    File peakFile = new File(projectInternalPath, fileName);
                                    if(peakFile.exists()){
                                        peakFiles.add(peakFile);
                                    }
                                });

                                String fileName = resolveOutputPath(outputFolder, file.getAssayAccession(), inputProjectFolder);

                                try{
                                    PrideMzIDParameterExtractor extractor = new PrideMzIDParameterExtractor(inputFile, peakFiles, fileName, false, false);
                                    extractor.analyze();
                                }catch (Exception e){
                                    LOGGER.error("Error in File -- " + inputFile + " -- Message Error -- " + e.getMessage());
                                }

                            }else{  // Process a PRIDE XML

                            }


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

    /**
     * This method resolve the parameters output. The method will take the latest folder to construct
     * a parameter file path like PX000-AssayId.parm
     *
     * @param outputFolder path to the output folder
     * @param assayAccession accession
     * @param inputFile input file
     * @return returnPath
     */
    private static String resolveOutputPath(String outputFolder, String assayAccession, String inputFile) {
        String[] inputproject = inputFile.split("/");
        String finalFolder = "";
        for(String idFolder: inputproject){
            if(idFolder.length() > 0)
                finalFolder = idFolder;
        }
        return outputFolder + "/" + finalFolder + "-" + assayAccession + FileTypes.PARAM_FILE;
    }

    /**
     * This function retrieve the List of mgf files for an specific mzIdentML
     * @param file MziDentML File
     * @return List of file Names
     */
    private static List<String> retrieveListPeakFileNames(DataFile file) {

        List<String> fileNames = new ArrayList<>();
        List<DataFile> fileMapping = file.getFileMappings();
        fileNames = fileMapping.stream()
                .filter( fileMap -> fileMap.getFileType() == ProjectFileType.PEAK)
                .map(fileMap -> FileTypes.removeGzip(fileMap.getFileName()))
                .collect(Collectors.toList());
        return fileNames;
    }
}
