package uk.ac.ebi.pride.spectracluster.export;

import com.lordjoe.filters.TypedFilterCollection;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.spectracluster.archive.ArchiveSpectra;
import uk.ac.ebi.pride.spectracluster.filters.SpectrumFilters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * uk.ac.ebi.pride.spectracluster.export.Exporter
 *
 * @author Steve Lewis
 * @date 21/05/2014
 */
public class Exporter {

    public static final String PRIDE_MZTAB_SUFFIX = ".pride.mztab";
    public static final String PRIDE_MGF_SUFFIX = ".pride.mgf";
    public static final String MGF_SUFFIX = ".mgf";
    public static final String INTERNAL_DIRECTORY = "internal";

    private final File outputDirectory;
    private final File activeDirectory;
    private final String projectAccession;
    private final TypedFilterCollection filters;

    public Exporter(File outputDirectory, File activeDirectory, TypedFilterCollection filters) {
        this.filters = filters;
        this.outputDirectory = outputDirectory;
        this.activeDirectory = activeDirectory;


        String name = activeDirectory.getAbsolutePath();
        name = name.replace("\\", "/");
        projectAccession = name.substring(name.lastIndexOf("/") + 1);

    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public File getActiveDirectory() {
        return activeDirectory;
    }

    public TypedFilterCollection getFilters() {
        return filters;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getProjectAccession() {
        return projectAccession;
    }

    public void exportDirectory() throws IOException {
        // output file
        File outFile = buildOutputFile(outputDirectory, activeDirectory);


        PrintWriter out = null;
        try {
            // find all the PRIDE generated mzTab files
            File projectInternalPath = new File(activeDirectory, INTERNAL_DIRECTORY);
            List<File> files = readMZTabFiles(activeDirectory);
            if (!files.isEmpty()) {
                out = new PrintWriter(new FileWriter(outFile));

                for (File mzTab : files) {
                    // map the relationship between mzTab file and its mgf files
                    ArchiveSpectra spec = buildArchiveSpectra(mzTab, projectInternalPath);
                    if (spec == null) {
                        System.err.println("Bad mzTab file " + mzTab);
                        continue;
                    }

                    // export spectra
                    MZTabProcessor processor = new MZTabProcessor(spec);
                    processor.handleCorrespondingMGFs(filters, out);

                    out.flush();
                }
            }
        } finally {
            if (out != null)
                out.close();
        }
    }


    protected List<File> readMZTabFiles(final File pFile1) {
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
    protected File buildOutputFile(File baseDirectory, File directory) {
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
    protected ArchiveSpectra buildArchiveSpectra(File mzTab, File inputPath) throws IOException {
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
    Uses a filter file like
       <Filters>
           <FileFilter extension="mgf"/>
           <!-- will not run without spectrum class
          <Filter applicableType="uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum" charge="2" />
          -->
           <SpectrumFilter identified="true"/>
           <SpectrumFilter minimumLength="100"/>
           <SpectrumFilter withPrecursors="true"/>

       </Filters>
     */
    public static void main(String[] args) throws IOException {
        int index = 0;
        File outputDirectory = new File(args[index++]);
        System.out.println("Output to: " + outputDirectory.getAbsolutePath());

        TypedFilterCollection.registerHandler(SpectrumFilters.TAG, new SpectrumFilters.SpectrumFilterSaxHandler(null));
        File filtersFile = new File(args[index++]);
        TypedFilterCollection filters = TypedFilterCollection.parse(filtersFile);
        for (; index < args.length; index++) {
            String arg = args[index];
            File dir = new File(arg);
            Exporter exp = new Exporter(outputDirectory, dir, filters);
            exp.exportDirectory();
            System.out.println("exported " + dir);
        }
    }
}
