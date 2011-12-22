package org.movsim.output.fileoutput;

import java.io.File;
import java.io.PrintWriter;

import org.movsim.output.LoopDetector;
import org.movsim.utilities.FileUtils;
import org.movsim.utilities.ObserverInTime;

/**
 * The Class FileDetector.
 */
public class FileDetector extends FileOutputBase implements ObserverInTime {

    private static final String extensionFormat = ".det.road_%d.x_%d.csv";

    private static final String outputHeadingTime = String.format("%s%10s,", COMMENT_CHAR, "t[s]");
    private static final String outputHeadingLaneAverage = String.format("%10s,%10s,%10s,%10s,%10s,%10s,", 
            "nVehTotal[1]", "V[km/h]", "flow[1/h/lane]", "occup[1]", "1/<1/v>[km/h]", "<1/Tbrut>[1/s]");
    private static final String outputHeadingLane = String.format("%10s,%10s,%10s,%10s,%10s,%10s,", 
            "nVeh[1]", "V[km/h]", "flow[1/h]", "occup[1]", "1/<1/v>[km/h]", "<1/Tbrut>[1/s]");

    // note: number before decimal point is total width of field, not width of
    // integer part
    private static final String outputFormatTime = "%10.1f, ";
    private static final String outputFormat = "%10d, %10.3f, %10.1f, %10.7f, %10.3f, %10.5f, ";

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
    	super();
        final int xDetectorInt = (int)detector.getDetPosition();
        this.detector = detector;
        this.laneCount = laneCount;

        final String filename = path + File.separator + baseFilename
                + String.format(extensionFormat, roadId, xDetectorInt);

        writer = initFile(filename);
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
    	writer = FileUtils.getWriter(filename);
    	writer.printf(COMMENT_CHAR
                + " number of lanes = %d. (Numbering starts from the most left lane as 1.)%n", laneCount);
    	writer.printf(COMMENT_CHAR + " dtSample in seconds = %-8.4f%n", detector.getDtSample());
    	writer.printf(outputHeadingTime);
        if (laneCount > 1) {
        	writer.printf(outputHeadingLaneAverage);
        }
        for (int i = 0; i < laneCount; i++) {
        	writer.printf(outputHeadingLane);
        }
        writer.printf("%n");
        writer.flush();
        return writer;
    }


    /**
     * Pulls data and writes aggregated data to output file.
     * 
     * @param time
     *            the time
     */
    private void writeAggregatedData(double time) {
    	writer.printf(outputFormatTime, time);
        if(laneCount > 1){
            writeLaneAverages();
        }
        writeQuantitiesPerLane();
        writer.printf("%n");
        writer.flush();
    }

    /**
     * Writes out the values per lane.
     * 
     * @param time
     */
    private void writeQuantitiesPerLane() {
        for (int i = 0; i < laneCount; i++) {
        	writer.printf(outputFormat, detector.getVehCountOutput(i), 3.6 * detector.getMeanSpeed(i),
                    3600 * detector.getFlow(i), detector.getOccupancy(i), 3.6 * detector.getMeanSpeedHarmonic(i),
                    detector.getMeanTimegapHarmonic(i));
        }
    }

    /**
     * Writes out the values over all lanes.
     * 
     * @param time
     */
    private void writeLaneAverages() {
    	writer.printf(outputFormat, detector.getVehCountOutputAllLanes(),
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
