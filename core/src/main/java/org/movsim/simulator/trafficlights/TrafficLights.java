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

import java.util.HashMap;
import java.util.Map;

import org.movsim.simulator.SimulationTimeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The Class TrafficLights.
 */
public class TrafficLights implements SimulationTimeStep {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(TrafficLights.class);

    private final Map<String, TrafficLight> trafficLights = new HashMap<>();

    public interface RecordDataCallback {
        /**
         * Callback to allow the application to process or record the traffic light data.
         * 
         * @param simulationTime
         *            the current logical time in the simulation
         * @param iterationCount
         * @param trafficLights
         */
        public void recordData(double simulationTime, long iterationCount, Iterable<TrafficLight> trafficLights);
    }

    private RecordDataCallback recordDataCallback;

    /**
     * Constructor.
     * 
     * @param roadLength
     * @param trafficLightsInput
     */
    public TrafficLights(org.movsim.core.autogen.TrafficLights trafficLightsInput) {
        Preconditions.checkNotNull(trafficLightsInput);
        for (final org.movsim.core.autogen.TrafficLight tlData : trafficLightsInput.getTrafficLight()) {
            TrafficLight put = trafficLights.put(tlData.getId(), new TrafficLight(tlData));
            if (put != null) {
                throw new IllegalArgumentException("traffic light with id=" + tlData.getId()
                        + " already exists. Check your input configuration.");
            }
        }
    }

    /**
     * Sets the traffic light recorder.
     * 
     * @param recordDataCallback
     */
    public void setRecorder(RecordDataCallback recordDataCallback) {
        this.recordDataCallback = recordDataCallback;
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
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        for (final TrafficLight trafficLight : trafficLights.values()) {
            trafficLight.update(simulationTime);
        }
        if (recordDataCallback != null) {
            recordDataCallback.recordData(simulationTime, iterationCount, trafficLights.values());
        }
    }

    // @Override
    // public Iterator<TrafficLight> iterator() {
    // return trafficLights.iterator();
    // }
}
