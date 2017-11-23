package uk.ac.eb.pride.cluster.reanalysis.model.processing.standard;


import org.apache.log4j.Logger;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.PladipusProcessingException;
import uk.ac.eb.pride.cluster.reanalysis.model.processing.ProcessingStep;

/**
 *
 * @author Kenneth Verheggen
 */
public class TestingStep extends ProcessingStep {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(TestingStep.class);

    @Override
    public boolean doAction() throws PladipusProcessingException {
        System.out.println("EXECUTING DUMMY STEP " + System.currentTimeMillis());
        return true;
    }

    @Override
    public String getDescription() {
        return "A STEP TO TEST THE PLADIPUS INFRASTRUCTURE";
    }

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }

}
