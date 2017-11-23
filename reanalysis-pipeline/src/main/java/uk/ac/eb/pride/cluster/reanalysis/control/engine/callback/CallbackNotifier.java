package uk.ac.eb.pride.cluster.reanalysis.control.engine.callback;

import org.apache.log4j.Logger;
import uk.ac.eb.pride.cluster.reanalysis.model.feedback.Checkpoint;


import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Kenneth Verheggen
 */
public class CallbackNotifier {

    /**
     * The a Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CallbackNotifier.class);
    /**
     * The id of the process that's being monitored, if it is -1 nothing gets
     * notified
     */
    private final int processID;
    private final Collection<Checkpoint> checkpoints;

    public CallbackNotifier() {
        this.checkpoints = new HashSet<>();
        this.processID = -1;
    }

    public CallbackNotifier(int processID) {
        this.checkpoints = new HashSet<>();
        this.processID = processID;
    }

    public CallbackNotifier(String message) throws Exception {
        this.checkpoints = new HashSet<>();
        this.processID = message.hashCode();
    }



    public Collection<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public void addCheckpoints(Collection<Checkpoint> checkpoints) {
        this.checkpoints.addAll(checkpoints);
    }

     public void addCheckpoint(Checkpoint checkpoint) {
        this.checkpoints.add(checkpoint);
    }
}
