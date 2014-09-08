package uk.ac.ebi.pride.spectracluster.export;

/**
 * uk.ac.ebi.pride.spectracluster.export.MZTabProcessor
 * User: Steve
 * Date: 8/6/2014
 */


// todo rewrite after new interface

import com.lordjoe.filters.*;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.spectracluster.filter.archive.*;
import uk.ac.ebi.pride.spectracluster.io.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.export.MZTabProcessor
 *
 * @author Steve Lewis
 * @date 22/05/2014
 */
public class MZTabProcessor {
    public static final int MAX_MS_RUNS = 1000;

    /**
     * convert a modificaction string pt a PSM
     * @param p
     * @return
     */
    public static final String buildModificationString(PSM p) {
        SplitList<Modification> modifications = p.getModifications();
        int size = modifications.size();
        boolean empty = modifications.isEmpty();
        if (!empty && size > 0) {
            StringBuilder sb = new StringBuilder();
            boolean isFirst = false;
            String s = modifications.toString();
            for (Modification modification : modifications) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    sb.append(",");
                }
                sb.append(modification.toString());
            }

            return sb.toString();
        }
        else {
            return null;
        }
    }


    private final Exporter exporter;
    @SuppressWarnings("UnusedDeclaration")
    private Map<String, Protein> idToProtein = new HashMap<String, Protein>();
    Map<String, PSM> spectrumToPSM = new HashMap<String, PSM>();
    private final ArchiveSpectra tabHandler;
    private String accession;

    public MZTabProcessor(Exporter exporter, ArchiveSpectra th) {
        this.exporter = exporter;
        tabHandler = th;
        if (tabHandler.getMzTabFile() == null)
            return;
        addTabHandler(tabHandler);
    }

    public String getAccession() {
        return accession;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Exporter getExporter() {
        return exporter;
    }

    public ArchiveSpectra getTabHandler() {
        return tabHandler;
    }

//    protected void buildMSRunFiles(final ArchiveSpectra e) {
//
//        String fileName = "";
//        MZTabFile mzTabFile = e.getMzTabFile();
//        final SortedMap<Integer, MsRun> msRunMap = mzTabFile.getMetadata().getMsRunMap();
//        for (int i = 1; i < MAX_MS_RUNS; i++) {
//            final MsRun msRun = msRunMap.get(i);
//            if (msRun == null)
//                break;
//            idToProcessor.put(i, new MSRunProcessor(this));
//            final URL location = msRun.getLocation();
//            fileName = location.getPath();
//            fileName = fileName.substring(fileName.lastIndexOf("/"));
//            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
//            fileName = fileName.replace(".xml", ""); // drop .xml
//            final File baseDirectory = getExporter().getActiveDirectory();
//            final File generatedDirectory = new File(baseDirectory, "generated");
//            final File e1 = new File(generatedDirectory, fileName + ".pride.mgf");
//            msRunFiles.put(msRun,e1);
//        }
//
//    }

    @SuppressWarnings("UnusedDeclaration")
    public Protein getProtein(String proteinAccession) {
        return idToProtein.get(proteinAccession);
    }

    public int handleCorrespondingMGFs(Appendable out) {
        int totalWritten = 0;
        for (File run : tabHandler.getMgfFiles()) {
            totalWritten += handleMFGFile(run, out);
        }
        return totalWritten;
    }

    protected int handleMFGFile(File file, Appendable out) {
        int numberProcessed = 0;
        int totalWritten = 0;
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(file));
            ISpectrum psm = ParserUtilities.readMGFScan(rdr);
            while (psm != null) {
                if (processPSM(psm, out))
                    totalWritten++;
                psm = ParserUtilities.readMGFScan(rdr);
                numberProcessed++;
            }
        }
        catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
        return totalWritten;
    }

    protected PSM getPSM(String peptideId) {
        int index = peptideId.indexOf("spectrum=");
        if (index < 0)
            return null;
        String number = peptideId.substring(index + "spectrum=".length());
        final PSM psm = spectrumToPSM.get(number);
        return psm;
    }

    protected boolean processPSM(ISpectrum spectrum, Appendable out) {
        final String id = spectrum.getId();

        if (getAccession() != null)
            spectrum.setProperty(KnownProperties.TAXONOMY_KEY, getAccession());

        PSM peptide = getPSM(id);
        if (peptide != null) {
            final String sequence = peptide.getSequence();
            spectrum.setProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY, sequence);

            SplitList<Modification> modifications = peptide.getModifications();
            if(modifications != null && !modifications.isEmpty())   {
                spectrum.setProperty(KnownProperties.MODIFICATION_KEY,modifications.toString()  );

            }

            String proteinAccession = peptide.getAccession();
            if (proteinAccession != null) {
                final Protein protein = idToProtein.get(proteinAccession);
                if (protein != null) {
                    final String database = protein.getDatabase();
                    spectrum.setProperty(KnownProperties.PROTEIN_KEY, database + ":" + proteinAccession);
                }
            }
            exporter.incrementIdentifiedSpectra();
        }
        else {
            exporter.incrementUnidentifiedSpectra();
        }

        final TypedFilterCollection filters = getExporter().getFilters();
        ISpectrum passed = (ISpectrum) filters.passes(spectrum);

        final boolean ret = passed != null;
        if (ret)
            MGFSpectrumAppender.INSTANCE.appendSpectrum(out, spectrum);

        return ret; // true if appended
    }

    protected void addTabHandler(ArchiveSpectra tab) {
        final MZTabFile mzTabs = tab.getMzTabFile();
        setAccessionFromHeader(mzTabs);
        //     buildMSRunFiles(tab);
        final Collection<PSM> psMs = mzTabs.getPSMs();
        handleProteins(mzTabs);
        handlePSMs(psMs);

    }

    private void setAccessionFromHeader(MZTabFile mzTabs) {
        final Metadata metadata = mzTabs.getMetadata();
        final SortedMap<Integer, Sample> sampleMap = metadata.getSampleMap();
        if (sampleMap.size() == 1) {
            for (Integer index : sampleMap.keySet()) {
                Sample s = sampleMap.get(index);
                final List<Param> speciesList = s.getSpeciesList();
                if (speciesList.size() == 1) {
                    Param specias = speciesList.get(0);
                    accession = specias.getAccession();
                }
            }

        }
        else {
            System.out.println("More than 1 sample");
        }
    }

    protected void handlePSMs(Collection<PSM> psMs) {
        for (PSM psM : psMs) {
            final String psm_id = psM.getPSM_ID();
            spectrumToPSM.put(psm_id, psM);
//            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
//            final String sequence = psM.getSequence();
//            final SplitList<SpectraRef> spectraRef = psM.getSpectraRef();
//            for (SpectraRef ref : spectraRef) {
//                final MsRun msRun = ref.getMsRun();
//                final MSRunProcessor msRunProcessor = idToProcessor.get(msRun.getId());
//                //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
//                final String reference = ref.getReference();
//                msRunProcessor.addPSM(psm_id, psM);
//            }

        }
    }

    protected void handleProteins(MZTabFile mzTabs) {
        final Collection<Protein> proteins = mzTabs.getProteins();
        for (Protein protein : proteins) {
            final String accession = protein.getAccession();
            idToProtein.put(accession, protein);
        }
    }
}
