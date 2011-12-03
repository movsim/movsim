/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.longitudinalmodel;

import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class TrafficLightApproachingImpl.
 */
public class TrafficLightApproaching {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TrafficLightApproaching.class);

    /** The consider traffic light. */
    private boolean considerTrafficLight;

    /** The acc traffic light. */
    private double accTrafficLight;

    private double distanceToTrafficlight;

    /**
     * Instantiates a new traffic light approaching.
     */
    public TrafficLightApproaching() {
        considerTrafficLight = false;
        distanceToTrafficlight = MovsimConstants.INVALID_GAP;
    }

    /**
     * Update.
     * 
     * @param me
     *            the me
     * @param time
     *            the time
     * @param trafficLight
     *            the traffic light
     * @param longModel
     *            the long model
     */
    public void update(Vehicle me, double time, TrafficLight trafficLight, LongitudinalModelBase longModel) {
        accTrafficLight = 0;
        considerTrafficLight = false;

        distanceToTrafficlight = trafficLight.position() - me.getPosition() - 0.5 * me.getLength();

        if (distanceToTrafficlight <= 0) {
            distanceToTrafficlight = MovsimConstants.INVALID_GAP; // not relevant
        } else if (!trafficLight.isGreen()) {
            // TODO define it as parameter ("range of sight" or so) ?!
            final double maxRangeOfSight = MovsimConstants.GAP_INFINITY;
            if (distanceToTrafficlight < maxRangeOfSight) {
                final double speed = me.getSpeed();
                accTrafficLight = Math.min(0, longModel.calcAccSimple(distanceToTrafficlight, speed, speed));

                if (accTrafficLight < 0) {
                    considerTrafficLight = true;
                    logger.debug("distance to trafficLight = {}, accTL = {}", distanceToTrafficlight, accTrafficLight);
                }

                // TODO: decision logic while approaching yellow traffic light
                // ignore traffic light if accTL exceeds two times comfortable
                // deceleration or if kinematic braking is not possible anymore
                final double bKinMax = 6; // typical value: bIDM <
                                          // comfortBrakeDecel < bKinMax < bMax
                final double comfortBrakeDecel = 4;
                final double brakeDist = (speed * speed) / (2 * bKinMax);
                if (trafficLight.isGreenRed()
                        && (accTrafficLight <= -comfortBrakeDecel || brakeDist >= Math.abs(trafficLight.position()
                                - me.getPosition()))) {
                    // ignore traffic light
                    considerTrafficLight = false;
                }
                // if(me.getVehNumber()==1){
                // logger.debug("considerTrafficLight=true: distToTrafficlight={}, accTrafficLight={}",
                // distanceToTrafficlight, accTrafficLight);
                // }
            }
        }
    }

    /**
     * Consider traffic light.
     * 
     * @return true, if successful
     */
    public boolean considerTrafficLight() {
        return considerTrafficLight;
    }

    /**
     * Acc approaching.
     * 
     * @return the double
     */
    public double accApproaching() {
        return accTrafficLight;
    }

    /**
     * Gets the distance to trafficlight.
     * 
     * @return the distance to trafficlight
     */
    public double getDistanceToTrafficlight() {
        return distanceToTrafficlight;
    }

}
