package uk.ac.ebi.pride.cluster.dbmanager;

import java.io.File;
import java.io.IOException;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This Interface control how all the downloader will work. For example, how do
 * we download an specific taxonomy into a file or folder.
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 24/11/2017.
 */
public interface IDatabaseDownload {

    //This function dump a set of taxonomies in an specific path
    void download(File folderPath, String  taxonomy) throws IOException;

    // This function dump an specific taxonomy into a file.
    void downloadToDirectory(File pathFile, String... taxonomy) throws IOException;

}
