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
package org.movsim.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.input.InputData;
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.VehiclesInput;
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.FuelConsumptionOnRouteInput;
import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.input.model.output.TravelTimesInput;
import org.movsim.input.model.vehicle.VehicleInput;
import org.movsim.output.fileoutput.FileFloatingCars;
import org.movsim.output.fileoutput.FileFuelConsumptionOnRoute;
import org.movsim.output.fileoutput.FileFundamentalDiagram;
import org.movsim.output.fileoutput.FileSpatioTemporal;
import org.movsim.output.fileoutput.FileTrajectories;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SimulationOutput.
 */
public class SimulationOutput implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulationOutput.class);

    private List<SpatioTemporal> spatioTemporals;
    private List<FileSpatioTemporal> filesSpatioTemporal;
    private FloatingCars floatingCars;
    private FileFloatingCars fileFloatingCars;
    private final Map<Route, FileTrajectories> filesTrajectories = new HashMap<Route, FileTrajectories>();
    private final Map<Route, FileFuelConsumptionOnRoute> filesFuelConsumptionOnRoute = new HashMap<Route, FileFuelConsumptionOnRoute>();
    private TravelTimes travelTimes;  // TODO for route
    
    private final RoadNetwork roadNetwork;

    /**
     * Constructor.
     * 
     * @param simulationTimestep
     * 
     * @param simInput
     *            the sim input
     */
    public SimulationOutput(double simulationTimestep, boolean writeOutput, InputData simInput,
            RoadNetwork roadNetwork, Map<String, Route> routes) {
        this.roadNetwork = roadNetwork;

        final SimulationInput simulationInput = simInput.getSimulationInput();
        if (simulationInput == null) {
            return;
        }

        if (writeOutput) {
            writeFundamentalDiagrams(simulationTimestep, simInput.getVehiclesInput());
        }

        final OutputInput outputInput = simulationInput.getOutputInput();

        // Floating Car Output
        final FloatingCarInput floatingCarInput = outputInput.getFloatingCarInput();
        if (floatingCarInput != null) {
            floatingCars = new FloatingCars(floatingCarInput);
            if (writeOutput) {
                fileFloatingCars = new FileFloatingCars(roadNetwork, floatingCars);
            }
        }

        // Travel times output
        final List<TravelTimesInput> travelTimesInput = outputInput.getTravelTimesInput();
        if (travelTimesInput != null) {
            travelTimes = new TravelTimes(travelTimesInput, routes, roadNetwork);
        }

        // Spatio temporal output
        final List<SpatioTemporalInput> spatioTemporalInputs = outputInput.getSpatioTemporalInput();
        if (spatioTemporalInputs != null) {
            spatioTemporals = new ArrayList<SpatioTemporal>();
            if (writeOutput) {
                filesSpatioTemporal = new ArrayList<FileSpatioTemporal>();
            }
            for (final SpatioTemporalInput spatioTemporalInput : spatioTemporalInputs) {
                final Route route = routes.get(spatioTemporalInput.getRouteLabel());
                final SpatioTemporal spatioTemporal = new SpatioTemporal(spatioTemporalInput.getDx(),
                        spatioTemporalInput.getDt(), route);
                spatioTemporals.add(spatioTemporal);
                if (writeOutput) {
                    filesSpatioTemporal.add(new FileSpatioTemporal(spatioTemporal));
                }
            }
        }

        final List<TrajectoriesInput> trajInput = outputInput.getTrajectoriesInput();
        if (trajInput != null) {
            if (writeOutput) {
                for (final TrajectoriesInput traj : trajInput) {
                    final Route route = routes.get(traj.getRouteLabel());
                    if(filesTrajectories.containsKey(route)){
                        logger.warn("trajectory output for route \"{}\" already defined!", route.getName());
                        continue;
                    }
                    filesTrajectories.put(route, new FileTrajectories(traj, route));
                }
            }
        }

        final List<FuelConsumptionOnRouteInput> fuelInput = outputInput.getFuelInput();
        if (fuelInput != null) {
            if (writeOutput) {
                for (final FuelConsumptionOnRouteInput fuel : fuelInput) {
                    final Route route = routes.get(fuel.getRouteLabel());
                    if(filesFuelConsumptionOnRoute.containsKey(route)){
                        logger.warn("fuel consumption for route \"{}\" already defined!", route.getName());
                        continue;
                    }
                    filesFuelConsumptionOnRoute.put(route, new FileFuelConsumptionOnRoute(fuel, route));
                }
            }
        }
    }

    private static void writeFundamentalDiagrams(double simulationTimestep, VehiclesInput vehiclesInput) {
        if (!vehiclesInput.isWriteFundamentalDiagrams()) {
            return;
        }
        final String ignoreLabel = "Obstacle"; // quick hack
        logger.info("write fundamental diagrams but ignore label {}.", ignoreLabel);
        for (VehicleInput vehicleInput : vehiclesInput.getVehicleInputMap().values()) {
            if (!ignoreLabel.equalsIgnoreCase(vehicleInput.getLabel())) {
                FileFundamentalDiagram.writeToFile(simulationTimestep, vehicleInput);
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        if (floatingCars != null) {
            floatingCars.timeStep(dt, simulationTime, iterationCount);
        }

        if (spatioTemporals != null) {
            for (final SpatioTemporal sp : spatioTemporals) {
                sp.timeStep(dt, simulationTime, iterationCount);
            }
        }

        for (final FileTrajectories filetraj : filesTrajectories.values()) {
            filetraj.timeStep(dt, simulationTime, iterationCount);
        }

        if (travelTimes != null) {
            travelTimes.timeStep(dt, simulationTime, iterationCount);
        }

        for (final FileFuelConsumptionOnRoute fuel : filesFuelConsumptionOnRoute.values()) {
            fuel.timeStep(dt, simulationTime, iterationCount);
        }

    }

    /**
     * Gets the spatio temporals.
     * 
     * @return the spatio temporals
     */
    public List<SpatioTemporal> getSpatioTemporals() {
        return spatioTemporals;
    }

    /**
     * Gets the floating cars.
     * 
     * @return the floating cars
     */
    public FloatingCars getFloatingCars() {
        return floatingCars;
    }

    /**
     * Gets the loop detectors.
     * 
     * @return the loop detectors
     */
    public static List<LoopDetector> getLoopDetectors(RoadSegment roadSegment) {
        return roadSegment.getLoopDetectors().getDetectors();
    }

    public TravelTimes getTravelTimes() {
        return travelTimes;
    }
}
