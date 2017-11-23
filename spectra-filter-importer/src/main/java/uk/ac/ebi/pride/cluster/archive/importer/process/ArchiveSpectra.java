package uk.ac.ebi.pride.spectracluster.archive.importer.process;

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

    private final File    source;

    private final List<File> mgfFiles;

    public ArchiveSpectra(MZTabFile mzTabFile, File source) {
        this.mzTabFile = mzTabFile;
        this.mgfFiles = new ArrayList<>();
        this.source = source;
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

    public File getSource(){
        return this.source;
    }

}
