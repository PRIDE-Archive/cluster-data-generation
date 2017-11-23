package uk.ac.eb.pride.cluster.reanalysis.model.exception;

/**
 *
 * @author Kenneth Verheggen
 */
public class PladipusProcessingException extends Exception {

    public PladipusProcessingException(String msg) {
        super(msg);
    }

    public PladipusProcessingException(Exception e) {
        super(e);
    }

}
