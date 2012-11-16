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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FloatingCars.
 */
public class FloatingCars implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FloatingCars.class);

    private final Collection<Integer> floatingCarVehicleNumbers;
    private final int nDtOut;

    private final double randomFraction;

    private final RoadNetwork roadNetwork;

    private final FileFloatingCars fileWriter;

    private final Map<Vehicle, PrintWriter> printWriters;

    /**
     * Constructor.
     * 
     * @param roadSegment
     *            the road segment
     * @param input
     *            the input
     */
    public FloatingCars(FloatingCarInput input, RoadNetwork roadNetwork, boolean writeFileOutput) {
        this.roadNetwork = roadNetwork;
        this.nDtOut = input.getNDt();
        this.randomFraction = (input.getRandomFraction() < 0 || input.getRandomFraction() > 1) ? 0 : input
                .getRandomFraction();
        floatingCarVehicleNumbers = new ArrayList<Integer>();
        floatingCarVehicleNumbers.addAll(input.getFloatingCars());
        fileWriter = (writeFileOutput) ? new FileFloatingCars() : null;
        printWriters = new HashMap<Vehicle, PrintWriter>(149, 0.75f);
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (fileWriter != null && iterationCount % nDtOut == 0) {
            logger.debug("update FloatingCars: iterationCount={}", iterationCount);
            writeOutput(simulationTime);
        }
    }

    private void writeOutput(double simulationTime) {
        for (final RoadSegment roadSegment : roadNetwork) {
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
        if (floatingCarVehicleNumbers.contains(vehNumber) || vehicle.getRandomFix() < randomFraction) {
            floatingCarVehicleNumbers.remove(vehNumber);
            final PrintWriter writer = fileWriter.createWriter(vehicle);
            FileFloatingCars.writeHeader(writer, vehicle);
            writer.flush();
            printWriters.put(vehicle, writer);
            return writer;
        }
        return null;
    }

}
