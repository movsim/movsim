package org.movsim.ui.desktop;

public class FloatingCarDataPoint {

    private double position;
    private double speed;
    private double acc;
    private double time;

    public FloatingCarDataPoint(double time, double position, double speed, double acc) {
        this.time = time;
        this.position = position;
        this.speed = speed;
        this.acc = acc;

    }

    public double getPosition() {
        return position;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAcc() {
        return acc;
    }

    public double getTime() {
        return time;
    }

}
