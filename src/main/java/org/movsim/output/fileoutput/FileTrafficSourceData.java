package org.movsim.output.fileoutput;

import java.io.File;

import org.movsim.simulator.roadnetwork.TrafficSource;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileTrafficSourceData.
 * 
 */
public class FileTrafficSourceData extends FileOutputBase implements TrafficSource.RecordDataCallback {

    final static Logger logger = LoggerFactory.getLogger(FileTrafficSourceData.class);

    private static final String extensionFormat = ".source.road_%d.csv";
    private static final String outputHeading = COMMENT_CHAR
            + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],    count,      queue\n";
    private static final String outputFormat = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %8d, %10.5f%n";

    /**
     * Instantiates a new file upstream boundary data.
     * 
     * @param roadId
     * 
     */
    public FileTrafficSourceData(int roadId) {
    	super();
        final String filename = path + File.separator + baseFilename + String.format(extensionFormat, roadId);
        writer = FileUtils.getWriter(filename);
        writer.printf(outputHeading);
    }

    @Override
	public void recordData(double simulationTime, int laneEnter, double xEnter, double vEnter,
            double totalInflow, int enteringVehCounter, double nWait) {
        writer.printf(outputFormat, simulationTime, laneEnter, xEnter, 3.6 * vEnter,
                3600 * totalInflow, enteringVehCounter, nWait);
        writer.flush();
    }
}
