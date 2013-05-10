package org.movsim.simulator.roadnetwork;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.movsim.simulator.roadnetwork.TrafficSign.TrafficSignType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

class TrafficSignsImpl implements TrafficSigns {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficSignsImpl.class);

    final RoadSegment roadSegment;
    
    // sorted in ascending order
    final EnumMap<TrafficSignType, SortedSet<TrafficSign>> trafficSigns = new EnumMap<>(TrafficSignType.class);

    Predicate<TrafficSign> predicate = null;

    public TrafficSignsImpl(RoadSegment roadSegment) {
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        initMap();
    }

    private void initMap() {
        for (TrafficSignType type : EnumSet.allOf(TrafficSignType.class)) {
            trafficSigns.put(type, new TreeSet<TrafficSign>());
        }
    }

    @Override
    public void add(TrafficSign trafficSign) {
        Preconditions.checkNotNull(trafficSign);
        SortedSet<TrafficSign> sortedSet = trafficSigns.get(trafficSign.getType());
        if (!sortedSet.subSet(trafficSign, trafficSign).isEmpty()) {
            throw new IllegalStateException("cannot have identical positions of same type of trafficSigns="
                    + trafficSign.position());
        }
        if (!sortedSet.add(trafficSign)) {
            throw new IllegalStateException("cannot add trafficSign=" + trafficSign);
        }
    }

    @Override
    public boolean hasTrafficSign(TrafficSignType type) {
        return !trafficSigns.get(type).isEmpty();
    }

    @Override
    public <T extends TrafficSign> T getNextTrafficSign(TrafficSignType type, double position, int lane) {
        TrafficSignWithDistance nextTrafficSignWithDistance = getNextTrafficSignWithDistance(type, position, lane);
        return (T) (nextTrafficSignWithDistance == null ? null : nextTrafficSignWithDistance.trafficSign());
    }
    
    @Override
    public TrafficSignWithDistance getNextTrafficSignWithDistance(TrafficSignType type, double position, int lane) {
        TrafficSign firstDownstreamOnLane = getFirstDownstream(type, position, lane);
        double distance = (firstDownstreamOnLane == null) ? roadSegment.roadLength() - position : firstDownstreamOnLane
                .position() - position;

        if (distance > type.getLookAheadDistance()) {
            return null;
        }

        if (firstDownstreamOnLane == null) {
            // continue searching in downstream link(s)
            RoadSegment nextSegment = roadSegment.sinkRoadSegment(lane);
            while (firstDownstreamOnLane == null && nextSegment != null && distance < type.getLookAheadDistance()) {
                firstDownstreamOnLane = nextSegment.getTrafficSigns().getNextTrafficSign(type, position, lane);
                distance += (firstDownstreamOnLane != null) ? firstDownstreamOnLane.position() : nextSegment
                        .roadLength();
                nextSegment = nextSegment.sinkRoadSegment(lane);
            }
        }
        return firstDownstreamOnLane == null || distance > type.getLookAheadDistance() ? null
                : new TrafficSignWithDistanceImpl(firstDownstreamOnLane, distance);
    }
    
    private TrafficSign getFirstDownstream(TrafficSignType type, double position, int lane) {
        TrafficSignBase dummy = new TrafficSignBase(type, position, roadSegment);
        for (TrafficSign sign : trafficSigns.get(type).tailSet(dummy)) {
            if (sign.isValidLane(lane)) {
                return sign;
            }
        }
        return null;
     }

    // public double calcDistance(double position, int lane, TrafficSign sign){
    // return 0;
    // }

    class TrafficSignWithDistanceImpl implements TrafficSignWithDistance {

        private final TrafficSign trafficSign;
        private final double distance;

        public TrafficSignWithDistanceImpl(@Nullable TrafficSign trafficSign, double distance) {
            this.trafficSign = trafficSign;
            this.distance = distance;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends TrafficSign> T trafficSign() {
            return (T) trafficSign;
        }

        @Override
        public double distance() {
            return distance;
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TrafficSign> Iterator<T> iterator(TrafficSignType type) {
        return Iterators.unmodifiableIterator((Iterator<T>) trafficSigns.get(type).iterator());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends TrafficSign> Iterable<T> values(TrafficSignType type) {
        return Iterables.unmodifiableIterable((Iterable<T>) trafficSigns.get(type));
    }

}
