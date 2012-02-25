package org.movsim.simulator.roadnetwork;

public class Slope {

    private final double position;
    private final double gradient;

    public Slope(double pos, double gradient) {
        this.position = pos;
        this.gradient = gradient;
    }

    public double getPosition() {
        return position;
    }

    public double getGradient() {
        return gradient;
    }
}
