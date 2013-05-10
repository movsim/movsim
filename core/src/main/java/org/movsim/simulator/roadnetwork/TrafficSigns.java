package org.movsim.simulator.roadnetwork;

import java.util.Iterator;

import org.movsim.simulator.roadnetwork.TrafficSign.TrafficSignType;

public interface TrafficSigns {

    /**
     * Adds the {@link TrafficSign} to the {@link RoadSegment} and performs a sorting to assure ascending order
     * of positions along the road stretch.
     * <p>
     * The caller has to assure that trafficlight id is unique in the whole network.
     * </p>
     * 
     * @param trafficSign
     */
    void add(TrafficSign trafficSign);

    boolean hasTrafficSign(TrafficSignType type);

    <T extends TrafficSign> Iterator<T> iterator(TrafficSignType type);

    <T extends TrafficSign> Iterable<T> values(TrafficSignType type);

    <T extends TrafficSign> T getNextTrafficSign(TrafficSignType type, double position, int lane);

    TrafficSignWithDistance getNextTrafficSignWithDistance(TrafficSignType type, double position, int lane);

}
