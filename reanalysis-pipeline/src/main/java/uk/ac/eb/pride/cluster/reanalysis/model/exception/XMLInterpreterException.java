package uk.ac.eb.pride.cluster.reanalysis.model.exception;

/**
 *
 * @author Kenneth Verheggen
 */
public class XMLInterpreterException extends Exception {

    public XMLInterpreterException(String msg) {
        super(msg);
    }

    public XMLInterpreterException(Exception ex) {
          super(ex);
    }

}
