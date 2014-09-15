package uk.ac.ebi.pride.spectracluster.export;

/**
 * uk.ac.ebi.pride.spectracluster.export.MZTabProcessor
 * User: Steve
 * Date: 8/6/2014
 */


// todo rewrite after new interface

import com.lordjoe.filters.TypedFilterCollection;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.spectracluster.archive.ArchiveSpectra;
import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumAppender;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.KnownProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.export.MZTabProcessor
 *
 * @author Steve Lewis
 * @date 22/05/2014
 */
public class MZTabProcessor {

    private final Map<String, Protein> idToProtein = new HashMap<String, Protein>();
    // For different PSMs, they could have the same spectra reference
    private final Map<PSM, String> psmToSpectrum = new HashMap<PSM, String>();
    private final Map<String, MsRun> fileToMsRun = new HashMap<String, MsRun>();
    private final ArchiveSpectra archiveSpectra;
    private final Collection<String> taxonomyIds = new HashSet<String>();

    public MZTabProcessor(ArchiveSpectra th) {
        archiveSpectra = th;
        if (archiveSpectra.getMzTabFile() == null)
            return;
        addMzTabHandler(archiveSpectra);
    }

    public Collection<String> getTaxonomyId() {
        return taxonomyIds;
    }

    public ArchiveSpectra getArchiveSpectra() {
        return archiveSpectra;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Protein getProtein(String proteinAccession) {
        return idToProtein.get(proteinAccession);
    }

    public int handleCorrespondingMGFs(TypedFilterCollection filters, Appendable out) {
        int totalWritten = 0;
        for (File run : archiveSpectra.getMgfFiles()) {
            totalWritten += handleMFGFile(run, filters, out);
        }
        return totalWritten;
    }

    protected int handleMFGFile(File file, TypedFilterCollection filters, Appendable out) {
        int totalWritten = 0;
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(file));
            ISpectrum psm = ParserUtilities.readMGFScan(rdr);
            while (psm != null) {
                if (processPSM(psm, filters, out))
                    totalWritten++;
                psm = ParserUtilities.readMGFScan(rdr);
            }
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
        return totalWritten;
    }

    protected List<PSM> getPSM(String spectrumId) {
        String[] parts = spectrumId.split(";");
        if (parts.length < 3)
            throw new IllegalStateException("Wrongly formatted spectrum id: " + spectrumId);

        // get ms run
        MsRun msRun = fileToMsRun.get(parts[1]);
        if (msRun == null) {
            throw new IllegalStateException("Failed to find MS run for spectrum: " + spectrumId);
        }

        ArrayList<PSM> psms = new ArrayList<PSM>(1);
        // spectrum id
        String spectrumRef = msRun.getReference()+ ":" + parts[2];

        for (Map.Entry<PSM, String> psmStringEntry : psmToSpectrum.entrySet()) {
            if (psmStringEntry.getValue().equals(spectrumRef)) {
                psms.add(psmStringEntry.getKey());
            }
        }

        return psms;
    }

    protected boolean processPSM(ISpectrum spectrum, TypedFilterCollection filters, Appendable out) {
        final String id = spectrum.getId();

        if (!getTaxonomyId().isEmpty()) {
            String species = combineSpecies(taxonomyIds);
            spectrum.setProperty(KnownProperties.TAXONOMY_KEY, species);
        }

        List<PSM> peptides = getPSM(id);
        if (!peptides.isEmpty()) {
            String sequence = combinePeptideSequences(peptides);
            spectrum.setProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY, sequence);

            String modifications = combineModifications(peptides);
            if (modifications != null) {
                spectrum.setProperty(KnownProperties.MODIFICATION_KEY, modifications);
            }

            // TODO: disabled protein accession to avoid causing problem in clustering process, this can be enabled in the future
//            String proteinAccession = peptides.getAccession();
//            if (proteinAccession != null) {
//                Protein protein = idToProtein.get(proteinAccession);
//                if (protein != null) {
//                    String database = protein.getDatabase();
//                    spectrum.setProperty(KnownProperties.PROTEIN_KEY, database + ":" + proteinAccession);
//                }
//            }
        }

        ISpectrum passed = (ISpectrum) filters.passes(spectrum);

        final boolean ret = passed != null;
        if (ret)
            MGFSpectrumAppender.INSTANCE.appendSpectrum(out, spectrum);

        return ret; // true if appended
    }

    private String combineSpecies(Collection<String> taxonomyIds) {
        String species = "";

        for (String taxonomyId : taxonomyIds) {
            species += taxonomyId + ",";
        }

        return species.substring(0, species.length() - 1);
    }

    private String combinePeptideSequences(Collection<PSM> psms) {
        String sequences = "";

        for (PSM psm : psms) {
            sequences += psm.getSequence() + ",";
        }

        return sequences.substring(0, sequences.length() - 1);
    }

    private String combineModifications(Collection<PSM> psms) {
        String modifications = "";

        for (PSM psm : psms) {
            SplitList<Modification> mods = psm.getModifications();
            if (mods != null && !mods.isEmpty()) {
                modifications += mods.toString() + ";";
            }
        }

        if (modifications.length() > 0) {
            return modifications.substring(0, modifications.length() - 1);
        } else {
            return null;
        }
    }

    protected void addMzTabHandler(ArchiveSpectra tab) {
        MZTabFile mzTab = tab.getMzTabFile();
        getTaxonomyIdFromHeader(mzTab);
        Collection<PSM> psMs = mzTab.getPSMs();
        handleMsRuns(mzTab);
        handleProteins(mzTab);
        handlePSMs(psMs);

    }

    private void handleMsRuns(MZTabFile mzTab) {
        SortedMap<Integer, MsRun> msRunMap = mzTab.getMetadata().getMsRunMap();
        for (MsRun msRun : msRunMap.values()) {
            String msRunFile = msRun.getLocation().getFile();
            String msRunFileName = new File(msRunFile).getName();
            fileToMsRun.put(msRunFileName, msRun);
        }
    }

    private void getTaxonomyIdFromHeader(MZTabFile mzTabs) {
        Metadata metadata = mzTabs.getMetadata();
        SortedMap<Integer, Sample> sampleMap = metadata.getSampleMap();
        for (Integer index : sampleMap.keySet()) {
            Sample s = sampleMap.get(index);
            List<Param> speciesList = s.getSpeciesList();
            for (Param species : speciesList) {
                taxonomyIds.add(species.getAccession());
            }
        }
    }

    protected void handlePSMs(Collection<PSM> psMs) {
        for (PSM psM : psMs) {
            SplitList<SpectraRef> spectraRefs = psM.getSpectraRef();
            // TODO: For the moment, we are ignoring the cases where there are multiple spectrum references
            // TODO: this case doesn't exist in our current system
            if (spectraRefs != null && !spectraRefs.isEmpty()) {
                SpectraRef spectraRef = spectraRefs.get(0);
                psmToSpectrum.put(psM, spectraRef.toString());
            }
        }
    }

    protected void handleProteins(MZTabFile mzTabs) {
        Collection<Protein> proteins = mzTabs.getProteins();
        for (Protein protein : proteins) {
            String accession = protein.getAccession();
            idToProtein.put(accession, protein);
        }
    }
}
