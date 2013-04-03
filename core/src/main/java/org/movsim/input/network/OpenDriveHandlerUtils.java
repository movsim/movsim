package org.movsim.input.network;

import org.movsim.simulator.roadnetwork.RoadSegment;

public final class OpenDriveHandlerUtils {

    private OpenDriveHandlerUtils() {
        throw new IllegalStateException("do not invoke.");
    }

    // public static int rightLaneIdToLaneIndex(RoadSegment roadSegment, int rightLaneId) {
    // assert rightLaneId < 0;
    // final int laneIndex = roadSegment.laneCount() + rightLaneId;
    // assert laneIndex >= org.movsim.simulator.roadnetwork.Lanes.LANE1;
    // return laneIndex;
    // }

    // public static int leftLaneIdToLaneIndex(RoadSegment roadSegment, int leftLaneId) {
    // assert leftLaneId >= 0;
    // final int laneIndex = leftLaneId;
    // assert laneIndex >= org.movsim.simulator.roadnetwork.Lanes.LANE1;
    // return laneIndex;
    // }

    public static int laneIdToLaneIndex(RoadSegment roadSegment, int laneId) {
        if (laneId >= 0) {
            return laneId;
        }
        // return roadSegment.laneCount() + laneId;
        // return roadSegment.laneCount() + laneId + 1;
        return Math.abs(laneId); // TODO
    }
}
