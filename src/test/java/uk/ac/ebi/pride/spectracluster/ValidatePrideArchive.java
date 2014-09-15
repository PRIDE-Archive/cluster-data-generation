package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.archive.*;

import java.io.*;

/**
 * Main class for running mgf filter on individual PRIDE Archive project
 * uk.ac.ebi.pride.spectracluster.ArchiveProjectSpectraFilter
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ValidatePrideArchive {



    private static void validateTopLevelDirectory(final File pDir, PrintWriter out) {
        for (File file : pDir.listFiles()) {
            for (File file1 : file.listFiles()) {
                if (file1.isDirectory())
                    validateDirectory(file1, out);
            }
        }
    }

    private static void validateDirectory(final File pFile1, PrintWriter out) {
        File projectInternalPath = new File(pFile1, "internal");
        if (!projectInternalPath.exists()) {
             return;
        }
        boolean hasMZTab = false;
        for (File mzTab : projectInternalPath.listFiles()) {
            // searching for mztab file
             if (mzTab.getName().endsWith(ArchiveProjectSpectraFilter.PRIDE_MZTAB_SUFFIX)) {
                hasMZTab = true;
//                ElapsedTimer timer = new ElapsedTimer();
//                ArchiveSpectra spectra = ArchiveProjectSpectraFilter.buildArchiveSpectra(mzTab, projectInternalPath);
//                timer.showElapsed("Finished handling " + mzTab.getName());
//                if (spectra == null) {
//                    out.println(mzTab.getPath() + " bad parse");
//                }
//                else {
//                    out.println(mzTab.getPath() + " good parse");
//                    for (File file : spectra.getMgfFiles()) {
//                        out.println(file.getPath());
//                    }
//                }
            }
        }
        if(hasMZTab) {
            String s = pFile1.getPath() ;
            s = s.replace("\\","/");
            out.println(s);
        }
     }


    private static void validateDirectoryComplete(final File pFile1, PrintWriter out) {
        File projectInternalPath = new File(pFile1, "internal");
        if (!projectInternalPath.exists()) {
            out.println(pFile1.getPath() + " no internal");
            return;
        }
        for (File mzTab : projectInternalPath.listFiles()) {
            // searching for mztab file
            if (mzTab.getName().endsWith(ArchiveProjectSpectraFilter.PRIDE_MZTAB_SUFFIX)) {
                ElapsedTimer timer = new ElapsedTimer();
                ArchiveSpectra spectra = ArchiveProjectSpectraFilter.buildArchiveSpectra(mzTab, projectInternalPath);
                timer.showElapsed("Finished handling " + mzTab.getName());
                if (spectra == null) {
                    out.println(mzTab.getPath() + " bad parse");
                }
                else {
                    out.println(mzTab.getPath() + " good parse");
                    for (File file : spectra.getMgfFiles()) {
                        out.println(file.getPath());
                    }
                }
            }
        }
     }


    private static void validatePrideArchive(final PrintWriter pOut) {
        for (int i = 2005; i < 2015; i++) {
            File dir = new File(Integer.toString(i));
            validateTopLevelDirectory(dir, pOut);
         }

        pOut.close();
    }

    private static void printUsage() {
        System.out.println("Usage:   [Output file containing the log] [ optional test dir - otherwise all of pride]");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            return;
        }
        // output file for the filtered results
        PrintWriter out = new PrintWriter(new FileWriter(args[0]));

        // pass in a directory validate that
        if (args.length > 1) {
            validateDirectory(new File(args[1]), out);
        }
        else {
            validatePrideArchive(out);
        }

    }


}
