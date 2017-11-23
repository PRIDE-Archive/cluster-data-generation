package uk.ac.eb.pride.cluster.reanalysis.control.engine;


import org.apache.log4j.Logger;
import uk.ac.eb.pride.cluster.reanalysis.control.engine.callback.CallbackNotifier;
import uk.ac.eb.pride.cluster.reanalysis.model.exception.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessingEngine implements Callable {

    /**
     * the message that is being processed
     */
    private String currentMessage;
    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(ProcessingEngine.class);


    public ProcessingEngine() {

    }


    /**
     *
     * @param message the message to process
     * @throws IOException
     */
    public ProcessingEngine(String message) throws Exception, IOException {
        this.currentMessage = (String) message;
    }

    /**
     *
     * @return the message that's being processed
     */
    public String getCurrentMessage() {
        return currentMessage;
    }

    /**
     *
     * @param executable the jar that should be started on this jvm
     * @param arguments list of arguments + values required to start the jar
     * @return the system exit value of the process
     */
    public int startProcess(File executable, List<String> arguments) {
        CallbackNotifier callbackNotifier = new CallbackNotifier(-1);
        return startProcess(executable, arguments, callbackNotifier);
    }

    /**
     *
     * @param executable the jar that should be started on this jvm
     * @param arguments array of arguments + values required to start the jar
     * @return the system exit value of the process
     */
    public int startProcess(File executable, String[] arguments) {
        CallbackNotifier callbackNotifier = new CallbackNotifier(-1);
        return startProcess(executable, arguments, callbackNotifier);
    }

    /**
     *
     * @param executable the executable that should be started on this jvm
     * @param arguments list of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @return the system exit value of the process
     */
    public int startProcess(File executable, List<String> arguments, CallbackNotifier callbackNotifier) {
        try {
        } catch (Exception ex) {
            LOGGER.error(ex);
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     *
     * @param executable the executable that should be started on this jvm
     * @param arguments array of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @return the system exit value of the process
     */
    public int startProcess(File executable, String[] arguments, CallbackNotifier callbackNotifier) {
        return this.startProcess(executable,Arrays.asList(arguments),callbackNotifier);
    }

    /**
     *
     * @param executable the executable that should be started on this jvm
     * @param arguments list of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @param errorTerms collection of terms that are specificly to be throwing
     * an exception
     * @return the system exit value of the process
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int startProcess(File executable, List<String> arguments, CallbackNotifier callbackNotifier, Collection<String> errorTerms) throws IOException, InterruptedException, ExecutionException {

        return 0;
    }

    /**
     *
     * @param executable the executable that should be started on this jvm
     * @param arguments array of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @param errorTerms collection of terms that are specificly to be throwing
     * an exception
     * @return the system exit value of the process
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int startProcess(File executable, String[] arguments, CallbackNotifier callbackNotifier, Collection<String> errorTerms) throws IOException, InterruptedException, ExecutionException {

        return 0;
    }

//    private ProcessingMonitor getPreparedMonitor(File executable, List<String> arguments, CallbackNotifier callbackNotifier) {
//        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
//        processBuilder.directory(executable.getParentFile());
//        LOGGER.info(arguments.toString()
//                .replace("[", "")
//                .replace("]", "")
//                .replace(", ", " "));
//        return new ProcessingMonitor(processBuilder, callbackNotifier);
//    }
//
//    private ProcessingMonitor getPreparedMonitor(File executable, String[] args, CallbackNotifier callbackNotifier) {
//        List<String> arguments = new ArrayList<>(Arrays.asList(args));
//        return getPreparedMonitor(executable, arguments, callbackNotifier);
//    }

//    /**
//     *
//     * @param aJob, the processingjob that has to be run
//     * @return a boolean indicating wether the job was succesfull;
//     * @throws Exception
//     */
//    public boolean runJob(ProcessingJob aJob) throws PladipusProcessingException {
//        for (ProcessingStep aStep : aJob) {
//            try {
//                aStep.getCallbackNotifier().onNotification(aStep.getDescription(), false);
//                aStep.doAction();
//                aStep.getCallbackNotifier().onNotification(aStep.getDescription(), true);
//            } catch (Exception e) {
//                //     e.printStackTrace();
//                throw new PladipusProcessingException(e);
//            }
//        }
//        LOGGER.info("Done !");
//        return true;
//    }
//
//    /**
//     *
//     * @param aJobMessage, the text representation of a processingjob that has
//     * to be run
//     * @param aJobMessage
//     * @return a boolean indicating wether the job was succesfull;
//     * @throws
//       */
//    public boolean runJob(ProcessingJob aJobMessage) throws PladipusProcessingException, ProcessStepInitialisationException, PladipusTrafficException, XMLInterpreterException {
//        try {
//            return runJob(XMLJobInterpreter.getInstance().convertXMLtoJob(aJobMessage));
//        } catch (IOException | ParserConfigurationException | SAXException ex) {
//            throw new XMLInterpreterException(ex);
//        } catch (Exception ex) {
//            throw new PladipusTrafficException(ex);
//        }
//    }

    @Override
    public Object call() throws UnspecifiedPladipusException {
        try {
           // return runJob(currentMessage);
            return null;
        } catch (Throwable e) {
            throw new UnspecifiedPladipusException(e);
        }
    }

}
