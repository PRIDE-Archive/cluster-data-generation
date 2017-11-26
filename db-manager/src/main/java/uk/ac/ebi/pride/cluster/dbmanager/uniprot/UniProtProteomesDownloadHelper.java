package uk.ac.ebi.pride.cluster.dbmanager.uniprot;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.dbmanager.IDatabaseDownload;
import uk.ac.ebi.pride.cluster.dbmanager.utils.DBManagerUtilities;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
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
 * one or multiple taxonomies.
 *
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 24/11/2017.
 */
public class UniProtProteomesDownloadHelper implements IDatabaseDownload{


    // The Uniprot url to download the data.
    private static final String UNIPROT_URL = "http://www.uniprot.org/uniprot/";

    //LOGGER to trace all the Download processes
    private static final Logger LOGGER = Logger.getLogger(UniProtProteomesDownloadHelper.class);

    /**
     * This function Download the databases for Uniprot Complete Proteomes into an
     * specif folder.
     * @param folderPath folder to dump all the taxonomies files.
     * @param taxonomies Taxonomies to be download.
     *
     * @throws IOException
     */
    @Override
    public void downloadToDirectory(File folderPath, String ... taxonomies) throws IOException {

        if(!folderPath.exists() || !folderPath.isDirectory()) {
            String message = "The provided directory do not exists or is not a directory";
            LOGGER.error(message);
            throw new IOException(message);
        }

        Arrays.asList(taxonomies).parallelStream().forEach(taxonomy -> {
            try {
                URL url;
                url = new URL(UNIPROT_URL + "?query=taxonomy:" + taxonomy + "&AND+keyword:"+'"'+"Complete+proteome"+'"'+"&force=yes&format=fasta&include=yes&compress=yes");
                BufferedOutputStream outputFile = DBManagerUtilities.downloadURLToDirectory(url, folderPath, taxonomy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * This function download a File from URL to specific taxonomy and copy into an specific file name
     *
     * @param pathFile pathFile to store the download file
     * @param taxonomy taxonomy from uniprot
     * @throws IOException Exception in case of error
     */
    @Override
    public void download(File pathFile, String taxonomy) throws IOException {

        if(!pathFile.exists()) {
            String message = "The provided file output do not exists";
            LOGGER.error(message);
            throw new IOException(message);
        }

        URL url;
        url = new URL(UNIPROT_URL + "?query=taxonomy:" + taxonomy + "&AND+keyword:"+'"'+"Complete+proteome"+'"'+"&force=yes&format=fasta&include=yes&compress=yes");
        BufferedOutputStream outputFile = DBManagerUtilities.downloadURL(url, pathFile, taxonomy);

    }
}
