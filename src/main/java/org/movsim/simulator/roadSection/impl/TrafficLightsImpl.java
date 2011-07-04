/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.simulator.roadSection.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.model.simulation.TrafficLightData;
import org.movsim.input.model.simulation.TrafficLightsInput;
import org.movsim.output.fileoutput.FileTrafficLightRecorder;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.roadSection.TrafficLights;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class TrafficLightsImpl.
 */
public class TrafficLightsImpl implements TrafficLights {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TrafficLightsImpl.class);

    /** The n dt. */
    private final int nDt;

    /** The traffic lights. */
    private List<TrafficLight> trafficLights;

    /** The traffic light recorder. */
    private FileTrafficLightRecorder fileTrafficLightRecorder = null;

    /**
     * Instantiates a new traffic lights impl.
     * 
     * @param projectName
     *            the project name
     * @param trafficLightsInput
     *            the traffic lights input
     */
    public TrafficLightsImpl(String projectName, TrafficLightsInput trafficLightsInput) {

        initTrafficLights(trafficLightsInput);

        nDt = trafficLightsInput.getnDtSample();

        if (trafficLightsInput.isWithLogging()) {
            fileTrafficLightRecorder = new FileTrafficLightRecorder(projectName, nDt, trafficLights);
        }

    }

    /**
     * Inits the traffic lights.
     * 
     * @param trafficLightsInput
     *            the traffic lights input
     */
    private void initTrafficLights(TrafficLightsInput trafficLightsInput) {
        trafficLights = new ArrayList<TrafficLight>();
        final List<TrafficLightData> trafficLightData = trafficLightsInput.getTrafficLightData();
        for (final TrafficLightData tlData : trafficLightData) {
            trafficLights.add(new TrafficLightImpl(tlData));
        }
    }

    /**
     * Update.
     * 
     * @param itime
     *            the itime
     * @param time
     *            the time
     * @param vehicles
     *            the vehicles
     */
    public void update(int itime, double time, List<Vehicle> vehicles) {

        if (!trafficLights.isEmpty()) {
            // first update traffic light status
            for (final TrafficLight trafficLight : trafficLights) {
                trafficLight.update(time);
            }
            // second update vehicle status approaching traffic lights
            for (final Vehicle veh : vehicles) {
                for (final TrafficLight trafficLight : trafficLights) {
                    veh.updateTrafficLight(time, trafficLight);
                }
            }
        }

        if (fileTrafficLightRecorder != null) {
            fileTrafficLightRecorder.update(itime, time, trafficLights);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLights#getTrafficLights()
     */
    @Override
    public List<TrafficLight> getTrafficLights() {
        return trafficLights;
    }
}
