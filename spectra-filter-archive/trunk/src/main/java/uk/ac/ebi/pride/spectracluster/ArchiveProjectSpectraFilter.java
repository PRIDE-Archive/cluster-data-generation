package uk.ac.ebi.pride.spectracluster;

import org.apache.commons.io.FilenameUtils;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.spectracluster.filter.archive.ArchiveSpectra;
import uk.ac.ebi.pride.spectracluster.filter.archive.ArchiveSpectraFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Main class for running mgf filter on individual PRIDE Archive project
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ArchiveProjectSpectraFilter {

    public static final String PRIDE_MZTAB_SUFFIX = ".pride.mztab";
    public static final String PRIDE_MGF_SUFFIX = ".pride.mgf";

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }

        // check if input path exists
        String inputPath = args[0] + (args[0].endsWith(File.separator) ? "" : File.separator) + "internal";
        File projectInternalPath = new File(inputPath);
        if (!projectInternalPath.exists()) {
            System.err.println("Input file path must exists: " + inputPath);
        }

        // output file for the filtered results
        File outputFile = new File(args[1]);

        List<ArchiveSpectra> archiveSpectra = new ArrayList<ArchiveSpectra>();
        for (File mzTab : projectInternalPath.listFiles()) {
            // searching for mztab file
            if (mzTab.getName().endsWith(PRIDE_MZTAB_SUFFIX)) {
                try {
                    ArchiveSpectra spectra = buildArchiveSpectra(mzTab, inputPath);
                    archiveSpectra.add(spectra);
                } catch (IOException e) {
                    System.err.println("mzTab file parsing exception: " + mzTab.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }


        // filter the spectra
        ArchiveSpectraFilter archiveSpectraFilter = new ArchiveSpectraFilter(outputFile);
        archiveSpectraFilter.filter(archiveSpectra);

    }

    /**
     * Build an ArchiveSpectra object for a given mzTab object
     *
     * @param inputPath
     * @param mzTab
     * @return
     * @throws IOException
     */
    private static ArchiveSpectra buildArchiveSpectra(File mzTab, String inputPath) throws IOException {
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
                String msRunFileName = new File(msRunFile).getName();
                String msRunFileNameWithoutExtension = FilenameUtils.removeExtension(msRunFileName);

                String mgfFileName = msRunFileNameWithoutExtension + PRIDE_MGF_SUFFIX;
                File mgfFile = new File(inputPath + File.separator + mgfFileName);
                if (mgfFile.exists()) {
                    spectra.addMgfFile(mgfFile);
                }
            }

            return spectra;
        }

        return null;
    }

    private static void printUsage() {
        System.out.println("Usage: [Input path to Archive project location] [Output file containing the filtered spectra]");
    }
}
