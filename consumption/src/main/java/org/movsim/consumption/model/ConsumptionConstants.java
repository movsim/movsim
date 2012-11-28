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
package org.movsim.consumption.model;

public interface ConsumptionConstants {

    /** grav. acceleration (m/s^2) */
    double GRAVITATION = 9.81;

    /** 1.29 (kg/m^3) at 0 degress celsius, 1014 hPa */
    double RHO_AIR = 1.29;

    /** density of gasoline (kg/m^3) */
    double RHO_FUEL = 760;

    /** density of gasoline (kg/l) */
    double RHO_FUEL_PER_LITER = RHO_FUEL / 1000.;

    /** caloric density of gasoline: 44 MJ/kg (--> 0.76*44 JM/liter) */
    double CALORIC_DENSITY = 44e6;

    /** tranforming factor g/kWh => m^3/(Ws): 0.001 kg/(1000W*3600s) = 1/(3.6e9) */
    double CONVERSION_GRAMM_PER_KWH_TO_SI = 1. / (RHO_FUEL * 3.6e9);

    double CONVERSION_BAR_TO_PASCAL = 1e5;

}
