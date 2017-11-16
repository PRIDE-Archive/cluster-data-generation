package uk.ac.ebi.pride.spectracluster.export;

import de.mpc.pia.intermediate.Modification;
import de.mpc.pia.intermediate.compiler.PIACompiler;
import de.mpc.pia.intermediate.compiler.PIASimpleCompiler;
import de.mpc.pia.intermediate.compiler.parser.InputFileParserFactory;
import de.mpc.pia.modeller.PIAModeller;
import de.mpc.pia.modeller.psm.ReportPSM;
import de.mpc.pia.modeller.psm.ReportPSMSet;
import de.mpc.pia.modeller.report.filter.AbstractFilter;
import de.mpc.pia.modeller.report.filter.FilterComparator;
import de.mpc.pia.modeller.report.filter.impl.PSMScoreFilter;
import de.mpc.pia.modeller.score.ScoreModel;
import de.mpc.pia.modeller.score.ScoreModelEnum;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.spectracluster.archive.ArchiveSpectra;
import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumAppender;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.mztab.CVTermConstant;
import uk.ac.ebi.pride.spectracluster.mztab.FDRFilter;
import uk.ac.ebi.pride.spectracluster.mztab.IDFilterName;
import uk.ac.ebi.pride.spectracluster.mztab.IFilter;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.KnownProperties;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Steve Lewis
 * @date 22/05/2014
 */
public class MZTabProcessor {

    private static final Logger LOGGER = Logger.getLogger(MZTabProcessor.class);

    private final Map<String, Protein>   idToProtein = new HashMap<>();
    private final Map<ReportPSM, String> psmToSpectrum = new HashMap<>();  // For different PSMs, they could have the same spectra reference
    private final Map<String, MsRun>     fileToMsRun = new HashMap<>();
    private final ArchiveSpectra         archiveSpectra;
    private final Collection<String>     taxonomyIds = new HashSet<>();
    private final Map<String, IFilter>   idPredicates;
    private final Long fileID = 1L;

    private Metadata metadata = new Metadata(); // We need to parse the corresponding metadata to see the Assay information.

    public MZTabProcessor(ArchiveSpectra th) {
        archiveSpectra = th;
        idPredicates = new HashMap<>();
        if (archiveSpectra.getMzTabFile() == null)
            return;
        addMzTabHandler(archiveSpectra, null);
    }

    public MZTabProcessor(Map<String, IFilter> idPredicates, ArchiveSpectra th) throws IOException {
        archiveSpectra = th;
        this.idPredicates = idPredicates;
        if (archiveSpectra.getMzTabFile() == null) {
            return;
        }

        MZTabFileParser mzTabParser = new MZTabFileParser(th.getSource(), new FileOutputStream(th.getSource().getAbsolutePath() + "errors.out"));
        metadata = mzTabParser.getMZTabFile().getMetadata();
    }

    /**
     * This method process the file adding all PSMs information.
     */
    public void proccessPSMs() throws IOException{

        PIAModeller modeller = computeFDRPSMLevel(archiveSpectra.getSource());

        if(modeller == null){
            LOGGER.error("ERROR | SPECTRUM INFORMATION | PSM NOT AVAILABLE | " + archiveSpectra.getSource().getName());
            return;
        }

        if(!modeller.getPSMModeller().getAllFilesHaveFDRCalculated() || modeller.getPSMModeller().getFileFDRData().get(fileID).getNrFDRGoodDecoys() == 0){
            LOGGER.error("ERROR | FDR | INFORMATION about FDR NOT AVAILABLE for: " + archiveSpectra.getSource().getName());
            for(ReportPSMSet idSet: modeller.getPSMModeller().getReportPSMSets().values())
                LOGGER.error("ERROR | FDR | " + idSet.toString());
            return;
        }
        addMzTabHandler(archiveSpectra, modeller);

    }

