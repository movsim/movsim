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
package org.movsim.simulator.vehicles;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface VehicleContainer.
 */
public interface VehicleContainer extends MoveableContainer {

    /**
     * Gets the vehicles.
     * 
     * @return the vehicles
     */
    List<Vehicle> getVehicles();

    /**
     * Size.
     * 
     * @return the int
     */
    @Override
    int size();

    /**
     * Gets the.
     * 
     * @param index
     *            the index
     * @return the vehicle
     */
    Vehicle get(int index);

    /**
     * Gets the most upstream.
     * 
     * @return the most upstream
     */
    Vehicle getMostUpstream();

    /**
     * Gets the most downstream.
     * 
     * @return the most downstream
     */
    Vehicle getMostDownstream();

    /**
     * Adds the.
     * 
     * @param veh
     *            the veh
     * @param xInit
     *            the x init
     * @param vInit
     *            the v init
     * @param laneInit
     *            the lane init
     */
    void add(Vehicle veh, double xInit, double vInit, int laneInit);

    /**
     * Adds the from ramp.
     * 
     * @param veh
     *            the veh
     * @param xInit
     *            the x init
     * @param vInit
     *            the v init
     * @param laneInit
     *            the lane init
     */
    void addFromRamp(Vehicle veh, double xInit, double vInit, int laneInit);

    /**
     * Removes the vehicles downstream.
     * 
     * @param roadLength
     *            the road length
     */
    void removeVehiclesDownstream(double roadLength);

    /**
     * Removes the vehicle most downstream.
     */
    void removeVehicleMostDownstream();

}
