package org.movsim.simulator.roadnetwork;


public interface TrafficSign extends Comparable<TrafficSign> {

    public enum TrafficSignType {
        TRAFFICLIGHT(1000);
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

}
