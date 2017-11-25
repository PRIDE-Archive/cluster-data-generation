package uk.ac.ebi.pride.cluster.dbmanager.contaminants;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.dbmanager.IFileDownload;
import uk.ac.ebi.pride.cluster.dbmanager.uniprot.UniProtProteomesDownloadHelper;
import uk.ac.ebi.pride.cluster.dbmanager.utils.ConstantsURLs;
import uk.ac.ebi.pride.cluster.dbmanager.utils.DBManagerUtilities;

import java.io.File;
import java.io.IOException;
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
 * Created by ypriverol (ypriverol@gmail.com) on 25/11/2017.
 */
public class MaxQuantContaminantHelper implements IFileDownload {

    //LOGGER to trace all the Download processes
    private static final Logger LOGGER = Logger.getLogger(MaxQuantContaminantHelper.class);

    @Override
    public void download(File localPath) throws IOException{

        if(!localPath.exists()){
            String message = "The provided Path do not exists, please check the Path";
            LOGGER.error(message);
            throw new IOException(message);
        }

        DBManagerUtilities.downloadURL(new URL(ConstantsURLs.MAXQUANT_CONTAMINANTS), localPath, "MaxQuant Contaminat");}
}
