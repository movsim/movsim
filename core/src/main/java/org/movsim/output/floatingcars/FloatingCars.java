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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.movsim.autogen.FloatingCarOutput;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class FloatingCars implements SimulationTimeStep {

    private static final Logger LOG = LoggerFactory.getLogger(FloatingCars.class);

    private final Collection<Integer> floatingCarVehicleNumbers;

    private final int nDtOut;

    private final double randomFraction;

    private final Route route;

    private final FileFloatingCars fileFloatingCars;

    private final Map<Vehicle, PrintWriter> printWriters;

    public FloatingCars(FloatingCarOutput floatingCarOutput, Route route, boolean writeFileOutput) {
        Preconditions.checkNotNull(route);
        this.nDtOut = floatingCarOutput.getNTimestep();
        this.randomFraction = (floatingCarOutput.getRandomFraction() < 0 || floatingCarOutput.getRandomFraction() > 1) ? 0
                : floatingCarOutput.getRandomFraction();
        this.route = route;
        floatingCarVehicleNumbers = new HashSet<>();
        for (org.movsim.autogen.FloatingCar fc : floatingCarOutput.getFloatingCar()) {
            floatingCarVehicleNumbers.add(Integer.valueOf(fc.getNumber()));
        }
        fileFloatingCars = (writeFileOutput) ? new FileFloatingCars() : null;
        printWriters = new HashMap<>(149, 0.75f);
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (fileFloatingCars != null && iterationCount % nDtOut == 0) {
            LOG.debug("update FloatingCars: iterationCount={}", iterationCount);
            writeOutput(simulationTime);
        }
    }

    private void writeOutput(double simulationTime) {
        for (final RoadSegment roadSegment : route) {
            for (Vehicle vehicle : roadSegment) {
                PrintWriter writer = checkFloatingCar(vehicle);
                if (writer != null) {
                    final Vehicle frontVeh = roadSegment.frontVehicleOnLane(vehicle);
                    FileFloatingCars.writeData(simulationTime, vehicle, frontVeh, writer);
                }
            }
        }
    }

    private PrintWriter checkFloatingCar(Vehicle vehicle) {
        PrintWriter printWriter = printWriters.get(vehicle);
        if (printWriter != null) {
            return printWriter;
        }
        final int vehNumber = vehicle.getVehNumber();
        if (floatingCarVehicleNumbers.contains(vehNumber) || selectRandomPercentage(vehicle)) {
            floatingCarVehicleNumbers.remove(vehNumber);
            final PrintWriter writer = fileFloatingCars.createWriter(vehicle, route);
            FileFloatingCars.writeHeader(writer, vehicle, route);
            writer.flush();
            printWriters.put(vehicle, writer);
            return writer;
        }
        return null;
    }

    private boolean selectRandomPercentage(Vehicle vehicle) {
        return (vehicle.getRandomFix() < randomFraction) && (vehicle.roadSegmentId() == route.getOrigin().id());
    }

}
