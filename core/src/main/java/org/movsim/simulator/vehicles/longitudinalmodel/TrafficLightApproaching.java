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

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.movsim.autogen.TrafficLightStatus;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.controller.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The class TrafficLightApproaching.
 * 
 */
public class TrafficLightApproaching {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TrafficLightApproaching.class);

    // order of trafficlights given by signal points
    private Deque<TrafficLight> trafficLights = new LinkedList<>();

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

    public void addTrafficLight(TrafficLight trafficLight) {
        Preconditions.checkNotNull(trafficLight);
        assert !alreadyAdded(trafficLight); // check not necessarily needed
        trafficLights.add(trafficLight);
        LOG.debug("vehicle: trafficLightSize={}, added trafficlight={}", trafficLights.size(), trafficLight);
    }

    private boolean alreadyAdded(TrafficLight trafficLightToAdd) {
        for (TrafficLight trafficLight : trafficLights) {
            if (trafficLightToAdd == trafficLight) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update.
     * 
     * @param vehicle
     */
    public void update(Vehicle vehicle, RoadSegment roadSegment) {
        reset();
        removePassedTrafficLights(vehicle, roadSegment);

        TrafficLight trafficLight = findNonGreenTrafficLight(vehicle, roadSegment);
        if (trafficLight == null) {
            return;
        }
        distanceToTrafficlight = trafficLight.distanceTo(vehicle, roadSegment, TrafficLight.MAX_LOOK_AHEAD_DISTANCE);
        LOG.debug("approaching non-green trafficlight: distanceToTrafficlight={}, trafficLight={}",
                distanceToTrafficlight, toString());
        Preconditions.checkArgument(distanceToTrafficlight >= 0,
                        "trafficlight already passed, removal of passed lights not working! [check if vehicle has passed start-signal]");
        assert trafficLight.status() != TrafficLightStatus.GREEN;

        accTrafficLight = calcAccelerationToTrafficlight(vehicle, distanceToTrafficlight);
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
            final double brakeDist = (vehicle.getSpeed() * vehicle.getSpeed()) / (2 * bKinMax);
            if ((accTrafficLight <= -comfortBrakeDecel || brakeDist >= distanceToTrafficlight)) {
                // ignore traffic light
                considerTrafficLight = false;
            }
        } else if (trafficLight.status() == TrafficLightStatus.RED) {
            final double maxDeceleration = vehicle.getMaxDeceleration();
            final double minBrakeDist = (vehicle.getSpeed() * vehicle.getSpeed()) / (2 * maxDeceleration);
            if (accTrafficLight <= -maxDeceleration || minBrakeDist >= distanceToTrafficlight) {
                // ignore traffic light
                LOG.info(String
                        .format("veh id=%d in dilemma zone is going to pass red light at distance=%.2fm due to physics (assuming user-defined max. possible braking=%.2fm/s^2!",
                                vehicle.getId(), distanceToTrafficlight, maxDeceleration));
                considerTrafficLight = false;
            }
        }
    }

    private TrafficLight findNonGreenTrafficLight(Vehicle vehicle, RoadSegment roadSegment) {
        TrafficLight trafficLight = null;
        Iterator<TrafficLight> iterator = trafficLights.iterator();
        while (iterator.hasNext()) {
            trafficLight = iterator.next();
            LOG.debug("trafficlight={}", trafficLight);
            if (trafficLight.status() != TrafficLightStatus.GREEN
                    && trafficLight.status() != TrafficLightStatus.RED_GREEN) {
                return trafficLight;
            }
        }
        return null;
    }

    private void removePassedTrafficLights(Vehicle vehicle, RoadSegment roadSegment) {
        for (Iterator<TrafficLight> iterator = trafficLights.iterator(); iterator.hasNext();) {
            TrafficLight trafficLight = iterator.next();
            double distance = trafficLight.distanceTo(vehicle, roadSegment, TrafficLight.MAX_LOOK_AHEAD_DISTANCE);
            if (!Double.isNaN(distance) && distance < 0) {
                LOG.debug("vehicle at pos={}, remove trafficLight={}", vehicle.getFrontPosition(), trafficLight);
                iterator.remove();
            } else {
                return; // skip loop since trafficlights are ordered
            }
        }
    }

    private void reset() {
        accTrafficLight = 0;
        considerTrafficLight = false;
        distanceToTrafficlight = MovsimConstants.GAP_INFINITY;
    }

    private static double calcAccelerationToTrafficlight(Vehicle me, double distanceToTrafficlight) {
        final double speed = me.getSpeed();
        return Math.min(0, me.getLongitudinalModel().calcAccSimple(distanceToTrafficlight, speed, speed));
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

    private static double calcEffectiveFrontVehicleLengths(Vehicle me, TrafficLight trafficLight,
            double distanceToSecondTrafficlight) {
        double sumEffectiveLengths = 0;
        Vehicle frontVehicle = trafficLight.roadSegment().laneSegment(me.lane()).frontVehicle(me);
        while (frontVehicle != null && me.getBrutDistance(frontVehicle) < distanceToSecondTrafficlight) {
            sumEffectiveLengths += frontVehicle.getEffectiveLength();
            Vehicle prevFront = frontVehicle;
            frontVehicle = trafficLight.roadSegment().laneSegment(frontVehicle.lane()).frontVehicle(frontVehicle);
            if (frontVehicle != null && prevFront.getId() == frontVehicle.getId()) {
                // FIXME seems to be a real bug: get back the *same* vehicle when its entered the downstream roadsegment
                break;
            }
        }
        return sumEffectiveLengths;
    }

    @Override
    public String toString() {
        return "TrafficLightApproaching [trafficLights.size=" + trafficLights.size() + ", considerTrafficLight="
                + considerTrafficLight + ", accTrafficLight=" + accTrafficLight + ", distanceToTrafficlight="
                + distanceToTrafficlight + "]";
    }

    /**
     * Returns this vehicle's acceleration considering the traffic light.
     * 
     * @param roadSegment
     * 
     * @return acceleration considering traffic light or NaN if no traffic light is present
     */
    public double accelerationConsideringTrafficLight(Vehicle vehicle, RoadSegment roadSegment) {
        update(vehicle, roadSegment);
        if (considerTrafficLight()) {
            return accApproaching();
        }
        return Double.NaN;
    }
}