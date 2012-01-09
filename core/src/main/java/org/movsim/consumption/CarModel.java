/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.consumption;

import org.movsim.input.model.consumption.ConsumptionCarModelInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(CarModel.class);

    private final double rhoAir = FuelConstants.RHO_AIR;
    private final double gravConstant = FuelConstants.GRAVITATION;

    private double mass; // mass of vehicle (kg)
    private double cwValue; // hydrodynamical cwValue-value (dimensionless)
    private double crossSectionSurface; // front area of vehicle (m^2)
    private double consFrictionCoefficient; // constant friction coefficient (dimensionless)
    private double vFrictionCoefficient; // friction coefficient prop to v (s/m)
    private double electricPower; // power for electrical consumption (W)
    private double dynamicRadius; // dynamic tire radius (<static r) (m)

    public CarModel(ConsumptionCarModelInput carInput) {
        initialize(carInput);
    }

    private void initialize(ConsumptionCarModelInput carInput) {
        mass = carInput.getVehicleMass();
        cwValue = carInput.getCwValue();
        crossSectionSurface = carInput.getCrossSectionSurface();
        consFrictionCoefficient = carInput.getConsFrictionCoefficient();
        vFrictionCoefficient = carInput.getvFrictionCoefficient();
        electricPower = carInput.getElectricPower();
        dynamicRadius = carInput.getDynamicTyreRadius();
    }

    public double getFreeWheelingDecel(double v) {
        return -(gravConstant * consFrictionCoefficient + gravConstant * vFrictionCoefficient * v + cwValue * rhoAir
                * crossSectionSurface * v * v / (2 * mass));
    }

    public double getForceMech(double v, double acc) {
        final double c = mass * gravConstant * consFrictionCoefficient;
        final double d = mass * gravConstant * vFrictionCoefficient;
        final double e = 0.5 * rhoAir * cwValue * crossSectionSurface;
        return c + d * v + e * v * v + mass * acc;
    }

    public double getMass() {
        return mass;
    }

    public double getEmptyMass() {
        return mass;
    }

    public double getCwValue() {
        return cwValue;
    }

    public double getCrossSectionSurface() {
        return crossSectionSurface;
    }

    public double getConsFrictionCoefficient() {
        return consFrictionCoefficient;
    }

    public double getvFrictionCoefficient() {
        return vFrictionCoefficient;
    }

    public double getElectricPower() {
        return electricPower;
    }

    public double getDynamicRadius() {
        return dynamicRadius;
    }

    public double getDynamicWheelCircumfence() {
        return 2 * Math.PI * dynamicRadius;
    }

}
