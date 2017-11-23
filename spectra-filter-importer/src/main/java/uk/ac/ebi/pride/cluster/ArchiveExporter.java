package uk.ac.ebi.pride.spectracluster;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.spectracluster.archive.importer.filters.SpectrumPredicateParser;
import uk.ac.ebi.pride.spectracluster.archive.importer.process.ArchiveSpectra;
import uk.ac.ebi.pride.spectracluster.archive.importer.process.MZTabProcessor;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.function.Functions;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;
import uk.ac.ebi.pride.spectracluster.util.function.spectrum.RemoveSpectrumEmptyPeakFunction;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;
import uk.ac.ebi.pride.spectracluster.utilities.FileTypes;
import uk.ac.ebi.pride.spectracluster.utilities.mztab.IFilter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Export spectra from PRIDE Archive to MGF files. This Script take a folder to tools the data.
 *
 * @author Yasset Perez-Riverol
 */
public class ArchiveExporter {

    private final IFunction<ISpectrum, ISpectrum> spectrumFilter;

    private final Map<String, IFilter> idPredicates;

    private static final Logger LOGGER = Logger.getLogger(MZTabProcessor.class);

    public ArchiveExporter(IFunction<ISpectrum, ISpectrum> spectrumFilter) {
        this.spectrumFilter = spectrumFilter;
        this.idPredicates = null;
    }

    public ArchiveExporter(IFunction<ISpectrum, ISpectrum> spectrumFilter, Map<String, IFilter> idPredicates){
        this.spectrumFilter = spectrumFilter;
        this.idPredicates = idPredicates;
    }

    /**
     * Convert the input from a input directory (mztab + mgf) into and enriched mgf file format containing
     * the spectra and the identified sequence.
     * @param inputDirectory input directory with the mztab + mgf files.
     * @param outputDirectory output directory to write the enriched mgf.
     * @throws IOException
     */
    public void export(File inputDirectory, File outputDirectory, Boolean splitOuput) throws IOException {
        // output file

        File output;

        if(splitOuput)
            output = buildFolderOutput(outputDirectory, inputDirectory);
        else
            output = buildOutputFile(outputDirectory, inputDirectory);

        PrintWriter out = null;
        try {
            // find all the PRIDE generated mzTab files
            File projectInternalPath = new File(inputDirectory, FileTypes.INTERNAL_DIRECTORY);
            List<File> files = readMZTabFiles(inputDirectory);

            if (!files.isEmpty()) {
                for (File mzTab : files) {
                    // map the relationship between mzTab file and its mgf files
                    ArchiveSpectra spec = buildArchiveSpectra(mzTab, projectInternalPath);
                    if (spec == null) {
                        System.err.println("Bad mzTab file " + mzTab);
                        continue;
                    }
                    try{
                        MZTabProcessor processor = new MZTabProcessor(idPredicates, spec);
                        try{
                            processor.proccessPSMs();
                        }catch (IOException | IllegalArgumentException exception){
                            LOGGER.error("The current mztab can't provide Peptide|FDR information " + exception.getMessage());
                        }
                        if(!splitOuput)
                            if(out == null)
                                out = new PrintWriter(new BufferedWriter(new FileWriter(output)), false);
                            else
                                out = new PrintWriter(new BufferedWriter(new FileWriter(output, true)), false);
                        else {
                            File outputMzTabFile = buildOutputFile(output, processor.getAssayId());
                            out = new PrintWriter(new BufferedWriter(new FileWriter(outputMzTabFile)), false);
                        }
                        processor.handleCorrespondingMGFs(spectrumFilter, out);
                        out.flush();
                    }catch (Exception exception){
                        LOGGER.error("The mzTab is not correct, or valid" + exception.getMessage());
                    }
                }
            }
        } finally {

        }
    }

    private List<File> readMZTabFiles(final File pFile1) {

        File projectInternalPath = new File(pFile1, FileTypes.INTERNAL_DIRECTORY);
        List<File> ret = new ArrayList<>();
        if (!projectInternalPath.exists()) {
            return ret;
        }

        File[] files = projectInternalPath.listFiles();
        if (files == null)
            return ret;

        // searching for mztab file
        ret = Arrays.stream(files).filter(mzTab -> mzTab.getName().endsWith(FileTypes.PRIDE_MZTAB_SUFFIX)).collect(Collectors.toList());
        return ret;
    }


    /**
     * Returns for a given File for a fileOutput and and inputDirectory.
     * @param inputDirectory Input directory
     * @return given a project
     */
    private File buildOutputFile(File outputDirectory, File inputDirectory) {
        if (!outputDirectory.exists() && !outputDirectory.mkdirs())
            throw new IllegalStateException("bad base directory");

        String child = inputDirectory.getName() + FileTypes.MGF_SUFFIX;
        return new File(outputDirectory, child);
    }

    /**
     *
     * @param inputFileName that will be used.
     * @return given a project
     */
    private File buildOutputFile(File outputDirectory, String inputFileName) {
        if (!outputDirectory.exists() && !outputDirectory.mkdirs())
            throw new IllegalStateException("bad base directory");

        String child = inputFileName + FileTypes.MGF_SUFFIX;
        return new File(outputDirectory, child);
    }

    /**
     * Build the directories by Submissions PX including all the files inside
     * @param outputDirectory output folder, where to put all the mgf files.
     * @param inputDirectory input file for the project
     * @return return the new folder.
     */
    private File buildFolderOutput(File outputDirectory, File inputDirectory){
        if (!outputDirectory.exists() && !outputDirectory.mkdirs())
            throw new IllegalStateException("Bad base directory");

        String child = inputDirectory.getName();
        File outputFolder = new File(outputDirectory, child);

        if(!outputFolder.mkdir() && !outputFolder.exists()){
            throw new IllegalStateException("Bad base directory");
        }

        return outputFolder;

    }

    /**
     * Build an ArchiveSpectra object for a given mzTab object
     *
     * @param inputPath
     * @param mzTab
     * @return
     * @throws IOException
     */
    private ArchiveSpectra buildArchiveSpectra(File mzTab, File inputPath) throws IOException {
        // parse mztab object

        MZTabFileParser mzTabFileParser = new MZTabFileParser(mzTab, System.out);
        MZTabFile mzTabFile = mzTabFileParser.getMZTabFile();

        // check whether there is any parsing error
        MZTabErrorList errorList = mzTabFileParser.getErrorList();
        if (errorList.isEmpty()) {

            // construct ArchiveSpectra object
            ArchiveSpectra spectra = new ArchiveSpectra(mzTabFile, mzTab);

            SortedMap<Integer, MsRun> msRunMap = mzTabFile.getMetadata().getMsRunMap();
            msRunMap.values().stream().forEach(msRun -> {
                String msRunFile = msRun.getLocation().getFile();
                String msRunFileName = FilenameUtils.getName(msRunFile);
                String msRunFileNameWithoutExtension = FilenameUtils.removeExtension(msRunFileName);

                String mgfFileName = msRunFileNameWithoutExtension + FileTypes.PRIDE_MGF_SUFFIX;
                File mgfFile = new File(inputPath, mgfFileName);
                if (mgfFile.exists()) {
                    spectra.addMgfFile(mgfFile);
                }
            });

            return spectra;
        }
        return null;
    }
}
