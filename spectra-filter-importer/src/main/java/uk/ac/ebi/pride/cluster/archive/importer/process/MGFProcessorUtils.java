package uk.ac.ebi.pride.cluster.archive.importer.process;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumAppender;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class export submissions ony with mgf information an not mztab information.
 *
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 05/12/2017.
 */
public class MGFProcessorUtils {


    private static final Logger LOGGER = Logger.getLogger(MGFProcessorUtils.class);

    /**
     * Append a corresponding MGF File to the output file
     * @param filters spectrum file filters
     * @param out outpuf result file with all the spectra
     * @param mgfFile current mgf File
     * @return number of spectra added to the file.
     * @throws IllegalStateException
     */
    public static int handleCorrespondingMGFs(IFunction<ISpectrum, ISpectrum> filters, Appendable out, File mgfFile) throws IllegalStateException {
        return handleMFGFile(mgfFile, filters, out);
    }

    /**
     * Handle an spectra file, adding the spectra to the output file
     * @param file file mgf
     * @param filters filters to filter non necessary spectra
     * @param out output file
     * @return number of spectra added
     * @throws IllegalStateException
     */
    protected static int handleMFGFile(File file, IFunction<ISpectrum, ISpectrum> filters, Appendable out) throws IllegalStateException{
        int totalWritten = 0;
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(file));
            ISpectrum psm = ParserUtilities.readMGFScan(rdr);
            while (psm != null) {
                if (appendSpectra(psm, filters, out))
                    totalWritten++;
                psm = ParserUtilities.readMGFScan(rdr);
            }
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
        LOGGER.info("The Appender has written -- " + totalWritten + " spectra from file -- " + file );
        return totalWritten;
    }

    /**
     * Append one spectrum to the output file.
     * @param psm spectrum
     * @param filters filters from the the spectrum filters
     * @param out output File
     * @return true if the spectrum has been added
     */
    private static boolean appendSpectra(ISpectrum psm, IFunction<ISpectrum, ISpectrum> filters, Appendable out) {
        final String id = psm.getId();

        ISpectrum filteredSpectrum = filters.apply(psm);

        if (filteredSpectrum != null && validateSpectrum(psm)) {
            MGFSpectrumAppender.INSTANCE.appendSpectrum(out, psm);
            return true;
        }

        return false;
    }

    private static boolean validateSpectrum(ISpectrum spectrum) {
        return spectrum.getPrecursorCharge() >= 0;
    }


}
