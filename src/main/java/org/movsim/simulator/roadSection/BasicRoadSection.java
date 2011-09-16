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

import org.movsim.simulator.vehicles.VehicleContainer;



// TODO: Auto-generated Javadoc
/**
 * The Interface AbstractRoadSection.
 */
public interface BasicRoadSection {
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
         * Gets the veh container.
         *
         * @param laneIndex the lane index
         * @return the veh container
         */
        VehicleContainer getVehContainer(int laneIndex);
        
        /**
         * Gets the veh containers.
         *
         * @return the veh containers
         */
        List<VehicleContainer> getVehContainers();

        /**
         * Gets the timestep.
         * 
         * @return the timestep
         */
        double getTimestep();

        /**
         * Check for inconsistencies.
         *
         * @param iterationCount the iteration count
         * @param time the time
         * @param isWithCrashExit the is with crash exit
         */
        void checkForInconsistencies(long iterationCount, double time, boolean isWithCrashExit);
        
        /**
         * Lane changing.
         *
         * @param iterationCount the iteration count
         * @param dt the dt
         * @param time the time
         */
        void laneChanging(long iterationCount, double dt, double time);
        
        /**
         * Accelerate.
         *
         * @param iterationCount the iteration count
         * @param dt the dt
         * @param time the time
         */
        void accelerate(long iterationCount, double dt, double time);
        
        /**
         * Update road conditions.
         *
         * @param iterationCount the iteration count
         * @param time the time
         */
        void updateRoadConditions(long iterationCount, double time);
        
        /**
         * Update position and speed.
         *
         * @param iterationCount the iteration count
         * @param dt the dt
         * @param time the time
         */
        void updatePositionAndSpeed(long iterationCount, double dt, double time);
        
        /**
         * Update downstream boundary.
         */
        void updateDownstreamBoundary();
        
        /**
         * Update upstream boundary.
         *
         * @param iterationCount the iteration count
         * @param dt the dt
         * @param time the time
         */
        void updateUpstreamBoundary(long iterationCount, double dt, double time);
        

        /**
         * Gets the ramp merging length.
         *
         * @return the ramp merging length
         */
        double getRampMergingLength();  // TODO redesign for network view
        
        /**
         * Gets the ramp position to mainroad.
         *
         * @return the ramp position to mainroad
         */
        double getRampPositionToMainroad(); // TODO redesign for network view
       
    }

