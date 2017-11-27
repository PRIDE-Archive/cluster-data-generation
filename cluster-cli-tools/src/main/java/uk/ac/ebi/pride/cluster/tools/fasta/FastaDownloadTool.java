package uk.ac.ebi.pride.cluster.tools.fasta;

import com.compomics.pridesearchparameterextractor.cmd.PrideSearchparameterExtractor;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.dbmanager.IDatabaseDownload;
import uk.ac.ebi.pride.cluster.dbmanager.utils.DBConstants;
import uk.ac.ebi.pride.cluster.dbmanager.utils.DBManagerUtilities;
import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.cluster.tools.utilities.FileUtilities;

import java.io.*;
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
 * Created by ypriverol (ypriverol@gmail.com) on 26/11/2017.
 */
public class FastaDownloadTool implements ICommandTool {

    private static final Logger LOGGER = Logger.getLogger(FastaDownloadTool.class);

    public static void main(String[] args) {

        FastaDownloadTool tool = new FastaDownloadTool();
        Options options = tool.initOptions();

        try{
            tool.runCommand(options, args);
        }catch (ClusterDataImporterException e){
            LOGGER.error("The Fasta Download Tool has failed -- " + e.getMessage());
        }
    }

    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption("t", "taxonomy", true, "Specific Taxonomy to be download");
        options.addOption("lc", "list-conf", true, "File with List of Taxonomies to be download, one per line");
        options.addOption("p", "provider", true, "Provider to Download a database (default: proteomes) -- list of database supported: " + DBConstants.SupportedDatabase.getKeyValues().toString());
        options.addOption("o", "output-folder", true, "The output folder");
        options.addOption("r", "rename", true, "Rename the taxonomy file with the name of the Provider-Taxonomy (uniprot-proteomes-9606.fatsa)");
        options.addOption("d", "decompress", false, "This fucntion detect if the download file is compress and decompressed");
        return options;
    }

    @Override
    public void runCommand(Options options, String[] args) throws ClusterDataImporterException {

        //parse the command
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            // Parse the output file
            if (!cmd.hasOption("o")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("ant", options);
                System.exit(-1);
            }

            Boolean decompress = false;
            if(cmd.hasOption("d"))
                decompress = true;

            // Setting the database information.
            DBConstants.SupportedDatabase database = DBConstants.SupportedDatabase.UNIPROT_PROTEOMES;
            if (cmd.hasOption("p")) {
                String databaseName = cmd.getOptionValue("p");
                database = DBConstants.SupportedDatabase.getDatabaseByName(databaseName);
                if(database == DBConstants.SupportedDatabase.UKNOWN_DATABASE){
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("ant", options);
                }
            }

            String outputFolder = cmd.getOptionValue("o");

            if(cmd.hasOption("lc")){
                String pathConfig = cmd.getOptionValue("lc");
                processConfigTaxonomies(outputFolder, pathConfig, database, decompress);
            }

        }catch(ParseException e){
            throw new ClusterDataImporterException(e.getMessage(), e);
        }

    }

    /**
     * This function read a set of taxonomies from PRIDE and download the Data from the specific repository.
     * @param outputFolder
     * @param pathConfig
     */
    private void processConfigTaxonomies(String outputFolder, String pathConfig, DBConstants.SupportedDatabase database, Boolean decompress) throws ClusterDataImporterException {

        try{
            BufferedReader br = new BufferedReader(new FileReader(pathConfig));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            List<String> taxonomies = new ArrayList<>();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
                if(line != null && line.trim().length() > 0)
                    taxonomies.add(line.trim());
            }

            taxonomies.parallelStream().forEach( taxonomy ->{

                IDatabaseDownload tool = DBConstants.getDatabaseToolByKey(database);
                File taxonomyPath = buildPathForTaxonomy(outputFolder,  database, taxonomy);
                File taxonomyPathFasta = buildPathForTaxonomyFasta(outputFolder, database, taxonomy);
                try {
                    tool.download(taxonomyPath, taxonomy);
                    if(database.getOutputExtension().equalsIgnoreCase(".gz") && decompress){
                        FileUtilities.decompressGZip(taxonomyPath, taxonomyPathFasta);
                    }
                    Boolean correct = DBManagerUtilities.checkFastaIntegrity(taxonomyPathFasta);
                    if(!correct)
                        LOGGER.error("The following Taxonomy has failed to produce a valid Fasta File -- " + taxonomy);
                } catch (IOException e) {
                    LOGGER.error("Error to Download the following Taxonomy -- " + taxonomy);
                    e.printStackTrace();
                }

            });

        }catch(FileNotFoundException e){
            throw new ClusterDataImporterException("The Config file hasn't been found in the following path -- " + pathConfig, e);
        }catch(IOException e){
            throw new ClusterDataImporterException("Problem reading the config file with the taxonomies -- " + pathConfig, e);
        }

    }

    /**
     * The output file for the Fasta version of the File is build from the name, taxonomy and
     * @param outputFolder output folder
     * @param database database name
     * @param taxonomy Taxonomy.
     * @return
     */
    private File buildPathForTaxonomyFasta(String outputFolder, DBConstants.SupportedDatabase database, String taxonomy) {
        File fileOutput =  new File(outputFolder + "/" + database.getKey() + "-" + taxonomy + ".fasta");
        return fileOutput;
    }

    /**
     * This function take an output path, a key database and the taxonomy and build a file, path extension
     * for example:
     *    path = /home/user/
     *    taxonomy = 9606
     *    database = proteomes
     *    extension = .gz
     *  final path = /home/user/proteomes-9606.gz
     * @param outputFolder
     * @param database
     * @return
     */
    private File buildPathForTaxonomy(String outputFolder, DBConstants.SupportedDatabase database,  String taxonomy) {
        File fileOutput =  new File(outputFolder + "/" + database.getKey() + "-" + taxonomy + database.getOutputExtension());
        return fileOutput;
    }


}
