package org.movsim.simulator.roadnetwork;

import org.movsim.simulator.vehicles.Vehicle;



public interface TrafficSign extends Comparable<TrafficSign> {

    public enum TrafficSignType {
        TRAFFICLIGHT(1000), SPEEDLIMIT(0);
        /* TODO SPEEDLIMIT(0), VMS, DETECTOR, ....; */

        private final double lookAheadDistance;

        private TrafficSignType(double lookAheadDistance) {
            this.lookAheadDistance = lookAheadDistance;
        }

        double getLookAheadDistance() {
            return lookAheadDistance;
        }

    }

    TrafficSignType getType();

    double position();

    RoadSegment roadSegment();

    boolean isValidLane(int lane);

    void apply(Vehicle vehicle);

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
