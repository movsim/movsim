package org.movsim.input.model.consumption;

import java.util.Map;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarConsumptionModelInput {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(CarConsumptionModelInput.class);
    
    private double vehicleMass; // in kg
    private double crossSectionSurface;  // in m^2
    private double cwValue; // unitless
    private double electricPower; // in Watt
    private double consFrictionCoefficient; // unitless
    private double vFrictionCoefficient;
    private double dynamicTyreRadius;  // in m
    
    
    public CarConsumptionModelInput(Map<String, String> map){
        this.vehicleMass = Double.parseDouble(map.get("mass"));
        this.crossSectionSurface = Double.parseDouble(map.get("cross_section_surface"));
        this.cwValue = Double.parseDouble(map.get("cw_value"));
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
        return cwValue;
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
