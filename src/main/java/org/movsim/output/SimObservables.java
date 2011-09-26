/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.output;

import java.util.List;

import org.movsim.output.impl.TravelTimesImpl;

// TODO: Auto-generated Javadoc
/**
 * The Interface SimObservables.
 */
public interface SimObservables {

    /**
     * Gets the spatio temporal.
     * 
     * @return the spatio temporal
     */
    SpatioTemporal getSpatioTemporal();

    /**
     * Gets the floating cars.
     * 
     * @return the floating cars
     */
    FloatingCars getFloatingCars();

    /**
     * Gets the loop detectors.
     * 
     * @return the loop detectors
     */
    List<LoopDetector> getLoopDetectors();
    
    
    TravelTimesImpl getTravelTimes();

}
