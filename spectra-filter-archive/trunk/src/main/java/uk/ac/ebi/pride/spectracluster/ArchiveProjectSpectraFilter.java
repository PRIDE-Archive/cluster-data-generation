package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.utilities.*;
import org.apache.commons.io.*;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.*;
import uk.ac.ebi.pride.jmztab.utils.errors.*;
import uk.ac.ebi.pride.spectracluster.filter.archive.*;

import java.io.*;
import java.util.*;

/**
 * Main class for running mgf filter on individual PRIDE Archive project
 * uk.ac.ebi.pride.spectracluster.ArchiveProjectSpectraFilter
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ArchiveProjectSpectraFilter {

    public static final String PRIDE_MZTAB_SUFFIX = ".pride.mztab";
    public static final String PRIDE_MGF_SUFFIX = ".pride.mgf";


    /**
     * Build an ArchiveSpectra object for a given mzTab object
     *
     * @param inputPath
     * @param mzTab
     * @return
     * @throws IOException
     */
    public static ArchiveSpectra buildArchiveSpectra(File mzTab, File inputPath) {
        try {
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
                      File mgfFile = new File(inputPath, mgfFileName);
                      if (mgfFile.exists()) {
                        spectra.addMgfFile(mgfFile);
                    }
                }

                return spectra;
            }
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    private static void printUsage() {
        System.out.println("Usage: [Input path to Archive project location] [Output file containing the filtered spectra]");
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            return;
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
                ElapsedTimer timer = new ElapsedTimer();
                ArchiveSpectra spectra = buildArchiveSpectra(mzTab, projectInternalPath);
                timer.showElapsed("Finished handling " + mzTab.getName());
                archiveSpectra.add(spectra);
            }
        }


        // filter the spectra
        ArchiveSpectraFilter archiveSpectraFilter = new ArchiveSpectraFilter(outputFile);
        archiveSpectraFilter.filter(archiveSpectra);

    }
}
