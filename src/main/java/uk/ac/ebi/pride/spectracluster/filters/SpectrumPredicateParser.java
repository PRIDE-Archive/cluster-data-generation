package uk.ac.ebi.pride.spectracluster.filters;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;
import uk.ac.ebi.pride.spectracluster.util.predicate.Predicates;
import uk.ac.ebi.pride.spectracluster.util.predicate.spectrum.IdentifiedPredicate;
import uk.ac.ebi.pride.spectracluster.util.predicate.spectrum.MinimumNumberOfPeaksPredicate;
import uk.ac.ebi.pride.spectracluster.util.predicate.spectrum.WithPrecursorPredicate;

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

        List<IPredicate<ISpectrum>> predicates = new ArrayList<IPredicate<ISpectrum>>();

        // filter by identified spectrum
        String filterIdentifiedSpectrum = properties.getProperty("identified.spectrum");
        if (filterIdentifiedSpectrum != null && "true".equalsIgnoreCase(filterIdentifiedSpectrum)) {
            predicates.add(new IdentifiedPredicate());
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

        return Predicates.and(predicates);
    }
}
