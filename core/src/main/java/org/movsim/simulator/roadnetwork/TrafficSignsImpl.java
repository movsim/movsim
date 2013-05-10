package org.movsim.simulator.roadnetwork;

import java.util.EnumMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.movsim.simulator.roadnetwork.TrafficSign.TrafficSignType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

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
        for (TrafficSignType type : trafficSigns.keySet()) {
            trafficSigns.put(type, new TreeSet<TrafficSign>());
        }
    }

    // private void initMap() {
    // for (TrafficSignType type : trafficSigns.keySet()) {
    // trafficSigns.put(type, new TreeSet<>(new Comparator<TrafficSign>() {
    // @Override
    // public int compare(TrafficSign a, TrafficSign b) {
    // if (a != b && Double.compare(a.position(), b.position()) == 0) {
    // throw new IllegalStateException("cannot have identical positions of same type of trafficSigns="
    // + a.position());
    // }
    // return Double.compare(a.position(), b.position());
    // }
    // }));
    // }
    // }

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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TrafficSign> T getNextTrafficSign(TrafficSignType type, double position, int lane) {
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
                : (T) firstDownstreamOnLane;
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

}
