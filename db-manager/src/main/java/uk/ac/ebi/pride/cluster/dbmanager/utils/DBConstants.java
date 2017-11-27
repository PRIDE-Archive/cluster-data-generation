package uk.ac.ebi.pride.cluster.dbmanager.utils;

import uk.ac.ebi.pride.cluster.dbmanager.IDatabaseDownload;
import uk.ac.ebi.pride.cluster.dbmanager.uniprot.UniProtProteomesDownloadHelper;

import java.util.ArrayList;
import java.util.List;

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
public class DBConstants {

    public enum SupportedDatabase{
        UKNOWN_DATABASE("unknow", "Uknown Database", "unknow"),
        UNIPROT_PROTEOMES("proteomes", "Uniprot Proteomes", ".gz");


        private final String name;
        private final String key;
        private final String outputExtension;

        SupportedDatabase(String key, String name, String outputExtension) {
            this.name = name;
            this.key = key;
            this.outputExtension = outputExtension;
        }

        public String getName() {
            return name;
        }

        public String getKey() {
            return key;
        }

        public String getOutputExtension() {
            return outputExtension;
        }

        /**
         * Retrieve the database supported by the key of the database.
         * @param key key representing the database.
         * @return SupportedDatabase.
         */
        public static SupportedDatabase getDatabaseByName(String key){
            for(SupportedDatabase keyValue: values()){
                if(keyValue.key.equalsIgnoreCase(key))
                    return keyValue;
            }
            return UKNOWN_DATABASE;
        }

        /**
         * Get all the keys from Database Structure
         * @return List of Keys
         */
        public static List<String> getKeyValues(){
            List<String> valueKeys = new ArrayList<>();
            for(SupportedDatabase key: values())
                valueKeys.add(key.key);
            return valueKeys;
        }

    }

    /**
     * This function should be extended
     * @param database
     * @return
     */
    public static IDatabaseDownload getDatabaseToolByKey(SupportedDatabase database) {
        IDatabaseDownload tool = null;
        if(database == SupportedDatabase.UNIPROT_PROTEOMES)
            tool = new UniProtProteomesDownloadHelper();
        return tool;
    }

    // This urls change a lot, this is MaxQuant URL
    public final static String MAXQUANT_CONTAMINANTS  = "http://lotus1.gwdg.de/mpg/mmbc/maxquant_input.nsf/7994124a4298328fc125748d0048fee2/$FILE/contaminants.fasta";

    // This is the url for CRAP
    public final static String CRAP_CONTAMINANTS  = "ftp://ftp.thegpm.org/fasta/cRAP/crap.fasta";
}
