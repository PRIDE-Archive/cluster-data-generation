package uk.ac.ebi.pride.cluster.utilities.mztab;

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
 * Created by ypriverol (ypriverol@gmail.com) on 02/05/2017.
 */
public enum IDFilterName {

    PSM_FDR_FILTER("psm.decoy.filter"),
    PEPTIDE_FDR_FILTER("peptide.decoy.filter");

    private String name;

    IDFilterName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
