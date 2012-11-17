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
import org.movsim.input.model.output.ConsumptionOnRouteInput;
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.input.model.output.TravelTimeOnRouteInput;
import org.movsim.input.model.vehicle.VehicleInput;
import org.movsim.output.fileoutput.FileFundamentalDiagram;
import org.movsim.output.fileoutput.FileTrajectories;
import org.movsim.output.floatingcars.FloatingCars;
import org.movsim.output.route.ConsumptionOnRoute;
import org.movsim.output.route.TravelTimeOnRoute;
import org.movsim.output.spatiotemporal.SpatioTemporal;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The Class SimulationOutput.
 */
public class SimulationOutput implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulationOutput.class);

    private List<FloatingCars> floatingCarOutputs = new ArrayList<FloatingCars>();

    private final List<SpatioTemporal> spatioTemporals = new ArrayList<SpatioTemporal>();

    private final Map<Route, FileTrajectories> filesTrajectories = new HashMap<Route, FileTrajectories>();

    private final Map<Route, ConsumptionOnRoute> consumptionOnRoutes = new HashMap<Route, ConsumptionOnRoute>();

    private final Map<Route, TravelTimeOnRoute> travelTimeOnRoutes = new HashMap<Route, TravelTimeOnRoute>();

    private final RoadNetwork roadNetwork;

    private final Map<String, Route> routes;

    public SimulationOutput(double simulationTimestep, boolean writeOutput, InputData simInput,
            RoadNetwork roadNetwork, Map<String, Route> routes) {
        this.roadNetwork = roadNetwork;
        this.routes = routes;

        final SimulationInput simulationInput = simInput.getSimulationInput();
        if (simulationInput == null) {
            return;
        }

        if (writeOutput) {
            writeFundamentalDiagrams(simulationTimestep, simInput.getVehiclesInput());
        }

        final OutputInput outputInput = simulationInput.getOutputInput();

        initFloatingCars(writeOutput, outputInput);

        initConsumption(writeOutput, simulationTimestep, outputInput);
        
        initTravelTimes(writeOutput, simulationTimestep, outputInput);

        initSpatioTemporalOutput(writeOutput, outputInput);

        initTrajectories(writeOutput, outputInput);

    }

    private void initConsumption(boolean writeOutput, double simulationTimestep, final OutputInput outputInput) {
        for (final ConsumptionOnRouteInput fuelRouteInput : outputInput.getFuelInput()) {
            final Route route = getCheckedRoute(fuelRouteInput.getRouteLabel());
            final ConsumptionOnRoute consumption = new ConsumptionOnRoute(simulationTimestep, fuelRouteInput, roadNetwork, route, writeOutput);
            consumptionOnRoutes.put(route, consumption);
        }
    }
    
    private void initTravelTimes(boolean writeOutput, double simulationTimestep, final OutputInput outputInput) {
        for (final TravelTimeOnRouteInput travelTimeInput : outputInput.getTravelTimesInput()) {
            final Route route = getCheckedRoute(travelTimeInput.getRouteLabel());
            final TravelTimeOnRoute travelTime = new TravelTimeOnRoute(simulationTimestep, travelTimeInput, roadNetwork, route, writeOutput);
            travelTimeOnRoutes.put(route, travelTime);
        }
    }

    private void initTrajectories(boolean writeOutput, final OutputInput outputInput) {
        final List<TrajectoriesInput> trajInput = outputInput.getTrajectoriesInput();
        if (writeOutput) {
            for (final TrajectoriesInput traj : trajInput) {
                final Route route = getCheckedRoute(traj.getRouteLabel());
                if (filesTrajectories.containsKey(route)) {
                    logger.warn("trajectory output for route \"{}\" already defined!", route.getName());
                    continue;
                }
                filesTrajectories.put(route, new FileTrajectories(traj, route));
            }
        }
    }

    private void initSpatioTemporalOutput(boolean writeOutput, final OutputInput outputInput) {
        final List<SpatioTemporalInput> spatioTemporalInputs = outputInput.getSpatioTemporalInput();
        for (final SpatioTemporalInput spatioTemporalInput : spatioTemporalInputs) {
            final Route route = getCheckedRoute(spatioTemporalInput.getRouteLabel());
            final SpatioTemporal spatioTemporal = new SpatioTemporal(spatioTemporalInput.getDx(),
                    spatioTemporalInput.getDt(), roadNetwork, route, writeOutput);
            spatioTemporals.add(spatioTemporal);
        }
    }

    private Route getCheckedRoute(final String routeLabel) {
        Preconditions.checkArgument(routes.containsKey(routeLabel), "route with label=" + routeLabel + "not defined. ");
        return routes.get(routeLabel);
    }

    private void initFloatingCars(boolean writeOutput, final OutputInput outputInput) {
        final List<FloatingCarInput> floatingCarInputs = outputInput.getFloatingCarInputs();
        for(FloatingCarInput floatingCarInput : floatingCarInputs){
            Route route = getCheckedRoute(floatingCarInput.getRouteLabel());
            floatingCarOutputs.add(new FloatingCars(floatingCarInput, route, writeOutput));
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

        for (FloatingCars floatingCars : floatingCarOutputs) {
            floatingCars.timeStep(dt, simulationTime, iterationCount);
        }

        for (final SpatioTemporal sp : spatioTemporals) {
            sp.timeStep(dt, simulationTime, iterationCount);
        }

        for (final FileTrajectories filetraj : filesTrajectories.values()) {
            filetraj.timeStep(dt, simulationTime, iterationCount);
        }

        for (final TravelTimeOnRoute travelTime : travelTimeOnRoutes.values()) {
            travelTime.timeStep(dt, simulationTime, iterationCount);
        }
        
        for (final ConsumptionOnRoute consumption : consumptionOnRoutes.values()){
            consumption.timeStep(dt, simulationTime, iterationCount);
        }

    }

}
