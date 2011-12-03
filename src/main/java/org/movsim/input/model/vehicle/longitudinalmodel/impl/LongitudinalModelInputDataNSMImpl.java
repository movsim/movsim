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
package org.movsim.input.model.vehicle.longitudinalmodel.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNSM;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class LongitudinalModelInputDataNSMImpl.
 */
public class LongitudinalModelInputDataNSMImpl extends LongitudinalModelInputDataImpl implements
        LongitudinalModelInputDataNSM {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModelInputDataNSMImpl.class);

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
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public LongitudinalModelInputDataNSMImpl(Map<String, String> map) {
        super(ModelName.NSM);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        pSlowToStartDefault = pSlowToStart = Double.parseDouble(map.get("p_slow_start"));
        pSlowdownDefault = pSlowdown = Double.parseDouble(map.get("p_slowdown"));
        checkParameters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.AccelerationModelInputDataImpl #checkParameters()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.AccelerationModelInputDataImpl #resetParametersToDefault()
     */
    @Override
    public void resetParametersToDefault() {
        v0 = v0Default;
        pSlowToStart = pSlowToStartDefault;
        pSlowdown = pSlowdownDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataNSM#getV0()
     */
    @Override
    public double getV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataNSM#getP()
     */
    @Override
    public double getSlowdown() {
        return pSlowdown;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataNSM#getP0()
     */
    @Override
    public double getSlowToStart() {
        return pSlowToStart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM #getV0Default()
     */
    @Override
    public double getV0Default() {
        return v0Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM #getpSlowdown()
     */
    @Override
    public double getpSlowdown() {
        return pSlowdown;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM #getpSlowdownDefault()
     */
    @Override
    public double getpSlowdownDefault() {
        return pSlowdownDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM #getpSlowToStart()
     */
    @Override
    public double getpSlowToStart() {
        return pSlowToStart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM #getpSlowToStartDefault()
     */
    @Override
    public double getpSlowToStartDefault() {
        return pSlowToStartDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM #setV0(double)
     */
    @Override
    public void setV0(double v0) {
        this.v0 = v0;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM #setpSlowdown(double)
     */
    @Override
    public void setpSlowdown(double pSlowdown) {
        this.pSlowdown = pSlowdown;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM #setpSlowToStart(double)
     */
    @Override
    public void setpSlowToStart(double pSlowToStart) {
        this.pSlowToStart = pSlowToStart;
        parametersUpdated();
    }

}
