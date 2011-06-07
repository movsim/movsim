package org.movsim.ui.model;

public class DetectorDataPoint {

    private double time;
    private double flow;
    private double density;
    private double speed;

    public DetectorDataPoint(double time, double flow, double density, double speed) {
        this.time = time;
        this.flow = flow;
        this.density = density;
        this.speed = speed;
    }

    public double getTime() {
        return time;
    }

    public double getFlow() {
        return flow;
    }

    public double getDensity() {
        return density;
    }

    public double getSpeed() {
        return speed;
    }

}
