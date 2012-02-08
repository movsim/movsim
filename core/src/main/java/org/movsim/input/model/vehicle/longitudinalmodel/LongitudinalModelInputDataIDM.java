/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.input.model.vehicle.longitudinalmodel;

import java.util.Map;

import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LongitudinalModelInputDataIDM.
 */
public class LongitudinalModelInputDataIDM extends LongitudinalModelInputData {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModelInputDataIDM.class);

    private double v0;

    private final double v0Default;

    private double T;
    private final double TDefault;

    private double s0;
    private final double s0Default;

    private double s1;
    private final double s1Default;

    private double delta;
    private final double deltaDefault;

    private double a;
    private final double aDefault;

    private double b;
    private final double bDefault;

    /**
     * Instantiates a new model input data IDM.
     * 
     * @param map
     *            the map
     */
    public LongitudinalModelInputDataIDM(Map<String, String> map) {
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

    public void resetParametersToDefault() {
        v0 = v0Default;
        T = TDefault;
        s0 = s0Default;
        s1 = s1Default;
        delta = deltaDefault;
        a = aDefault;
        b = bDefault;
    }

    public double getV0() {
        return v0;
    }

    public double getT() {
        return T;
    }

    public double getS0() {
        return s0;
    }

    public double getS1() {
        return s1;
    }

    public double getDelta() {
        return delta;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
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

}