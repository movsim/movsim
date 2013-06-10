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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.movsim.autogen.TrafficLightStatus;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
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

    // order in list given by signal points
    private List<TrafficLight> trafficLights = new ArrayList<>();

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
        assert !alreadyAdded(trafficLight); // not necessarily needed
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

        // TODO for lane-specific trafficlight check more than first TL
        TrafficLight trafficLight = getRelevantTrafficLight(vehicle, roadSegment);
        if (trafficLight == null) {
            return;
        }

        if (trafficLight.status() != TrafficLightStatus.GREEN) {
            final double maxRangeOfSight = MovsimConstants.GAP_INFINITY;
            if (distanceToTrafficlight < maxRangeOfSight) {
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
                }

                // traffic light is already red
                if (trafficLight.status() == TrafficLightStatus.RED) {
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
        }
    }

    private void removePassedTrafficLights(Vehicle vehicle, RoadSegment roadSegment) {
        // order of trafficlights not defined, loop over all
        for (Iterator<TrafficLight> iterator = trafficLights.iterator(); iterator.hasNext();) {
            TrafficLight trafficLight = iterator.next();
            double distance = trafficLight.distanceTo(vehicle, roadSegment);
            if (distance < 0) {
                LOG.debug("vehicle at pos={} , remove trafficLight={}", vehicle.getFrontPosition(), trafficLight);
                iterator.remove();
            }
        }
    }

    private TrafficLight getRelevantTrafficLight(Vehicle vehicle, RoadSegment roadSegment) {
        for(TrafficLight trafficLight : trafficLights){
            distanceToTrafficlight = trafficLight.distanceTo(vehicle, roadSegment);
            assert distanceToTrafficlight >= 0 : "trafficlight already passed, cleaning not working!";
            int relevantLaneDownstream = determineRelevantLane(vehicle, roadSegment, trafficLight);
            if (relevantLaneDownstream < 0) {
                // no trafficlight in downstream roadsegment that is directly connected to vehicle's lane
                continue;
            }
            // trafficlight's valid lane is relative to RoadSegment on which it is located.
            if (trafficLight.isValidLane(relevantLaneDownstream)) {
                return trafficLight;
            }
        }
        reset();
        return null;
    }

    private static int determineRelevantLane(Vehicle vehicle, RoadSegment roadSegment, TrafficLight trafficLight) {
        if (roadSegment == trafficLight.roadSegment()) {
            return vehicle.lane();
        }
        LaneSegment laneSegment = roadSegment.laneSegment(vehicle.lane());
        if (laneSegment.hasSinkLaneSegment()
                && laneSegment.sinkLaneSegment().roadSegment() == trafficLight.roadSegment()) {
            // vehicle considers downstream trafficlight in directly connected lane
            return roadSegment.laneSegment(vehicle.lane()).sinkLaneSegment().lane();
        }
        return -1; // invalid lane
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

    private void checkSpaceBeforePassingTrafficlight(Vehicle me, TrafficLight trafficLight,
            double distanceToTrafficlight) {
        // relative to position of first traffic light

        // FIXME
        // TrafficSignWithDistance trafficLightWithDistance = trafficLight.roadSegment().roadObjects()
        // .getNextTrafficSignWithDistance(RoadObjectType.TRAFFICLIGHT, trafficLight.position(), me.lane());
        // if (trafficLightWithDistance != null) {
        // double distanceBetweenTrafficlights = trafficLightWithDistance.distance();
        // if (distanceBetweenTrafficlights < 500) {
        // double effectiveFrontVehicleLengths = calcEffectiveFrontVehicleLengths(me, trafficLight,
        // distanceToTrafficlight + distanceBetweenTrafficlights);
        // LOG.debug("distanceBetweenTrafficlights={}, effectiveLengths+ownLength={}",
        // distanceBetweenTrafficlights, effectiveFrontVehicleLengths + me.getEffectiveLength());
        // if (effectiveFrontVehicleLengths > 0
        // && distanceBetweenTrafficlights < effectiveFrontVehicleLengths + me.getEffectiveLength()) {
        // considerTrafficLight = true;
        // accTrafficLight = calcAccelerationToTrafficlight(me, distanceToTrafficlight);
        // LOG.debug(
        // "stop in front of green trafficlight, not sufficient space: nextlight={}, space for vehicle(s)={}",
        // distanceBetweenTrafficlights, effectiveFrontVehicleLengths + me.getEffectiveLength());
        // }
        // }
        // }
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
}