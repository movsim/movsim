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
package org.movsim.utilities.impl;

import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelAbstract.ModelName;

/**
 * The Class ScalingHelper.
 */
public class ScalingHelper {

    /**
     * Instantiates a new scaling helper.
     */
    private ScalingHelper() {

    }

    /**
     * Gets the scaling length. Which differs in the cellular automata.
     * 
     * @param modelName
     *            the model name
     * @return the scaling length
     */
    public static double getScalingLength(final ModelName modelName) {
        double scaleCA = 1;
        if (modelName == ModelName.NSM) {
            scaleCA = 7.5;
        } else if (modelName == ModelName.KKW) {
            scaleCA = 0.5;
        }
        return scaleCA;
    }
}
