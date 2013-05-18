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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.controller.RoadObject.RoadObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class RoadObjects implements Iterable<RoadObject> {

    private static final Logger LOG = LoggerFactory.getLogger(RoadObjects.class);

    final RoadSegment roadSegment;

    // sorted in ascending order
    final EnumMap<RoadObjectType, SortedSet<RoadObject>> roadObjects = new EnumMap<>(RoadObjectType.class);

    Predicate<RoadObject> predicate = null;

    public RoadObjects(RoadSegment roadSegment) {
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        initMap();
    }

    private void initMap() {
        for (RoadObjectType type : EnumSet.allOf(RoadObjectType.class)) {
            roadObjects.put(type, new TreeSet<RoadObject>());
        }
    }

    public void add(RoadObject roadObject) {
        Preconditions.checkNotNull(roadObject);
        SortedSet<RoadObject> sortedSet = roadObjects.get(roadObject.getType());
        if (!sortedSet.subSet(roadObject, roadObject).isEmpty()) {
            throw new IllegalStateException("cannot have identical positions of same type of roadObjects="
                    + roadObject.position());
        }
        if (!sortedSet.add(roadObject)) {
            throw new IllegalStateException("cannot add roadObject=" + roadObject);
        }
    }

    public boolean hasRoadObject(RoadObjectType type) {
        return !roadObjects.get(type).isEmpty();
    }

    // public <T extends RoadObject> T getNextTrafficSign(RoadObjectType type, double position, int lane) {
    // TrafficSignWithDistance nextTrafficSignWithDistance = getNextTrafficSignWithDistance(type, position, lane);
    // return (T) (nextTrafficSignWithDistance == null ? null : nextTrafficSignWithDistance.trafficSign());
    // }
    //
    // public TrafficSignWithDistance getNextTrafficSignWithDistance(RoadObjectType type, double position, int lane) {
    // RoadObject firstDownstreamOnLane = getFirstDownstream(type, position, lane);
    // double distance = (firstDownstreamOnLane == null) ? roadSegment.roadLength() - position : firstDownstreamOnLane
    // .position() - position;
    //
    // if (distance > type.getLookAheadDistance()) {
    // return null;
    // }
    //
    // if (firstDownstreamOnLane == null) {
    // // continue searching in downstream link(s)
    // LaneSegment nextLaneSegment = roadSegment.laneSegment(lane).sinkLaneSegment();
    //
    // while (firstDownstreamOnLane == null && nextLaneSegment != null && distance < type.getLookAheadDistance()) {
    // int nextLane = nextLaneSegment.lane();
    // firstDownstreamOnLane = nextLaneSegment.roadSegment().roadObjects()
    // .getNextTrafficSign(type, position, nextLane);
    // distance += (firstDownstreamOnLane != null) ? firstDownstreamOnLane.position() : nextLaneSegment
    // .roadLength();
    // nextLaneSegment = nextLaneSegment.sinkLaneSegment();
    // }
    // }
    // return firstDownstreamOnLane == null || distance > type.getLookAheadDistance() ? null
    // : new TrafficSignWithDistanceImpl(firstDownstreamOnLane, distance);
    // }
    //
    // private RoadObject getFirstDownstream(RoadObjectType type, double position, int lane) {
    // RoadObjectController dummy = new RoadObjectController(type, position, roadSegment);
    // for (RoadObject sign : roadObjects.get(type).tailSet(dummy)) {
    // if (sign.isValidLane(lane)) {
    // return sign;
    // }
    // }
    // return null;
    // }
    //
    // // public double calcDistance(double position, int lane, RoadObject sign){
    // // return 0;
    // // }
    //
    // class TrafficSignWithDistanceImpl implements TrafficSignWithDistance {
    //
    // private final RoadObject trafficSign;
    // private final double distance;
    //
    // public TrafficSignWithDistanceImpl(@Nullable RoadObject trafficSign, double distance) {
    // this.trafficSign = trafficSign;
    // this.distance = distance;
    // }
    //
    // @Override
    // @SuppressWarnings("unchecked")
    // public <T extends RoadObject> T trafficSign() {
    // return (T) trafficSign;
    // }
    //
    // @Override
    // public double distance() {
    // return distance;
    // }
    //
    // }

    @SuppressWarnings("unchecked")
    public <T extends RoadObject> Iterator<T> iterator(RoadObjectType type) {
        return Iterators.unmodifiableIterator((Iterator<T>) roadObjects.get(type).iterator());
    }

    @SuppressWarnings("unchecked")
    public <T extends RoadObject> Iterable<T> values(RoadObjectType type) {
        return Iterables.unmodifiableIterable((Iterable<T>) roadObjects.get(type));
    }

    @Override
    public Iterator<RoadObject> iterator() {
        return Iterables.concat(roadObjects.values()).iterator();
    }

}
