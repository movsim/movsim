/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.output.impl;

import java.io.PrintWriter;
import java.util.List;

import org.movsim.input.model.output.TrafficLightRecorderInput;
import org.movsim.output.TrafficLightRecorder;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.utilities.FileUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class TrafficLightRecorderImpl.
 */
public class TrafficLightRecorderImpl implements TrafficLightRecorder {

    /** The fstr. */
    private PrintWriter fstr = null;

    /** The n dt. */
    private final int nDt;

    /**
     * Instantiates a new traffic light recorder impl.
     * 
     * @param projectName
     *            the project name
     * @param writeOutput
     *            the write output
     * @param input
     *            the input
     * @param trafficLights
     *            the traffic lights
     */
    public TrafficLightRecorderImpl(String projectName, boolean writeOutput, TrafficLightRecorderInput input,
            List<TrafficLight> trafficLights) {

        nDt = input.getnDt();

        if (writeOutput) {
        	// road id hard coded as 1 for the moment
            final String filename = projectName + ".R1_tl_log.csv";
            fstr = FileUtils.getWriter(filename);
            writeHeader(trafficLights);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.output.impl.TrafficLightRecorder#update(int,
     * double, java.util.List)
     */
    @Override
    public void update(int itime, double time, List<TrafficLight> trafficLights) {

        if (itime % nDt != 0)
            // no update; nothing to do
            return;

        // write data:
        if (fstr != null) {
            fstr.printf("%8.2f   ", time);
            for (final TrafficLight trafficLight : trafficLights) {
                fstr.printf("%.1f  %d  ", trafficLight.position(), trafficLight.status());
            }
            fstr.printf("%n");
            fstr.flush();
        }
    }

    /**
     * Write header.
     * 
     * @param trafficLights
     *            the traffic lights
     */
    private void writeHeader(List<TrafficLight> trafficLights) {
        // write header:
        fstr.printf(Constants.COMMENT_CHAR + " number codes for traffic lights status: %n");
        fstr.printf(Constants.COMMENT_CHAR + " green         %d %n", TrafficLight.GREEN_LIGHT);
        fstr.printf(Constants.COMMENT_CHAR + " green --> red %d %n", TrafficLight.GREEN_RED_LIGHT);
        fstr.printf(Constants.COMMENT_CHAR + " red           %d %n", TrafficLight.RED_LIGHT);
        fstr.printf(Constants.COMMENT_CHAR + " red --> green %d %n", TrafficLight.RED_GREEN_LIGHT);

        int counter = 1;
        for (final TrafficLight trafficLight : trafficLights) {
            fstr.printf(Constants.COMMENT_CHAR + " position of traffic light no. %d: %5.2f m%n", counter,
                    trafficLight.position());
            counter++;
        }
        fstr.printf(Constants.COMMENT_CHAR + " %-8s  %-8s  %-8s  %-8s %n", "time[s]", "position[m]_TL1", 
        		"status[1]_TL1", " etc. ");
        fstr.flush();
    }

}
