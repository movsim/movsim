package org.movsim.simulator.roadnetwork.controller;


public interface TrafficLightRecordDataCallback {
    /**
     * Callback to allow the application to process or record the traffic light data.
     * 
     * @param simulationTime
     *            the current logical time in the simulation
     * @param iterationCount
     * @param trafficLights
     */
    void recordData(double simulationTime, long iterationCount, Iterable<TrafficLight> trafficLights);
}
