/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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

/**
 * The Interface RoadSection.
 */
public interface RoadSection {

    /**
     * Road length.
     * 
     * @return the double
     */
    double roadLength();

    /**
     * N lanes.
     * 
     * @return the int
     */
    int nLanes();

    long id();
    
    /**
     * Update.
     * 
     * @param itime
     *            the itime
     * @param time
     *            the time
     */
    void update(int itime, double time);

    /**
     * Veh container.
     * 
     * @return the vehicle container
     */
    VehicleContainer vehContainer();

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

}
