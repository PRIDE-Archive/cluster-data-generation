package uk.ac.ebi.pride.cluster.tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;

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
 * Created by ypriverol (ypriverol@gmail.com) on 27/11/2017.
 */
public interface ICommandTool {

    /**
     * Init options for the Commandline Tool
     * @return Options for the tool.
     */
    public Options initOptions();

    /**
     * run the commandline tool.
     * @param options Option for the specific tool
     */
    public void runCommand(Options options, String[] args) throws ClusterDataImporterException;
}
