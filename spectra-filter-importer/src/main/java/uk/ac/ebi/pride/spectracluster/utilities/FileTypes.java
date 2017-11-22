package uk.ac.ebi.pride.spectracluster.utilities;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class define all the types of files for the importer pipelines
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 16/11/2017.
 */
public class FileTypes {

    public static final String PRIDE_MZTAB_SUFFIX = ".pride.mztab";

    public static final String COMPRESS_MZIDENTML= ".mzid.gz";
    public static final String COMPRESS_MGF      = ".mgf.gz";
    public static final String COMPRESS_PRIDE    = ".pride.gz";

    public static final String PRIDE_MGF_SUFFIX   = ".pride.mgf";
    public static final String MGF_SUFFIX         = ".mgf";
    public static final String INTERNAL_DIRECTORY = "internal";
    public static final String SUBMISSION_DIRECTORY = "submission";
    public static final String SUBMISSION_FILE      = "submission.px";
    public static final String PARAM_FILE = ".par";

    /**
     * This function check if one file name ends with an specific fileType
     * @param fileName
     * @param fileType
     * @return
     */
    public static boolean isTypeFile(String fileName, String fileType){
        return fileName.toLowerCase().endsWith(fileType.toLowerCase());
    }

    public static String removeGzip(String fileName) {
        if(fileName.toLowerCase().endsWith(".gz")){
            fileName = fileName.substring(0, fileName.length() - 3);
        }
        return fileName;
    }
}
