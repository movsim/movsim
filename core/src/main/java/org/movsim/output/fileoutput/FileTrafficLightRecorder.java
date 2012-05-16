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

import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.TrafficLight;
import org.movsim.simulator.roadnetwork.TrafficLight.TrafficLightStatus;
import org.movsim.simulator.roadnetwork.TrafficLights;

/**
 * The Class FileTrafficLightRecorder.
 */
public class FileTrafficLightRecorder extends FileOutputBase implements TrafficLights.RecordDataCallback {

    private static final String extensionFormat = ".trafficlights.road_%s.csv";
    private final int nDt;

    /**
     * Constructor.
     * 
     * @param nDt
     *            the n dt
     * @param trafficLights
     *            the traffic lights
     */
    public FileTrafficLightRecorder(int nDt, Iterable<TrafficLight> trafficLights, RoadSegment roadSegment) {
        super();
        this.nDt = nDt;

        writer = createWriter(String.format(extensionFormat, roadSegment.userId()));
        writeHeader(trafficLights);
    }

    /**
     * Update.
     * 
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     * @param trafficLights
     *            the traffic lights
     */
    @Override
    public void recordData(double simulationTime, long iterationCount, Iterable<TrafficLight> trafficLights) {

        if (iterationCount % nDt != 0) {
            // no update; nothing to do
            return;
        }

        // write data:
        if (writer != null) {
            writer.printf("%8.2f   ", simulationTime);
            for (final TrafficLight trafficLight : trafficLights) {
                writer.printf("%.1f  %d  ", trafficLight.position(), trafficLight.status().ordinal());
            }
            writer.printf("%n");
            writer.flush();
        }
    }

    /**
     * Write header.
     * 
     * @param trafficLights
     *            the traffic lights
     */
    private void writeHeader(Iterable<TrafficLight> trafficLights) {
        writer.printf(COMMENT_CHAR + " number codes for traffic lights status: %n");
        for(TrafficLightStatus status : TrafficLightStatus.values()){
            writer.printf(COMMENT_CHAR + " %s --> %d %n", status.toString(), status.ordinal());
        }

        int counter = 1;
        for (final TrafficLight trafficLight : trafficLights) {
            writer.printf(COMMENT_CHAR + " position of traffic light no. %d: %5.2f m%n", counter,
                    trafficLight.position());
            counter++;
        }
        writer.printf(COMMENT_CHAR + " %-8s  %-8s  %-8s  %-8s %n", "time[s]", "position[m]_TL1", "status[1]_TL1",
                " etc. ");
        writer.flush();
    }
}
