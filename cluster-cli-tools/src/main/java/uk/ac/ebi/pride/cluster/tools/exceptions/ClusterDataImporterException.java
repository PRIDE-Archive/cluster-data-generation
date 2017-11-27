package uk.ac.ebi.pride.cluster.tools.exceptions;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 27/11/2017.
 */
public class ClusterDataImporterException extends Exception{

    /**
     * Default constructor for the exception
     * @param message
     */
    public ClusterDataImporterException(String message, Throwable exception) {
        super(message, exception);
    }
}
