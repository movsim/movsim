package org.movsim.output.fileoutput;

import org.movsim.output.SpatioTemporal;
import org.movsim.utilities.ObserverInTime;

/**
 * The Class FileSpatioTemporal.
 */
public class FileSpatioTemporal extends FileOutputBase implements ObserverInTime {

    private static final String extensionFormat = ".st.road_%d.csv";
    private static final String outputHeading = COMMENT_CHAR
            + "     t[s],       x[m],     v[m/s],   a[m/s^2],  rho[1/km],     Q[1/h]\n";
    private static final String outputFormat = "%10.2f, %10.1f, %10.4f, %10.4f, %10.4f, %10.4f%n";

    private final SpatioTemporal spatioTemporal;

    /**
     * Constructor.
     * 
     * @param roadSectionID
     *            the road section id
     * @param spatioTemporal
     *            the spatio temporal
     */
    public FileSpatioTemporal(long roadSectionID, SpatioTemporal spatioTemporal) {
        this.spatioTemporal = spatioTemporal;
        spatioTemporal.registerObserver(this);
        writer = createWriter(String.format(extensionFormat, roadSectionID));
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
