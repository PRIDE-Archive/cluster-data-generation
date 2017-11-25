package uk.ac.ebi.pride.cluster.dbmanager.uniprot;


import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.dbmanager.IDatabaseDownload;
import uk.ac.ebi.pride.cluster.dbmanager.utils.DBManagerUtilities;
import uk.ac.ebi.pride.cluster.dbmanager.utils.ProgressBarConsole;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
                URL url = null;
                url = new URL(UNIPROT_URL + "?query=taxonomy:" + taxonomy + "&AND+keyword:"+'"'+"Complete+proteome"+'"'+"&force=yes&format=fasta&include=yes&compress=yes");
                BufferedOutputStream outputFile = DBManagerUtilities.downloadURL(url, filePath, taxonomy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
