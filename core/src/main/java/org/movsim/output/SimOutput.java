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
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.RouteInput;
import org.movsim.input.model.output.RoutesInput;
import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.input.model.output.TravelTimesInput;
import org.movsim.output.fileoutput.FileFloatingCars;
import org.movsim.output.fileoutput.FileSpatioTemporal;
import org.movsim.output.fileoutput.FileTrajectories;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SimOutput.
 */
public class SimOutput implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimOutput.class);

    private List<SpatioTemporal> spatioTemporals = null;
    private List<FileSpatioTemporal> fileSpatioTemporal;
    private FloatingCars floatingCars = null;
    private FileFloatingCars fileFloatingCars;
    private FileTrajectories fileTrajectories = null;
    private final RoadNetwork roadNetwork;
    private TravelTimes travelTimes;

    /**
     * Constructor.
     * 
     * @param simInput
     *            the sim input
     */
    public SimOutput(boolean writeOutput, InputData simInput, RoadNetwork roadNetwork) {
        this.roadNetwork = roadNetwork;
        RoadSegment roadSegment = roadNetwork.size() == 0 ? null : roadNetwork.iterator().next();
        // TODO - test route is hardcoded for now
        final Route testRoute = new Route();
        testRoute.setName("testRoute1");
        if (roadSegment != null && roadSegment.userId().equals("1")) {
            testRoute.add(roadSegment);
            RoadSegment nextRoadSegment = roadNetwork.findByUserId("2");
            if (nextRoadSegment != null) {
                testRoute.add(nextRoadSegment);
                nextRoadSegment = roadNetwork.findByUserId("3");
                if (nextRoadSegment != null) {
                    testRoute.add(nextRoadSegment);
                }
            }
        }

        final SimulationInput simulationInput = simInput.getSimulationInput();
        if (simulationInput == null) {
            return;
        }
        final OutputInput outputInput = simulationInput.getOutputInput();

        RoutesInput routesInput = outputInput.getRoutesInput();
        Map<String, Route> routes = new HashMap<String, Route>();
        if (routesInput != null) {
            for (RouteInput routeInput : routesInput.getRoutes()) {
                Route route = new Route();
                route.setName(routeInput.getName());
                List<String> roadIds = routeInput.getRoadIds();
                for (String road : roadIds) {
                    route.add(roadNetwork.findByUserId(road));
                }
                routes.put(route.getName(), route);
            }
        }

        final TravelTimesInput travelTimesInput = outputInput.getTravelTimesInput();
        if (travelTimesInput != null) {
            travelTimes = new TravelTimes(travelTimesInput, roadNetwork);
        }

        // Floating Car Output
        final FloatingCarInput floatingCarInput = outputInput.getFloatingCarInput();
        if (floatingCarInput.isWithFCD()) {
            floatingCars = new FloatingCars(roadSegment, floatingCarInput);
            if (writeOutput) {
                fileFloatingCars = new FileFloatingCars(floatingCars);
            }
        }

        final List<SpatioTemporalInput> spatioTemporalInputs = outputInput.getSpatioTemporalInput();
        if (spatioTemporalInputs != null) {
            spatioTemporals = new ArrayList<SpatioTemporal>();
            for (SpatioTemporalInput spatioTemporalInput : spatioTemporalInputs) {
                final SpatioTemporal spatioTemporal = new SpatioTemporal(spatioTemporalInput, routes);
                spatioTemporals.add(spatioTemporal);
                if (writeOutput) {
                    fileSpatioTemporal = new ArrayList<FileSpatioTemporal>();
                    fileSpatioTemporal.add(new FileSpatioTemporal(spatioTemporal));
                }
            }
        }

        final TrajectoriesInput trajInput = outputInput.getTrajectoriesInput();
        if (trajInput.isInitialized()) {
            if (writeOutput) {
                fileTrajectories = new FileTrajectories(trajInput, testRoute); // TODO remove testRoute
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        if (floatingCars != null) {
            floatingCars.timeStep(dt, simulationTime, iterationCount);
        }
        if (spatioTemporals != null) {
            for (SpatioTemporal sp : spatioTemporals) {
                sp.timeStep(dt, simulationTime, iterationCount);
            }
        }

        if (fileTrajectories != null) {
            fileTrajectories.timeStep(dt, simulationTime, iterationCount);
        }

        if (travelTimes != null) {
            travelTimes.timeStep(dt, simulationTime, iterationCount);
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
    public List<LoopDetector> getLoopDetectors(RoadSegment roadSegment) {
        return roadSegment.getLoopDetectors().getDetectors();
    }

    public TravelTimes getTravelTimes() {
        return travelTimes;
    }
}
