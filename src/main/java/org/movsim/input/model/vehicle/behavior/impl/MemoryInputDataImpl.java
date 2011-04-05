/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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


public class MemoryInputDataImpl implements MemoryInputData {

    // <MEMORY tau="600" alpha_a="1" alpha_v0="1" alpha_T="1.7" />

    private final double tau; // in seconds

    private final double resignationMaxAlphaT; // unitless

    private final double resignationMinAlphaV0; // unitless

    private final double resignationMinAlphaA; // unitless

    public MemoryInputDataImpl(Map<String, String> map) {
        tau = Double.parseDouble(map.get("tau"));
        resignationMaxAlphaT = Double.parseDouble(map.get("alpha_T"));
        resignationMinAlphaV0 = Double.parseDouble(map.get("alpha_v0"));
        resignationMinAlphaA = Double.parseDouble(map.get("alpha_a"));
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.impl.MemoryInputData#getTau()
     */
    public double getTau() {
        return tau;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.impl.MemoryInputData#getResignationMaxAlphaT()
     */
    public double getResignationMaxAlphaT() {
        return resignationMaxAlphaT;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.impl.MemoryInputData#getResignationMinAlphaV0()
     */
    public double getResignationMinAlphaV0() {
        return resignationMinAlphaV0;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.impl.MemoryInputData#getResignationMinAlphaA()
     */
    public double getResignationMinAlphaA() {
        return resignationMinAlphaA;
    }

}
