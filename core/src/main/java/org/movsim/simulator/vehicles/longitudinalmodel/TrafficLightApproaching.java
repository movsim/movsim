/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.longitudinalmodel;

import org.movsim.autogen.TrafficLightStatus;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.trafficlights.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class TrafficLightApproachingImpl.
 */
public class TrafficLightApproaching {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TrafficLightApproaching.class);

    private final double maxRangeLookAheadForTrafficlight = 1000;
    
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
     * @param trafficLight
     * @param distanceToTrafficlight
     * @param longModel
     */
    public void update(Vehicle me, TrafficLight trafficLight, double distanceToTrafficlight, LongitudinalModelBase longModel) {
        accTrafficLight = 0;
        considerTrafficLight = false;

        if (distanceToTrafficlight > maxRangeLookAheadForTrafficlight) {
            logger.debug("traffic light at distance={} to far away -- maxRangeLookAheadForTrafficlight={}",
                    distanceToTrafficlight, maxRangeLookAheadForTrafficlight);
            return;
        }
        
        //distanceToTrafficlight = trafficLight.position() - me.getFrontPosition();

        // happened earlier
//        if (distanceToTrafficlight <= 0) {
//            distanceToTrafficlight = MovsimConstants.INVALID_GAP; // not relevant
//            return;
//        }

        if (trafficLight.status() != TrafficLightStatus.GREEN) {
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

                if (trafficLight.status() == TrafficLightStatus.GREENRED) {
                    final double bKinMax = 6; // typical value: bIDM < comfortBrakeDecel < bKinMax < bMax
                    final double comfortBrakeDecel = 4;
                    final double brakeDist = (speed * speed) / (2 * bKinMax);
                    if ((accTrafficLight <= -comfortBrakeDecel || brakeDist >= distanceToTrafficlight)) {
                        // ignore traffic light
                        considerTrafficLight = false;
                    }
                }

                // traffic light is already red
                if (trafficLight.status() == TrafficLightStatus.RED) {
                    final double maxDeceleration = me.getMaxDeceleration();
                    final double minBrakeDist = (speed * speed) / (2 * maxDeceleration);
                    if (accTrafficLight <= -maxDeceleration || minBrakeDist >= distanceToTrafficlight) {
                        // ignore traffic light
                        logger.info(String
                                .format("veh id=%d in dilemma zone is going to pass red light at distance=%.2fm due to physics (assuming user-defined max. possible braking=%.2fm/s^2!",
                                        me.getId(), distanceToTrafficlight, maxDeceleration));
                        considerTrafficLight = false;
                    }
                }
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