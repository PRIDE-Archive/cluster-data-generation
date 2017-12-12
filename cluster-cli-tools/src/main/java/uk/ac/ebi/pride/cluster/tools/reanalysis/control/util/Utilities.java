/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.pride.cluster.tools.reanalysis.control.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@ugent.be>
 */
public class Utilities {

    private static final Logger LOGGER = Logger.getLogger(Utilities.class);

    public static void copyFile(File source, File dest) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
                FileChannel destChannel = new FileOutputStream(dest).getChannel();) {
            //        LOGGER.info("Storing " + source.getName() + " to " + dest.getAbsolutePath());
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
}
