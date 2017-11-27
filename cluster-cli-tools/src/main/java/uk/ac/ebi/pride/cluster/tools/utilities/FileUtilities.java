package uk.ac.ebi.pride.cluster.tools.utilities;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

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
public class FileUtilities {

    //The logger file output
    private static final Logger LOGGER = Logger.getLogger(FileUtilities.class);

    public static void decompressGZip(File gzipFile, File outputFile) throws IOException {

        byte[] buffer = new byte[1024];

        GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzipFile));

        FileOutputStream out = new FileOutputStream(outputFile);

        int len;
        while ((len = gzis.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }

        gzis.close();
        out.close();

        LOGGER.info("The File -- " + gzipFile.getName() + " -- has been converted");
    }
}
