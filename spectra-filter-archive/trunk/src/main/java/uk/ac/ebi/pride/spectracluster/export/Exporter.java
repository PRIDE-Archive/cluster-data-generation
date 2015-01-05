package uk.ac.ebi.pride.spectracluster.export;

import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.spectracluster.archive.ArchiveSpectra;
import uk.ac.ebi.pride.spectracluster.filters.SpectrumPredicateParser;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.function.Functions;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;
import uk.ac.ebi.pride.spectracluster.util.function.spectrum.RemoveSpectrumEmptyPeakFunction;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Export spectra from PRIDE Archive to MGF files
 *
 * @author Steve Lewis
 * @date 21/05/2014
 */
public class Exporter {

    public static final String PRIDE_MZTAB_SUFFIX = ".pride.mztab";
    public static final String PRIDE_MGF_SUFFIX = ".pride.mgf";
    public static final String MGF_SUFFIX = ".mgf";
    public static final String INTERNAL_DIRECTORY = "internal";

    private final IFunction<ISpectrum, ISpectrum> filter;

    public Exporter(IFunction<ISpectrum, ISpectrum> filter) {
        this.filter = filter;
    }

    public void export(File inputDirectory, File outputDirectory) throws IOException {
        // output file
        File outFile = buildOutputFile(outputDirectory, inputDirectory);


        PrintWriter out = null;
        try {
            // find all the PRIDE generated mzTab files
            File projectInternalPath = new File(inputDirectory, INTERNAL_DIRECTORY);
            List<File> files = readMZTabFiles(inputDirectory);
            if (!files.isEmpty()) {
                out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));

                for (File mzTab : files) {
                    // map the relationship between mzTab file and its mgf files
                    ArchiveSpectra spec = buildArchiveSpectra(mzTab, projectInternalPath);
                    if (spec == null) {
                        System.err.println("Bad mzTab file " + mzTab);
                        continue;
                    }

                    // export spectra
                    MZTabProcessor processor = new MZTabProcessor(spec);
                    processor.handleCorrespondingMGFs(filter, out);

                    out.flush();
                }
            }
        } finally {
            if (out != null)
                out.close();
        }
    }

    private List<File> readMZTabFiles(final File pFile1) {
        File projectInternalPath = new File(pFile1, INTERNAL_DIRECTORY);
        List<File> ret = new ArrayList<File>();
        if (!projectInternalPath.exists()) {
            return ret;
        }

        File[] files = projectInternalPath.listFiles();
        if (files == null)
            return ret;

        for (File mzTab : files) {
            // searching for mztab file
            if (mzTab.getName().endsWith(PRIDE_MZTAB_SUFFIX)) {
                ret.add(mzTab);
            }
        }

        return ret;
    }

    /**
     * Build output file
     *
     * @param directory
     * @return given a project
     */
    private File buildOutputFile(File baseDirectory, File directory) {
        if (!baseDirectory.exists() && !baseDirectory.mkdirs())
            throw new IllegalStateException("bad base directory");

        String child = directory.getName() + MGF_SUFFIX;
        return new File(baseDirectory, child);
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
            ArchiveSpectra spectra = new ArchiveSpectra(mzTabFile);

            SortedMap<Integer, MsRun> msRunMap = mzTabFile.getMetadata().getMsRunMap();
            for (MsRun msRun : msRunMap.values()) {
                String msRunFile = msRun.getLocation().getFile();
                String msRunFileName = FilenameUtils.getName(msRunFile);
                String msRunFileNameWithoutExtension = FilenameUtils.removeExtension(msRunFileName);

                String mgfFileName = msRunFileNameWithoutExtension + PRIDE_MGF_SUFFIX;
                File mgfFile = new File(inputPath, mgfFileName);
                if (mgfFile.exists()) {
                    spectra.addMgfFile(mgfFile);
                }
            }

            return spectra;
        }
        return null;
    }

    /**
     * usage outputDirectory filterFile directoryToProcess
     *
     * @param args
     */
    /*
        <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
        <properties>
            <comment>Properties can be used to define the Predicates and Functions</comment>
            <entry key="identified.spectrum">true</entry>
            <entry key="minimum.number.of.peaks">100</entry>
            <entry key="with.precursors">true</entry>
        </properties>
     */
    public static void main(String[] args) throws IOException {
        int index = 0;
        File outputDirectory = new File(args[index++]);
        System.out.println("Output to: " + outputDirectory.getAbsolutePath());

        // parse all the filters
        File filtersFile = new File(args[index++]);
        IPredicate<ISpectrum> predicate = SpectrumPredicateParser.parse(filtersFile);

        // add function to remove empty peak lists
        RemoveSpectrumEmptyPeakFunction removeEmptyPeakFunction = new RemoveSpectrumEmptyPeakFunction();
        IFunction<ISpectrum, ISpectrum> condition = Functions.condition(removeEmptyPeakFunction, predicate);

        for (; index < args.length; index++) {
            String arg = args[index];
            File dir = new File(arg);
            Exporter exp = new Exporter(condition);
            exp.export(dir, outputDirectory);
            System.out.println("exported " + dir);
        }
    }
}
