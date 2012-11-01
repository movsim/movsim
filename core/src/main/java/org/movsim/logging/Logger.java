package org.movsim.logging;

import java.io.File;
import java.net.URL;

import org.apache.log4j.PropertyConfigurator;

public class Logger {

    private static final String LOG4J_PROPERTIES = "log4j.properties";

    private static final String LOG4J_PATH = "/config/";

    private Logger() {
        throw new IllegalStateException();
    }

    public static String getLog4jPropertyName() {
        return LOG4J_PATH + LOG4J_PROPERTIES;
    }

    /**
     * Initializes the logger.
     */
    public static void initializeLogger(URL log4jConfig) {
        // Log Levels: DEBUG < INFO < WARN < ERROR;
        final File file = new File(LOG4J_PROPERTIES);
        if (file.exists() && file.isFile()) {
            PropertyConfigurator.configure(LOG4J_PROPERTIES);
        } else {
            // URL log4jConfig = MovsimCoreMain.class.getResource(LOG4J_PATH + LOG4J_PROPERTIES);
            PropertyConfigurator.configure(log4jConfig);
        }
    }


}
