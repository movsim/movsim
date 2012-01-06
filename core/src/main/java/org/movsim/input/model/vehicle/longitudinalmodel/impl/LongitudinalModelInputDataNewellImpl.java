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

import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LongitudinalModelInputDataNewellImpl.
 */
public class LongitudinalModelInputDataNewellImpl extends LongitudinalModelInputDataImpl {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModelInputDataNewellImpl.class);

    private double v0;
    private final double v0Default;

    private double s0;
    private final double s0Default;

    /**
     * Instantiates a new model input data newell impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public LongitudinalModelInputDataNewellImpl(Map<String, String> map) {
        super(ModelName.NEWELL);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        s0Default = s0 = Double.parseDouble(map.get("s0"));
        checkParameters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.AccelerationModelInputDataImpl #checkParameters()
     */
    @Override
    protected void checkParameters() {
        // TODO Auto-generated method stub
        if (v0 < 0 || s0 < 0) {
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
        s0 = s0Default;
    }

    public double getV0() {
        return v0;
    }

    public void setV0(double v0) {
        this.v0 = v0;
    }

    public double getS0() {
        return s0;
    }

    public void setS0(double s0) {
        this.s0 = s0;
    }

    public double getV0Default() {
        return v0Default;
    }

    public double getS0Default() {
        return s0Default;
    }
}
