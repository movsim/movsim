package org.movsim.simulator.roadnetwork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.movsim.input.model.simulation.TrafficLightData;
import org.movsim.input.model.simulation.TrafficLightsInput;
import org.movsim.output.fileoutput.FileTrafficLightRecorder;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TrafficLights.
 */
public class TrafficLights implements Iterable<TrafficLight>{

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TrafficLights.class);

    private final int nDt;
    private Collection<TrafficLight> trafficLights;
    private FileTrafficLightRecorder fileTrafficLightRecorder;

    /**
     * Constructor.
     * 
     * @param trafficLightsInput
     * @param roadSegment
     */
    public TrafficLights(TrafficLightsInput trafficLightsInput, RoadSegment roadSegment) {

        initTrafficLights(trafficLightsInput);
        nDt = trafficLightsInput.getnDtSample();
        if (trafficLightsInput.isWithLogging()) {
            fileTrafficLightRecorder = new FileTrafficLightRecorder(nDt, trafficLights, roadSegment);
        }
    }

    /**
     * Initializes the traffic lights.
     * 
     * @param trafficLightsInput
     */
    private void initTrafficLights(TrafficLightsInput trafficLightsInput) {
        trafficLights = new ArrayList<TrafficLight>();
        final List<TrafficLightData> trafficLightData = trafficLightsInput.getTrafficLightData();
        for (final TrafficLightData tlData : trafficLightData) {
            trafficLights.add(new TrafficLight(tlData));
        }
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
     * @param roadSegment
     */
    public void update(double dt, double simulationTime, long iterationCount, RoadSegment roadSegment) {

        if (!trafficLights.isEmpty()) {
            // first update traffic light status
            for (final TrafficLight trafficLight : trafficLights) {
                trafficLight.update(simulationTime);
            }
            // then update vehicle status approaching traffic lights
            final int laneCount = roadSegment.laneCount();
            for (int lane = 0; lane < laneCount; ++lane) {
                final LaneSegment laneSegment = roadSegment.laneSegment(lane);
                for (final Vehicle vehicle : laneSegment) {
                    for (final TrafficLight trafficLight : trafficLights) {
                        vehicle.updateTrafficLight(simulationTime, trafficLight);
                    }
                }
            }
        }
        if (fileTrafficLightRecorder != null) {
            fileTrafficLightRecorder.update(simulationTime, iterationCount, trafficLights);
        }
    }

	@Override
	public Iterator<TrafficLight> iterator() {
        return trafficLights.iterator();
	}
}
