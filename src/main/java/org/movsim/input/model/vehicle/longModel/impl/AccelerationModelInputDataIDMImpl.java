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

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelAbstract.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class AccelerationModelInputDataIDMImpl.
 */
public class AccelerationModelInputDataIDMImpl extends AccelerationModelInputDataImpl implements
        AccelerationModelInputDataIDM {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelInputDataIDMImpl.class);

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

    /**
     * Instantiates a new model input data idm impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public AccelerationModelInputDataIDMImpl(Map<String, String> map) {
        super(ModelName.IDM);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        TDefault = T = Double.parseDouble(map.get("T"));
        s0Default = s0 = Double.parseDouble(map.get("s0"));
        s1Default = s1 = Double.parseDouble(map.get("s1"));
        deltaDefault = delta = Double.parseDouble(map.get("delta"));
        aDefault = a = Double.parseDouble(map.get("a"));
        bDefault = b = Double.parseDouble(map.get("b"));
        checkParameters();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.AccelerationModelInputDataImpl
     * #checkParameters()
     */
    @Override
    protected void checkParameters() {
        if (v0 < 0 || T < 0 || s0 < 0 || s1 < 0 || delta < 0 || a < 0 || b < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName().name());
            System.exit(-1);
        }

        if (T == 0 || a == 0 || b == 0) {
            logger.error(" zero parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName().name());
            System.exit(-1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.AccelerationModelInputDataImpl
     * #resetParametersToDefault()
     */
    @Override
    public void resetParametersToDefault() {
        v0 = v0Default;
        T = TDefault;
        s0 = s0Default;
        s1 = s1Default;
        delta = deltaDefault;
        a = aDefault;
        b = bDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataIDM#getV0()
     */
    @Override
    public double getV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataIDM#getT()
     */
    @Override
    public double getT() {
        return T;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataIDM#getS0()
     */
    @Override
    public double getS0() {
        return s0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataIDM#getS1()
     */
    @Override
    public double getS1() {
        return s1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataIDM#getDelta
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
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataIDM#getA()
     */
    @Override
    public double getA() {
        return a;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataIDM#getB()
     */
    @Override
    public double getB() {
        return b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #getV0Default()
     */
    @Override
    public double getV0Default() {
        return v0Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #getTDefault()
     */
    @Override
    public double getTDefault() {
        return TDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #getS0Default()
     */
    @Override
    public double getS0Default() {
        return s0Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #getS1Default()
     */
    @Override
    public double getS1Default() {
        return s1Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #getDeltaDefault()
     */
    @Override
    public double getDeltaDefault() {
        return deltaDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #getaDefault()
     */
    @Override
    public double getaDefault() {
        return aDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #getbDefault()
     */
    @Override
    public double getbDefault() {
        return bDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #setV0(double)
     */
    @Override
    public void setV0(double v0) {
        this.v0 = v0;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #setT(double)
     */
    @Override
    public void setT(double timegap) {
        this.T = timegap;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #setS0(double)
     */
    @Override
    public void setS0(double s0) {
        this.s0 = s0;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #setS1(double)
     */
    @Override
    public void setS1(double s1) {
        this.s1 = s1;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #setDelta(double)
     */
    @Override
    public void setDelta(double delta) {
        this.delta = delta;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #setA(double)
     */
    @Override
    public void setA(double a) {
        this.a = a;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM
     * #setB(double)
     */
    @Override
    public void setB(double b) {
        this.b = b;
        parametersUpdated();
    }

}