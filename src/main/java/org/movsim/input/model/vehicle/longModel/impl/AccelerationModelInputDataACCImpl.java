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

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataACC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class AccelerationModelInputDataACCImpl.
 */
public class AccelerationModelInputDataACCImpl extends AccelerationModelInputDataImpl implements AccelerationModelInputDataACC {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelInputDataACCImpl.class);

    /** The v0. */
    private final double v0;
    
    /** The T. */
    private final double T;
    
    /** The s0. */
    private final double s0;
    
    /** The s1. */
    private final double s1;
    
    /** The delta. */
    private final double delta;
    
    /** The a. */
    private final double a;
    
    /** The b. */
    private final double b;
    
    /** The coolness. */
    private final double coolness;

    /**
     * Instantiates a new model input data acc impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public AccelerationModelInputDataACCImpl(String modelName, Map<String, String> map) {
        super(modelName);
        this.v0 = Double.parseDouble(map.get("v0"));
        this.T = Double.parseDouble(map.get("T"));
        this.s0 = Double.parseDouble(map.get("s0"));
        this.s1 = Double.parseDouble(map.get("s1"));
        this.delta = Double.parseDouble(map.get("delta"));
        this.a = Double.parseDouble(map.get("a"));
        this.b = Double.parseDouble(map.get("b"));
        this.coolness = Double.parseDouble(map.get("coolness"));
        if (coolness < 0 || coolness > 1) {
            logger.error(" coolness parameter = {} not well defined in input. please choose value within [0,1]. exit");
            System.exit(-1);
        }
        if (v0 < 0 || T < 0 || s0 < 0 || s1 < 0 || delta < 0 || a < 0 || b < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    modelName);
            System.exit(-1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACC#getV0()
     */
    @Override
    public double getV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACC#getT()
     */
    @Override
    public double getT() {
        return T;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACC#getS0()
     */
    @Override
    public double getS0() {
        return s0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACC#getS1()
     */
    @Override
    public double getS1() {
        return s1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACC#getDelta
     * ()
     */
    @Override
    public double getDelta() {
        return delta;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACC#getA()
     */
    @Override
    public double getA() {
        return a;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACC#getB()
     */
    @Override
    public double getB() {
        return b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACC#getCoolness
     * ()
     */
    @Override
    public double getCoolness() {
        return coolness;
    }

}
