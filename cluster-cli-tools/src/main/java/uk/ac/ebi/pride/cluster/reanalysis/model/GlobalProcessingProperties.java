/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.pride.cluster.reanalysis.model;

import java.io.File;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@ugent.be>
 */
public class GlobalProcessingProperties {

    public static final File FASTA_REPOSITORY_FOLDER = new File(System.getProperty("user.home") + "/pladipus/fasta/");
    public static final File TEMP_FOLDER = new File(System.getProperty("user.home") + "/pladipus/temp/");
    public static final File TEMP_FOLDER_SEARCHGUI = new File(TEMP_FOLDER, "processing/SearchGUI");
    public static final File TEMP_FOLDER_PEPTIDESHAKER = new File(TEMP_FOLDER, "processing/PeptideShaker");
    public static final File TOOL_FOLDER = new File(System.getProperty("user.home") + "/pladipus/tools");
    public static final File TOOL_DEFAULTS_FILE = new File(TOOL_FOLDER, "defaults.properties");
    public static final File TOOL_FOLDER_SEARCHGUI = new File(TOOL_FOLDER, "SearchGUI");
    public static final File TOOL_FOLDER_PEPTIDESHAKER = new File(TOOL_FOLDER, "PeptideShaker");
}
