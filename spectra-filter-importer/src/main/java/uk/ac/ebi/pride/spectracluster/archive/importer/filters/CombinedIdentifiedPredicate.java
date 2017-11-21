package uk.ac.ebi.pride.spectracluster.archive.importer.filters;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.KnownProperties;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;

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
 * Created by ypriverol (ypriverol@gmail.com) on 03/10/2017.
 */
public class CombinedIdentifiedPredicate implements IPredicate<ISpectrum> {

    private boolean identified = true;
    private boolean unidentified = true;

    public CombinedIdentifiedPredicate(){
    }

    public CombinedIdentifiedPredicate(boolean identified){
        this.identified = identified;
        this.unidentified = true;
    }

    public void setIdentified(boolean identified) {
        this.identified = identified;
    }

    public void setUnidentified(boolean unidentified) {
        this.unidentified = unidentified;
    }

    @Override
    public boolean apply(ISpectrum spectrum) {
        return (spectrum.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY) != null && identified) || (spectrum.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY) == null && unidentified);
    }

}