/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.input.model.simulation;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface TrafficLightsInput.
 */
public interface TrafficLightsInput {

    /**
     * Gets the traffic light data.
     * 
     * @return the traffic light data
     */
    List<TrafficLightData> getTrafficLightData();

    /**
     * Gets the n dt sample.
     * 
     * @return the n dt sample
     */
    int getnDtSample();

    /**
     * Checks if is with logging.
     * 
     * @return true, if is with logging
     */
    boolean isWithLogging();

}
