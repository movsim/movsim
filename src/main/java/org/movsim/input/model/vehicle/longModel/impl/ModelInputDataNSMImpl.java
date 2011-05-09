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

import org.movsim.input.model.vehicle.longModel.ModelInputDataNSM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ModelInputDataNSMImpl.
 */
public class ModelInputDataNSMImpl extends ModelInputDataImpl implements ModelInputDataNSM {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ModelInputDataNSMImpl.class);

    /** The v0. */
    private final double v0; // desired velocity (cell units/time unit)
    
    /** The p slowdown. */
    private final double pSlowdown; // Troedelwahrscheinlichkeit - slowdown probability
    
    /** The p slow to start. */
    private final double pSlowToStart; // slow-to-start rule (Barlovic)

    // dt = 1 constant

    /**
     * Instantiates a new model input data nsm impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public ModelInputDataNSMImpl(String modelName, Map<String, String> map) {
        super(modelName);
        this.v0 = Double.parseDouble(map.get("v0"));
        this.pSlowdown = Double.parseDouble(map.get("p_slowdown"));
        this.pSlowToStart = Double.parseDouble(map.get("p_slow_start"));
        if (pSlowToStart < pSlowdown) {
            logger.error("slow to start logic requires pSlowToStart > pSlowdown, but input {} < {} ", pSlowToStart,
                    pSlowdown);
            System.exit(-1);
        }

        if (v0 < 0 || pSlowdown < 0 || pSlowToStart < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    modelName);
            System.exit(-1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataNSM#getV0()
     */
    @Override
    public double getV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataNSM#getP()
     */
    @Override
    public double getSlowdown() {
        return pSlowdown;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataNSM#getP0()
     */
    @Override
    public double getSlowToStart() {
        return pSlowToStart;
    }

}
