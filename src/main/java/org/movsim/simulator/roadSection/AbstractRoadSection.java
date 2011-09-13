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



public interface AbstractRoadSection {
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
        
        VehicleContainer getVehContainer(int laneIndex);
        
        List<VehicleContainer> getVehContainers();

        /**
         * Gets the timestep.
         * 
         * @return the timestep
         */
        double getTimestep();

        void checkForInconsistencies(long iterationCount, double time, boolean isWithCrashExit);
        
        void laneChanging(long iterationCount, double dt, double time);
        
        void accelerate(long iterationCount, double dt, double time);
        
        void updateRoadConditions(long iterationCount, double time);
        
        void updatePositionAndSpeed(long iterationCount, double dt, double time);
        
        void updateDownstreamBoundary();
        
        void updateUpstreamBoundary(long iterationCount, double dt, double time);
        

        double getRampMergingLength();  // TODO redesign for network view
        double getRampPositionToMainroad(); // TODO redesign for network view
       
    }

