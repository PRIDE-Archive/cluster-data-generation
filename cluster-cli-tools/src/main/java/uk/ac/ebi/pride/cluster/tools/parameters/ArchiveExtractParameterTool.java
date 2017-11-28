package uk.ac.ebi.pride.cluster.tools.parameters;


import com.compomics.pridesearchparameterextractor.cmd.PrideSearchparameterExtractor;
import com.compomics.pridesearchparameterextractor.extraction.impl.PrideMzIDParameterExtractor;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.data.exception.SubmissionFileException;
import uk.ac.ebi.pride.data.io.SubmissionFileParser;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.cluster.utilities.FileTypes;

import java.io.File;
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
public class ArchiveExtractParameterTool implements ICommandTool {

    private static final Logger LOGGER = Logger.getLogger(PrideSearchparameterExtractor.class);

    public static void main(String[] args) {

        ArchiveExtractParameterTool tool = new ArchiveExtractParameterTool();
        Options options = tool.initOptions();

        try {
            tool.runCommand(options, args);
        } catch (ClusterDataImporterException e) {
            LOGGER.error("The following Archive Extractor Parameter Tool has failed --" + e.getMessage());
        }
    }

    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption("i", "input-folder",true, "Project Folder in PRIDE, (e.g /nfs/pride/prod/archive/2017/11/PXD007710)  ");
        options.addOption("o", "output-folder", true, "The output folder");
        options.addOption("s", "split-assay", false, "Split the output into Project Folders, <PXD00XXXXX>/PXD-AssayID");
        return options;
    }

    @Override
     public void runCommand(Options options, String[] args) throws ClusterDataImporterException {

        //parse the command
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            if (!(cmd.hasOption("i") || !cmd.hasOption("o"))) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("ant", options);
            }

            String inputProjectFolder = cmd.getOptionValue("i");
            String outputFolder = cmd.getOptionValue("o");

            File projectInternalPath = new File(inputProjectFolder, FileTypes.INTERNAL_DIRECTORY);

            Submission submission = SubmissionFileParser.parse(new File(projectInternalPath, FileTypes.SUBMISSION_FILE));

            submission.getDataFiles().stream()
                    .filter(file -> file.getFileType() == ProjectFileType.RESULT &&
                            (FileTypes.isTypeFile(file.getFileName(), FileTypes.COMPRESS_MZIDENTML) || (FileTypes.isTypeFile(file.getFileName(), FileTypes.COMPRESS_PRIDE))))
                    .forEach(
                            file -> {

                                // Processing File
                                File inputFile = new File(projectInternalPath, FileTypes.removeGzip(file.getFileName()));
                                String assayNumber = file.getAssayAccession();

                                LOGGER.info("Processing Assay -- " + assayNumber + " -- Following file -- " + inputFile.getAbsolutePath());


                                // Process an mzIdentml
                                if (FileTypes.isTypeFile(file.getFileName(), FileTypes.COMPRESS_MZIDENTML)) {

                                    List<File> peakFiles = new ArrayList<>();

                                    // List of files associated with the mzIdentML
                                    retrieveListPeakFileNames(file).stream().forEach(fileName -> {
                                        File peakFile = new File(projectInternalPath, fileName);
                                        if (peakFile.exists()) {
                                            peakFiles.add(peakFile);
                                        }
                                    });

                                    String fileName = resolveOutputPath(outputFolder, file.getAssayAccession(), inputProjectFolder);

                                    try {
                                        PrideMzIDParameterExtractor extractor = new PrideMzIDParameterExtractor(inputFile, assayNumber, peakFiles, fileName, false, false);
                                        extractor.analyze();
                                    } catch (Exception e) {
                                        LOGGER.error("Error in File -- " + inputFile + " -- Message Error -- " + e.getMessage());
                                    }

                                } else {  // Process a PRIDE XML
//
//                                String fileName = resolveOutputPath(outputFolder, file.getAssayAccession(), inputProjectFolder);
//
//                                try{
//                                    PrideXMLParameterExtractor extractor = new PrideXMLParameterExtractor(inputFile, assayNumber, fileName, false, false);
//                                    extractor.analyze();
//                                }catch (Exception e){
//                                    LOGGER.error("Error in File -- " + inputFile + " -- Message Error -- " + e.getMessage());
//                                }
                                }
                            });
        }catch (ParseException | SubmissionFileException e){
            throw new ClusterDataImporterException(e.getMessage(), e);
        }
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
