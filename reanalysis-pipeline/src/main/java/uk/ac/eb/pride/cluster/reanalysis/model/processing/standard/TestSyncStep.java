/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.eb.pride.cluster.reanalysis.model.processing.standard;


import org.apache.log4j.Logger;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.ProcessingException;
import uk.ac.eb.pride.cluster.reanalysis.model.processing.ProcessingStep;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Kenneth Verheggen
 */
public class TestSyncStep extends ProcessingStep {
 /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(TestSyncStep.class);

    @Override
    public boolean doAction() throws ProcessingException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println("Instruction executred at " + dateFormat.format(cal.getTime()));
        return true;
    }

    @Override
    public String getDescription() {
        return "A TIMING STEP TO TEST THE PLADIPUS INFRASTRUCTURE";
    }
}
