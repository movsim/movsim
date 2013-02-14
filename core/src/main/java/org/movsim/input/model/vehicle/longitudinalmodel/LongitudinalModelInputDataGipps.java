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

import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LongitudinalModelInputDataGipps.
 */
public class LongitudinalModelInputDataGipps extends LongitudinalModelInputData {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModelInputDataGipps.class);

    private double v0;
    private final double v0Default;

    private double a;
    private final double aDefault;

    private double b;
    private final double bDefault;

    private double s0;
    private final double s0Default;

    /**
     * Instantiates a new model input data GIPPS.
     * 
     * @param map
     *            the map
     */
    public LongitudinalModelInputDataGipps(Map<String, String> map) {
        super(ModelName.GIPPS);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        aDefault = a = Double.parseDouble(map.get("a"));
        bDefault = b = Double.parseDouble(map.get("b"));
        s0Default = s0 = Double.parseDouble(map.get("s0"));
        checkParameters();
    }

    @Override
    protected void checkParameters() {
        if (v0 < 0 || a < 0 || b < 0 || s0 < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName().name());
            System.exit(-1);
        }

        if (a == 0 || b == 0) {
            logger.error(" zero parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName().name());
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

    public double getV0() {
        return v0;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getS0() {
        return s0;
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
