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

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class AccelerationModelInputDataOVM_VDIFFImpl.
 */
public class AccelerationModelInputDataOVM_VDIFFImpl extends AccelerationModelInputDataImpl implements AccelerationModelInputDataOVM_VDIFF {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelInputDataOVM_VDIFFImpl.class);

    /** The s0. */
    private final double s0;
    
    /** The v0. */
    private final double v0;
    
    /** The tau. */
    private final double tau;
    
    /** The len interaction. */
    private final double lenInteraction;
    
    /** The beta. */
    private final double beta;
    
    /** The lambda. */
    private final double lambda;
    
    /** The variant. */
    private final int variant;

    /**
     * Instantiates a new model input data ov m_ vdiff impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public AccelerationModelInputDataOVM_VDIFFImpl(String modelName, Map<String, String> map) {
        super(modelName);
        this.s0 = Double.parseDouble(map.get("s0"));
        this.v0 = Double.parseDouble(map.get("v0"));
        this.tau = Double.parseDouble(map.get("tau"));
        this.lenInteraction = Double.parseDouble(map.get("l_int"));
        this.beta = Double.parseDouble(map.get("beta"));
        this.lambda = Double.parseDouble(map.get("lambda"));
        this.variant = Integer.parseInt(map.get("variant"));

        if (s0 < 0 || v0 < 0 || tau < 0 || lenInteraction < 0 || beta < 0 || lambda < 0 || variant < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    modelName);
            System.exit(-1);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF
     * #getS0()
     */
    @Override
    public double getS0() {
        return s0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF
     * #getV0()
     */
    @Override
    public double getV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF
     * #getTau()
     */
    @Override
    public double getTau() {
        return tau;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF
     * #getLenInteraction()
     */
    @Override
    public double getLenInteraction() {
        return lenInteraction;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF
     * #getBeta()
     */
    @Override
    public double getBeta() {
        return beta;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF
     * #getLambda()
     */
    @Override
    public double getLambda() {
        return lambda;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF
     * #getVariant()
     */
    @Override
    public int getVariant() {
        return variant;
    }

}
