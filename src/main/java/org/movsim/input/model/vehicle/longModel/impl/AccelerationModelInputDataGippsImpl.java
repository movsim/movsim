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

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class AccelerationModelInputDataGippsImpl.
 */
public class AccelerationModelInputDataGippsImpl extends AccelerationModelInputDataImpl implements AccelerationModelInputDataGipps {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelInputDataGippsImpl.class);

    /** The v0. */
    private double v0;
    private final double v0Default;
    
    /** The a. */
    private double a;
    private final double aDefault;
    
    /** The b. */
    private double b;
    private final double bDefault;
    
    /** The s0. */
    private double s0;
    private final double s0Default;
    
    /** The dt.
     *  cannot change update time step interactively, therefore no default nor set method */
    
    private final double dt;

    /**
     * Instantiates a new model input data gipps impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public AccelerationModelInputDataGippsImpl(String modelName, Map<String, String> map) {
        super(modelName);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        aDefault = a = Double.parseDouble(map.get("a"));
        bDefault = b = Double.parseDouble(map.get("b"));
        s0Default = s0 = Double.parseDouble(map.get("s0"));
        dt = Double.parseDouble(map.get("dt"));

        checkParameters();
        
    }
    
    
    @Override
    protected void checkParameters() {
        if (v0 < 0 || a < 0 || b < 0 || s0 < 0 || dt < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName());
            System.exit(-1);
        }
        if (v0 == 0 || a == 0 || b == 0 || dt == 0) {
            logger.error(" zero parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName());
            System.exit(-1);
        }
        
    }


    @Override
    public void resetParametersToDefault() {
        v0 = v0Default;
        a = aDefault;
        b = bDefault;
        s0 = s0Default;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps#getV0()
     */
    @Override
    public double getV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps#getA()
     */
    @Override
    public double getA() {
        return a;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps#getB()
     */
    @Override
    public double getB() {
        return b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps#getS0()
     */
    @Override
    public double getS0() {
        return s0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps#getDt()
     */
    @Override
    public double getDt() {
        return dt;
    }


    public double getV0Default() {
        return v0Default;
    }


    public double getaDefault() {
        return aDefault;
    }


    public double getbDefault() {
        return bDefault;
    }


    public double getS0Default() {
        return s0Default;
    }


    public void setV0(double v0) {
        this.v0 = v0;
        parametersUpdated();
    }


    public void setA(double a) {
        this.a = a;
        parametersUpdated();
    }


    public void setB(double b) {
        this.b = b;
        parametersUpdated();
    }


    public void setS0(double s0) {
        this.s0 = s0;
        parametersUpdated();
    }

    
}
