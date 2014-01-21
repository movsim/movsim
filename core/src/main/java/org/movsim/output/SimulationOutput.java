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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.movsim.autogen.ConsumptionCalculation;
import org.movsim.autogen.FloatingCarOutput;
import org.movsim.autogen.OutputConfiguration;
import org.movsim.autogen.SpatioTemporalConfiguration;
import org.movsim.autogen.Trajectories;
import org.movsim.autogen.TravelTimes;
import org.movsim.output.floatingcars.FloatingCars;
import org.movsim.output.route.ConsumptionOnRoute;
import org.movsim.output.route.FileTrajectories;
import org.movsim.output.route.SpatioTemporal;
import org.movsim.output.route.TravelTimeOnRoute;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.observer.ServiceProvider;
import org.movsim.simulator.observer.ServiceProviders;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.movsim.simulator.vehicles.VehicleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The Class SimulationOutput.
 */
public class SimulationOutput implements SimulationTimeStep {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SimulationOutput.class);

    private List<FloatingCars> floatingCarOutputs = new ArrayList<>();

    private final List<SpatioTemporal> spatioTemporals = new ArrayList<>();

    private final Map<Route, FileTrajectories> filesTrajectories = new HashMap<>();

    private final Map<Route, ConsumptionOnRoute> consumptionOnRoutes = new HashMap<>();

    private final Map<Route, TravelTimeOnRoute> travelTimeOnRoutes = new HashMap<>();

    private final ServiceProviders serviceProviders;
    
    private final RoadNetwork roadNetwork;

    private final Routing routing;

    public SimulationOutput(double simulationTimestep, boolean writeOutput, OutputConfiguration outputConfiguration,
            RoadNetwork roadNetwork, Routing routing, VehicleFactory vehicleFactory,
            @Nullable ServiceProviders serviceProviders) {

        Preconditions.checkNotNull(outputConfiguration);
        this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
        this.routing = Preconditions.checkNotNull(routing);
        this.serviceProviders = serviceProviders;

        initFloatingCars(writeOutput, outputConfiguration);
        initConsumption(writeOutput, simulationTimestep, outputConfiguration);
        initTravelTimes(writeOutput, simulationTimestep, outputConfiguration);
        initSpatioTemporalOutput(writeOutput, outputConfiguration);
        initTrajectories(writeOutput, outputConfiguration);

    }

    private Route getCheckedRoute(final String routeLabel) {
        return routing.get(routeLabel);
    }

    private void initConsumption(boolean writeOutput, double simulationTimestep,
            final OutputConfiguration outputConfiguration) {
        for (final ConsumptionCalculation fuelRouteInput : outputConfiguration.getConsumptionCalculation()) {
            final Route route = getCheckedRoute(fuelRouteInput.getRoute());
            final ConsumptionOnRoute consumption = new ConsumptionOnRoute(simulationTimestep, fuelRouteInput,
                    roadNetwork, route, writeOutput);
            consumptionOnRoutes.put(route, consumption);
        }
    }

    private void initTravelTimes(boolean writeOutput, double simulationTimestep,
            final OutputConfiguration outputConfiguration) {
        for (final TravelTimes travelTimeInput : outputConfiguration.getTravelTimes()) {
            final Route route = getCheckedRoute(travelTimeInput.getRoute());
            final TravelTimeOnRoute travelTime = new TravelTimeOnRoute(simulationTimestep, travelTimeInput,
                    roadNetwork, route, writeOutput);
            travelTimeOnRoutes.put(route, travelTime);
        }
    }

    private void initTrajectories(boolean writeOutput, final OutputConfiguration outputConfiguration) {
        if (writeOutput) {
            for (final Trajectories traj : outputConfiguration.getTrajectories()) {
                final Route route = getCheckedRoute(traj.getRoute());
                if (filesTrajectories.containsKey(route)) {
                    LOG.warn("trajectory output for route \"{}\" already defined!", route.getName());
                    continue;
                }
                filesTrajectories.put(route, new FileTrajectories(traj, route));
            }
        }
    }

    private void initSpatioTemporalOutput(boolean writeOutput, final OutputConfiguration outputConfiguration) {
        for (final SpatioTemporalConfiguration spatioTemporalInput : outputConfiguration
                .getSpatioTemporalConfiguration()) {
            final Route route = getCheckedRoute(spatioTemporalInput.getRoute());
            final SpatioTemporal spatioTemporal = new SpatioTemporal(spatioTemporalInput.getDx(),
                    spatioTemporalInput.getDt(), roadNetwork, route, writeOutput);
            spatioTemporals.add(spatioTemporal);
        }
    }

    private void initFloatingCars(boolean writeOutput, OutputConfiguration outputInput) {
        for (FloatingCarOutput floatingCarOutput : outputInput.getFloatingCarOutput()) {
            Route route = getCheckedRoute(floatingCarOutput.getRoute());
            floatingCarOutputs.add(new FloatingCars(floatingCarOutput, route, writeOutput));
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

        for (final ConsumptionOnRoute consumption : consumptionOnRoutes.values()) {
            consumption.timeStep(dt, simulationTime, iterationCount);
        }

        if (serviceProviders != null) {
            for (final ServiceProvider serviceProvider : serviceProviders) {
                serviceProvider.timeStep(dt, simulationTime, iterationCount);
            }
        }
        
    }

}
