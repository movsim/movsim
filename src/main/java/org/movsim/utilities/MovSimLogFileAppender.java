package org.movsim.utilities;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;

/**
 * Logs to file "<output path>projectname.log".
 * 
 * TTCC: Time Thread Category Component
 * 
 * The first field is the number of milliseconds elapsed since the start of the program. The second field is the thread making the log
 * request. The third field is the level of the log statement. The fourth field is the name of the logger associated with the log request.
 * The text after the '-' is the message of the statement. Pattern=%r [%t] %-5p (%F:%M:%L) - %m%n
 * 
 * @author Ralph Germ
 * 
 */
public class MovSimLogFileAppender {

    final static Logger logger = Logger.getLogger(Simulator.class);
    private FileAppender fileAppender;

    /**
     * Initializes a FileAppender and adds the appender.
     * 
     * View log4j.properties for defining logging levels for packages or to initialize other appenders like a rolling file appender.
     * 
     * Logs to file "<output path>projectname.log".
     */
    public MovSimLogFileAppender() {
        Layout layout = new PatternLayout("%r [%t] %-5p (%F:%M:%L) - %m%n");

        try {
            final String filename = ProjectMetaData.getInstance().getOutputPath() + File.separator
                    + ProjectMetaData.getInstance().getProjectName() + ".log";
            fileAppender = new FileAppender(layout, filename, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addAppender(fileAppender);
    }

}
