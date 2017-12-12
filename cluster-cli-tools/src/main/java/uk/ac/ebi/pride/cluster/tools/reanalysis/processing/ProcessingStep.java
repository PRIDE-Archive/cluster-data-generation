/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.pride.cluster.tools.reanalysis.processing;

import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.exception.ProcessingException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.exception.UnspecifiedException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processing Step is an Abstract Class to perform some processing jobs. It encapsulate some tool and parameters
 * behaviour.
 *
 * @author Kenneth Verheggen
 * @author Yasset Perez-Riverol
 */
public abstract class ProcessingStep implements ProcessingExecutable, AutoCloseable {

    // Parameters for the tool to perform the processing
    protected HashMap<String, String> parameters;

    // A boolean indicating whether the step has finished
    protected boolean isDone = false;

    // Default Constructor
    public ProcessingStep() {}

    /**
     * Set the parameter for the tool. The HashMap that contains the parameter and
     * the value for the specific parameter.
     * @param parameters Parameters for the specific tool
     */
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Get the parameters for the specific tool like a jey value pair.
     * @return Parameter Hash.
     */
    @Override
    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void startProcess(File executable, List<String> constructArguments) throws IOException {
        StringBuilder cmdBuilder = new StringBuilder();
        for(String arg:constructArguments){
            cmdBuilder.append(arg).append(" ");
        }       
        ProcessBuilder pb = new ProcessBuilder(cmdBuilder.substring(0, cmdBuilder.length()-1));
        pb.start();
    }

    @Override
    public void close() {
        isDone = true;
    }

    public boolean isIsDone() {
        return isDone;
    }

    private static String getCallerClass() throws ClassNotFoundException {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String rawFQN = stElements[3].toString().split("\\(")[0];
        return (rawFQN.substring(0, rawFQN.lastIndexOf('.')));
    }


    private static ProcessingStep loadStepFromClassName(String className) throws ClusterDataImporterException, IOException {
        try {
            Class<?> clazz = Class.forName(className);
            return (ProcessingStep) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException ex) {
            throw new ClusterDataImporterException(ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        try {
            HashMap<String, String> parameters = new HashMap<>();
            String currentClassName = getCallerClass();
            ProcessingStep step = loadStepFromClassName(currentClassName);
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if (i <= args.length - 1 && !args[i + 1].startsWith("-")) {
                        parameters.put(args[i].substring(1), args[i + 1]);
                    } else {
                        parameters.put(args[i], "");
                    }
                }
            }
            step.setParameters(parameters);
            step.process();
        } catch (UnspecifiedException | ProcessingException | ClassNotFoundException | ClusterDataImporterException | IOException ex) {
            Logger.getLogger(ProcessingStep.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
