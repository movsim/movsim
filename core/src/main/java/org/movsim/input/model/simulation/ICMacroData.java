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
package org.movsim.input.model.simulation;

import java.util.Map;

import org.movsim.utilities.Units;

public class ICMacroData {

    /** The x. */
    private final double x;

    /** The rho. Unit: 1/m */
    private final double rho; 

    /** The speed. Unit: m/s */
    private final double speed;

    /**
     * Instantiates a new iC macro data impl.
     * 
     * @param map
     *            the map
     */
    public ICMacroData(Map<String, String> map) {
        this.x = Double.parseDouble(map.get("x"));
        this.rho = Units.INVKM_TO_INVM*Double.parseDouble(map.get("rho_per_km"));
        // negative speed value allowed for using equilibrium speed 
        this.speed = Double.parseDouble(map.get("v"));
    }

    public double getX() {
        return x;
    }

    public double getRho() {
        return rho;
    }

    public double getSpeed() {
        return speed;
    }

}