    /**
     * Compute the FDR at PSM level for each mzTab file. The input of the file is an mzTab file and
     * a PIAModeller is returned containing the FDR objects.
     *
     * @param mzTabFile the mztab file submitted.
     * @return PIAModeller object with the FDR calculations.
     * @throws IOException
     */
    private PIAModeller computeFDRPSMLevel(File mzTabFile) throws IOException {

        PIAModeller piaModeller = null;
        try{
            PIACompiler piaCompiler = new PIASimpleCompiler();
            piaCompiler.getDataFromFile(mzTabFile.getName(), mzTabFile.getAbsolutePath(),null, InputFileParserFactory.InputFileTypes.MZTAB_INPUT.getFileTypeShort());
            piaCompiler.buildClusterList();
            piaCompiler.buildIntermediateStructure();

            if(piaCompiler.getAllPeptideSpectrumMatcheIDs() != null && !piaCompiler.getAllPeptideSpectrumMatcheIDs().isEmpty()){
                File inferenceTempFile = File.createTempFile(mzTabFile.getName(), ".tmp");
                piaCompiler.writeOutXML(inferenceTempFile);
                piaCompiler.finish();
                piaModeller = new PIAModeller(inferenceTempFile.getAbsolutePath());
                piaModeller.setCreatePSMSets(true);

                piaModeller.getPSMModeller().setAllDecoyPattern("searchengine");
                piaModeller.getPSMModeller().setAllTopIdentifications(0);

                // calculate peptide FDR
                piaModeller.getPeptideModeller().calculateFDR(fileID);

                // calculate FDR on PSM level
                piaModeller.getPSMModeller().calculateAllFDR();
                piaModeller.getPSMModeller().calculateCombinedFDRScore();

                if(inferenceTempFile.exists())
                    inferenceTempFile.deleteOnExit();
            }
        }catch (IllegalArgumentException e){
            LOGGER.error("ERROR | SPECTRUM INFORMATION | THE SEARCH ENGINE SCORE IS NOT SUPPORTED | " + e.getMessage());
        }
        return piaModeller;

    }

    public Collection<String> getTaxonomyId() {
        return taxonomyIds;
    }

    public int handleCorrespondingMGFs(IFunction<ISpectrum, ISpectrum> filters, Appendable out) throws IllegalStateException {
        return archiveSpectra.getMgfFiles().stream().mapToInt(run -> handleMFGFile(run, filters, out)).sum();
    }

