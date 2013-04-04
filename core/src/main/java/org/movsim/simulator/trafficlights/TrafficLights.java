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
import org.movsim.simulator.SimulationTimeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * The Class TrafficLights.
 */
public class TrafficLights implements SimulationTimeStep {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TrafficLights.class);

    private final List<TrafficLightControlGroup> trafficLightControlGroups = new ArrayList<>();

    private final Map<String, TrafficLight> trafficLights = new HashMap<>();

    public TrafficLights(@Nullable org.movsim.autogen.TrafficLights trafficLightsInput) {
        if (trafficLightsInput == null) {
            return;
        }
        createControllers(trafficLightsInput);
        createTrafficLightMapping();
    }

    private void createControllers(org.movsim.autogen.TrafficLights trafficLightsInput) {
        Set<String> controllGroupNames = new HashSet<>();
        for (ControllerGroup controllerGroup : trafficLightsInput.getControllerGroup()) {
            Preconditions.checkArgument(!controllerGroup.getPhase().isEmpty(),
                    "at least one phase must be defined in a controller group.");
            TrafficLightControlGroup trafficLightControlGroup = new TrafficLightControlGroup(controllerGroup);
            if (trafficLightsInput.isLogging()) {
                Preconditions.checkArgument(controllGroupNames.add(controllerGroup.getName()), "controlgroup name="
                        + controllerGroup.getName() + " not unique.");
                trafficLightControlGroup.setRecorder(new FileTrafficLightControllerRecorder(controllerGroup.getName(),
                        trafficLightsInput.getNDt(), trafficLightControlGroup.trafficLights()));
            }
            trafficLightControlGroups.add(trafficLightControlGroup);
        }
    }

    private void createTrafficLightMapping() {
        for (TrafficLightControlGroup controller : trafficLightControlGroups) {
            for (TrafficLight trafficLight : ImmutableList.copyOf(controller.trafficLightIterator())) {
                if(trafficLights.put(trafficLight.id(), trafficLight) != null){
                    throw new IllegalArgumentException("trafficLight=" + trafficLight.toString()
                            + " is referenced in more than one controller group in movsim input.");
                }
            }
        }
    }

    public TrafficLight get(String id) {
        if (!trafficLights.containsKey(id)) {
            throw new IllegalStateException("traffic light with id=" + id + " requested but not configured in input.");
        }
        return trafficLights.get(id);
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

}
