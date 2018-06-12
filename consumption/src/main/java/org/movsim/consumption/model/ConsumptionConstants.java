/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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
package org.movsim.consumption.model;

final class ConsumptionConstants {

    private ConsumptionConstants() {
        throw new IllegalStateException("do not invoke private constructor");
    }

    /** gravitation acceleration (m/s^2) */
    static final double GRAVITATION = 9.81;

    /** density of air, 1.29 kg/m^3 at 0 degrees Celsius, 1014 hPa */
    static final double RHO_AIR = 1.29;

    static final double CONVERSION_BAR_TO_PASCAL = 1e5;

    static final double HOUR_TO_SECOND = 1 / 3600.;

    static final double KW_TO_W = 1000.;

    static final double LITER_TO_MILLILITER = 1 / 1000.;

    /**
     * @param fuelDensity
     *            in SI units (kg/m^3)
     * @return the fuel density in kg/l
     */
    public static double getFuelDensityPerLiter(double fuelDensity) {
        return fuelDensity / 1000;
    }

    /** transforming factor from g/kWh to kg/(Ws): 0.001 kg/(1000W*3600s) = 1/(3.6e9) */
    static final double GRAMM_PER_KWH_TO_KG_PER_WS = 1 / 3.6e9;

    /** transforming factor from kg/kWh to kg/(Ws): kg/(1000W*3600s) = 1/(3.6e6) */
    static final double KILOGRAMM_PER_KWH_TO_KG_PER_WS = GRAMM_PER_KWH_TO_KG_PER_WS * 1000;

    /** conversion factor for converting from liters to m^3 */
    static final double LITER_TO_CUBICMETER = 1 / 1000.;

    /**
     * @param fuelDensity
     *            in kg/m^3
     * @return the conversion factor from g/kWh to m^3/Ws
     */
    public static double conversionFactorGrammPerEnergyToVolumePerEnergy(double fuelDensity) {
        return GRAMM_PER_KWH_TO_KG_PER_WS / fuelDensity;
    }

}
