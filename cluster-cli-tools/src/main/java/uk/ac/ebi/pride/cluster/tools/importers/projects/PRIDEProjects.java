package uk.ac.ebi.pride.cluster.tools.importers.projects;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class perform simple queries into oracle database an retrieve information about projects
 * like:
 *  - Path of public projects
 *  -
 *
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 04/12/2017.
 */
public class PRIDEProjects {

    //Logger file
    private static final Logger LOGGER = Logger.getLogger(PRIDEProjects.class);

    //Connection pull
    private final Connection conn = generateConnection();

    private Connection generateConnection() {
        Properties props = new Properties();

        InputStream input;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            input = PRIDEProjects.class.getClassLoader().getResourceAsStream("pride_archive.properties");
            props.load(input);
            LOGGER.info("Connection will be performed with the following properties -- " + props.toString());
            Connection con = DriverManager.getConnection(
                    props.getProperty("pride-machine"),
                    props.getProperty("pride-user"),
                    props.getProperty("pride-pwd"));
            LOGGER.info(con.toString());
            return con;

        } catch (IOException | SQLException | ClassNotFoundException e) {
               e.printStackTrace();
        }
        return null;
    }

    /**
     * This function retrieve the list of paths for public datasets.
     * @return List of public datasets paths.
     */
    public List<String> getPublicProjectURL(String taxonomy) {
       List<String> listProjects = new ArrayList<>();
       try {
           PreparedStatement stmt = null;
           if(taxonomy == null){
               String query = "select accession, publication_date from project where (submission_type='PRIDE' or submission_type='COMPLETE') and is_public = 1";
               stmt = conn.prepareStatement(query);
           }
           else {
               String query = "select PROJECT.ACCESSION, PROJECT.PUBLICATION_DATE, CV_PARAM.ACCESSION from PROJECT, CV_PARAM, PROJECT_CVPARAM where (submission_type='PRIDE' or submission_type='COMPLETE') and is_public = 1  AND PROJECT.PROJECT_PK = PROJECT_CVPARAM.PROJECT_FK  AND PROJECT_CVPARAM.CV_PARAM_FK=CV_PARAM.CV_PARAM_PK AND CV_PARAM.CV_LABEL='NEWT'  AND CV_PARAM.ACCESSION = ?";
               stmt = conn.prepareStatement(query);
               stmt.setString(1, taxonomy);
           }

           listProjects = tranformStatmentResultsToList(stmt.executeQuery());

       } catch (SQLException e) {
            e.getMessage();
        }
        return listProjects;

    }

    /**
     * List all the projects URL path in PRIDE for projects that has spectra information
     * @param taxonomy taxonomy to be study
     * @return list of project paths
     */
    public List<String> getPublicProjectWithSpectraURL(String taxonomy){
        List<String> listProjects = new ArrayList<>();
        try {
            PreparedStatement stmt = null;
            if(taxonomy == null){
                String query = "select accession, publication_date from PROJECT, ASSAY where (submission_type='PRIDE' or submission_type='COMPLETE') " +
                        "AND is_public = 1 " +
                        "AND PROJECT.PROJECT_PK = ASSAY.PROJECT_FK " +
                        "AND ASSAY.TOTAL_SPECTRUM_COUNT > 0";
                stmt = conn.prepareStatement(query);
            }
            else {
                String query = "select PROJECT.ACCESSION, PROJECT.PUBLICATION_DATE, CV_PARAM.ACCESSION " +
                        "from PROJECT, CV_PARAM, PROJECT_CVPARAM,ASSAY where (submission_type='PRIDE' or submission_type='COMPLETE') " +
                        "AND is_public = 1  AND PROJECT.PROJECT_PK = PROJECT_CVPARAM.PROJECT_FK  " +
                        "AND PROJECT_CVPARAM.CV_PARAM_FK=CV_PARAM.CV_PARAM_PK AND CV_PARAM.CV_LABEL='NEWT' " +
                        "AND PROJECT.PROJECT_PK = ASSAY.PROJECT_FK " +
                        "AND ASSAY.TOTAL_SPECTRUM_COUNT > 0 AND CV_PARAM.ACCESSION = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, taxonomy);
            }
            listProjects = tranformStatmentResultsToList(stmt.executeQuery());

        } catch (SQLException e) {
            e.getMessage();
        }
        return listProjects;

    }

    /**
     * Transform query result to List of String results.
     * @param rs results from Query
     * @return list of path
     * @throws SQLException
     */
    private List<String> tranformStatmentResultsToList(ResultSet rs) throws SQLException {
        List<String> listProjects = new ArrayList<>();
        while (rs.next()) {
            String idProject = rs.getString("ACCESSION");
            Date date = rs.getDate("PUBLICATION_DATE");
            LOGGER.info(rs.toString());
            listProjects.add(buildPath(idProject, date));
        }
        return listProjects;
    }

    /**
     * Build PRIDE project path
     * @param idProject id of the project
     * @param date date of the project publication
     * @return path of the project
     */
    private String buildPath(String idProject, Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY");
        String year = simpleDateFormat.format(date).toUpperCase();

        simpleDateFormat = new SimpleDateFormat("MM");
        String month = simpleDateFormat.format(date).toUpperCase();

        return "/" + String.valueOf(year) + "/" + String.valueOf(month) + "/" + idProject + "/";
    }
}
