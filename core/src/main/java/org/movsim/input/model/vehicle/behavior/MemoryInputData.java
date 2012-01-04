/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.input.model.vehicle.behavior;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class MemoryInputData.
 */
public class MemoryInputData {

    // <MEMORY tau="600" alpha_a="1" alpha_v0="1" alpha_T="1.7" />

    /** The tau. */
    private final double tau; // in seconds

    /** The resignation max alpha t. */
    private final double resignationMaxAlphaT; // unitless

    /** The resignation min alpha v0. */
    private final double resignationMinAlphaV0; // unitless

    /** The resignation min alpha a. */
    private final double resignationMinAlphaA; // unitless

    /**
     * Instantiates a new memory input data.
     * 
     * @param map
     *            the map
     */
    public MemoryInputData(Map<String, String> map) {
        tau = Double.parseDouble(map.get("tau"));
        resignationMaxAlphaT = Double.parseDouble(map.get("alpha_T"));
        resignationMinAlphaV0 = Double.parseDouble(map.get("alpha_v0"));
        resignationMinAlphaA = Double.parseDouble(map.get("alpha_a"));
    }

    /**
     * Gets the tau.
     * 
     * @return the tau
     */
    public double getTau() {
        return tau;
    }

    /**
     * Gets the resignation max alpha t.
     * 
     * @return the resignation max alpha t
     */
    public double getResignationMaxAlphaT() {
        return resignationMaxAlphaT;
    }

    /**
     * Gets the resignation min alpha v0.
     * 
     * @return the resignation min alpha v0
     */
    public double getResignationMinAlphaV0() {
        return resignationMinAlphaV0;
    }

    /**
     * Gets the resignation min alpha a.
     * 
     * @return the resignation min alpha a
     */
    public double getResignationMinAlphaA() {
        return resignationMinAlphaA;
    }

}
