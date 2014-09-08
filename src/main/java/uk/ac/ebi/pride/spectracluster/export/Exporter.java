package uk.ac.ebi.pride.spectracluster.export;

import com.lordjoe.filters.*;
import uk.ac.ebi.pride.spectracluster.*;
import uk.ac.ebi.pride.spectracluster.filter.archive.*;
import uk.ac.ebi.pride.spectracluster.filters.*;

import java.io.*;
import java.util.*;

//import com.lordjoe.filters.*;
//import uk.ac.ebi.pride.spectracluster.filters.*;
//import uk.ac.ebi.pride.spectracluster.retrievers.*;

/**
 * uk.ac.ebi.pride.spectracluster.export.Exporter
 *
 * @author Steve Lewis
 * @date 21/05/2014
 */
public class Exporter {

    private static String onlyExportedTaxonomy;

    public static String getOnlyExportedTaxonomy() {
        return onlyExportedTaxonomy;
    }

    public static void setOnlyExportedTaxonomy(String onlyExportedTaxonomy) {
        if (onlyExportedTaxonomy == null) {
            Exporter.onlyExportedTaxonomy = null;
            return;
        }
        if (onlyExportedTaxonomy.equals(Exporter.onlyExportedTaxonomy))
            return;
        if (Exporter.onlyExportedTaxonomy != null) {
            Exporter.onlyExportedTaxonomy = null;
        }
        else {
            Exporter.onlyExportedTaxonomy = onlyExportedTaxonomy;
        }
    }

    /**
     * @param directory
     * @return given a project
     */
    public static File fromDirectory(File baseDirectory, File directory) {
        if (!baseDirectory.exists() && !baseDirectory.mkdirs())
            throw new IllegalStateException("bad base directory");
        final String child = directory.getName() + ".mgf";
        File outFile = new File(baseDirectory, child);
        return outFile;
    }

    //   private final TypedFilterCollection filters;
    private final File outputDirectory;
    private final File activeDirectory;
    private final String experimentId;
    private final TypedFilterCollection filters;
    private int unidentifiedSpectra;
    private int identifiedSpectra;

    public Exporter(File outputDirectory, File activeDirectory, TypedFilterCollection filt) {
        filters = filt;
        this.outputDirectory = outputDirectory;
        this.activeDirectory = activeDirectory;
        String name = activeDirectory.getAbsolutePath();
        name = name.replace("\\", "/");
        //      name = name.replace("/generated", "");
        experimentId = name.substring(name.lastIndexOf("/") + 1);

    }

    public int getIdentifiedSpectra() {
        return identifiedSpectra;
    }

    public void setIdentifiedSpectra(final int pIdentifiedSpectra) {
        identifiedSpectra = pIdentifiedSpectra;
    }

    public void incrementIdentifiedSpectra() {
        identifiedSpectra++;
    }


    public int getUnidentifiedSpectra() {
        return unidentifiedSpectra;
    }

    public void setUnidentifiedSpectra(final int pUnidentifiedSpectra) {
        unidentifiedSpectra = pUnidentifiedSpectra;
    }

    public void incrementUnidentifiedSpectra() {
        unidentifiedSpectra++;
    }

    public TypedFilterCollection getFilters() {
        return filters;
    }

    public File getActiveDirectory() {
        return activeDirectory;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getExperimentId() {
        return experimentId;
    }

    public static List<File> getMZTabFiles(File directory) {
        ITypedFilter<File> mzTabFiler = FileFilters.getHasExtensionFilter("mztab");
        final List<File> files = FileFilters.applyFileFilters(directory, mzTabFiler);
        return files;
    }


    protected static List<File> readMZTabFiles(final File pFile1) {
        File projectInternalPath = new File(pFile1, "internal");
        List<File> ret = new ArrayList<File>();
        if (!projectInternalPath.exists()) {
            return ret;
        }
        File[] files = projectInternalPath.listFiles();
        if(files == null)
            return ret;
        for (File mzTab : files) {
            // searching for mztab file
            if (mzTab.getName().endsWith(ArchiveProjectSpectraFilter.PRIDE_MZTAB_SUFFIX)) {
                ret.add(mzTab);
            }
        }
        return ret;
    }


    public void exportDirectory() {
        final File outFile = fromDirectory(outputDirectory, activeDirectory);
        int numberWritten = 0;
        PrintWriter out = null;
        try {
            File projectInternalPath = new File(activeDirectory, "internal");
             final List<File> files = readMZTabFiles(activeDirectory);
            if(!files.isEmpty())
                out = new PrintWriter(new FileWriter(outFile));
            for (File mzTab : files) {
                ArchiveSpectra spec = ArchiveProjectSpectraFilter.buildArchiveSpectra(mzTab, projectInternalPath);
                if (spec == null) {
                    System.err.println("Bad mztab file " + mzTab);
                    continue;
                }
                MZTabProcessor processor = new MZTabProcessor(this, spec);
                processor.handleCorrespondingMGFs(out);
                final String onlyExportedTaxonomy1 = getOnlyExportedTaxonomy();
                final String accession = processor.getAccession();
                if (onlyExportedTaxonomy1 != null) {
                    if (!onlyExportedTaxonomy1.equals(accession))
                        continue; // skip files wrong taxomony
                }
                numberWritten += processor.handleCorrespondingMGFs(out);

            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            if(out != null)
                out.close();
            //    if (numberWritten == 0)
            //         outFile.delete();

        }
    }

    private static TypedFilterCollection buildFilters(File file) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        final TypedFilterCollection parse = TypedFilterCollection.parse(file);
        return parse;
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
    public static void main(String[] args) {
        int index = 0;
        File outputDirectory = new File(args[index++]);
        TypedFilterCollection.registerHandler(SpectrumFilters.TAG, new SpectrumFilters.SpectrumFilterSaxHandler(null));
        TypedFilterCollection filters = buildFilters(new File(args[index++]));
        for (; index < args.length; index++) {
            String arg = args[index];
            File dir = new File(arg);
            Exporter exp = new Exporter(outputDirectory, dir, filters);
            exp.exportDirectory();
            System.out.println("exported " + dir +
                            "  identified " + exp.getIdentifiedSpectra() +
                            " unidentified " + exp.getUnidentifiedSpectra()

            );

        }
        //   MaximialPeakFilter.showStatistics(System.out);

    }


}
