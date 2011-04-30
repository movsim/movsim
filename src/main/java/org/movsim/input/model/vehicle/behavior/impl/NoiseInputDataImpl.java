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

import org.movsim.input.model.vehicle.behavior.NoiseInputData;

// TODO: Auto-generated Javadoc
/**
 * The Class NoiseInputDataImpl.
 */
public class NoiseInputDataImpl implements NoiseInputData {

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
    public NoiseInputDataImpl(Map<String, String> map) {
        fluctStrength = Double.parseDouble(map.get("fluct_strength"));
        tau = Double.parseDouble(map.get("tau"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.impl.NoiseInputData#getFluctStrength()
     */
    @Override
    public double getFluctStrength() {
        return fluctStrength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.impl.NoiseInputData#getTau()
     */
    @Override
    public double getTau() {
        return tau;
    }

}
