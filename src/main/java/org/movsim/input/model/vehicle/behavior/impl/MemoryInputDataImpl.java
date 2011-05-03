/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.input.model.vehicle.behavior.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.behavior.MemoryInputData;

// TODO: Auto-generated Javadoc
/**
 * The Class MemoryInputDataImpl.
 */
public class MemoryInputDataImpl implements MemoryInputData {

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
     * Instantiates a new memory input data impl.
     * 
     * @param map
     *            the map
     */
    public MemoryInputDataImpl(Map<String, String> map) {
        tau = Double.parseDouble(map.get("tau"));
        resignationMaxAlphaT = Double.parseDouble(map.get("alpha_T"));
        resignationMinAlphaV0 = Double.parseDouble(map.get("alpha_v0"));
        resignationMinAlphaA = Double.parseDouble(map.get("alpha_a"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.impl.MemoryInputData#getTau()
     */
    @Override
    public double getTau() {
        return tau;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.impl.MemoryInputData#getResignationMaxAlphaT
     * ()
     */
    @Override
    public double getResignationMaxAlphaT() {
        return resignationMaxAlphaT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.impl.MemoryInputData#getResignationMinAlphaV0
     * ()
     */
    @Override
    public double getResignationMinAlphaV0() {
        return resignationMinAlphaV0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.impl.MemoryInputData#getResignationMinAlphaA
     * ()
     */
    @Override
    public double getResignationMinAlphaA() {
        return resignationMinAlphaA;
    }

}
