/**
 * 
 * Copyright (C) 2010 by Ralph Germ (http://www.ralphgerm.de)
 * 
 */
package org.movsim.output.fileoutput;

import java.io.PrintWriter;

import org.movsim.simulator.Constants;
import org.movsim.utilities.impl.FileUtils;

/**
 * @author ralph
 * 
 */
public class FileUpstreamBoundaryData {

    private static final String extensionFormat = ".S%d_log.csv";
    private static final String outputHeading = Constants.COMMENT_CHAR
            + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],    count,      queue\n";
    private static final String outputFormat = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %8d, %10.5f%n";

    private PrintWriter fstrLogging;

    /**
     * @param projectName
     */
    public FileUpstreamBoundaryData(String projectName) {

        final int roadId = 1; // road id hard coded as 1 for the moment
        final String filename = projectName + String.format(extensionFormat, roadId);
        fstrLogging = FileUtils.getWriter(filename);
        fstrLogging.printf(outputHeading);

    }

    /**
     * @param time
     * @param laneEnterLast
     * @param xEnterLast
     * @param d
     * @param e
     * @param enteringVehCounter2
     * @param nWait
     */
    public void update(double time, int laneEnterLast, double xEnterLast, double vEnterLast, double q, int enteringVehCounter,
            double nWait) {

      fstrLogging.printf(outputFormat, time, laneEnterLast, xEnterLast, vEnterLast, q,
              enteringVehCounter, nWait);
      fstrLogging.flush();
        
    }



}
