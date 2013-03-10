package org.movsim.simulator.vehicles;

import org.movsim.autogen.DistributionTypeEnum;

import com.google.common.base.Preconditions;

public class VehicleType {

    private final org.movsim.autogen.VehicleType configuration;

    private final TestVehicle testVehicle;

    public VehicleType(org.movsim.autogen.VehicleType configuration, VehiclePrototype vehiclePrototype) {
        Preconditions.checkNotNull(configuration);
        Preconditions.checkNotNull(vehiclePrototype);
        this.configuration = configuration;
        this.testVehicle = new TestVehicle(this, vehiclePrototype);
    }

    public double getFraction() {
        return configuration.getFraction();
    }

    public double getRelativeV0Randomization() {
        return configuration.getRelativeV0Randomization();
    }

    public DistributionTypeEnum getV0DistributionType() {
        return configuration.getV0DistributionType();
    }

    public String getVehiclePrototypeLabel() {
        return configuration.getLabel();
    }

    public boolean hasRouteLabel() {
        return configuration.isSetRouteLabel();
    }

    public String getRouteLabel() {
        return configuration.getRouteLabel();
    }

    public TestVehicle getTestVehicle() {
        return testVehicle;
    }

}
