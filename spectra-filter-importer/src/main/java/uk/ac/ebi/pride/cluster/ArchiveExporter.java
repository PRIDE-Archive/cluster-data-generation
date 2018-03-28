package uk.ac.ebi.pride.cluster;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.cluster.archive.importer.process.MGFProcessorUtils;
import uk.ac.ebi.pride.data.exception.SubmissionFileException;
import uk.ac.ebi.pride.data.io.SubmissionFileParser;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.cluster.archive.importer.filters.SpectrumPredicateParser;
import uk.ac.ebi.pride.cluster.archive.importer.process.ArchiveSpectra;
import uk.ac.ebi.pride.cluster.archive.importer.process.MZTabProcessor;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.function.Functions;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;
import uk.ac.ebi.pride.spectracluster.util.function.spectrum.RemoveSpectrumEmptyPeakFunction;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;
import uk.ac.ebi.pride.cluster.utilities.FileTypes;
import uk.ac.ebi.pride.cluster.utilities.mztab.IFilter;

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
        File currentOutput = null;

        // find all the PRIDE generated mzTab files
        File projectInternalPath = new File(inputDirectory, FileTypes.INTERNAL_DIRECTORY);
        List<File> files = readMZTabFiles(inputDirectory);

        List<String> rigthMztabImported = new ArrayList<>();

        if (!files.isEmpty()) {
            for (File mzTab : files) {
                // map the relationship between mzTab file and its mgf files
                ArchiveSpectra spec = buildArchiveSpectra(mzTab, projectInternalPath);
                currentOutput = output;
                if (spec == null) {
                    LOGGER.error("The following mztab is wrong formatted --  " + mzTab);
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
                        currentOutput = outputMzTabFile;
                        out = new PrintWriter(new BufferedWriter(new FileWriter(outputMzTabFile)), false);
                    }

                    processor.handleCorrespondingMGFs(spectrumFilter, out);
                    out.flush();
                    rigthMztabImported.add(processor.getAssayId());
                }catch (Exception exception){
                    LOGGER.error("The mzTab is not correct, or valid" + exception.getMessage());
                 }
                if(isEmpty(currentOutput)){
                    currentOutput.deleteOnExit();
                    LOGGER.info("The output file do not contains spectrum files -- " + currentOutput.getAbsolutePath());
                }

            }
        }

        /**
         * Some of the projects fails to be converted into mztab, in this case we can't assume that the projects contains
         * mztab. For those projects/files we will read directly the mgf for each assay.
         */
        try {
            Submission submission = SubmissionFileParser.parse(new File(projectInternalPath, FileTypes.SUBMISSION_FILE));
            List<DataFile> completeAssays = submission.getDataFiles().stream()
                    .filter(file -> file.getFileType() == ProjectFileType.RESULT &&
                            (FileTypes.isTypeFile(file.getFileName(), FileTypes.COMPRESS_MZIDENTML) ||
                                    (FileTypes.isTypeFile(file.getFileName(), FileTypes.PRIDE_PREFIX, FileTypes.COMPRESS_PRIDE))))
                   .collect(Collectors.toList());

            for(DataFile file: completeAssays) {

                try{

                    // Processing File
                    File inputFile = new File(projectInternalPath, FileTypes.removeGzip(file.getFileName()));
                    String assayNumber = file.getAssayAccession();
                    if (!rigthMztabImported.contains(assayNumber)) {
                        LOGGER.info("Processing Assay -- " + assayNumber + " -- Following file -- " + inputFile.getAbsolutePath());


                        // Process an mzIdentml
                        if (FileTypes.isTypeFile(file.getFileName(), FileTypes.COMPRESS_MZIDENTML)) {
                            List<File> peakFiles = new ArrayList<>();

                            // List of files associated with the mzIdentML
                            retrieveListPeakFileNames(file).stream().forEach(fileName -> {
                                File peakFile = new File(projectInternalPath, fileName);
                                if (peakFile.exists()) {
                                    peakFiles.add(peakFile);
                                }
                            });

                            if (!splitOuput)
                                if (out == null)
                                    out = new PrintWriter(new BufferedWriter(new FileWriter(output)), false);
                                else
                                    out = new PrintWriter(new BufferedWriter(new FileWriter(output, true)), false);
                            else {
                                File assayFile = buildOutputFile(output, assayNumber, inputDirectory);
                                currentOutput = assayFile;
                                out = new PrintWriter(new BufferedWriter(new FileWriter(assayFile)), false);
                            }

                            for(File peakFile: peakFiles)
                                MGFProcessorUtils.handleCorrespondingMGFs(spectrumFilter, out, peakFile);

                        } else if ((FileTypes.isTypeFile(inputFile.getName(), FileTypes.PRIDE_PREFIX, FileTypes.PRIDE_FORMAT))) {  // Process a PRIDE XML

                            if (!splitOuput)
                                if (out == null)
                                    out = new PrintWriter(new BufferedWriter(new FileWriter(output)), false);
                                else
                                    out = new PrintWriter(new BufferedWriter(new FileWriter(output, true)), false);
                            else {
                                File assayFile = buildOutputFile(output, assayNumber, inputDirectory);
                                currentOutput = assayFile;
                                out = new PrintWriter(new BufferedWriter(new FileWriter(assayFile)), false);
                            }

                            File peakList = getPeakListFileFromPRIDEName(projectInternalPath, inputFile.getName());
                            MGFProcessorUtils.handleCorrespondingMGFs(spectrumFilter, out, peakList);

                        }
                    } else {
                        LOGGER.info("The current Assay -- " + assayNumber + " has been correctly process with mztab");
                    }

                }catch(Exception ex){
                    LOGGER.info("The current assay -- " + file + " has not reference mgf file -- ");
                }
            }
        } catch (SubmissionFileException e) {
            LOGGER.error("Reading the submission file -- " +  projectInternalPath);
            e.printStackTrace();
        }


    }

    /**
     * This method is a little complex because it generated an output file buy combining the name of the
     * project and the output folder and assay. This is needed bacause sometimes is difficult to know the name of the project
     * @param output
     * @param assayNumber
     * @param inputDirectory
     * @return
     */
    private File buildOutputFile(File output, String assayNumber, File inputDirectory) {
        String[] projectFolderName = inputDirectory.getAbsolutePath().split("/");
        String lastname = "";
        for(String currentName: projectFolderName)
            if(currentName.trim().length() > 0)
                lastname = currentName;
        return new File(output, lastname + "-" + assayNumber + FileTypes.MGF_SUFFIX);
    }

    /**
     * Get the mgf file for the corresponding pride xml file.
     * @param projectInternalPath Project internal
     * @param name PRIDE XML
     * @return file
     * @throws IOException
     */
    private File getPeakListFileFromPRIDEName(File projectInternalPath, String name) throws IOException {
        String peakName = name.replace(FileTypes.PRIDE_FORMAT, "") + ".pride.mgf";
        File file = new File(projectInternalPath, peakName);
        if(!file.exists())
            throw new IOException("The mgf File do not exist for the corresponding PRIDE XML -- " + name );
        return file;
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

    /**
     * Check if the file is empty or not.
     * @param file Original File
     * @return check the size.
     */
    private boolean isEmpty(File file){
        return file.length() == 0;
    }

    /**
     * This function retrieve the List of mgf files for an specific mzIdentML
     * @param file MziDentML File
     * @return List of file Names
     */
    private static List<String> retrieveListPeakFileNames(DataFile file) {

        List<String> fileNames = new ArrayList<>();
        List<DataFile> fileMapping = file.getFileMappings();
        fileNames = fileMapping.stream()
                .filter( fileMap -> fileMap.getFileType() == ProjectFileType.PEAK)
                .map(fileMap -> FileTypes.removeGzip(fileMap.getFileName()))
                .collect(Collectors.toList());
        return fileNames;
    }
}
