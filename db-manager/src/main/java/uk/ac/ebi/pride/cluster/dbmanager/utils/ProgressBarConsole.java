package uk.ac.ebi.pride.cluster.dbmanager.utils;

import java.util.HashSet;
import java.util.Set;

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
 * Created by ypriverol (ypriverol@gmail.com) on 24/11/2017.
 */
public class ProgressBarConsole {

    static int TOTAL_SIZE = 1000000;

    static Set<Long> values = new HashSet<>();

    public static void updateProgress(double size) {
        if(!values.contains(Math.round(size/TOTAL_SIZE))){
            if(Math.round(size/TOTAL_SIZE) % 5 == 0 && Math.round(size/TOTAL_SIZE) > 0){
                System.out.print(Math.round(size/TOTAL_SIZE) + "MB .. ");
            }else if(Math.round(size/TOTAL_SIZE) == 0){
                System.out.print("Downloading File -- " + Math.round(size/TOTAL_SIZE) + "MB .. ");
            }
            values.add(Math.round(size/TOTAL_SIZE));
        }
    }
}