    protected int handleMFGFile(File file, IFunction<ISpectrum, ISpectrum> filters, Appendable out) throws IllegalStateException{
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

    protected List<ReportPSM> getPSM(String spectrumId) throws IllegalStateException{

        String[] parts = spectrumId.split(";");
        if (parts.length < 3)
            throw new IllegalStateException("Wrongly formatted spectrum id: " + spectrumId);

        // get ms run
        MsRun msRun = fileToMsRun.get(parts[1]);
        if (msRun == null) {
            throw new IllegalStateException("Failed to find MS run for spectrum: " + spectrumId);
        }

        String spectrumRef = msRun.getReference() + ":" + parts[2];

        return psmToSpectrum.entrySet().parallelStream()
                .filter(psm -> psm.getValue().equals(spectrumRef))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList<ReportPSM>::new));
    }

    protected boolean processPSM(ISpectrum spectrum, IFunction<ISpectrum, ISpectrum> filters, Appendable out) throws IllegalStateException{
        final String id = spectrum.getId();

        if (!getTaxonomyId().isEmpty()) {
            String species = combineSpecies(taxonomyIds);
            spectrum.setProperty(KnownProperties.TAXONOMY_KEY, species);
        }

        try{
            List<ReportPSM> peptides = getPSM(id);
            if (!peptides.isEmpty()) {
                String sequence = combinePeptideSequences(peptides);
                spectrum.setProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY, sequence);

                String modifications = combineModifications(peptides);
                if (modifications != null) {
                    spectrum.setProperty(KnownProperties.MODIFICATION_KEY, modifications);
                }

                String decoyInformation = combineDecoyInformation(peptides);
                if(decoyInformation != null)
                    spectrum.setProperty(KnownProperties.PSM_DECOY_STATUS, decoyInformation);

                String combineScoresInformation = combinePeptideScores(peptides);
                if(combineScoresInformation != null)
                    spectrum.setProperty(KnownProperties.PSM_FDR_SCORES, combineScoresInformation);

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
        }catch( IllegalStateException exception){
            LOGGER.info("The Spectrum: " + id + " can't be found in the mzTab");
        }

        ISpectrum filteredSpectrum = filters.apply(spectrum);

        if (filteredSpectrum != null && validateSpectrum(spectrum)) {
            MGFSpectrumAppender.INSTANCE.appendSpectrum(out, spectrum);
            return true;
        }

        return false;
    }

    private boolean validateSpectrum(ISpectrum spectrum) {
        return spectrum.getPrecursorCharge() >= 0;
    }

    private String combinePeptideScores(List<ReportPSM> peptides) {
        StringBuilder peptideScore = new StringBuilder();

        for (ReportPSM psm : peptides) {
            peptideScore.append(ScoreModelEnum.PSM_LEVEL_COMBINED_FDR_SCORE.getShortName()).append(":").append(psm.getFDRScore().getValue());
            peptideScore.append(";");
        }
        return peptideScore.substring(0, peptideScore.length() - 1).replaceAll("NO-INFO", "");

    }

    private String combineSpecies(Collection<String> taxonomyIds) {
        StringBuilder species = new StringBuilder();

        for (String taxonomyId : taxonomyIds) {
            species.append(taxonomyId).append(",");
        }

        return species.substring(0, species.length() - 1);
    }

    private String combinePeptideSequences(Collection<ReportPSM> psms) {
        StringBuilder sequences = new StringBuilder();

        for (ReportPSM psm : psms) {
            sequences.append(psm.getSequence()).append(",");
        }

        return sequences.substring(0, sequences.length() - 1);
    }

    private String combineModifications(Collection<ReportPSM> psms) {
        StringBuilder modifications = new StringBuilder();

        for (ReportPSM psm : psms) {
            Map<Integer, Modification> mods = psm.getModifications();
            if (mods != null && !mods.isEmpty()) {
                for (Map.Entry entry: mods.entrySet()){
                    Modification mod = (Modification)entry.getValue();
                    Integer key = (Integer) entry.getKey();
                    modifications.append(key);
                    if(mod.getProbability() != null && mod.getProbability().size()>0){
                        for(ScoreModel score: mod.getProbability()){
                            if(score != null && CVTermConstant.getCVTermConstant(score.getAccession()) != null)
                                modifications.append("[")
                                        .append(CVTermConstant.getCVTermConstant(score.getAccession()).getName())
                                        .append("=").append(score.getValue())
                                        .append("]");
                        }
                    }
                    modifications.append("-").append(mod.getAccession());
                    modifications.append(",");
                }
                modifications = new StringBuilder(modifications.substring(0,modifications.length() -1));
                modifications.append(";");
            } else {
                modifications.append("NO-MOD;");
            }
        }
        return modifications.substring(0, modifications.length() - 1).replaceAll("NO-MOD", "");
    }

    private String combineDecoyInformation(Collection<ReportPSM> psms) {
        StringBuilder decoyInformation = new StringBuilder();

        for (ReportPSM psm : psms) {
            if (psm.getIsDecoy())
                decoyInformation.append("decoy:true").append(";");
            else
                decoyInformation.append("decoy:false;");
        }
        return decoyInformation.substring(0, decoyInformation.length() - 1).replaceAll("NO-INFO", "");
    }

    protected void addMzTabHandler(ArchiveSpectra tab, PIAModeller modeller) {
        MZTabFile mzTab = tab.getMzTabFile();
        getTaxonomyIdFromHeader(mzTab);
        Collection<PSM> psMs = mzTab.getPSMs();
        handleMsRuns(mzTab);
        handleProteins(mzTab);
        handlePSMs(psMs, modeller);
    }

    private void handleMsRuns(MZTabFile mzTab) {
        SortedMap<Integer, MsRun> msRunMap = mzTab.getMetadata().getMsRunMap();
        for (MsRun msRun : msRunMap.values()) {
            String msRunFile = msRun.getLocation().getFile();
            String msRunFileName = FilenameUtils.getName(msRunFile);
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

    protected void handlePSMs(Collection<PSM> psMs, PIAModeller modeller) {

        Map<String, String> spectraIdList = new HashMap<>();
        for(Map.Entry entry: modeller.getSpectraData().entrySet()){
            spectraIdList.put(entry.getKey().toString(), ((SpectraData) entry.getValue()).getName());
        }
        List<ReportPSM> reportPSMS;
        List<AbstractFilter> filters = new ArrayList<>();
        if(idPredicates != null && idPredicates.size() > 0 && modeller != null){
            if(idPredicates.containsKey(IDFilterName.PSM_FDR_FILTER.getName())){
                filters.add(new PSMScoreFilter(FilterComparator.less_equal, false,
                        ((FDRFilter)idPredicates.get(IDFilterName.PSM_FDR_FILTER.getName())).getFilterValue(), ScoreModelEnum.PSM_LEVEL_FDR_SCORE.getShortName()));
                }
        }
        reportPSMS =  modeller.getPSMModeller().getFilteredReportPSMs(fileID, filters);
        LOGGER.info("NUMBER OF PSMs: " + reportPSMS.size());

        for (ReportPSM psM : reportPSMS) {
            // spectraRefs = psM.getSourceID();
            // TODO: For the moment, we are ignoring the cases where there are multiple spectrum references
            // TODO: this case doesn't exist in our current system
            //if (spectraRefs != null && !spectraRefs.isEmpty()) {
            //    SpectraRef spectraRef = spectraRefs.get(0);
                psmToSpectrum.put(psM, psM.getSpectrumTitle());
           // }
        }
    }

    protected void handleProteins(MZTabFile mzTabs) {
        Collection<Protein> proteins = mzTabs.getProteins();
        for (Protein protein : proteins) {
            String accession = protein.getAccession();
            idToProtein.put(accession, protein);
        }
    }

    /**
     * This function return the mztab id for the mzTab file in the form of a key-value pair
     * PXD003534-61487
     *
     * @return the ID of the file Project-Assay.
      */
    public String getAssayId(){
        if(metadata != null){
            return metadata.getMZTabID();
        }
        return null;
    }

}
