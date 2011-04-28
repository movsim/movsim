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
package org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl;

import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;

// TODO: Auto-generated Javadoc
/**
 * The Class LongitudinalModelImpl.
 */
public abstract class LongitudinalModelImpl {

    private final String modelName;

    private final int modelCategory;

    /**
     * Instantiates a new longitudinal model impl.
     * 
     * @param modelName
     *            the model name
     * @param modelCategory
     *            the model category
     */
    public LongitudinalModelImpl(String modelName, int modelCategory) {
        this.modelName = modelName;
        this.modelCategory = modelCategory;
    }

    /**
     * Model name.
     * 
     * @return the string
     */
    public String modelName() {
        return modelName;
    }

    /**
     * Checks if is cellular automation.
     * 
     * @return true, if is cA
     */
    public boolean isCA() {
        return (modelCategory == AccelerationModelCategory.CELLULAR_AUTOMATON);
    }

    /**
     * Checks if is iterated map.
     * 
     * @return true, if is iterated map
     */
    public boolean isIteratedMap() {
        return (modelCategory == AccelerationModelCategory.INTERATED_MAP_MODEL);
    }

    /**
     * Gets the model category.
     * 
     * @return the model category
     */
    public int getModelCategory() {
        return modelCategory;
    }

    // TODO: fuer alle Modelle ?! analog: T !?
    /**
     * Parameter v0.
     * 
     * @return the double
     */
    public abstract double parameterV0();

    /**
     * Gets the required update time.
     * 
     * @return the required update time
     */
    public abstract double getRequiredUpdateTime();

}
