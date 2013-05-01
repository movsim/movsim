package org.movsim.simulator.roadnetwork;

import com.google.common.base.Preconditions;

public final class RoadSegments {

    private RoadSegments() {

    }

    /**
     * Returns true if the {@code RoadSegment} is connected in downstream direction to the provided argument and false
     * otherwise. Connection exists if at least one {@code LaneSegment} is connected.
     * 
     * @param upstreamRoadSegment
     * @return
     */
    public static boolean isConnected(RoadSegment upstream, RoadSegment downstream) {
        Preconditions.checkNotNull(upstream);
        Preconditions.checkNotNull(downstream);
        for (LaneSegment laneSegment : upstream.laneSegments()) {
            if (laneSegment.sinkLaneSegment() != null && laneSegment.sinkLaneSegment().roadSegment().equals(downstream)) {
                return true;
            }
        }
        return false;
    }

}

