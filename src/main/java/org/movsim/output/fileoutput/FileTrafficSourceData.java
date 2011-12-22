package org.movsim.output.fileoutput;

import java.io.File;
import java.io.PrintWriter;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileTrafficSourceData.
 * 
 */
public class FileTrafficSourceData {

    final static Logger logger = LoggerFactory.getLogger(FileTrafficSourceData.class);

    private static final String extensionFormat = ".log_upBC.road_%d.csv";
    private static final String outputHeading = MovsimConstants.COMMENT_CHAR
            + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],    count,      queue\n";
    private static final String outputFormat = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %8d, %10.5f%n";

    private PrintWriter fstrLogging;

    /**
     * Instantiates a new file upstream boundary data.
     * 
     * @param roadId
     * 
     */
    public FileTrafficSourceData(int roadId) {
        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        final String outputPath = projectMetaData.getOutputPath();
        final String filename = outputPath + File.separator + projectMetaData.getProjectName()
                + String.format(extensionFormat, roadId);
        fstrLogging = FileUtils.getWriter(filename);
        fstrLogging.printf(outputHeading);
    }

    public void update(double simulationTime, int laneEnterLast, double xEnterLast, double vEnterLast,
            double totalInflow, int enteringVehCounter, double nWait) {
        fstrLogging.printf(outputFormat, simulationTime, laneEnterLast, xEnterLast, 3.6 * vEnterLast,
                3600 * totalInflow, enteringVehCounter, nWait);
        fstrLogging.flush();
    }

}
