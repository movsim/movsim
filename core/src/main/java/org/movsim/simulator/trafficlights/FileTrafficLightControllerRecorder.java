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
package org.movsim.simulator.trafficlights;

import org.movsim.autogen.TrafficLightStatus;
import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;

import com.google.common.base.Preconditions;

/**
 * The Class FileTrafficLightControllerRecorder.
 */
public class FileTrafficLightControllerRecorder extends FileOutputBase implements
        TrafficLightControlGroup.RecordDataCallback {

    private static final String extensionFormat = ".controllerGroup_%s.firstSignal_%s.csv";
    private final int nTimestep;

    /**
     * Constructor.
     * 
     * @param nTimestep
     *            the n'th timestep
     * @param trafficLights
     *            the traffic lights
     */
    public FileTrafficLightControllerRecorder(TrafficLightControlGroup group, int nTimestep) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        Preconditions.checkArgument(!group.groupId().isEmpty());
        Preconditions.checkArgument(!group.firstSignalId().isEmpty());
        this.nTimestep = nTimestep;
        String groupName = group.groupId().replaceAll("\\s", "");
        String firstSignalId = group.firstSignalId().replaceAll("\\s", "");
        writer = Preconditions.checkNotNull(createWriter(String.format(extensionFormat, groupName, firstSignalId)));
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
        write("%n");
    }

    /**
     * Write header.
     * 
     * @param trafficLights
     *            the traffic lights
     */
    private void writeHeader(Iterable<TrafficLight> trafficLights) {
        writer.printf(COMMENT_CHAR + " number codes for traffic lights status: %n");
        for (TrafficLightStatus status : TrafficLightStatus.values()) {
            writer.printf(COMMENT_CHAR + " %s --> %d %n", status.toString(), status.ordinal());
        }

        int counter = 0;
        for (final TrafficLight trafficLight : trafficLights) {
            writer.printf(COMMENT_CHAR + " position of traffic light no. %d: %5.2fm, name=%s, groupId=%s%n",
                    ++counter, trafficLight.position(), trafficLight.name(), trafficLight.groupId());
        }
        writer.printf(COMMENT_CHAR + " %-8s  %-8s  %-8s  %-8s %n", "time[s]", "position[m]_TL1", "status[1]_TL1",
                " etc.");
        writer.flush();
    }
}
