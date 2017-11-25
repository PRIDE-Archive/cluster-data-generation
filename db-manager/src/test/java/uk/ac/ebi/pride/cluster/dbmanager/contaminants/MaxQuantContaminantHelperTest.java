package uk.ac.ebi.pride.cluster.dbmanager.contaminants;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

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
public class MaxQuantContaminantHelperTest {

    @Test
    public void download() throws Exception {
        MaxQuantContaminantHelper uniprotDownloader = new MaxQuantContaminantHelper();
        uniprotDownloader.download(File.createTempFile("file", "zip"));
    }

}