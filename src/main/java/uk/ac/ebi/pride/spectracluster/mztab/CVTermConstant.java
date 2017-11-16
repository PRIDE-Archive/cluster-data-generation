package uk.ac.ebi.pride.spectracluster.mztab;

import java.util.Arrays;
import java.util.Collections;
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
 * Created by ypriverol (ypriverol@gmail.com) on 18/05/2017.
 */
public enum CVTermConstant {

    PHOSPHO_PROBABILITY_SCORE("probability", Collections.singletonList("MS:1001971"));

    String name;

    List<String> accessions;

    CVTermConstant(String name, List<String> accessions) {
        this.name = name;
        this.accessions = accessions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAccessions() {
        return accessions;
    }

    public void setAccessions(List<String> accessions) {
        this.accessions = accessions;
    }

    public static CVTermConstant getCVTermConstant(String accession){
        for(CVTermConstant value: values()){
            if(value.getAccessions().contains(accession))
                return value;
        }
        return null;
    }
}
