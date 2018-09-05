/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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
package org.movsim.output;

import org.movsim.autogen.TrafficLightStatus;
import org.movsim.input.ProjectMetaData;
import org.movsim.io.FileOutputBase;
import org.movsim.simulator.roadnetwork.controller.TrafficLight;
import org.movsim.simulator.roadnetwork.controller.TrafficLightController;
import org.movsim.simulator.roadnetwork.controller.TrafficLightRecordDataCallback;

import com.google.common.base.Preconditions;

public class FileTrafficLightControllerRecorder extends FileOutputBase implements TrafficLightRecordDataCallback {

    private static final String EXTENSION_FORMAT = ".controllerGroup_%s.firstSignal_%s.csv";
    private final int nTimestep;

    public FileTrafficLightControllerRecorder(TrafficLightController controller, int nTimestep) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        Preconditions.checkArgument(!controller.groupId().isEmpty());
        Preconditions.checkArgument(!controller.firstSignalId().isEmpty());
        this.nTimestep = nTimestep;
        String groupName = controller.groupId().replaceAll("\\s", "");
        String firstSignalId = controller.firstSignalId().replaceAll("\\s", "");
        writer = Preconditions.checkNotNull(createWriter(String.format(EXTENSION_FORMAT, groupName, firstSignalId)));
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
        if (iterationCount == 0) {
            writeHeader(trafficLights);
        }
        if (iterationCount % nTimestep != 0) {
            return;
        }
        String formattedTime = ProjectMetaData.getInstance().getFormatedTimeWithOffset(simulationTime);
        writeData(simulationTime, formattedTime, trafficLights);
    }

    private void writeData(double simulationTime, String formattedTime, Iterable<TrafficLight> trafficLights) {
        writer.printf("%8.2f, %s,  ", simulationTime, formattedTime);
        for (TrafficLight trafficLight : trafficLights) {
            writer.printf("%.1f,  %d,  ", trafficLight.position(), trafficLight.status().ordinal());
        }
        write(NEWLINE);
    }

    private void writeHeader(Iterable<TrafficLight> trafficLights) {
        writer.printf(COMMENT_CHAR + " number codes for traffic lights status: %n");
        for (TrafficLightStatus status : TrafficLightStatus.values()) {
            writer.printf(COMMENT_CHAR + " %s --> %d %n", status.toString(), status.ordinal());
        }

        int counter = 0;
        for (final TrafficLight trafficLight : trafficLights) {
            writer.printf(COMMENT_CHAR + " position of traffic light no. %d: %5.2fm, name=%s, groupId=%s%n", ++counter,
                    trafficLight.position(), trafficLight.signalType(), trafficLight.groupId());
        }
        writer.printf(COMMENT_CHAR + " %-8s  %-8s  %-8s  %-8s %n", "time[s]", "position[m]_TL1", "status[1]_TL1",
                " etc.");
        writer.flush();
    }
}
