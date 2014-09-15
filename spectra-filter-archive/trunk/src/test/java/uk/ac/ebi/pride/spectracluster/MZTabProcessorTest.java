package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.filters.TypedFilterCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.spectracluster.export.MZTabProcessor;
import uk.ac.ebi.pride.spectracluster.archive.ArchiveSpectra;
import uk.ac.ebi.pride.spectracluster.filters.SpectrumFilters;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.KnownProperties;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MZTabProcessorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static TypedFilterCollection filters;

    static {
        TypedFilterCollection.registerHandler(SpectrumFilters.TAG, new SpectrumFilters.SpectrumFilterSaxHandler(null));
        ClassLoader classLoader = FilterLoadingTest.class.getClassLoader();
        File filterXmlFile = null;
        try {
            filterXmlFile = new File(classLoader.getResource("IdentifiedFilter.xml").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        filters = TypedFilterCollection.parse(filterXmlFile);

    }

    private File outputFile;

    @Before
    public void setUp() throws Exception {

        // output file
        outputFile = temporaryFolder.newFile();
        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));

        // initialize ArchiveSpectra
        ClassLoader classLoader = FilterLoadingTest.class.getClassLoader();
        File mzTabFile = new File(classLoader.getResource("example.mztab").toURI());
        MZTabFileParser mzTabFileParser = new MZTabFileParser(mzTabFile, System.out);
        MZTabFile mzTab = mzTabFileParser.getMZTabFile();

        ArchiveSpectra archiveSpectra = new ArchiveSpectra(mzTab);

        // add related mgf
        File mgfFile = new File(classLoader.getResource("example.mgf").toURI());
        archiveSpectra.addMgfFile(mgfFile);

        // initialize MZTabProcessor
        MZTabProcessor mzTabProcessor = new MZTabProcessor(archiveSpectra);
        mzTabProcessor.handleCorrespondingMGFs(filters, writer);
        writer.flush();
        writer.close();

    }

    @Test
    public void testFirstSpectrum() throws Exception {
        LineNumberReader rdr = new LineNumberReader(new FileReader(outputFile));
        ISpectrum spectrum = ParserUtilities.readMGFScan(rdr);

        assertEquals("PRD000715;PRIDE_Exp_Complete_Ac_24805.xml;spectrum=1", spectrum.getId());

        assertEquals(3, spectrum.getPrecursorCharge());

        assertEquals(1163.8679f, spectrum.getPrecursorMz());

        assertEquals("9606", spectrum.getProperty(KnownProperties.TAXONOMY_KEY));

        assertEquals("VHKLTIDDVTPADEADYSFVPEGFACNLSAK", spectrum.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY));

        assertEquals("10-MOD:01455,26-MOD:00397", spectrum.getProperty(KnownProperties.MODIFICATION_KEY));
    }


    @Test
    public void testSecondSpectrumOmittedDueToNumberOfPeaks() throws Exception {
        LineNumberReader rdr = new LineNumberReader(new FileReader(outputFile));
        ParserUtilities.readMGFScan(rdr);
        ISpectrum spectrum = ParserUtilities.readMGFScan(rdr);

        assertEquals("PRD000715;PRIDE_Exp_Complete_Ac_24805.xml;spectrum=3", spectrum.getId());
    }


    @Test
    public void testFourthSpectrumOmittedDueToLargePrecursorMass() throws Exception {
        LineNumberReader rdr = new LineNumberReader(new FileReader(outputFile));
        ParserUtilities.readMGFScan(rdr);
        ParserUtilities.readMGFScan(rdr);
        ISpectrum spectrum = ParserUtilities.readMGFScan(rdr);

        assertEquals("PRD000715;PRIDE_Exp_Complete_Ac_24805.xml;spectrum=5", spectrum.getId());

    }

    @Test
    public void testSixthSpectrumOmittedDueToSmallPrecursorMass() throws Exception {
        LineNumberReader rdr = new LineNumberReader(new FileReader(outputFile));
        ParserUtilities.readMGFScan(rdr);
        ParserUtilities.readMGFScan(rdr);
        ParserUtilities.readMGFScan(rdr);
        ISpectrum spectrum = ParserUtilities.readMGFScan(rdr);

        assertEquals("PRD000715;PRIDE_Exp_Complete_Ac_24805.xml;spectrum=7", spectrum.getId());

    }


    @Test
    public void testUnIdentifiedSpectrumOmitted() throws Exception {
        Set<String> spectrumIds = new HashSet<String>();

        LineNumberReader rdr = new LineNumberReader(new FileReader(outputFile));
        ISpectrum spectrum;
        do {
            spectrum = ParserUtilities.readMGFScan(rdr);
            if (spectrum != null) spectrumIds.add(spectrum.getId());
        } while (spectrum != null);

        assertTrue(!spectrumIds.contains("PRD000715;PRIDE_Exp_Complete_Ac_24805.xml;spectrum=16"));
    }
}
