/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.eb.pride.cluster.reanalysis.model.feedback;

/**
 *
 * @author Kenneth
 * @author Yasset Perez-Riverol
 */
public class Checkpoint {

    private final String checkpoint;
    private final String feedback;

    public Checkpoint(String checkpoint, String feedback) {
        this.checkpoint = checkpoint;
        this.feedback = feedback;
    }

    public String getCheckpoint() {
        return checkpoint;
    }

    public String getFeedback() {
        return feedback;
    }
    
}
