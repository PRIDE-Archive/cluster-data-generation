package uk.ac.eb.pride.cluster.reanalysis.model.processing;

import uk.ac.eb.pride.cluster.reanalysis.model.exception.PladipusProcessingException;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.UnspecifiedPladipusException;

import java.util.HashMap;

public interface ProcessingExecutable {

    /**
     * Executes the executable
     *
     * @return a boolean to indicate if the process finished correctly
    */
    boolean doAction() throws UnspecifiedPladipusException,PladipusProcessingException;

    /**
     *
     * @return the description of the executable
     */
    String getDescription();

    /**
     *
     * @return the parameters of the executable
     */
    HashMap<String, String> getParameters();
}
