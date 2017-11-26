package uk.ac.ebi.pride.cluster.tools;

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
 * Created by ypriverol (ypriverol@gmail.com) on 26/11/2017.
 */
public abstract class AbstractCommand {

    private String[] commandOptions = new String[] {};

    public AbstractCommand(String[] options) {
        this.commandOptions = options;
    }

    public String[] getCommandOptions() {
        return this.commandOptions;
    }

    public abstract void execute();

    public static String dasherizeName(Class<? extends AbstractCommand> cmd) {
        String n = cmd.getSimpleName().
                replaceAll("Command$", "");
        return n.replaceAll("([a-z])([A-Z])", "$1-$2").
                    replaceAll("_", "-").toLowerCase();
    }

}
