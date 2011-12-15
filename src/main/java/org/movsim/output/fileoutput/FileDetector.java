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

    private static final String extensionFormat = ".det.road_%d.x_%d.csv";
    // old
    // private static final String outputHeading = " t[s],  v[km/h], rho[1/km],   Q[1/h],   nVeh,  Occup[1],1/<1/v>[km/h],<1/Tbrutto>[1/s]";
    //
    // // note: number before decimal point is total width of field, not width of
    // // integer part
    // private static final String outputFormat = "%8.1f, %8.3f, %8.3f, %8.1f, %8d, %8.7f, %8.3f, %8.5f, ";

    private static final String outputHeading = MovsimConstants.COMMENT_CHAR + "   t[s],     nVeh,  v[km/h],   Q[1/h],  Occup[1],1/<1/v>[km/h], <1/Tbrutto>[1/s]";
    private static final String outputHeadingLanes = ",    t[s],     nVeh,  v[km/h],   Q[1/h],  Occup[1],1/<1/v>[km/h], <1/Tbrutto>[1/s]";

    // note: number before decimal point is total width of field, not width of
    // integer part
    private static final String outputFormat = "%8.1f, %8d, %8.3f, %8.1f, %8.7f, %12.3f, %15.5f, ";

    private PrintWriter printWriter = null;
    private final LoopDetector detector;
    private int laneCount;

    /**
     * Instantiates a new file detector.
     * 
     * @param detector
     *            the detector
     * @param laneCount
     */
    public FileDetector(long roadId, LoopDetector detector, int laneCount) {
        final int xDetectorInt = (int) detector.getDetPosition();
        this.detector = detector;
        this.laneCount = laneCount;

        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        final String outputPath = projectMetaData.getOutputPath();
        final String filename = outputPath + File.separator + projectMetaData.getProjectName()
                + String.format(extensionFormat, roadId, xDetectorInt);

        printWriter = initFile(filename);

        detector.registerObserver(this);

    }

    /**
     * Inits the printwriter and prints the header.
     * 
     * @param filename
     *            the filename
     * @return the printWriter
     */
    private PrintWriter initFile(String filename) {
        printWriter = FileUtils.getWriter(filename);
        printWriter.printf(MovsimConstants.COMMENT_CHAR
                + " number of lanes =  %d. (Numbering starts from the most left lane 1.)%n", laneCount);
        printWriter.printf(MovsimConstants.COMMENT_CHAR + " dtSample in s = %-8.4f%n", detector.getDtSample());
        printWriter.printf(MovsimConstants.COMMENT_CHAR + " position xDet in m = %-8.4f%n", detector.getDetPosition());
        printWriter.printf(MovsimConstants.COMMENT_CHAR + " All lanes                                                               ");

        for (int i = 1; i <= laneCount; i++) {
            printWriter.printf(",,,,,,,lane %d", i);
        }
        printWriter.printf("%n");
        printUnitsHeaderForAllLanes();
        return printWriter;
    }

    private void printUnitsHeaderForAllLanes() {
        printWriter.printf(outputHeading);
        for (int i = 1; i <= laneCount; i++) {
            printWriter.printf(outputHeadingLanes);
//            if (laneCount == 1) {
//                break;
//            }
        }
        printWriter.printf("%n");
        printWriter.flush();
    }

    /**
     * Pulls data and writes aggregated data to output file.
     * 
     * @param time
     *            the time
     */
    private void writeAggregatedData(double time) {
        allLanesTogether(time);
        if (laneCount > 1) {
            perLane(time);
        }
        printWriter.printf("%n");
        printWriter.flush();
    }

    /**
     * Writes out the values per lane.
     * 
     * @param time
     */
    private void perLane(double time) {
        for (int i = 0; i < laneCount; i++) {
            // old
            // printWriter.printf(outputFormat, time, 3.6 * detector.getMeanSpeed(i),
            // 1000 * detector.getDensityArithmetic(i), 3600 * detector.getFlow(i), detector.getVehCountOutput(i),
            // detector.getOccupancy(i), 3.6 * detector.getMeanSpeedHarmonic(i),
            // detector.getMeanTimegapHarmonic(i));

            printWriter.printf(outputFormat, time, detector.getVehCountOutput(i), 3.6 * detector.getMeanSpeed(i),
                    3600 * detector.getFlow(i), detector.getOccupancy(i), 3.6 * detector.getMeanSpeedHarmonic(i),
                    detector.getMeanTimegapHarmonic(i));
        }
    }

    /**
     * Writes out the values over all lanes.
     * 
     * @param time
     */
    private void allLanesTogether(double time) {
        // old
        // printWriter.printf(outputFormat, time, 3.6 * detector.getMeanSpeedAllLanes(),
        // 1000 * detector.getDensityArithmeticAllLanes(), 3600 * detector.getFlowAllLanes(),
        // detector.getVehCountOutputAllLanes(), detector.getOccupancyAllLanes(),
        // 3.6 * detector.getMeanSpeedHarmonicAllLanes(), detector.getMeanTimegapHarmonicAllLanes());

        printWriter.printf(outputFormat, time, detector.getVehCountOutputAllLanes(),
                3.6 * detector.getMeanSpeedAllLanes(), 3600 * detector.getFlowAllLanes(),
                detector.getOccupancyAllLanes(), 3.6 * detector.getMeanSpeedHarmonicAllLanes(),
                detector.getMeanTimegapHarmonicAllLanes());
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
