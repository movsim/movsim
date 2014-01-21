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
package org.movsim.output.floatingcars;

import java.io.PrintWriter;
import java.util.Map;

import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.vehicles.PhysicalQuantities;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileFloatingCars.
 */

// TODO output of physical quantities for Cellular Automata. Test scenario test_speedlimits.xml
class FileFloatingCars extends FileOutputBase {

    /** The Constant LOG. */
    final static Logger LOG = LoggerFactory.getLogger(FileFloatingCars.class);

    private static final String extensionFormat = ".car.route_%s.%06d.csv";
    private static final String extensionRegex = "[.]car[.]route_.*[.]\\d+[.]csv";

    private static final String outputHeading = COMMENT_CHAR
            + "     t[s],    roadId,      lane,      x[m], totalX[m],    v[m/s],  a[m/s^2],aModel[m/s^2], gap[m],   dv[m/s],distToTL[m],fuelFlow[ml/s],frontVehID,slope[rad]";

    // note: number before decimal point is total width of field, not width of integer part
    private static final String outputFormat = "%10.2f,%10d,%10d,%10.2f,%10.2f,%10.3f,%10.5f,%10.5f,%10.3f,%10.5f,%10.2f,%10f,%10d,%8.5f%n";

    /**
     * Instantiates a new FileFloatingCars.
     * 
     * @param floatingCars
     *            the floating cars
     */
    FileFloatingCars() {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        String regex = baseFilename + extensionRegex;
        FileUtils.deleteFileList(path, regex);
    }

    PrintWriter createWriter(Vehicle vehicle, Route route) {
        return createWriter(String.format(extensionFormat, route.getName(), vehicle.getVehNumber()));
    }

    static void writeHeader(PrintWriter writer, Vehicle vehicle, Route route) {
        writer.println(String.format("%s vehicle id = %d", COMMENT_CHAR, vehicle.getId()));
        writer.println(String.format("%s random fix = %.8f", COMMENT_CHAR, vehicle.getRandomFix()));
        writer.println(String.format("%s model label = %s", COMMENT_CHAR, vehicle.getLabel()));
        writer.println(String.format("%s model category = %s", COMMENT_CHAR, vehicle.getLongitudinalModel().modelName()
                .getCategory().toString()));
        writer.println(String.format("%s model name = %s (short name: %s)", COMMENT_CHAR, vehicle
                .getLongitudinalModel().modelName().getDetailedName(), vehicle.getLongitudinalModel().modelName()
                .getShortName()));
        writer.println(String.format("%s physical vehicle length (in m) = %.2f", COMMENT_CHAR, vehicle
                .physicalQuantities().getLength()));
        writer.println(String.format("%s position x is defined by vehicle front (on the given road segment)",
                COMMENT_CHAR));
        writer.println(String.format("%s origin roadsegment id= %d, exit roadsegment id= %d (not set=%d)",
                COMMENT_CHAR, vehicle.originRoadSegmentId(), vehicle.exitRoadSegmentId(),
                Vehicle.ROAD_SEGMENT_ID_NOT_SET));
        writer.println(String.format("%s %s", COMMENT_CHAR, route.toString()));
        for(Map.Entry<String, String> entry : vehicle.getUserData()){
            writer.println(String.format("%s userData: %s=%s", COMMENT_CHAR, entry.getKey(), entry.getValue()));
        }
        writer.println(outputHeading);
    }

    /**
     * Write data in physical (not scaled) quantities
     * 
     * @param time
     *            the time
     * @param veh
     *            the veh
     * @param frontVeh
     *            the front veh
     * @param writer
     *            the writer
     */
    static void writeData(double time, Vehicle veh, Vehicle frontVeh, PrintWriter writer) {
        final PhysicalQuantities physicalQuantities = veh.physicalQuantities();
        writer.printf(outputFormat, time, veh.roadSegmentId(), veh.lane(), physicalQuantities.getFrontPosition(),
                physicalQuantities.totalTravelDistance(), physicalQuantities.getSpeed(), physicalQuantities.getAcc(),
                physicalQuantities.accModel(), physicalQuantities.getNetDistance(frontVeh),
                physicalQuantities.getRelSpeed(frontVeh),
                physicalQuantities.getxScale() * veh.getDistanceToTrafficlight(),
                1000 * veh.getEnergyModel().getActualFuelFlowLiterPerS(),
                frontVeh == null ? -1 : frontVeh.getVehNumber(),
                veh.getSlope());
        writer.flush();
    }

}
