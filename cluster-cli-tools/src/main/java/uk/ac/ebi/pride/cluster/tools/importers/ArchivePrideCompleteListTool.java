package uk.ac.ebi.pride.cluster.tools.importers;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.cluster.tools.importers.projects.PRIDEProjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
 * This class generate all the path for the PRIDE Archive datasets public submissions
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 05/12/2017.
 */
public class ArchivePrideCompleteListTool implements ICommandTool {

    private static final Logger LOGGER = Logger.getLogger(ArchivePrideCompleteListTool.class);

    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption("p", "root-path", true, "Root Path to where all the projects are stored (e.g /Users/yperez/)");
        options.addOption("o","output-file", true, "The output txt file that contains all the project path.");
        return options;
    }

    @Override
    public void runCommand(Options options, String[] args) throws ClusterDataImporterException {

        CommandLineParser parser = new DefaultParser();
        try{
            CommandLine cmd = parser.parse( options, args);
            if(cmd.hasOption("p") && cmd.hasOption("o")){

                // Projects from the PRIDE that are public
                PRIDEProjects prideProjects = new PRIDEProjects();
                List<String> publicProjects = prideProjects.getPublicProjectURL();

                String rootPath = cmd.getOptionValue("p");

                PrintWriter printWriter = new PrintWriter(new File(cmd.getOptionValue("o")));
                if(publicProjects != null && publicProjects.size() > 0){
                    for(String projectPath: publicProjects){
                        printWriter.println(rootPath + "/" + projectPath);
                    }
                }else{
                    throw new ClusterDataImporterException("Error connecting to the database or database empty, not project found", new IOException());
                }
            }else{
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "ant", options );
            }
        }catch (ParseException | FileNotFoundException e){
            throw new ClusterDataImporterException(e.getMessage(), e);
        }
    }

    /**
     * This tool compute the number of projects in PRIDE and retrieve the absolute path of the project.
     * Each path for each project is composed by the date of the publication of the project and the id of the project.
     * @param args
     */
    public static void main(String[] args){
        ArchivePrideCompleteListTool tool = new ArchivePrideCompleteListTool();
        Options options = tool.initOptions();
        try {
            tool.runCommand(options, args);
        } catch (ClusterDataImporterException e) {
            LOGGER.error("The project path generation tool has an error -- " + e.getMessage());
            e.printStackTrace();
        }

    }
}
