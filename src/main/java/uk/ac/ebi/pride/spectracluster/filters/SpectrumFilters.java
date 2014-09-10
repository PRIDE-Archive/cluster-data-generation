package uk.ac.ebi.pride.spectracluster.filters;

import com.lordjoe.filters.*;
import org.xml.sax.*;
import uk.ac.ebi.pride.spectracluster.export.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.annotation.*;

/**
 * uk.ac.ebi.pride.spectracluster.filters.SpectrumFilters
 *
 * @author Steve Lewis
 * @date 16/05/2014
 */
public class SpectrumFilters {

    public static final String TAG = "SpectrumFilter";

    static {
        TypedFilterCollection.registerHandler(TAG, new SpectrumFilterSaxHandler(null));
    }

    /**
     * filter of type file
     */
    protected static abstract class AbstractSpectrumTypedFilter extends AbstractTypedFilter<ISpectrum> {
        public AbstractSpectrumTypedFilter() {
            super(ISpectrum.class);
        }
    }


    /**
     * return true of a ISpectrum has a property for a given taxonomy
     *
     * @param length max allowed length
     * @return
     */
    public static ITypedFilter<ISpectrum> getTaxonomyFilter(final String accession) {
        return new AbstractSpectrumTypedFilter() {
            /**
             * return 0 if it passes the filter otherwise return null
             *
             * @param testObject
             * @return as above
             */
            @Override
            public ISpectrum passes(@Nonnull ISpectrum testObject) {
                if (accession.equals(testObject.getProperty(KnownProperties.TAXONOMY_KEY)))
                    return testObject;
                return null;
            }
        };
    }


    /**
     * return true of a ISpectrum a length greater than maxlength
     *
     * @param length max allowed length
     * @return
     */
    public static ITypedFilter<ISpectrum> getMinimumLengthFilter(final long length) {
        return new AbstractSpectrumTypedFilter() {
            /**
             * return 0 if it passes the filter otherwise return null
             *
             * @param testObject
             * @return as above
             */
            @Override
            public ISpectrum passes(@Nonnull ISpectrum testObject) {
                if (testObject.getPeaksCount() >= length)
                    return testObject;
                return null;
            }
        };
    }



    /**
     * return true of a ISpectrum mz is >= min and <= max
     *
     * @param length max allowed length
     * @return
     */
    public static ITypedFilter<ISpectrum> getPrecursorMZLimitsFilter(final double maxMZ, final double minMZ) {
        if(minMZ < 0 || minMZ >= maxMZ)
            throw new IllegalArgumentException("bad MZ Limits min " + minMZ + " max " + maxMZ);

        return new AbstractSpectrumTypedFilter() {
            /**
             * return 0 if it passes the filter otherwise return null
             *
             * @param testObject
             * @return as above
             */
            @Override
            public ISpectrum passes(@Nonnull ISpectrum testObject) {
                double mz = testObject.getPrecursorMz();
                if (mz < minMZ )
                    return null;
                if (  mz > maxMZ)
                     return null;
                 return testObject;
            }
        };
    }


    /**
     * return true of a ISpectrum a length greater than maxlength
     *
     * @param length max allowed length
     * @return
     */
    public static ITypedFilter<ISpectrum> getIdentifiedFilter() {
        return new AbstractSpectrumTypedFilter() {
            /**
             * return 0 if it passes the filter otherwise return null
             *
             * @param testObject
             * @return as above
             */
            @Override
            public ISpectrum passes(@Nonnull ISpectrum testObject) {
                ISpectrum ps = (ISpectrum) testObject;
                if (ps.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY) != null)
                    return testObject; // passes
                return null; // fails

            }
        };
    }


    public static final float MINIMUM_PRECURSOR_CHARGE = 10F;

    /**
     * return true of a ISpectrum  has known precursor mz and charge
     *
     * @param length max allowed length
     * @return
     */
    public static ITypedFilter<ISpectrum> getWithPrecursorsFilter() {
        return new AbstractSpectrumTypedFilter() {
            /**
             * return 0 if it passes the filter otherwise return null
             *
             * @param testObject
             * @return as above
             */
            @Override
            public ISpectrum passes(@Nonnull ISpectrum testObject) {
                      ISpectrum ps = (ISpectrum) testObject;
                    if (ps.getPrecursorCharge() == 0)
                        return null; // fails
                    // really testing 0 but this works
                    if (ps.getPrecursorMz() < MINIMUM_PRECURSOR_CHARGE)
                        return null; // fails
                    return testObject; // passes

             }
        };
    }


    /**
     * com.lordjoe.filters.FilterCollectionSaxHandler
     * reads xml document <Filters></Filters>
     *
     * @author Steve Lewis
     * @date 16/05/2014
     */
    public static class SpectrumFilterSaxHandler extends AbstractFilterCollectionSaxHandler<ISpectrum> {


        public SpectrumFilterSaxHandler(FilterCollectionSaxHandler parent) {
            super(TAG, parent);
        }


        @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
        @Override
        public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            super.handleAttributes(uri, localName, qName, attributes);    //To change body of overridden methods use ISpectrum | Settings | ISpectrum Templates.
            String value;

            value = attributes.getValue("identified");
            if (value != null) {
                setElementObject(getIdentifiedFilter());
                return;
            }

            value = attributes.getValue("withPrecursors");
            if ("true".equals(value)) {
                setElementObject(getWithPrecursorsFilter());
                return;
            }

            value = attributes.getValue("minimumLength");
            if (value != null) {
                int length = Integer.parseInt(value);
                setElementObject(getMinimumLengthFilter(length));
                return;
            }

            value = attributes.getValue("taxonomy");
            if (value != null) {
                 Exporter.setOnlyExportedTaxonomy(value);
                 setElementObject(getTaxonomyFilter(value));
                return;
            }
            value = attributes.getValue("maximumMZ");
             if (value != null) {
                 int max = Integer.parseInt(value);
                 int min = MZIntensityUtilities.LOWEST_USABLE_MZ;
                 value = attributes.getValue("minimumMZ");
                 if(value != null  )
                     min = Integer.parseInt(value);
                    setElementObject(getPrecursorMZLimitsFilter(max,min));
                 return;
             }

            StringBuilder sb = new StringBuilder();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < attributes.getLength(); i++) {
                sb.append(attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\" ");

            }
            throw new UnsupportedOperationException("no ISpectrum filters we understand " + sb);
        }

        @Override
        public void endElement(String elx, String localName, String el) throws SAXException {
            super.endElement(elx, localName, el);

        }


    }
}
