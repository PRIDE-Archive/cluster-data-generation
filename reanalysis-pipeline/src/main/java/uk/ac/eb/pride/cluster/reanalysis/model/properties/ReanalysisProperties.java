package uk.ac.eb.pride.cluster.reanalysis.model.properties;

import java.io.File;
import java.util.Properties;

/**
 *
 * @author Kenneth Verheggen
 */
public abstract class ReanalysisProperties extends Properties {

    protected static File defaultPropFile;

    public File getPropertiesFile() {
        return this.defaultPropFile;
    }

    /**
     * redirect the properties from another location than the default
     *
     * @param propertiesFile
     */
    public void setPropertiesFile(File propertiesFile) {
        this.defaultPropFile = propertiesFile;
    }
}
