/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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
import org.movsim.simulator.roadnetwork.RoadSegment.TrafficLightLocationWithDistance;
import org.movsim.simulator.trafficlights.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class TrafficLightApproaching.
 * 
 */
public class TrafficLightApproaching {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TrafficLightApproaching.class);

    public static final double MAX_LOOK_AHEAD_DISTANCE = 1000;

    private boolean considerTrafficLight;

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
    public void update(Vehicle me, TrafficLight trafficLight, double distanceToTrafficlight) {
        accTrafficLight = 0;
        considerTrafficLight = false;

        if (distanceToTrafficlight > MAX_LOOK_AHEAD_DISTANCE) {
            LOG.debug("traffic light at distance={} to far away -- MAX_LOOK_AHEAD_DISTANCE={}", distanceToTrafficlight,
                    MAX_LOOK_AHEAD_DISTANCE);
            return;
        }

        // TODO consider refactoring if function really needed
        if (trafficLight.status() == TrafficLightStatus.GREEN) {
            // check if space is sufficient
            TrafficLightLocationWithDistance nextTrafficlight = trafficLight.roadSegment()
                    .getNextDownstreamTrafficLight(trafficLight.position(), me.lane(), MAX_LOOK_AHEAD_DISTANCE);
            if (nextTrafficlight.trafficLightLocation != null) {
                // TODO different roadsegments
                double distanceBetweenTrafficlights = nextTrafficlight.trafficLightLocation.position()
                        - trafficLight.position();
                Vehicle frontVehicle = trafficLight.roadSegment().laneSegment(me.lane()).frontVehicle(me);
                double effectiveFrontVehicleLength = 0;
                if (frontVehicle != null
                        && frontVehicle.getFrontPosition() < nextTrafficlight.trafficLightLocation.position()) {
                    effectiveFrontVehicleLength = frontVehicle.getEffectiveLength();
                }
                if (distanceBetweenTrafficlights < effectiveFrontVehicleLength + me.getEffectiveLength()) {
                    considerTrafficLight = true;
                    accTrafficLight = calcAcceleration(me, distanceToTrafficlight);
                    LOG.debug(
                            "stop in front of green traffic light because of insufficient space: space to next light={}, space for vehicle(s)={}",
                            distanceBetweenTrafficlights, effectiveFrontVehicleLength + me.getEffectiveLength());
                    LOG.debug("...and brake with acc={}", accTrafficLight);
                }
            }
        } else if (trafficLight.status() != TrafficLightStatus.GREEN) {
            // TODO define it as parameter ("range of sight" or so) ?!
            final double maxRangeOfSight = MovsimConstants.GAP_INFINITY;
            if (distanceToTrafficlight < maxRangeOfSight) {
                accTrafficLight = calcAcceleration(me, distanceToTrafficlight);
                if (accTrafficLight < 0) {
                    considerTrafficLight = true;
                    LOG.debug("distance to trafficLight = {}, accTL = {}", distanceToTrafficlight, accTrafficLight);
                }

                // TODO: decision logic while approaching yellow traffic light
                // ignore traffic light if accTL exceeds two times comfortable
                // deceleration or if kinematic braking is not possible anymore

                if (trafficLight.status() == TrafficLightStatus.GREEN_RED) {
                    final double bKinMax = 6; // typical value: bIDM < comfortBrakeDecel < bKinMax < bMax
                    final double comfortBrakeDecel = 4;
                    final double brakeDist = (me.getSpeed() * me.getSpeed()) / (2 * bKinMax);
                    if ((accTrafficLight <= -comfortBrakeDecel || brakeDist >= distanceToTrafficlight)) {
                        // ignore traffic light
                        considerTrafficLight = false;
                    }
                }

                // traffic light is already red
                if (trafficLight.status() == TrafficLightStatus.RED) {
                    final double maxDeceleration = me.getMaxDeceleration();
                    final double minBrakeDist = (me.getSpeed() * me.getSpeed()) / (2 * maxDeceleration);
                    if (accTrafficLight <= -maxDeceleration || minBrakeDist >= distanceToTrafficlight) {
                        // ignore traffic light
                        LOG.info(String
                                .format("veh id=%d in dilemma zone is going to pass red light at distance=%.2fm due to physics (assuming user-defined max. possible braking=%.2fm/s^2!",
                                        me.getId(), distanceToTrafficlight, maxDeceleration));
                        considerTrafficLight = false;
                    }
                }
            }
        }
    }

    private static double calcAcceleration(Vehicle me, double distanceToTrafficlight) {
        final double speed = me.getSpeed();
        return Math.min(0,
                me.getLongitudinalModel().calcAccSimple(distanceToTrafficlight, speed, speed));
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