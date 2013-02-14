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
 * The Class LongitudinalModelInputDataNSM.
 */
public class LongitudinalModelInputDataNSM extends LongitudinalModelInputData {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModelInputDataNSM.class);

    /**
     * The v0. desired velocity (cell units/time unit)
     */
    private double v0;
    private final double v0Default;

    /**
     * The p slowdown. Troedelwahrscheinlichkeit - slowdown probability
     */
    private double pSlowdown;
    private final double pSlowdownDefault;

    /**
     * The p slow to start. slow-to-start rule (Barlovic)
     */
    private double pSlowToStart;
    private final double pSlowToStartDefault;

    /**
     * Instantiates a new model input data nsm impl.
     * 
     * @param map
     *            the map
     */
    public LongitudinalModelInputDataNSM(Map<String, String> map) {
        super(ModelName.NSM);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        pSlowToStartDefault = pSlowToStart = Double.parseDouble(map.get("p_slow_start"));
        pSlowdownDefault = pSlowdown = Double.parseDouble(map.get("p_slowdown"));
        checkParameters();
    }

    @Override
    protected void checkParameters() {
        if (pSlowToStart < pSlowdown) {
            logger.error("slow to start logic requires pSlowToStart > pSlowdown, but input {} < {} ", pSlowToStart,
                    pSlowdown);
            logger.error("please check parameters. exit", getModelName().name());
            System.exit(-1);
        }

        if (v0 < 0 || pSlowdown < 0 || pSlowToStart < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName().name());
            System.exit(-1);
        }
    }

    @Override
    public void resetParametersToDefault() {
        v0 = v0Default;
        pSlowToStart = pSlowToStartDefault;
        pSlowdown = pSlowdownDefault;
    }

    public double getV0() {
        return v0;
    }

    public double getSlowdown() {
        return pSlowdown;
    }

    public double getSlowToStart() {
        return pSlowToStart;
    }

    public double getV0Default() {
        return v0Default;
    }

    public double getpSlowdown() {
        return pSlowdown;
    }

    public double getpSlowdownDefault() {
        return pSlowdownDefault;
    }

    public double getpSlowToStart() {
        return pSlowToStart;
    }

    public double getpSlowToStartDefault() {
        return pSlowToStartDefault;
    }

    public void setV0(double v0) {
        this.v0 = v0;
        parametersUpdated();
    }

    public void setpSlowdown(double pSlowdown) {
        this.pSlowdown = pSlowdown;
        parametersUpdated();
    }

    public void setpSlowToStart(double pSlowToStart) {
        this.pSlowToStart = pSlowToStart;
        parametersUpdated();
    }

}
