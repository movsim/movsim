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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.movsim.autogen.ControllerGroup;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Controller.Control;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The Class TrafficLights.
 * 
 * Sets the traffic lights for each road segment by connecting the trafficlights (and the controllers) with the road segment
 * locations parsed from the infrastructure input.
 * 
 */
public class TrafficLights implements SimulationTimeStep {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TrafficLights.class);

    private final List<TrafficLightControlGroup> trafficLightControlGroups = new ArrayList<>();

    /** mapping from signalIds to controllers */
    private final Map<String, TrafficLightControlGroup> signalIdToController = new HashMap<>();

    public TrafficLights(@Nullable org.movsim.autogen.TrafficLights trafficLightsInput, RoadNetwork roadNetwork) {
        if (trafficLightsInput == null) {
            return;
        }
        setUp(trafficLightsInput, roadNetwork);
        checkIfAllTrafficlightsAreReferenced();
        if (trafficLightsInput.isLogging()) {
            setUpLogging(trafficLightsInput.getNTimestep());
        }
    }

    /**
     * Update.
     * 
     * @param dt
     *            simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        for (TrafficLightControlGroup group : trafficLightControlGroups) {
            group.timeStep(dt, simulationTime, iterationCount);
        }
    }

    private void setUp(org.movsim.autogen.TrafficLights trafficLightsInput, RoadNetwork roadNetwork) {
        Map<String, ControllerGroup> controllerGroupInput = createControllerMapping(trafficLightsInput);
        for (RoadSegment roadSegment : roadNetwork) {
            for (TrafficLightLocation location : roadSegment.trafficLightLocations()) {
                ControllerGroup controllerGroup = controllerGroupInput.get(location.controllerId());
                if (controllerGroup == null) {
                    throw new IllegalStateException("no controllerGroup for id=" + location.controllerId()
                            + " defined in input");
                }
                TrafficLight trafficLight = getOrCreate(location, controllerGroup);
                trafficLight.setPosition(location.position());
                trafficLight.setRoadSegment(roadSegment);
                location.setTrafficLight(trafficLight);
            }
        }
    }

    private TrafficLight getOrCreate(TrafficLightLocation location, ControllerGroup controllerGroup) {
        TrafficLightControlGroup group = signalIdToController.get(location.signalId());
        if (group == null) {
            LOG.info("create new controllergroup for location={}", location.toString());
            group = new TrafficLightControlGroup(controllerGroup, location.getController().getControl().get(0)
                    .getSignalId());
            trafficLightControlGroups.add(group);
            for (Control control : location.getController().getControl()) {
                signalIdToController.put(control.getSignalId(), group);
            }
        }

        return group.getTrafficLight(location.signalName());
    }

    private static Map<String, ControllerGroup> createControllerMapping(org.movsim.autogen.TrafficLights input) {
        Map<String, ControllerGroup> controllerMap = new HashMap<>();
        Set<String> controllGroupNames = new HashSet<>();
        for (ControllerGroup controllerGroup : input.getControllerGroup()) {
            Preconditions.checkArgument(!controllerGroup.getPhase().isEmpty(),
                    "at least one phase must be defined in a controller group.");
            Preconditions.checkArgument(controllGroupNames.add(controllerGroup.getId()), "controlgroup name="
                    + controllerGroup.getId() + " not unique.");
            controllerMap.put(controllerGroup.getId(), controllerGroup);
        }
        return controllerMap;
    }

    private void checkIfAllTrafficlightsAreReferenced() {
        for (TrafficLightControlGroup group : trafficLightControlGroups) {
            for (TrafficLight trafficLight : group.trafficLights()) {
                Preconditions.checkArgument(trafficLight.hasPosition(), "trafficlight=" + trafficLight.name()
                        + " within group=" + trafficLight.groupId() + " is not referenced to a signal on a road!");
            }
        }
    }

    private void setUpLogging(int nTimestep) {
        for (TrafficLightControlGroup group : trafficLightControlGroups) {
            group.setRecorder(new FileTrafficLightControllerRecorder(group, nTimestep));
        }
    }

}
