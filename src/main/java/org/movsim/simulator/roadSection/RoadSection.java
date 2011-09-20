/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.roadSection;

import java.util.List;

import org.movsim.input.InputData;
import org.movsim.output.LoopDetector;

// TODO: Auto-generated Javadoc
/**
 * The Interface RoadSection.
 */
public interface RoadSection extends BasicRoadSection {

   
    /**
     * Gets the traffic lights.
     * 
     * @return the traffic lights
     */
    List<TrafficLight> getTrafficLights();

    /**
     * Gets the loop detectors.
     * 
     * @return the loop detectors
     */
    List<LoopDetector> getLoopDetectors();
    
    //void updateOnramps(long iterationCount, double dt, double time);

    /**
     * Update detectors.
     *
     * @param iterationCount the iteration count
     * @param dt the dt
     * @param simulationTime the simulation time
     */
    void updateDetectors(long iterationCount, double dt, double simulationTime);
    
//    // hack for debugging/testing
//   RoadSection getMobilRampHack();
    
    /**
 * Ramp factory.
 *
 * @param inputData the input data
 * @return the list
 */
List<RoadSection> rampFactory(final InputData inputData);
    
    /**
     * Lane changing to offramps.
     *
     * @param ramps the ramps
     * @param iterationCount the iteration count
     * @param dt the dt
     * @param time the time
     */
    void laneChangingToOfframps(List<RoadSection> ramps, long iterationCount, double dt, double time);

    void updateBoundaryVehicles(long iterationCount, double time);

    long getFromId();

    long getToId();    
}

