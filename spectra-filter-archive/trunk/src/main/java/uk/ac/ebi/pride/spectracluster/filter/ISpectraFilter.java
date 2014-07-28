package uk.ac.ebi.pride.spectracluster.filter;

import java.util.List;

/**
 * Interface for filtering spectra
 *
 * Two generics have been defined here:
 *
 * F: the output of the filter, can be Void if not output
 * S: object that represents input spectra
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectraFilter<F, S> {

    F filter(List<S> spectra);
}
