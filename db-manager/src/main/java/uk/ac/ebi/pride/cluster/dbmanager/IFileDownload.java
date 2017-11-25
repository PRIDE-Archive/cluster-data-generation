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
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 25/11/2017.
 */
public interface IFileDownload {

    /**
     * This function define a place to download an specific File.
     * @param localPath
     */
    void download(File localPath) throws IOException;
}
