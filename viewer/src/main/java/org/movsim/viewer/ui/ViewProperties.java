package org.movsim.viewer.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.movsim.input.ProjectMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewProperties {

    final static Logger logger = LoggerFactory.getLogger(ViewProperties.class);
    
    final static String defaultPropertyName = "/config/defaultviewerconfig.properties";
    private static Properties defaultProperties;

    /**
     * Load default properties from the {code /config/defaultviewerconfig.properties} path. Needed for applet initialization.
     * 
     * @return the properties
     */
    public static Properties loadDefaultProperties() {
        if (defaultProperties == null) {
            defaultProperties = new Properties();
            try {
                // create and load default properties
                logger.info("read default properties from file "+defaultPropertyName);
                final InputStream is = ViewProperties.class.getResourceAsStream(defaultPropertyName);
                defaultProperties.load(is);
                is.close();
                defaultProperties = new Properties(defaultProperties);
            } catch (FileNotFoundException e) {
                // ignore exception.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultProperties;
    }

    
    public static Properties loadProperties(ProjectMetaData projectMetaData) {
        if (projectMetaData.hasProjectName()) {
            return loadProperties(projectMetaData.getProjectName(), projectMetaData.getPathToProjectFile());
        }
        return loadDefaultProperties();
    }

    /**
     * Load default properties and overwrites them with project specific properties if available
     * 
     * @param projectName
     * @param path
     * @return properties
     */
    public static Properties loadProperties(String projectName, String path) {
        Properties applicationProps = loadDefaultProperties();
        final File file = new File(path + projectName + ".properties");
        try {
            logger.debug("try to read from file=" + file.getName() + ", path=" + file.getAbsolutePath());
            if (ProjectMetaData.getInstance().isXmlFromResources()) {
                final InputStream inputStream = ViewProperties.class.getResourceAsStream(file.toString());
                if (inputStream != null) {
                    applicationProps.load(inputStream);
                    inputStream.close();
                }
            } else {
                final InputStream in = new FileInputStream(file);
                applicationProps.load(in);
                in.close();
            }

        } catch (FileNotFoundException e) {
            logger.info("cannot find "+file.toString()+". Fall back to default properties.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return applicationProps;
    }
    
    
}
