/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.output.fileoutput;

import java.io.PrintWriter;

import org.movsim.output.SpatioTemporal;
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.ObserverInTime;
import org.movsim.utilities.impl.FileUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class FileSpatioTemporal.
 */
public class FileSpatioTemporal implements ObserverInTime {

    private static final String extensionFormat = ".id%d_st.csv";
    private static final String outputHeading = MovsimConstants.COMMENT_CHAR
            + "     t[s],       x[m],     v[m/s],   a[m/s^2],  rho[1/km],     Q[1/h]\n";
    private static final String outputFormat = "%10.2f, %10.1f, %10.4f, %10.4f, %10.4f, %10.4f%n";

    /** The writer. */
    private PrintWriter writer;

    private SpatioTemporal spatioTemporal;

    /**
     * Instantiates a new file spatio temporal.
     * 
     * @param projectName
     *            the project name
     * @param roadSectionID
     *            the road section id
     * @param spatioTemporal
     *            the spatio temporal
     */
    public FileSpatioTemporal(String projectName, long roadSectionID, SpatioTemporal spatioTemporal) {

        this.spatioTemporal = spatioTemporal;
        spatioTemporal.registerObserver(this);

        final String filename = projectName + String.format(extensionFormat, roadSectionID);
        writer = FileUtils.getWriter(filename);
        writer.printf(outputHeading);
        writer.flush();

    }

    /**
     * Write output.
     * 
     * @param time
     *            the time
     */
    private void writeOutput(double time) {
        for (int j = 0, N = spatioTemporal.getDensity().length; j < N; j++) {
            final double x = j * spatioTemporal.getDxOut();
            // 0.0 is placeholder for acceleration which is not yet implemented
            writer.printf(outputFormat, time, x, spatioTemporal.getAverageSpeed()[j], 0.0,
                    1000 * spatioTemporal.getDensity()[j], 3600 * spatioTemporal.getFlow()[j]);
        }
        writer.printf("%n"); // block ends
        writer.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.utilities.ObserverInTime#notifyObserver(double)
     */
    @Override
    public void notifyObserver(double time) {
        writeOutput(time);
    }

}
