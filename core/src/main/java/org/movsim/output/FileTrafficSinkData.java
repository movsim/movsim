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

import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTrafficSinkData extends FileOutputBase implements
        org.movsim.simulator.roadnetwork.boundaries.TrafficSink.RecordDataCallback {

    private static final Logger LOG = LoggerFactory.getLogger(FileTrafficSourceData.class);

    private static final String extensionFormat = ".sink.road_%s.csv";
    private static final String outputHeading = COMMENT_CHAR
            + "     t[s], timeFormatted, totalVehiclesRemoved, lane, route, vehicleId, vehicleLabel, vehicleUserData ...\n";
    private static final String outputFormat = "%10.2f, %s, %6d, %2d, %s, %s, %s, %s %n";

    public FileTrafficSinkData(String roadId) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        writer = createWriter(String.format(extensionFormat, roadId));
        writer.printf(outputHeading);
    }

    @Override
    public void recordData(double simulationTime, int totalVehiclesRemoved, Vehicle vehicle) {
        String formattedTime = ProjectMetaData.getInstance().getFormatedTimeWithOffset(simulationTime);
        writer.printf(outputFormat, simulationTime, formattedTime, totalVehiclesRemoved, vehicle.lateralModel().lane(),
                vehicle.getRouteName(), vehicle.getId(), vehicle.getLabel(),
                vehicle.getUserData().getString(SEPARATOR_CHAR));
        writer.flush();
    }
}
