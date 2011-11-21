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
package org.movsim.input.model.simulation;

import java.util.Map;

public class TrafficCompositionInputData {

    /** The key name. */
    private final String keyName;

    /** The fraction. */
    private final double fraction;

    
    private final double relativeRandomizationDesiredSpeed;
    
    /**
     * Instantiates a new heterogeneity input data impl.
     * 
     * @param map
     *            the map
     */
    public TrafficCompositionInputData(Map<String, String> map) {
        this.keyName = map.get("label");
        this.fraction = Double.parseDouble(map.get("fraction"));
        System.out.println("rand="+map.get("relative_v0_randomization")+ "     key:"+keyName);
        this.relativeRandomizationDesiredSpeed = Double.parseDouble(map.get("relative_v0_randomization"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.simulation.impl.HeterogeneityInputData#getKeyName
     * ()
     */
    public String getKeyName() {
        return keyName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.simulation.impl.HeterogeneityInputData#getFraction
     * ()
     */
    public double getFraction() {
        return fraction;
    }
    
    
    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.HeterogeneityInputData#getRelativeRandomizationDesiredSpeed()
     */
    public double getRelativeRandomizationDesiredSpeed() {
        return relativeRandomizationDesiredSpeed;
    }
}
