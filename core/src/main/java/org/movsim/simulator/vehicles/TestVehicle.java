package org.movsim.simulator.vehicles;

import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.EquilibriumProperties;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;

public class TestVehicle {

    private final VehicleType vehicleType;

    private final LongitudinalModelBase longitudinalModelBase;
    private final EquilibriumProperties equiProperties;
    private final double length;

    TestVehicle(VehicleType vehicleType, VehiclePrototype prototype) {
        this.vehicleType = vehicleType;
        this.longitudinalModelBase = prototype.createAccelerationModel();
        this.equiProperties = prototype.getEquiProperties();
        this.length = prototype.getLength();
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public LongitudinalModelBase getLongitudinalModel() {
        return longitudinalModelBase;
    }

    public double getRelativeRandomizationV0() {
        return vehicleType.getRelativeV0Randomization();
    }

    public double length() {
        return length;
    }

    public double getEquilibriumSpeed(double density) {
        return equiProperties.getVEq(density);
    }

    public double getRhoQMax() {
        return equiProperties.getRhoQMax();
    }

}
