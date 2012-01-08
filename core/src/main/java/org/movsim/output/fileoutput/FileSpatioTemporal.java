/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.output.fileoutput;

import org.movsim.output.SpatioTemporal;
import org.movsim.utilities.ObserverInTime;

/**
 * The Class FileSpatioTemporal.
 */
public class FileSpatioTemporal extends FileOutputBase implements ObserverInTime {

    private static final String extensionFormat = ".st.route_%s.csv";
    private static final String outputHeading = COMMENT_CHAR
            + "     t[s],       x[m],     v[m/s],   a[m/s^2],  rho[1/km],     Q[1/h]\n";
    private static final String outputFormat = "%10.2f, %10.1f, %10.4f, %10.4f, %10.4f, %10.4f%n";

    private final SpatioTemporal spatioTemporal;

    /**
     * Constructor.
     * 
     * @param roadId
     *            the road section id
     * @param spatioTemporal
     *            the spatio temporal
     */
    public FileSpatioTemporal(String roadId, SpatioTemporal spatioTemporal) {
        this.spatioTemporal = spatioTemporal;
        spatioTemporal.registerObserver(this);
        writer = createWriter(String.format(extensionFormat, roadId));
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
        final int count = spatioTemporal.size();
        final double dx = spatioTemporal.getDxOut();
        for (int i = 0; i < count; i++) {
            final double x = i * dx;
            // TODO - output acceleration
            // 0.0 is placeholder for acceleration which is not yet implemented
            writer.printf(outputFormat, time, x, spatioTemporal.getAverageSpeed(i), 0.0,
                    1000 * spatioTemporal.getDensity(i), 3600 * spatioTemporal.getFlow(i));
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
