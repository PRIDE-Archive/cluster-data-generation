package uk.ac.ebi.pride.cluster.tools.parameters;

import com.compomics.pride_asa_pipeline.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.model.ParameterExtractionException;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.cluster.utilities.FileTypes;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.compomics.pridesearchparameterextractor.extraction.impl.*;
/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class take the experiment folder from peptideAtlas and get the parameters using the
 * original parameters provided by the PeptideAtlas team:
 *  - _tandem.params
 *  -
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 27/11/2017.
 */
public class PeptideAtlasExtractParameterTool implements ICommandTool{

    private static final Logger LOGGER = Logger.getLogger(PeptideAtlasExtractParameterTool.class);

    public static void main(String[] args) {
        PeptideAtlasExtractParameterTool tool = new PeptideAtlasExtractParameterTool();
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
       options.addOption("i", "input-folder", true, "The input folder to be use for the the parameters detection");
       options.addOption("o", "output-file", true, "The path where the file will be generated, if the output is not provided the name of the folder will be used");
       options.addOption("mgf","generate-mgf", false, "Indicates if mgf files have to be exported");
       return options;
    }

    @Override
    public void runCommand(Options options, String[] args) throws ClusterDataImporterException {

        //parse the command
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if(!cmd.hasOption("i")){
                // This prints out some help
                HelpFormatter formater = new HelpFormatter();
                formater.printHelp("Main", options);
                System.exit(-1);
            }

            File inputPath = new File(cmd.getOptionValue("i"));
            if(!inputPath.exists()){
                LOGGER.error("The provided folder do not exists, please review your parameters");
                HelpFormatter formater = new HelpFormatter();
                formater.printHelp("Main", options);
                System.exit(-1);
            }

            File[] configFiles = inputPath.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.contains(FileTypes.PEPTIDEATLAS_XTANDEM_PARAMS_EXTENSION));
                }
            });

            if(configFiles.length != 1 || configFiles[0] == null || !configFiles[0].exists()){
                LOGGER.error("More than one Xtandem File provided, Please Review the following project -- " + inputPath.getName());
                HelpFormatter formater = new HelpFormatter();
                formater.printHelp("Main", options);
                System.exit(-1);
            }

            XTandemParametersExtractor extractor = new XTandemParametersExtractor(inputPath, configFiles[0], cmd.hasOption("mgf"));

            if(extractor.analyze()){
                LOGGER.info("Paramters has been extracted successfully from file -- " + configFiles[0]);
            }else{
                LOGGER.info("Parameters from the config file -- " + configFiles[0] + " where not estimated");
            }

        } catch (ParseException e) {
            throw new ClusterDataImporterException("Error parsing the commandline options -- " + e.getMessage(), e);
        } catch (MGFExtractionException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParameterExtractionException e) {
            e.printStackTrace();
        }
    }
}
