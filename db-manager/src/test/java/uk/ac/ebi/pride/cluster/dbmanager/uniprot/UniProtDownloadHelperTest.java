package uk.ac.ebi.pride.cluster.dbmanager.uniprot;


import org.junit.Test;
import uk.ac.ebi.pride.cluster.dbmanager.utils.GZipDecompressToFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class Test the UniProt Download databases.
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 24/11/2017.
 */

public class UniProtDownloadHelperTest {

    @Test
    public void download() throws Exception {

        File tempFile = File.createTempFile("file", ".gz");
        File tempFileUncompress =  File.createTempFile("file", ".fasta");
        UniProtProteomesDownloadHelper uniprotDownloader = new UniProtProteomesDownloadHelper();
        uniprotDownloader.download(tempFile, "9606");
        GZipDecompressToFile.decompress(tempFile, tempFileUncompress);
        tempFile.deleteOnExit();
    }

    @Test
    public void downloadToDirectory() throws Exception {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwx--x");
        FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);

        File tempFile = Files.createTempDirectory("example", fileAttributes).toFile();
        UniProtProteomesDownloadHelper uniprotDownloader = new UniProtProteomesDownloadHelper();
        uniprotDownloader.downloadToDirectory(tempFile, "9606");
        tempFile.deleteOnExit();
    }

}
