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
package org.movsim.input.model.vehicle.longModel.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKrauss;

/**
 * @author ralph
 * 
 */
public class AccelerationModelInputDataKraussImpl extends AccelerationModelInputDataGippsImpl implements
        AccelerationModelInputDataKrauss {

    /** The epsilon. */
    private double epsilon;
    private final double epsilonDefault;

    /**
     * @param modelName
     * @param map
     */
    public AccelerationModelInputDataKraussImpl(String modelName, Map<String, String> map) {
        super(modelName, map);
        epsilon = epsilonDefault = Double.parseDouble(map.get("epsilon"));
        checkParameters();
    }

    @Override
    protected void checkParameters() {
        super.checkParameters();
        // TODO epsilon
    }

    @Override
    public void resetParametersToDefault() {
        super.resetParametersToDefault();
        epsilon = epsilonDefault;
    }

    @Override
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
        parametersUpdated();
    }

    @Override
    public double getEpsilon() {
        return epsilon;
    }
}
