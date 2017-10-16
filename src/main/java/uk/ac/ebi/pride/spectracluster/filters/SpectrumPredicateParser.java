package uk.ac.ebi.pride.spectracluster.filters;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;
import uk.ac.ebi.pride.spectracluster.util.predicate.Predicates;
import uk.ac.ebi.pride.spectracluster.util.predicate.spectrum.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Parse a filter file and convert them into a Function
 *
 * @author Rui Wang
 * @version $Id$
 */
public final class SpectrumPredicateParser {

    public static IPredicate<ISpectrum> parse(File filterXmlFile) throws IOException {
        Properties properties = new Properties();
        properties.loadFromXML(new FileInputStream(filterXmlFile));

        List<IPredicate<ISpectrum>> predicates = new ArrayList<>();

        CombinedIdentifiedPredicate combinedIdentifiedPredicate = new CombinedIdentifiedPredicate();

        // filter by identified spectrum
        String filterIdentifiedSpectrum = properties.getProperty("identified.spectrum");
        if (!(filterIdentifiedSpectrum != null || "false".equalsIgnoreCase(filterIdentifiedSpectrum))) {
            combinedIdentifiedPredicate.setIdentified(false);
        }

        // filter by identified spectrum
        String filterUnidentifiedSpectrum = properties.getProperty("unidentified.spectrum");
        if (!(filterUnidentifiedSpectrum != null || "false".equalsIgnoreCase(filterUnidentifiedSpectrum))) {
            combinedIdentifiedPredicate.setUnidentified(false);
        }

        predicates.add(combinedIdentifiedPredicate);

        // filter by taxonomy id
        String filterByTaxonomy = properties.getProperty("taxonomy");
        if (filterByTaxonomy != null) {
            predicates.add(new TaxonomyPredicate(filterByTaxonomy));
        }

        // minimum number of peaks
        String miniNumOfPeaks = properties.getProperty("minimum.number.of.peaks");
        if (miniNumOfPeaks != null) {
            int miniPeakCount = Integer.parseInt(miniNumOfPeaks);
            predicates.add(new MinimumNumberOfPeaksPredicate(miniPeakCount));
        }

        // check whether precursors exists
        String precursorPresent = properties.getProperty("with.precursors");
        if (precursorPresent != null && "true".equalsIgnoreCase(precursorPresent)) {
            predicates.add(new WithPrecursorPredicate());
        }

        // check none negative peaks
        String noneNegativePeaks = properties.getProperty("none.negative.peaks");
        if (noneNegativePeaks != null && "true".equalsIgnoreCase(noneNegativePeaks)) {
            predicates.add(new NoneNegativePeakPredicate());
        }

        // check amino acid range
        String precursorMzRange = properties.getProperty("precursor.mz.range");
        if (precursorMzRange != null) {
            String[] parts = precursorMzRange.split("-");
            if (parts.length != 2) {
                throw new IllegalStateException("Illegal precursor mz range: " + precursorMzRange);
            }
            WithinPrecursorMZRangePredicate withinPrecursorMZRangePredicate = new WithinPrecursorMZRangePredicate(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
            predicates.add(withinPrecursorMZRangePredicate);
        }

        return Predicates.and(predicates);
    }
}
