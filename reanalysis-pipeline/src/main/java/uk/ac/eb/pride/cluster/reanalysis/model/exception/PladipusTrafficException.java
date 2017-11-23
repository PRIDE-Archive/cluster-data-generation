package uk.ac.eb.pride.cluster.reanalysis.model.exception;

/**
 *
 * @author Kenneth Verheggen
 */
public class PladipusTrafficException extends Exception {

    public PladipusTrafficException(String msg) {
        super(msg);
    }

    public PladipusTrafficException(Exception ex) {
        super(ex);
    }

}
