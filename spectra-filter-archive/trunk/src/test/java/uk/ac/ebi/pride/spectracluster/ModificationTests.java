package uk.ac.ebi.pride.spectracluster;

import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.spectracluster.archive.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.ModificationTests
 * User: Steve
 * Date: 9/8/2014
 */
public class ModificationTests {
    public static final ModificationTests[] EMPTY_ARRAY = {};



    public static void main(String[] args) throws Exception {
        File mzTab = new File(args[0]);
        File projectInternalPath = new File(System.getProperty("user.dir"));
        ArchiveSpectra spec = ArchiveProjectSpectraFilter.buildArchiveSpectra(mzTab, projectInternalPath);

        MZTabFile mzTabFile = spec.getMzTabFile();
        for (PSM p : mzTabFile.getPSMs()) {
            SplitList<Modification> modifications = p.getModifications();
            int size = modifications.size();
            boolean empty = modifications.isEmpty();
            if(!empty && size  > 0)   {

                String s = modifications.toString();
                for (Modification modification : modifications) {
                    System.out.print(modification);
                  }
                System.out.println();
            }
            else {
                System.out.println("No modifications");
            }
          }
        List<File> mgfFiles = spec.getMgfFiles();


    }
}
