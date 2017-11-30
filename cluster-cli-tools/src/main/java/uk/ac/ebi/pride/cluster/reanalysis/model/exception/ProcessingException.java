package uk.ac.ebi.pride.cluster.reanalysis.model.exception;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessingException extends Exception {

    public ProcessingException(String msg) {
        super(msg);
    }

    public ProcessingException(Exception e) {
        super(e);
    }

}
