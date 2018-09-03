package org.movsim.viewer.ui;

import org.movsim.input.ProjectMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class ViewProperties {

    private static final Logger LOG = LoggerFactory.getLogger(ViewProperties.class);

    private static final String DEFAULT_PROPERTY_NAME = "/config/defaultviewerconfig.properties";

    private static Properties defaultProperties;

    private ViewProperties() {
        // TODO review static context
    }

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
                LOG.info("read default properties from file {}", DEFAULT_PROPERTY_NAME);
                final InputStream is = ViewProperties.class.getResourceAsStream(DEFAULT_PROPERTY_NAME);
                defaultProperties.load(is);
                is.close();
                defaultProperties = new Properties(defaultProperties);
            } catch (FileNotFoundException e) {
                // ignore exception.
            } catch (IOException e) {
                LOG.error("error", e);
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
            LOG.debug("try to read from file={}, path={}", file.getName(), file.getAbsolutePath());
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
            LOG.info("cannot find {} Fall back to default properties", file);
        } catch (IOException e) {
            LOG.error("error", e);
        }
        return applicationProps;
    }

}
