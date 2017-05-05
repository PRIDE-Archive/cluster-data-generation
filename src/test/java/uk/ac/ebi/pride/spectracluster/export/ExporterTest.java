package uk.ac.ebi.pride.spectracluster.export;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.filters.IdentificationPredicateParser;
import uk.ac.ebi.pride.spectracluster.filters.SpectrumPredicateParser;
import uk.ac.ebi.pride.spectracluster.filters.SpectrumPredicateParserTest;
import uk.ac.ebi.pride.spectracluster.mztab.IFilter;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.function.Functions;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;
import uk.ac.ebi.pride.spectracluster.util.function.spectrum.RemoveSpectrumEmptyPeakFunction;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * This class test the formal exporter from the Archive to the PRIDE Cluster mgf that
 * is consume by PRIDE Cluster.
 *
 * This test will need to copy a proper internal folder from a PRIDE project in to input folder.
 *
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 20/04/2017.
 */
public class ExporterTest {

    private File folderInternal;
    private File folderOutput;
    private File filterFile;

    @Before
    public void setUp() throws Exception {
       folderInternal = new File("/Users/yperez");
       folderOutput   = new File("/Users/yperez");
       filterFile     = new File(ExporterTest.class.getClassLoader().getResource("filter.xml").toURI());
    }

    @Test
    public void export() throws Exception {


        System.out.println("Output to: " + folderOutput.getAbsolutePath());

        // parse all the filters
        IPredicate<ISpectrum> predicate = SpectrumPredicateParser.parse(filterFile);
        Map<String, IFilter> idFilters = IdentificationPredicateParser.parse(filterFile);

        // add function to remove empty peak lists
        RemoveSpectrumEmptyPeakFunction removeEmptyPeakFunction = new RemoveSpectrumEmptyPeakFunction();
        IFunction<ISpectrum, ISpectrum> condition = Functions.condition(removeEmptyPeakFunction, predicate);

        Exporter exp = new Exporter(condition, idFilters);
        exp.export(folderInternal, folderOutput);
        System.out.println("exported " + folderOutput);
    }

}