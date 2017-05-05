package uk.ac.ebi.pride.spectracluster.filters;

import uk.ac.ebi.pride.spectracluster.mztab.FDRFilter;
import uk.ac.ebi.pride.spectracluster.mztab.IDFilterName;
import uk.ac.ebi.pride.spectracluster.mztab.IFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

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
 * Created by ypriverol (ypriverol@gmail.com) on 21/04/2017.
 */
public final class IdentificationPredicateParser {

    public static Map<String, IFilter> parse(File filterXmlFile) throws IOException {

        Properties properties = new Properties();
        properties.loadFromXML(new FileInputStream(filterXmlFile));

        Map<String, IFilter> predicates = new HashMap<>();

        // filter by identified spectrum
        String decoyFilter = properties.getProperty(IDFilterName.PSM_FDR_FILTER.getName());
        if (decoyFilter != null && !"NaN".equalsIgnoreCase(decoyFilter)) {
            predicates.put(IDFilterName.PSM_FDR_FILTER.getName(), new FDRFilter(Double.parseDouble(decoyFilter)));
        }

        // filter by identified spectrum
        String peptideDecoy = properties.getProperty(IDFilterName.PEPTIDE_FDR_FILTER.getName());
        if (peptideDecoy != null && !"NaN".equalsIgnoreCase(peptideDecoy)) {
            predicates.put(IDFilterName.PEPTIDE_FDR_FILTER.getName(), new FDRFilter(Double.parseDouble(peptideDecoy)));
        }

        return predicates;

    }
}
