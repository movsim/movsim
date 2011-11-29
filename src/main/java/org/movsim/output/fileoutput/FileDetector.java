package org.movsim.output.fileoutput;

import java.io.File;
import java.io.PrintWriter;

import org.movsim.input.ProjectMetaData;
import org.movsim.output.LoopDetector;
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.ObserverInTime;
import org.movsim.utilities.impl.FileUtils;

/**
 * The Class FileDetector.
 */
public class FileDetector implements ObserverInTime {

    private static final String extensionFormat = ".id%dx%d_det.csv";
    private static final String outputHeading = MovsimConstants.COMMENT_CHAR
            + "   t[s],  v[km/h], rho[1/km],   Q[1/h],  nVeh,  Occup[1],1/<1/v>(km/h),<1/Tbrutto>(1/s)";

    // note: number before decimal point is total width of field, not width of
    // integer part
    private static final String outputFormat = "%8.1f, %8.3f, %8.3f, %8.1f, %5d, %8.7f, %8.3f, %8.5f%n";

    private PrintWriter printWriter = null;
    private LoopDetector detector;

    /**
     * Instantiates a new file detector.
     * 
     * @param detector
     *            the detector
     */
    public FileDetector(long roadId, LoopDetector detector) {

        final int xDetectorInt = (int) detector.getDetPosition();
        this.detector = detector;

        // road id hard coded as 1 for the moment
        ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        final String outputPath = projectMetaData.getOutputPath();
        final String filename = outputPath + File.separator + projectMetaData.getProjectName()
                + String.format(extensionFormat, roadId, xDetectorInt);

        printWriter = initFile(filename);

        detector.registerObserver(this);

    }

    /**
     * Inits the file.
     * 
     * @param filename
     *            the filename
     * @return the prints the writer
     */
    private PrintWriter initFile(String filename) {
        printWriter = FileUtils.getWriter(filename);
        printWriter.printf(MovsimConstants.COMMENT_CHAR + " dtSample in s = %-8.4f%n", detector.getDtSample());
        printWriter.printf(MovsimConstants.COMMENT_CHAR + " position xDet = %-8.4f%n", detector.getDetPosition());
        printWriter.printf(MovsimConstants.COMMENT_CHAR + " arithmetic average for density rho%n");
        printWriter.printf(outputHeading + "%n");
        printWriter.flush();
        return printWriter;
    }

    /**
     * Write aggregated data.
     * 
     * @param time
     *            the time
     */
    private void writeAggregatedData(double time) {
        printWriter.printf(outputFormat, time, 3.6 * detector.getMeanSpeed(), 1000 * detector.getDensityArithmetic(),
                3600 * detector.getFlow(), detector.getVehCountOutput(), detector.getOccupancy(),
                3.6 * detector.getMeanSpeedHarmonic(), detector.getMeanTimegapHarmonic());
        printWriter.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.utilities.ObserverInTime#notifyObserver(double)
     */
    @Override
    public void notifyObserver(double time) {
        writeAggregatedData(time);
    }

}
