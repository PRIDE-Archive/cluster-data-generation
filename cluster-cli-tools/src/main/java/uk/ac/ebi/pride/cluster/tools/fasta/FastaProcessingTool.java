package uk.ac.ebi.pride.cluster.tools.fasta;

import EDU.oswego.cs.dl.util.concurrent.FJTask;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.dbmanager.utils.DBManagerUtilities;
import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;

import java.io.File;
import java.io.IOException;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class process an input Fasta file and append the corresponding contaminants, or append the decoys. Because multiple operations are supported
 * the granualrity of the tool follow the following flow:
 *  - Append another database.
 *  - Append Contaminants
 *  - Append Decoys.
 *
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 27/11/2017.
 */
public class FastaProcessingTool implements ICommandTool{

    private static final Logger LOGGER = Logger.getLogger(FastaProcessingTool.class);
    private static final String PREFIX_FILE_OUTPUT = "total";

    public static void main(String[] args){

        FastaProcessingTool tool = new FastaProcessingTool();
        Options options = tool.initOptions();
        try {
            tool.runCommand(options, args);
        } catch (ClusterDataImporterException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Options initOptions() {
       Options options = new Options();
       options.addOption("i", "input-file", true, "Input file to be processed, it must be in Fasta file.");
       options.addOption("a", "append-file", true, "Append another Fasta file to the database, the tool will check the duplicity (e.g Contaminants)");
       options.addOption("d", "decoy", false, "Append decoy to the database, be aware that the script can't check for duplicity if the file has already decoys");
       options.addOption("o", "output", true, "Output file whee to put the results of the processing step, if not output is provided, a new name is created using the prefix total provided parameters");
       return options;
    }

    @Override
    public void runCommand(Options options, String[] args) throws ClusterDataImporterException {

        //parse the command
        CommandLineParser parser = new DefaultParser();

        try {

            CommandLine cmd = parser.parse(options, args);

            if(!cmd.hasOption("i")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("ant", options);
                System.exit( -1);
            }

            String inputPath = cmd.getOptionValue("i");

            String outputFile = buildCompletePath(inputPath);
            if(cmd.hasOption("o")){
                outputFile = cmd.getOptionValue("o");
            }

            //This file will be used to process each step, it should be deleted at the end of the processing.
            File tempFile = File.createTempFile(new File(inputPath).getName(), "_tmp.fasta");
            boolean append = false;

            // Append to the file the corresponding append argument file.
            if(cmd.hasOption("a")){
                File appendFile = new File(cmd.getOptionValue("a"));
                DBManagerUtilities.mergeToFastaFileTemp(new File(inputPath), appendFile, tempFile);
                append = true;
            }

            if(cmd.hasOption("d")){
                SequenceFactory factory = SequenceFactory.getInstance();
                //If the the append has happend then use the temporary file.
                if(append) factory.loadFastaFile(tempFile);
                else factory.loadFastaFile(new File(inputPath));
                try {
                    factory.appendDecoySequences(new File(outputFile));
                } catch (InterruptedException e) {
                    throw new ClusterDataImporterException("The generation of the Decoy proteins has failed for file -- " + inputPath, e);
                }
            }

            //Remove the temporary file
            if(tempFile.exists())
                tempFile.deleteOnExit();

        }catch (ParseException e){
            throw new ClusterDataImporterException("Error parsing the provided paramters", e);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * This is the function to build the path for the output file.
     * @param inputPath
     * @return
     */
    private String buildCompletePath(String inputPath) {
        String fastaRemove = inputPath.substring(0, inputPath.indexOf(".fasta"));
        return fastaRemove + "-" + PREFIX_FILE_OUTPUT + ".fasta";
    }
}
