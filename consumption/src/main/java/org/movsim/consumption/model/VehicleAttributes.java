package org.movsim.consumption.model;

import org.movsim.consumption.autogen.VehicleData;

public class VehicleAttributes {

    /** mass of vehicle (kg) */
    private final double mass;

    /** hydrodynamical cd-value (dimensionless) */
    private final double cdValue;

    /** front area of vehicle (m^2) */
    private final double crossSectionSurface;

    /** constant friction coefficient (dimensionless) */
    private final double constantFrictionCoefficient;

    /** friction coefficient prop to v (s/m) */
    private final double speedFrictionCoefficient;

    /** power for electrical consumption (W) */
    private final double electricPower;

    public VehicleAttributes(VehicleData vehicle) {
        mass = vehicle.getMass();
        cdValue = vehicle.getCdValue();
        crossSectionSurface = vehicle.getCrossSectionSurface();
        constantFrictionCoefficient = vehicle.getConstFriction();
        speedFrictionCoefficient = vehicle.getVFriction();
        electricPower = vehicle.getElectricPower();
    }

    public double electricPower() {
        return electricPower;
    }

    public double mass() {
        return mass;
    }

    public double cwValue() {
        return cdValue;
    }

    public double crossSectionSurface() {
        return crossSectionSurface;
    }

    public double constantFrictionCoefficient() {
        return constantFrictionCoefficient;
    }

    public double speedFrictionCoefficient() {
        return speedFrictionCoefficient;
    }

}
