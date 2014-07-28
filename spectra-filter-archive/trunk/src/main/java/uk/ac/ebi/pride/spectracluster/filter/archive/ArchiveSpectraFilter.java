package uk.ac.ebi.pride.spectracluster.filter.archive;

import uk.ac.ebi.pride.spectracluster.filter.ISpectraFilter;

import java.io.File;
import java.util.List;

/**
 * SpectraFilter implementation of PRIDE Archive
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ArchiveSpectraFilter implements ISpectraFilter<Void, ArchiveSpectra> {

    private final File outputFile;

    public ArchiveSpectraFilter(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public Void filter(List<ArchiveSpectra> spectra) {

        for (ArchiveSpectra archiveSpectra : spectra) {

            for (File mgfFile : archiveSpectra.getMgfFiles()) {
                System.out.println(mgfFile.getAbsoluteFile());
            }
        }

        return null;
    }
}
