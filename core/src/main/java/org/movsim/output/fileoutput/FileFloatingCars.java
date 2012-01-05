/*
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                             <movsim.org@gmail.com>
 * --------------------------------------------------------------------------------------
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
 * --------------------------------------------------------------------------------------
 */
package org.movsim.output.fileoutput;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;

import org.movsim.output.FloatingCars;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.PhysicalQuantities;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.FileUtils;
import org.movsim.utilities.ObserverInTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileFloatingCars.
 */

// TODO output of physical quantities for Cellular Automata. Test scenario test_speedlimits.xml
public class FileFloatingCars extends FileOutputBase implements ObserverInTime {

    private static final String extensionFormat = ".car.origin_%d.%06d.csv";
    private static final String extensionRegex = "[.]car[.]origin_\\d+[.]\\d+[.]csv";
    
    private static final String outputHeading = String.format("%s%9s,%10s,%10s,%10s,%10s,%10s,%10s,%10s,%10s,%10s,%10s,%10s", COMMENT_CHAR,
            "t[s]", "lane", "x[m]", "roadId", "totalTravelledDistance[m]", "v[m/s]", "a[m/s^2]", "aModel[m/s^2]", "gap[m]", "dv[m/s]", "distToTrafficlight[m]", "fuelFlow[ml/s]");

    // note: number before decimal point is total width of field, not width of
    // integer part
    private static final String outputFormat = "%10.2f,%10d,%10.1f,%10d,%10.2f,%10.3f,%10.5f,%10.5f,%10.3f,%10.5f,%10.2f,%10f%n";

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FileFloatingCars.class);
    private final HashMap<Integer, PrintWriter> hashMap = new HashMap<Integer, PrintWriter>(149, 0.75f);
    private final FloatingCars floatingCars;
    private Collection<Integer> fcdNumbers;

    /**
     * Instantiates a new FileFloatingCars.
     * 
     * @param floatingCars
     *            the floating cars
     */
    public FileFloatingCars(FloatingCars floatingCars) {
        super();
        this.floatingCars = floatingCars;
        floatingCars.registerObserver(this);

        final String regex = baseFilename + extensionRegex;
        FileUtils.deleteFileList(path, regex);

        fcdNumbers = floatingCars.getFloatingCarVehicleNumbers();
    }

    /**
     * Adds the fcd.
     * 
     * @param vehNumber
     *            the vehicle number
     */
    private void addFloatingCar(final Vehicle veh, int vehNumber) {
        final long originId = veh.roadSegmentId();
        final String filename = createFileName(originId, vehNumber);
        final PrintWriter writer = FileUtils.getWriter(filename);
        hashMap.put(vehNumber, writer);
        writeHeader(writer, veh);
        writer.flush();
    }

    private void writeHeader(final PrintWriter writer, final Vehicle veh) {
        writer.println(String.format("%s vehicle id = %d", COMMENT_CHAR, veh.getId()));
        writer.println(String.format("%s model label  = %s", COMMENT_CHAR, veh.getLabel()));
        writer.println(String.format("%s model category = %s", COMMENT_CHAR, 
                veh.getLongitudinalModel().modelName().getCategory().toString()));
        writer.println(String.format("%s model name = %s (short name: %s)", COMMENT_CHAR, 
                veh.getLongitudinalModel().modelName().getDetailedName(), 
                veh.getLongitudinalModel().modelName().getShortName()));
        writer.println(String.format("%s physical vehicle length (in m) = %.2f", COMMENT_CHAR, veh.physicalQuantities().getLength()));
        writer.println(String.format("%s position x is defined by vehicle front (on the given road segment)", COMMENT_CHAR));
        
        writer.println(outputHeading);
    }

    /**
     * Write output.
     * 
     * @param updateTime
     *            the update time
     */
    public void writeOutput(double updateTime) {

        final RoadSegment roadSegment = floatingCars.getRoadSegment();
        final int laneCount = roadSegment.laneCount();
        for (int lane = 0; lane < laneCount; ++lane) {
            final LaneSegment laneSegment = roadSegment.laneSegment(lane);
            for (final Vehicle vehOnLane : laneSegment) {
                final int vehNumber = vehOnLane.getVehNumber();
                if (fcdNumbers != null && fcdNumbers.contains(vehNumber)) {
                    addFloatingCar(vehOnLane, vehNumber);
                    fcdNumbers.remove(vehNumber);
                    if (fcdNumbers.isEmpty()) {
                        fcdNumbers = null;
                    }
                }
                if (hashMap.containsKey(vehNumber)) {
                    final Vehicle frontVeh = laneSegment.frontVehicle(vehOnLane);
                    writeData(updateTime, vehOnLane, frontVeh, hashMap.get(vehNumber));
                }
            }
        }
    }

    /**
     * Creates the file name.
     * 
     * @param vehicleNumber
     *            the vehicleNumber
     * @param ending
     *            the ending
     * @return the string
     */
    private String createFileName(long originId, int vehicleNumber) {
        final String filename = path + File.separator + baseFilename
                + String.format(extensionFormat, originId, vehicleNumber);
        return filename;
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
    private void writeData(double time, Vehicle veh, Vehicle frontVeh, PrintWriter writer) {
        final PhysicalQuantities physicalQuantities = veh.physicalQuantities();
        writer.printf(outputFormat, time, veh.getLane(), physicalQuantities.getFrontPosition(),
                 veh.roadSegmentId(), physicalQuantities.totalTraveledDistance(),
                 physicalQuantities.getSpeed(), physicalQuantities.getAcc(),
                 physicalQuantities.accModel(), physicalQuantities.getNetDistance(frontVeh),
                 physicalQuantities.getRelSpeed(frontVeh), physicalQuantities.getxScale() * veh.getDistanceToTrafficlight(),
                 1000 * veh.getActualFuelFlowLiterPerS());
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
