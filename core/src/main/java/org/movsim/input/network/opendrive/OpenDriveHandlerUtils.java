package org.movsim.input.network.opendrive;

import org.movsim.simulator.roadnetwork.RoadSegment;

public final class OpenDriveHandlerUtils {

    private OpenDriveHandlerUtils() {
        throw new IllegalStateException("do not invoke.");
    }

    public static int rightLaneIdToLaneIndex(RoadSegment roadSegment, int rightLaneId) {
        assert rightLaneId < 0;
        final int lane = roadSegment.laneCount() + rightLaneId;
        assert lane >= org.movsim.simulator.roadnetwork.Lane.LANE1;
        return lane;
    }

    public static int leftLaneIdToLaneIndex(RoadSegment roadSegment, int leftLaneId) {
        assert leftLaneId >= 0;
        final int lane = leftLaneId;
        assert lane >= org.movsim.simulator.roadnetwork.Lane.LANE1;
        return lane;
    }

    public static int laneIdToLaneIndex(RoadSegment roadSegment, int laneId) {
        if (laneId >= 0) {
            return laneId;
        }
        return roadSegment.laneCount() + laneId;
    }
}
