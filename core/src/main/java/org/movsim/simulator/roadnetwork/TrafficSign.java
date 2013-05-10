package org.movsim.simulator.roadnetwork;


public interface TrafficSign {

    public enum TrafficSignType {
        TRAFFICLIGHT(1000), SPEEDLIMIT(0);

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

    boolean valid(int lane);

}
