package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.filters.TypedFilterCollection;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.filters.SpectrumFilters;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.io.File;

import static junit.framework.Assert.assertEquals;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class FilterLoadingTest {
    private TypedFilterCollection filters;


    @Before
    public void setUp() throws Exception {
        ClassLoader classLoader = FilterLoadingTest.class.getClassLoader();
        File filterXmlFile = new File(classLoader.getResource("IdentifiedFilter.xml").toURI());
        TypedFilterCollection.registerHandler(SpectrumFilters.TAG, new SpectrumFilters.SpectrumFilterSaxHandler(null));
        filters = TypedFilterCollection.parse(filterXmlFile);
    }

    @Test
    public void testNumberOfFiltersCorrect() throws Exception {
        assertEquals(4, filters.getApplicableFilters(ISpectrum.class).size());
        assertEquals(1, filters.getApplicableFilters(File.class).size());
    }

}
