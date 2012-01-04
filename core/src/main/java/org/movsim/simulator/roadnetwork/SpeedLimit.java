package org.movsim.simulator.roadnetwork;

public class SpeedLimit {

    private final double position;

    private final double speed;

    public SpeedLimit(double pos, double speed) {
        this.position = pos;
        this.speed = speed;
    }

    public double getPosition() {
        return position;
    }

    public double getSpeedLimitKmh() {
        return speed * 3.6;
    }
}
