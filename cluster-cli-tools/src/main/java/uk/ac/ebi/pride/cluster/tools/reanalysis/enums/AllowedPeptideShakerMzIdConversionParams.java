package uk.ac.ebi.pride.cluster.tools.reanalysis.enums;

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
 * Created by ypriverol (ypriverol@gmail.com) on 13/12/2017.
 */
public enum AllowedPeptideShakerMzIdConversionParams {

    ///////////////////////////////////////////////////////////////////////////
    OUTPUT_FILE("output_file", "The output mzId File", true),
    CONTACT_FIRST_NAME("contact_first_name", "The contact first name of the file producer", true),
    CONTACT_LAST_NAME("contact_last_name", "The contact last name of the producer of the file", true),
    CONTACT_EMAIL("contact_email", "The contact email for the producer of the file", true),
    CONTACT_ADDRESS("contact_address", "Contact address of the producer of the file", true),
    ORGANIZATION_NAME("organization_name", "ORganization of the Producer of the file", true),
    ORGANIZATION_EMAIL("organization_email", "The main email for the organization", true),
    ORGANIZATION_ADDRESS("organization_address", "The address of the organization of the producer of the file", true),
    INPUT_FILE("in", "Input File from Peptide Shacker", true),
    THREADS("threads", "The number of threads to use. Defaults to the number of available CPUs.", false);

    /**
     * Short Id for the CLI parameter.
     */
    public String id;
    /**
     * Explanation for the CLI parameter.
     */
    public String description;
    /**
     * Boolean indicating whether the parameter is mandatory.
     */
    public boolean mandatory;

    /**
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param id the id
     * @param description the description
     * @param mandatory is the parameter mandatory
     */
    private AllowedPeptideShakerMzIdConversionParams(String id, String description, boolean mandatory) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
