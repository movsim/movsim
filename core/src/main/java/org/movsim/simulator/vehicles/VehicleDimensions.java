package org.movsim.simulator.vehicles;

public class VehicleDimensions {

    private double length; // not final, can be set in micro-boundary conditions
    private final double width;

    public VehicleDimensions(double length, double width) {
        this.length = length;
        this.width = width;
    }

    public VehicleDimensions(VehicleDimensions dimensions) {
        this(dimensions.length, dimensions.width);
    }

    public double length() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double width() {
        return width;
    }

}
