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

import org.movsim.output.LoopDetector;
import org.movsim.simulator.vehicles.VehicleContainer;

// TODO: Auto-generated Javadoc
/**
 * The Interface RoadSection.
 */
public interface RoadSection {

    /**
     * Road length.
     * 
     * @return the double
     */
    double getRoadLength();

    /**
     * N lanes.
     * 
     * @return the int
     */
    int getNumberOfLanes();

    /**
     * Id.
     * 
     * @return the long
     */
    long getId();

    /**
     * Update.
     * 
     * @param iterationCount
     *            the number of updates
     * @param time
     *            the simulation time
     */
    void update(int iterationCount, double time);

    
    VehicleContainer getVehContainer(int laneIndex);
    
    List<VehicleContainer> getVehContainers();

    /**
     * Gets the timestep.
     * 
     * @return the timestep
     */
    double getTimestep();

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
    
    void checkForInconsistencies(int iterationCount, double time);
    
    void accelerate(int iterationCount, double dt, double time);
    
    void updateRoadConditions(int iterationCount, double time);
    
    void updatePositionAndSpeed(int iterationCount, double dt, double time);
    
    void updateDownstreamBoundary();
    
    void updateUpstreamBoundary(int iterationCount, double dt, double time);
    
    void updateOnramps(int iterationCount, double dt, double time);

    /**
     * @param iterationCount
     * @param dt
     * @param simulationTime
     */
    void updateDetectors(int iterationCount, double dt, double simulationTime);

}
