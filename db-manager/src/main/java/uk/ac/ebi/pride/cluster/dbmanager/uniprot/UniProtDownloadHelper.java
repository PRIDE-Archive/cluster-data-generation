package uk.ac.ebi.pride.cluster.dbmanager.uniprot;


import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.dbmanager.IDatabaseDownload;
import uk.ac.ebi.pride.cluster.dbmanager.utils.ProgressBarConsole;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class allows to download Uniprot databases to specific path by
 * one or multiple taxonomies
 *
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 24/11/2017.
 */
public class UniProtDownloadHelper implements IDatabaseDownload{

    private static final String UNIPROT_URL = "http://www.uniprot.org/uniprot/";

    private String filePath;

    private static final Logger LOGGER = Logger.getLogger(UniProtDownloadHelper.class);


    /**
     * Download Path for the database.
     * @param filePath
     */
    public UniProtDownloadHelper(String filePath){
        this.filePath = filePath;
    }

    public void download(String ... taxonomies) throws IOException {

        Arrays.asList(taxonomies).parallelStream().forEach(taxonomy -> {

            try {
                URL url = new URL(UNIPROT_URL + "?query=taxonomy:" + taxonomy + "&AND+keyword:"+'"'+"Complete+proteome"+'"'+"&force=yes&format=fasta&include=yes&compress=yes");
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

                java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
                java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath);
                BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
                byte[] data = new byte[1024];
                long downloadedFileSize = 0;
                LOGGER.info("Staring Downloading Uniprot Proteome -- " + taxonomy);
                int x = 0;
                while ((x = in.read(data, 0, 1024)) >= 0) {
                    downloadedFileSize += x;
                    bout.write(data, 0, x);
                    ProgressBarConsole.updateProgress(downloadedFileSize);
                }
                bout.close();
                in.close();

            } catch ( IOException e) {

            }

        });
    }
}
