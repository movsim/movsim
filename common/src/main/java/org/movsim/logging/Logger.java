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

    /**
     * Initializes the logger.
     */
    public static void initializeLogger() {
        // Log Levels: DEBUG < INFO < WARN < ERROR;
        final File file = new File(LOG4J_PROPERTIES);
        if (file.exists() && file.isFile()) {
            System.out.println("log4j configuration read from=" + file.getAbsolutePath());
            PropertyConfigurator.configure(LOG4J_PROPERTIES);
        } else {
            final URL log4jConfig = Logger.class.getResource(LOG4J_PATH + LOG4J_PROPERTIES);
            System.out.println("no log4j from commandline provided. Use default log4j configuration from="
                    + log4jConfig.toExternalForm());
            PropertyConfigurator.configure(log4jConfig);
        }
    }
}
