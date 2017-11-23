package uk.ac.eb.pride.cluster.reanalysis.model.exception;

/**
 *
 * @author Kenneth Verheggen
 */
public class UnspecifiedException extends Exception {

    public UnspecifiedException(String msg) {
        super(msg);
    }

    public UnspecifiedException(Exception e) {
        super(e);
    }

    public UnspecifiedException(Throwable e) {
        super(e);
    }

    public Throwable getOriginalCause() {
        return getCause().getCause();
    }

}
