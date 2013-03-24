/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.logging;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.movsim.input.ProjectMetaData;

/**
 * Logs to file "[output path]projectname.log".
 * 
 * TTCC: Time Thread Category Component
 * 
 * The first field is the number of milliseconds elapsed since the start of the program. The second field is the thread
 * making the log request. The third field is the level of the log statement. The fourth field is the name of the logger
 * associated with the log request.
 * The text after the '-' is the message of the statement. Pattern=%r [%t] %-5p (%F:%M:%L) - %m%n
 * 
 * @author Ralph Germ
 * 
 */
public class LogFileAppender {

    private static final Logger LOG = Logger.getLogger(LogFileAppender.class);
    private FileAppender fileAppender;

    public static void initialize(ProjectMetaData projectMetaData) {
        new LogFileAppender(projectMetaData);
    }

    /**
     * Initializes a FileAppender and adds the appender.
     * 
     * View log4j.properties for defining logging levels for packages or to initialize other appenders like a rolling
     * file appender.
     * 
     * Logs to file "[output path]projectname.log".
     */
    private LogFileAppender(ProjectMetaData projectMetaData) {
        Layout layout = new PatternLayout("%r [%t] %-5p (%F:%M:%L) - %m%n");

        try {
            final String filename = projectMetaData.getOutputPath() + File.separator + projectMetaData.getProjectName()
                    + ".log";
            fileAppender = new FileAppender(layout, filename, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.addAppender(fileAppender);
    }
}
