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

import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.vehicles.lanechanging.impl.LaneChangingModelImpl;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;

// TODO: Auto-generated Javadoc
/**
 * The Interface Vehicle.
 */
public interface Vehicle extends Moveable {

    /**
     * Sets the speedlimit.
     * 
     * @param speedlimit
     *            the new speedlimit
     */
    void setSpeedlimit(double speedlimit);

    /**
     * Sets the veh number.
     * 
     * @param vehNumber
     *            the new veh number
     */
    void setVehNumber(int vehNumber);

    /**
     * Inits the.
     * 
     * @param pos
     *            the pos
     * @param v
     *            the v
     * @param lane
     *            the lane
     */
    void init(double pos, double v, int lane);

    /**
     * Update postion and speed.
     * 
     * @param dt
     *            the dt
     */
    void updatePostionAndSpeed(double dt);

    /**
     * Calc acceleration.
     * 
     * @param dt
     *            the dt
     * @param vehContainer
     *            the veh container
     * @param alphaT
     *            the alpha t
     * @param alphaV0
     *            the alpha v0
     */
    void calcAcceleration(double dt, VehicleContainer vehContainer, double alphaT, double alphaV0);

    /**
     * Update traffic light.
     * 
     * @param time
     *            the time
     * @param trafficLight
     *            the traffic light
     */
    void updateTrafficLight(double time, TrafficLight trafficLight);

    /**
     * Removes the observers.
     */
    void removeObservers();
    
    LaneChangingModelImpl getLaneChangingModel();

    AccelerationModel getAccelerationModel();
    
    
    void setPosition(double newPos);

    boolean doLaneChanging(final List<VehicleContainer> vehContainers);
    

    int getTargetLane();
    
    void setTargetLane(int targetLane);

    boolean isLaneChanging();

    void updateContinuousLaneChange(double dt);
}
