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

package org.movsim.simulator.roadnetwork.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

public abstract class RoadObjectController implements RoadObject {

    protected static final Logger LOG = LoggerFactory.getLogger(RoadObjectController.class);

    protected final RoadObjectType type;

    protected final double position;

    protected final RoadSegment roadSegment;

    public RoadObjectController(RoadObjectType type, double position, RoadSegment roadSegment) {
        this.type = type;
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        Preconditions.checkArgument(position >= 0 && position <= roadSegment.roadLength(),
                "inconsistent input data: roadObject " + type + " at position=" + position
                        + " does not fit onto roadId=" + roadSegment.userId());
        this.position = position;
    }

    @Override
    public RoadObjectType getType() {
        return type;
    }

    @Override
    public int compareTo(RoadObject compareSign) {
        return Double.compare(position, compareSign.position()); // in ascending order
    }

    @Override
    public double position() {
        return position;
    }

    @Override
    public RoadSegment roadSegment() {
        return roadSegment;
    }

    @Override
    public String toString() {
        return "RoadObjectController [roadSegment=" + roadSegment + ", position=" + position + ", type=" + type + "]";
    }

    /**
     * returns distance from vehicle to trafficLight (positive if trafficlight is downstream, negative if trafficlight is upstream). The
     * search radius is limited to {@code maxLookAheadDistance} while the road network links are searched recursively.
     *
     * @param vehicle
     * @param vehicleRoadSegment
     * @param maxLookAheadDistance
     * @return the distance from vehicle to trafficLight or 'NaN' if vehicle is not found within radius
     */
    public double distanceTo(Vehicle vehicle, RoadSegment vehicleRoadSegment, double maxLookAheadDistance) {
        if (roadSegment == vehicleRoadSegment) {
            // trivial case: vehicle and trafficlight on same roadSegment
            return position - vehicle.getFrontPosition();
        }

        double accumDistance = vehicleRoadSegment.roadLength() - vehicle.getFrontPosition();
        // look downstream from vehicle's roadsegment until found the trafficlight or maxLookAheadDistance reached
        Set<RoadSegment> visitedRoadSegments = Sets.newHashSet();
        accumDistance = checkDownstreamRoadSegments(vehicleRoadSegment, accumDistance, maxLookAheadDistance,
                visitedRoadSegments);

        if (!Double.isNaN(accumDistance)) {
            return accumDistance;
        }

        // now checks *one* roadSegment downstream from trafficlight, results in negative distance.
        double distanceFromTrafficLight = position - roadSegment.roadLength();
        Iterator<LaneSegment> laneSegmentIterator = roadSegment.laneSegmentIterator();
        while (laneSegmentIterator.hasNext()) {
            LaneSegment laneSegment = laneSegmentIterator.next();
            if (laneSegment.hasSinkLaneSegment() && laneSegment.sinkLaneSegment().roadSegment() == vehicleRoadSegment) {
                distanceFromTrafficLight -= vehicle.getFrontPosition();
                return distanceFromTrafficLight;
            }
        }
        return Double.NaN;
    }

    private double checkDownstreamRoadSegments(RoadSegment startRoadSegment, double distance,
            final double maxLookAheadDistance, Set<RoadSegment> visitedRoadSegments) {

        for (LaneSegment laneSegment : startRoadSegment.laneSegments()) {
            if (laneSegment.hasSinkLaneSegment()) {
                RoadSegment sinkRoadSegment = laneSegment.sinkLaneSegment().roadSegment();

                if (!visitedRoadSegments.contains(sinkRoadSegment)) {
                    visitedRoadSegments.add(sinkRoadSegment);
                    if (sinkRoadSegment == roadSegment) {
                        return distance + position;
                    } else if (distance + sinkRoadSegment.roadLength() > maxLookAheadDistance) {
                        return Double.NaN;
                    } else {
                        double newDistance = checkDownstreamRoadSegments(sinkRoadSegment,
                                distance + sinkRoadSegment.roadLength(), maxLookAheadDistance, visitedRoadSegments);
                        if (!Double.isNaN(newDistance)) {
                            return newDistance;
                        }
                    }
                }
            }
        }
        return Double.NaN;
    }

}
