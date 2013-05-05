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
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.movsim.autogen.ControllerGroup;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Controller.Control;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * The Class TrafficLights.
 * 
 * Sets the trafficlights for each road segment by connecting the 'logical' trafficlights (and the controllers) with the 'physical' traffic
 * signals on a roadSegment locations. The specific 'physical' representation is parsed from the infrastructure input.
 * 
 */
public class TrafficLights implements SimulationTimeStep {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TrafficLights.class);

    private final List<TrafficLightControlGroup> trafficLightControlGroups;

    public TrafficLights(@Nullable org.movsim.autogen.TrafficLights trafficLightsInput, RoadNetwork roadNetwork) {
        this.trafficLightControlGroups = new ArrayList<>();
        if (trafficLightsInput == null) {
            if (networkContainsTrafficlights(roadNetwork)) {
                throw new IllegalStateException(
                        "inconsistent input: traffic lights defined in network but not in movsim input.");
            }
        } else {
            setUp(trafficLightsInput, roadNetwork);
            checkIfAllTrafficlightsAreReferenced();
            if (trafficLightsInput.isLogging()) {
                setUpLogging(trafficLightsInput.getNTimestep());
            }
        }
    }

    private static boolean networkContainsTrafficlights(RoadNetwork roadNetwork) {
        for (RoadSegment roadSegment : roadNetwork) {
            if (Iterables.size(roadSegment.trafficLights()) > 0) {
                return true;
            }
        }
        return false;
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
        Map<String, TrafficLightControlGroup> signalIdToController = new HashMap<>();
        Map<String, ControllerGroup> controllerGroupInput = createControllerMapping(trafficLightsInput);

        for (RoadSegment roadSegment : roadNetwork) {
            for (TrafficLight trafficLight : roadSegment.trafficLights()) {
                LOG.debug("trafficLight={}, roadSegment={}", trafficLight, roadSegment);
                ControllerGroup controllerGroup = controllerGroupInput.get(trafficLight.controllerId());
                if (controllerGroup == null) {
                    throw new IllegalStateException("no controllerGroup for id=" + trafficLight.controllerId()
                            + " defined in input");
                }
                TrafficLightControlGroup trafficLightControlGroup = signalIdToController.get(trafficLight.signalId());
                if (trafficLightControlGroup == null) {
                    LOG.debug("create new TrafficLightControllerGroup for trafficLight={}", trafficLight.toString());
                    trafficLightControlGroup = new TrafficLightControlGroup(controllerGroup);
                    trafficLightControlGroups.add(trafficLightControlGroup);
                    for (Control control : trafficLight.getController().getControl()) {
                        signalIdToController.put(control.getSignalId(), trafficLightControlGroup);
                    }
                }
                trafficLightControlGroup.add(trafficLight);
            }
        }
    }

    private static Map<String, ControllerGroup> createControllerMapping(org.movsim.autogen.TrafficLights input) {
        Map<String, ControllerGroup> controllerMap = new HashMap<>();
        for (ControllerGroup controllerGroup : input.getControllerGroup()) {
            Preconditions.checkArgument(!controllerGroup.getPhase().isEmpty(),
                    "at least one phase must be defined in a movsim controller group.");
            Preconditions.checkArgument(!controllerMap.containsKey(controllerGroup.getId()), "controlgroup name="
                    + controllerGroup.getId() + " not unique.");
            controllerMap.put(controllerGroup.getId(), controllerGroup);
        }
        return controllerMap;
    }

    private void checkIfAllTrafficlightsAreReferenced() {
        for (TrafficLightControlGroup group : trafficLightControlGroups) {
            group.checkIfAllSignalTypesAdded();
            for (TrafficLight trafficLight : group.trafficLights()) {
                Preconditions.checkArgument(trafficLight.hasTriggerCallback(),
                        "trafficlight-type=" + trafficLight.signalType() + " within group=" + trafficLight.groupId()
                                + " is not referenced to a signal on a road!");
            }
        }
    }

    private void setUpLogging(int nTimestep) {
        for (TrafficLightControlGroup group : trafficLightControlGroups) {
            group.setRecorder(new FileTrafficLightControllerRecorder(group, nTimestep));
        }
    }

}
