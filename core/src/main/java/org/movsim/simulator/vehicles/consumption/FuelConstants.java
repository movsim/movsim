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
package org.movsim.simulator.vehicles.consumption;

public class FuelConstants {

    private FuelConstants() {
    } // prevents instantiation

    public static double GRAVITATION = 9.81;// grav. acceleration (m/s^2)

    public static double RHO_AIR = 1.29; // 1.29 (kg/m^3) @ 0 cels, 1014 hPa

    public static double RHO_FUEL = 760; // density of "Benzin" (kg/m^3)
    public static double RHO_FUEL_PER_LITER = RHO_FUEL / 1000.; // density of "Benzin" (kg/l)

    public static double CALORIC_DENS = 44e6;// "Benzin": 44 MJ/kg (--> 0.76*44 JM/liter)

    // Tranform g/kWh => m^3/(Ws): 0.001 kg/(1000W*3600s) = 1/(3.6e9)
    public static double CONVERSION_GRAMM_PER_KWH_TO_SI = 1. / (RHO_FUEL * 3.6e9);

    public static double CONVERSION_BAR_TO_PASCAL = 1e5;

}
