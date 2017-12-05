package uk.ac.ebi.pride.cluster.tools.importers.projects;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
 * Created by ypriverol (ypriverol@gmail.com) on 04/12/2017.
 */
public class PRIDEProjectsTest {

    PRIDEProjects projectFactory;

    @Before
    public void setUp(){
        projectFactory = new PRIDEProjects();
    }

    @Test
    public void getPublicProjectURL() throws Exception {
        List<String> projectList = projectFactory.getPublicProjectURL();
        System.out.println(projectList.toString());
    }

}