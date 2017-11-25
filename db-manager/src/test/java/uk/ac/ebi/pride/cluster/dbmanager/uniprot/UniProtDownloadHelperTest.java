package uk.ac.ebi.pride.cluster.dbmanager.uniprot;


import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

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

public class UniProtDownloadHelperTest {

    @Test
    public void download() throws Exception {

        File tempFile = File.createTempFile("file", ".gzip");
        UniProtProteomesDownloadHelper uniprotDownloader = new UniProtProteomesDownloadHelper();
        uniprotDownloader.download(tempFile, "9606");
        tempFile.deleteOnExit();
    }

    @Test
    public void downloadToDirectory() throws Exception {
        UniProtProteomesDownloadHelper uniprotDownloader = new UniProtProteomesDownloadHelper();
        uniprotDownloader.downloadToDirectory(Files.createTempDirectory("example", FileAttribute).toFile(), "9606");
    }

}
