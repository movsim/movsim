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
package org.movsim.input.model.vehicle.behavior;

import java.util.Map;

/**
 * The Class NoiseInputData.
 */
public class NoiseInputData {

    /** The fluct strength. */
    private final double fluctStrength;

    /** The tau. */
    private final double tau;

    /**
     * Instantiates a new noise input data impl.
     * 
     * @param map
     *            the map
     */
    public NoiseInputData(Map<String, String> map) {
        fluctStrength = Double.parseDouble(map.get("fluct_strength"));
        tau = Double.parseDouble(map.get("tau"));
    }

    /**
     * Gets the fluct strength.
     * 
     * @return the fluct strength
     */
    public double getFluctStrength() {
        return fluctStrength;
    }

    /**
     * Gets the tau.
     * 
     * @return the tau
     */
    public double getTau() {
        return tau;
    }

}
