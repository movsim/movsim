package org.movsim.input.model.consumption;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionCarModelInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ConsumptionCarModelInput.class);

    private final double vehicleMass; // in kg
    private final double crossSectionSurface; // in m^2
    private final double cdValue; // unitless
    private final double electricPower; // in Watt
    private final double consFrictionCoefficient; // unitless
    private final double vFrictionCoefficient;
    private final double dynamicTyreRadius; // in m

    public ConsumptionCarModelInput(Map<String, String> map) {
        this.vehicleMass = Double.parseDouble(map.get("mass"));
        this.crossSectionSurface = Double.parseDouble(map.get("cross_section_surface"));
        this.cdValue = Double.parseDouble(map.get("cd_value"));
        this.electricPower = Double.parseDouble(map.get("electric_power"));
        this.consFrictionCoefficient = Double.parseDouble(map.get("const_friction"));
        this.vFrictionCoefficient = Double.parseDouble(map.get("v_friction"));
        this.dynamicTyreRadius = Double.parseDouble(map.get("dynamic_tyre_radius"));
    }

    public double getVehicleMass() {
        return vehicleMass;
    }

    public double getCrossSectionSurface() {
        return crossSectionSurface;
    }

    public double getCwValue() {
        return cdValue;
    }

    public double getElectricPower() {
        return electricPower;
    }

    public double getConsFrictionCoefficient() {
        return consFrictionCoefficient;
    }

    public double getvFrictionCoefficient() {
        return vFrictionCoefficient;
    }

    public double getDynamicTyreRadius() {
        return dynamicTyreRadius;
    }
}
