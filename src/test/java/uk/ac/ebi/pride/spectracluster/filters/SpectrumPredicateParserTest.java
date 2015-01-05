package uk.ac.ebi.pride.spectracluster.filters;

import org.junit.Before;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.KnownProperties;
import uk.ac.ebi.pride.spectracluster.spectrum.Peak;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpectrumPredicateParserTest {

    private IPredicate<ISpectrum> predicate;

    @Before
    public void setUp() throws Exception {
        URL resource = SpectrumPredicateParserTest.class.getClassLoader().getResource("filter.xml");
        predicate = SpectrumPredicateParser.parse(new File(resource.toURI()));

    }

    @org.junit.Test
    public void testParse() throws Exception {
        ISpectrum spectrum = mock(ISpectrum.class);
        when(spectrum.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY)).thenReturn("KKKKKK");
        when(spectrum.getPeaksCount()).thenReturn(200);
        ArrayList<IPeak> peaks = new ArrayList<IPeak>();
        peaks.add(new Peak(100, 100));
        when(spectrum.getPeaks()).thenReturn(peaks);
        when(spectrum.getPrecursorMz()).thenReturn(200f);
        when(spectrum.getPrecursorCharge()).thenReturn(-1);

        assertTrue(predicate.apply(spectrum));
    }
}