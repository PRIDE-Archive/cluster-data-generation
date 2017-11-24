package uk.ac.eb.pride.cluster.reanalysis.model.processing.processsteps;

import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.preferences.IdentificationParameters;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import uk.ac.eb.pride.cluster.reanalysis.control.util.ZipUtils;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.ProcessingException;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.UnspecifiedException;
import uk.ac.eb.pride.cluster.reanalysis.model.processing.ProcessingStep;
import uk.ac.eb.pride.cluster.reanalysis.control.util.PladipusFileDownloadingService;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import uk.ac.eb.pride.cluster.reanalysis.model.GlobalProcessingProperties;

/**
 *
 * @author Kenneth Verheggen
 */
public class SearchSetupStep extends ProcessingStep {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(SearchSetupStep.class);

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;

    /**
     * the fasta folder for processing. This is required to ensure peptideshaker
     * does not rebuild protein trees (computationally heavy task that takes a
     * lot of time, but should only be done once)
     */
    private final File fasta_repo;

    public SearchSetupStep() {
        tempResources = GlobalProcessingProperties.TEMP_FOLDER;
        tempResources.getParentFile().mkdirs();
        fasta_repo = GlobalProcessingProperties.FASTA_REPOSITORY_FOLDER;
        fasta_repo.mkdirs();
    }

    @Override
    public boolean doAction() throws ProcessingException {
        cleanUp();
        try {
            initialiseInputFiles();
        } catch (Exception ex) {
            throw new ProcessingException(ex);
        }
        return true;
    }

    private void initialiseInputFiles() throws Exception {
        //original
        String inputPath = parameters.get("spectrum_files");
        String fastaPath = parameters.get("fasta_file");

        if (!inputPath.equalsIgnoreCase(tempResources.getAbsolutePath())) {

            if (inputPath.toLowerCase().endsWith(".mgf")
                    || inputPath.toLowerCase().endsWith(".mgf.zip")) {

                //move the input file to the temporary file using the pladipus file downloading service (it should be able 
                //to handle uri's as well that way
                File downloadFile = PladipusFileDownloadingService.downloadFile(inputPath, tempResources);
                downloadFile.deleteOnExit();
                String inputFile = downloadFile.getAbsolutePath();
                //if it is zipped, unzip it...
                if (inputPath.toLowerCase().endsWith(".zip")) {
                    LOGGER.info("Unzipping input");
                    ZipUtils.unzipArchive(new File(inputFile), tempResources);
                }
                //set the mgf as a spectrum file
                parameters.put("spectrum_files", inputFile.replace(".zip", ""));
            }
        }
        LoadFasta(fastaPath);
        parameters.put("search_setup_done", "true");
    }

    public void LoadFasta(String fastaPath) throws FileNotFoundException, IOException, ClassNotFoundException, XMLStreamException, URISyntaxException, UnspecifiedException {
        LOGGER.info("Updating the provided search parameters with the requested fasta");
        String paramPath = parameters.get("id_params");
        //generate a repo folder for fasta files...
        //clear the repository save for the current fasta (temporary solution) 
        String fastaName = new File(fastaPath).getName();

        //check if this fasta was used before, so we can skip peptideshaker's 
        //protein tree building
        boolean fastaAlreadyExists = false;
        File fastaFile = null;
        for (File aFasta : fasta_repo.listFiles()) {
            if (aFasta.getName().equalsIgnoreCase(fastaName)) {
                fastaFile = aFasta;
                fastaAlreadyExists = true;
                break;
            }
        }
        //if it's not there, create it there
        if (!fastaAlreadyExists) {
            fastaFile = PladipusFileDownloadingService.downloadFile(fastaPath, fasta_repo, fastaName);
        } else {
            //do we want to delete all other fastas?
            /*
            for (File aFile : fasta_repo.listFiles()) {
                if (!aFile.getName().toLowerCase().contains(fastaName.toLowerCase())) {
                    aFile.delete();
                }
            }*/
        }

        //if all has completed properly, there should be a fasta file here now...
        if (fastaFile != null) {
            parameters.put("fasta_file", fastaFile.getAbsolutePath());

            //get and update parameters
            File paramFile;
            if (!paramPath.contains(tempResources.getAbsolutePath())) {
                paramFile = PladipusFileDownloadingService.downloadFile(paramPath, tempResources);
                parameters.put("id_params", paramFile.getAbsolutePath());
            }
            paramFile = new File(parameters.get("id_params"));
            SearchParameters sparameters = SearchParameters.getIdentificationParameters(paramFile);
            IdentificationParameters updatedIdentificationParameters = updateAlgorithmSettings(sparameters, fastaFile);
            IdentificationParameters.saveIdentificationParameters(updatedIdentificationParameters, paramFile);

            //output
            File outputFolder = new File(parameters.get("output_folder"));
            outputFolder.mkdirs();
            parameters.put("output_folder", outputFolder.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Fasta file was not found !");
        }
    }

    @Override
    public String getDescription() {
        return "Initialisation of the search process";
    }

    public IdentificationParameters updateAlgorithmSettings(SearchParameters searchParameters, File fasta) throws IOException, XMLStreamException, URISyntaxException, UnspecifiedException {
        searchParameters.setFastaFile(fasta);
        SpeciesFactory.getInstance().initiate(new SearchGUIStep().getJar().getParentFile().getAbsolutePath());
        IdentificationParameters temp = new IdentificationParameters(searchParameters);
        return temp;
    }

    private void cleanUp() throws ProcessingException {
        if (!parameters.containsKey("skip_cleaning") && tempResources.exists()) {
            LOGGER.info("Cleaning up resources : ");
            for (File aFile : tempResources.listFiles()) {
                if (aFile.exists()) {
                    LOGGER.info("Deleting " + aFile.getAbsoluteFile());
                    if (aFile.isFile()) {
                        aFile.delete();
                    } else {
                        try {
                            FileUtils.deleteDirectory(aFile);
                        } catch (IOException ex) {
                            throw new ProcessingException(ex);
                        }
                    }
                }
            }
        } else {
            tempResources.mkdirs();
        }
    }

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }

}
