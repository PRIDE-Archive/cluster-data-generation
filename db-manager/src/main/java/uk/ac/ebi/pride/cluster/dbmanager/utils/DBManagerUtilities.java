package uk.ac.ebi.pride.cluster.dbmanager.utils;

import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
 * Created by ypriverol (ypriverol@gmail.com) on 24/11/2017.
 */
public class DBManagerUtilities {

    private static final Logger LOGGER = Logger.getLogger(DBManagerUtilities.class);

    public static BufferedOutputStream downloadURL(URL url, String absolutePath, String taxonomy) throws IOException{

        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

        java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
        java.io.FileOutputStream fos = new java.io.FileOutputStream(absolutePath);
        BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
        byte[] data = new byte[1024];
        long downloadedFileSize = 0;
        LOGGER.info("Starting Downloading Uniprot Proteome -- " + taxonomy);
        int x = 0;
        while ((x = in.read(data, 0, 1024)) >= 0) {
            downloadedFileSize += x;
            bout.write(data, 0, x);
            ProgressBarConsole.updateProgress(downloadedFileSize);
        }
        bout.close();
        in.close();

        return bout;

    }
}
