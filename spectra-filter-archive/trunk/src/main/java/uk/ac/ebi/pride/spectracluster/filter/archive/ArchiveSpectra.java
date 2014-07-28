package uk.ac.ebi.pride.spectracluster.filter.archive;

import uk.ac.ebi.pride.jmztab.model.MZTabFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * ArchiveSpectra represents a mztab file and its related mgf files
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ArchiveSpectra {

    private final MZTabFile mzTabFile;

    private final List<File> mgfFiles;

    public ArchiveSpectra(MZTabFile mzTabFile) {
        this.mzTabFile = mzTabFile;
        this.mgfFiles = new ArrayList<File>();
    }

    public MZTabFile getMzTabFile() {
        return mzTabFile;
    }

    public List<File> getMgfFiles() {
        return mgfFiles;
    }

    public void addMgfFile(File mgfFile) {
        this.mgfFiles.add(mgfFile);
    }

}
