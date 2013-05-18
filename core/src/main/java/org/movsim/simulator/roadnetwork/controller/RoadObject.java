package org.movsim.simulator.roadnetwork.controller;

import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadSegment;

public interface RoadObject extends SimulationTimeStep {

    public enum RoadObjectType {
        TRAFFICLIGHT, SPEEDLIMIT, LOOPDETECTOR, VMS_DIVERSION, FLOW_CONSERVING_BOTTLENECK, GRADIENT_PROFILE;
    }

    RoadObjectType getType();

    double position();

    RoadSegment roadSegment();

    boolean isValidLane(int lane);

    void createSignalPositions();

    /**
     * Self-defined OpenDRIVE.Road.Objects.Object.type attribute values.
     * 
     */
    public enum XodrRoadObjectType {
        SPEEDLIMIT("speedlimit");

        private final String openDriveIdentifier;

        XodrRoadObjectType(String keyword) {
            this.openDriveIdentifier = keyword;
        }

        public String xodrIdentifier() {
            return openDriveIdentifier;
        }
    }
}
