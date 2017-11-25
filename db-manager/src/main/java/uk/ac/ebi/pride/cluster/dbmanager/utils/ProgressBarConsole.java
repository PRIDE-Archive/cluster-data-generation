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

    static Set<Long> values = new HashSet<>();

    public static void updateProgress(double size) {
        if( Math.round(size/1000000)> 0 && Math.round(size/1000000) % 5 == 0 && !values.contains(Math.round(size/1000000))){
            values.add(Math.round(size/1000000));
            System.out.printf(Math.round(size/1000000) + "MB .. ");
        }
    }
}
