package org.movsim.simulator.vehicles;

public class VehicleDimensions {

    private double length;
    private double width;

    public VehicleDimensions(double length, double width) {
        this.length = length;
        this.width = width;
    }

    public VehicleDimensions(VehicleDimensions dimensions) {
        this(dimensions.length, dimensions.width);
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        // can be set in micro-boundary conditions
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

}
