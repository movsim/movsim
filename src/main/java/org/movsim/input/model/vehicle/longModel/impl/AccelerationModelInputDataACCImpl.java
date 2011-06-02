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
    private double v0;
    private final double v0Default;
    
    /** The T. */
    private double T;
    private final double TDefault;
    
    /** The s0. */
    private double s0;
    private final double s0Default;
    
    /** The s1. */
    private double s1;
    private final double s1Default;
    
    /** The delta. */
    private double delta;
    private final double deltaDefault;
    
    /** The a. */
    private double a;
    private final double aDefault;
    
    /** The b. */
    private double b;
    private final double bDefault;
    
    /** The coolness. */
    private double coolness;
    private final double coolnessDefault;

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
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        TDefault = T = Double.parseDouble(map.get("T"));
        s0Default = s0 = Double.parseDouble(map.get("s0"));
        s1Default = s1 = Double.parseDouble(map.get("s1"));
        deltaDefault = delta = Double.parseDouble(map.get("delta"));
        aDefault = a = Double.parseDouble(map.get("a"));
        bDefault = b = Double.parseDouble(map.get("b"));
        coolnessDefault = coolness = Double.parseDouble(map.get("coolness"));
        checkParameters();
    }
    
    @Override
    protected void checkParameters() {
        if (coolness < 0 || coolness > 1) {
            logger.error(" coolness parameter = {} not well defined in input. please choose value within [0,1]. exit");
            System.exit(-1);
        }
        if (v0 < 0 || T < 0 || s0 < 0 || s1 < 0 || delta < 0 || a < 0 || b < 0 ) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName());
            System.exit(-1);
        }
        if (T == 0 || a == 0 || b == 0) {
            logger.error(" zero parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName());
            System.exit(-1);
        }
    }

    
    @Override
    public void resetParametersToDefault() {
        v0 = v0Default;
        T = TDefault;
        s0 = s0Default;
        s1 = s1Default;
        delta = deltaDefault;
        a = aDefault;
        b = bDefault;
        coolness = coolnessDefault;
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

    public double getV0Default() {
        return v0Default;
    }

    public double getTDefault() {
        return TDefault;
    }

    public double getS0Default() {
        return s0Default;
    }

    public double getS1Default() {
        return s1Default;
    }

    public double getDeltaDefault() {
        return deltaDefault;
    }

    public double getaDefault() {
        return aDefault;
    }

    public double getbDefault() {
        return bDefault;
    }

    public double getCoolnessDefault() {
        return coolnessDefault;
    }

    public void setV0(double v0) {
        this.v0 = v0;
        parametersUpdated();
    }

    public void setT(double timegap) {
        this.T = timegap;
        parametersUpdated();
    }

    public void setS0(double s0) {
        this.s0 = s0;
        parametersUpdated();
    }

    public void setS1(double s1) {
        this.s1 = s1;
        parametersUpdated();
    }

    public void setDelta(double delta) {
        this.delta = delta;
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

    public void setCoolness(double coolness) {
        this.coolness = coolness;
        parametersUpdated();
    }

   
}
