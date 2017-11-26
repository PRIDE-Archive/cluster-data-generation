package uk.ac.ebi.pride.cluster.dbmanager.utils;

import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
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
 * This class helps to Download databases from specific url.
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 24/11/2017.
 */
public class DBManagerUtilities {

    private static final Logger LOGGER = Logger.getLogger(DBManagerUtilities.class);

    /**
     * Download a file from an URL, the file is bulk into an absolute file path.
     * @param url URL fo the fhe file that would be download
     * @param absolutePath absolute path where the file where be bulk.
     * @param flagInformation flagInformation is used for Logger.
     * @return Downloaded file.
     * @throws IOException
     */
    public static BufferedOutputStream downloadURL(URL url, File absolutePath, String flagInformation) throws IOException{

        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

        java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
        java.io.FileOutputStream fos = new java.io.FileOutputStream(absolutePath);
        BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
        byte[] data = new byte[1024];
        long downloadedFileSize = 0;
        LOGGER.info("Starting Downloading the Proteome -- " + flagInformation);
        int x;
        while ((x = in.read(data, 0, 1024)) >= 0) {
            downloadedFileSize += x;
            bout.write(data, 0, x);
            ProgressBarConsole.updateProgress(downloadedFileSize);
        }
        bout.close();
        in.close();

        return bout;
    }

    /**
     * Different to other methods this method take the url and download to a folder
     * the file with the same name that is provided by the user.
     * @param url URL to download
     * @param absolutePath absolute path of the directory
     * @param taxonomy taxonomy
     * @return
     * @throws IOException
     */
    public static BufferedOutputStream downloadURLToDirectory(URL url, File absolutePath, String taxonomy) throws IOException{

        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

        java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());

        String fieldValue = httpConnection.getHeaderField("Content-Disposition");

        if (fieldValue == null || ! fieldValue.contains("filename="))
            throw new IOException("The file is not provided in the header, it can't be download with the original provided name.");

        if (! absolutePath.exists() || !absolutePath.isDirectory())
            throw new IOException("The file directory path provided do not exists!!");

        // parse the file name from the header field
        String filename = fieldValue.substring(fieldValue.indexOf("filename=") + 9, fieldValue.length());

        java.io.FileOutputStream fos = new java.io.FileOutputStream(absolutePath + File.separator + filename);
        BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
        byte[] data = new byte[1024];
        long downloadedFileSize = 0;
        LOGGER.info("Starting Downloading the Proteome -- " + taxonomy);
        int x;
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
