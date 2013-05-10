package org.movsim.simulator.roadnetwork;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.movsim.simulator.roadnetwork.TrafficSign.TrafficSignType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

class TrafficSignsImpl implements TrafficSigns {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficSignsImpl.class);

    final RoadSegment roadSegment;
    
    final EnumMap<TrafficSignType, SortedSet<TrafficSign>> trafficSigns = new EnumMap<>(TrafficSignType.class);

    public TrafficSignsImpl(RoadSegment roadSegment) {
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        initMap();
    }

    private void initMap() {
        for (TrafficSignType type : trafficSigns.keySet()) {
            trafficSigns.put(type, new TreeSet<>(new Comparator<TrafficSign>() {
                @Override
                public int compare(TrafficSign a, TrafficSign b) {
                    if (a != b && Double.compare(a.position(), b.position()) == 0) {
                        throw new IllegalStateException("cannot have identical positions of same type of trafficSigns="
                                + a.position());
                    }
                    return Double.compare(a.position(), b.position());
                }
            }));
        }
    }

    @Override
    public boolean add(TrafficSign trafficSign) {
        Preconditions.checkNotNull(trafficSign);
        Preconditions.checkArgument(trafficSign.position() >= 0 && trafficSign.position() <= roadSegment.roadLength(),
                "inconsistent input data: trafficSign " + trafficSign.getType().toString() + " at position="
                        + trafficSign.position() + " does not fit onto road with id=" + roadSegment.userId());
        return trafficSigns.get(trafficSign.getType()).add(trafficSign);
    }

    @Override
    public boolean hasTrafficSign(TrafficSignType type) {
        return !trafficSigns.get(type).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TrafficSign> T getNextTrafficSign(TrafficSignType type, double position, int lane) {
        for (TrafficSign trafficSign : trafficSigns.get(type)) {
            double distance = trafficSign.position() - position;
            if (distance > 0) {
                // TODO impl more efficient search, e.g. binary search
                // TODO extend lookAhead until maxDistance reached
                // TODO check if sign is applicable to lane
                return (T) trafficSign;
            }
        }
        return null;
    }


}
