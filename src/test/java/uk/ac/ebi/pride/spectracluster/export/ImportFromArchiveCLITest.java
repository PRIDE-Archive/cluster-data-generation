package uk.ac.ebi.pride.spectracluster.export;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.archive.importer.filters.IdentificationPredicateParser;
import uk.ac.ebi.pride.spectracluster.archive.importer.filters.SpectrumPredicateParser;
import uk.ac.ebi.pride.spectracluster.utilities.mztab.IFilter;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.function.Functions;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;
import uk.ac.ebi.pride.spectracluster.util.function.spectrum.RemoveSpectrumEmptyPeakFunction;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;

import java.io.File;
import java.util.Map;

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
public class ImportFromArchiveCLITest {

    private File folderInternal;
    private File folderInternalEmpty;
    private File folderOutput;
    private File filterFile;
    private File filterUnidentifedFile;
    private File folderInternalnoFDR;

    @Before
    public void setUp() throws Exception {
       folderInternal = new File(ImportFromArchiveCLITest.class.getClassLoader().getResource("example/").toURI());
       folderInternalEmpty = new File(ImportFromArchiveCLITest.class.getClassLoader().getResource("exampleempty/").toURI());
       folderOutput   = new File(ImportFromArchiveCLITest.class.getClassLoader().getResource("example/").toURI());
       filterFile     = new File(ImportFromArchiveCLITest.class.getClassLoader().getResource("filter.xml").toURI());
       filterUnidentifedFile = new File(ImportFromArchiveCLITest.class.getClassLoader().getResource("filter_unidentified.xml").toURI());
       folderInternalnoFDR = new File(ImportFromArchiveCLITest.class.getClassLoader().getResource("noFDR").toURI());
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

        uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI exp = new uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI(condition, idFilters);
        exp.export(folderInternal, folderOutput, false);
        System.out.println("exported " + folderOutput);
    }

    @Test
    public void exportSplit() throws Exception {

        System.out.println("Output to: " + folderOutput.getAbsolutePath());

        // parse all the filters
        IPredicate<ISpectrum> predicate = SpectrumPredicateParser.parse(filterFile);
        Map<String, IFilter> idFilters = IdentificationPredicateParser.parse(filterFile);

        // add function to remove empty peak lists
        RemoveSpectrumEmptyPeakFunction removeEmptyPeakFunction = new RemoveSpectrumEmptyPeakFunction();
        IFunction<ISpectrum, ISpectrum> condition = Functions.condition(removeEmptyPeakFunction, predicate);

        uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI exp = new uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI(condition, idFilters);
        exp.export(folderInternal, folderOutput, true);
        System.out.println("exported " + folderOutput);
    }

    @Test
    public void exportSplitnoFDR() throws Exception {

        System.out.println("Output to: " + folderOutput.getAbsolutePath());

        // parse all the filters
        IPredicate<ISpectrum> predicate = SpectrumPredicateParser.parse(filterUnidentifedFile);
        Map<String, IFilter> idFilters = IdentificationPredicateParser.parse(filterUnidentifedFile);

        // add function to remove empty peak lists
        RemoveSpectrumEmptyPeakFunction removeEmptyPeakFunction = new RemoveSpectrumEmptyPeakFunction();
        IFunction<ISpectrum, ISpectrum> condition = Functions.condition(removeEmptyPeakFunction, predicate);

        uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI exp = new uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI(condition, idFilters);
        exp.export(folderInternalnoFDR, folderOutput, true);
        System.out.println("exported " + folderOutput);
    }


    @Test
    public void exportUnidentified() throws Exception {

        System.out.println("Output to: " + folderOutput.getAbsolutePath());

        // parse all the filters
        IPredicate<ISpectrum> predicate = SpectrumPredicateParser.parse(filterUnidentifedFile);
        Map<String, IFilter> idFilters = IdentificationPredicateParser.parse(filterUnidentifedFile);

        // add function to remove empty peak lists
        RemoveSpectrumEmptyPeakFunction removeEmptyPeakFunction = new RemoveSpectrumEmptyPeakFunction();
        IFunction<ISpectrum, ISpectrum> condition = Functions.condition(removeEmptyPeakFunction, predicate);

        uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI exp = new uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI(condition, idFilters);
        exp.export(folderInternal, folderOutput, false);
        System.out.println("exported " + folderOutput);
    }


    @Test
    public void exportEmpty() throws Exception {


        System.out.println("Output to: " + folderOutput.getAbsolutePath());

        // parse all the filters
        IPredicate<ISpectrum> predicate = SpectrumPredicateParser.parse(filterFile);
        Map<String, IFilter> idFilters = IdentificationPredicateParser.parse(filterFile);

        // add function to remove empty peak lists
        RemoveSpectrumEmptyPeakFunction removeEmptyPeakFunction = new RemoveSpectrumEmptyPeakFunction();
        IFunction<ISpectrum, ISpectrum> condition = Functions.condition(removeEmptyPeakFunction, predicate);

        uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI exp = new uk.ac.ebi.pride.spectracluster.export.ImportFromArchiveCLI(condition, idFilters);
        exp.export(folderInternalEmpty, folderOutput, false);
        System.out.println("exported " + folderOutput);
    }

}