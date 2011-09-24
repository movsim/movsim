/**
 * 
 * Copyright (C) 2010 by Ralph Germ (http://www.ralphgerm.de)
 * 
 */
package org.movsim.output.fileoutput;

import java.io.PrintWriter;

import org.movsim.simulator.Constants;
import org.movsim.utilities.impl.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FileUpstreamBoundaryData.
 *
 * @author ralph
 */
public class FileUpstreamBoundaryData {

    final static Logger logger = LoggerFactory.getLogger(FileUpstreamBoundaryData.class);

    private static final String extensionFormat = ".S%d_log.csv";
    private static final String outputHeading = Constants.COMMENT_CHAR
            + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],    count,      queue\n";
    private static final String outputFormat = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %8d, %10.5f%n";

    private PrintWriter fstrLogging;

    /**
     * Instantiates a new file upstream boundary data.
     *
     * @param projectName the project name
     */
    public FileUpstreamBoundaryData(String projectName) {

        final int roadId = 1; // TODO road id hard coded as 1 for the moment
        final String filename = projectName + String.format(extensionFormat, roadId);
        fstrLogging = FileUtils.getWriter(filename);
        fstrLogging.printf(outputHeading);

    }

    /**
     * Update.
     *
     * @param time the time
     * @param laneEnterLast the lane enter last
     * @param xEnterLast the x enter last
     * @param vEnterLast the v enter last
     * @param q the q
     * @param enteringVehCounter the entering veh counter
     * @param nWait the n wait
     */
    public void update(double time, int laneEnterLast, double xEnterLast, double vEnterLast, double q, int enteringVehCounter,
            double nWait) {

      fstrLogging.printf(outputFormat, time, laneEnterLast, xEnterLast, vEnterLast, q,
              enteringVehCounter, nWait);
      fstrLogging.flush();
        
    }



}
