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

import org.movsim.simulator.roadSection.TrafficLight;

// TODO: Auto-generated Javadoc
/**
 * The Interface Vehicle.
 */
public interface Vehicle {

   

    /**
     * Gets the label.
     *
     * @return the label
     */
    String getLabel();
    
    /**
     * Length.
     * 
     * @return the double
     */
    double length();

    /**
     * Position.
     * 
     * @return the double
     */
    double position();

    /**
     * Pos front bumper.
     * 
     * @return the double
     */
    double posFrontBumper();

    /**
     * Pos read bumper.
     * 
     * @return the double
     */
    double posReadBumper();

    /**
     * Old position.
     * 
     * @return the double
     */
    double oldPosition();

    /**
     * Checks for reaction time.
     * 
     * @return true, if successful
     */
    boolean hasReactionTime();

    /**
     * Gets the desired speed parameter.
     * 
     * @return the desired speed parameter
     */
    double getDesiredSpeedParameter();

    /**
     * Speed.
     * 
     * @return the double
     */
    double speed();

    /**
     * Acc.
     * 
     * @return the double
     */
    double acc();

    /**
     * Acc model.
     * 
     * @return the double
     */
    double accModel();

    /**
     * Speedlimit.
     * 
     * @return the double
     */
    double speedlimit();

    /**
     * Sets the speedlimit.
     * 
     * @param speedlimit
     *            the new speedlimit
     */
    void setSpeedlimit(double speedlimit);

    /**
     * Id.
     * 
     * @return the int
     */
    int id();

    /**
     * Gets the veh number.
     * 
     * @return the veh number
     */
    int getVehNumber();

    /**
     * Sets the veh number.
     * 
     * @param vehNumber
     *            the new veh number
     */
    void setVehNumber(int vehNumber);

    /**
     * Checks if is from onramp.
     * 
     * @return true, if is from onramp
     */
    boolean isFromOnramp();

    /**
     * Gets the lane.
     * 
     * @return the lane
     */
    double getLane();

    /**
     * Gets the int lane.
     * 
     * @return the int lane
     */
    int getIntLane();

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
     * Net distance.
     * 
     * @param vehFront
     *            the veh front
     * @return the double
     */
    double netDistance(Vehicle vehFront);

    /**
     * Rel speed.
     * 
     * @param vehFront
     *            the veh front
     * @return the double
     */
    double relSpeed(Vehicle vehFront);

    /**
     * Distance to trafficlight.
     * 
     * @return the double
     */
    double distanceToTrafficlight();

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

}
