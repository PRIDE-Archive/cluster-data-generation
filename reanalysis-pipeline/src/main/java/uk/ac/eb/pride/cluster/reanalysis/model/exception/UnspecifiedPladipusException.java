package uk.ac.eb.pride.cluster.reanalysis.model.exception;

/**
 *
 * @author Kenneth Verheggen
 */
public class UnspecifiedPladipusException extends Exception {

    public UnspecifiedPladipusException(String msg) {
        super(msg);
    }

    public UnspecifiedPladipusException(Exception e) {
        super(e);
    }

    public UnspecifiedPladipusException(Throwable e) {
        super(e);
    }

    public Throwable getOriginalCause() {
        return getCause().getCause();
    }

}
