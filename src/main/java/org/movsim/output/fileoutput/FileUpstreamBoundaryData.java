//still in UpstreamBoundary
//
//package org.movsim.output.fileoutput;
//
//import java.io.File;
//import java.io.PrintWriter;
//
//import org.movsim.input.ProjectMetaData;
//import org.movsim.simulator.MovsimConstants;
//import org.movsim.utilities.impl.FileUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * The Class FileUpstreamBoundaryData.
// *
// */
//public class FileUpstreamBoundaryData {
//
//    final static Logger logger = LoggerFactory.getLogger(FileUpstreamBoundaryData.class);
//
//    private static final String extensionFormat = ".id%d_source_log.csv";
//    private static final String outputHeading = MovsimConstants.COMMENT_CHAR
//            + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],    count,      queue\n";
//    private static final String outputFormat = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %8d, %10.5f%n";
//
//    private PrintWriter fstrLogging;
//
//    /**
//     * Instantiates a new file upstream boundary data.
//     *
//     */
//    public FileUpstreamBoundaryData() {
//
//        final int roadId = 1; // TODO road id hard coded as 1 for the moment
//        ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
//        final String outputPath = projectMetaData.getOutputPath();
//        final String filename = outputPath + File.separator + projectMetaData.getProjectName() + String.format(extensionFormat, roadId);
//        fstrLogging = FileUtils.getWriter(filename);
//        fstrLogging.printf(outputHeading);
//
//    }
//
//    /**
//     * Update.
//     *
//     * @param time the time
//     * @param laneEnterLast the lane enter last
//     * @param xEnterLast the x enter last
//     * @param vEnterLast the v enter last
//     * @param q the q
//     * @param enteringVehCounter the entering veh counter
//     * @param nWait the n wait
//     */
//    public void update(double time, int laneEnterLast, double xEnterLast, double vEnterLast, double q, int enteringVehCounter,
//            double nWait) {
//
//      fstrLogging.printf(outputFormat, time, laneEnterLast, xEnterLast, vEnterLast, q,
//              enteringVehCounter, nWait);
//      fstrLogging.flush();
//        
//    }
//
//
//
//}
