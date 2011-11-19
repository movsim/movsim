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
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelAbstract.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class AccelerationModelInputDataOVM_VDIFFImpl.
 */
public class AccelerationModelInputDataOVM_VDIFFImpl extends AccelerationModelInputDataImpl implements
        AccelerationModelInputDataOVM_VDIFF {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelInputDataOVM_VDIFFImpl.class);

    /** The v0. */
    private double v0;
    private final double v0Default;

    /** The tau. */
    private double tau;
    private final double tauDefault;

    /** The len interaction. */
    private double lenInteraction;
    private final double lenInteractionDefault;

    /** The beta. */
    private double beta;
    private final double betaDefault;

    /** The lambda. */
    private double lambda;
    private final double lambdaDefault;

    /** The s0. */
    private double s0;
    private final double s0Default;

    /** The variant. */
    private int variant;
    private final int variantDefault;

    /**
     * Instantiates a new model input data ov m_ vdiff impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public AccelerationModelInputDataOVM_VDIFFImpl(Map<String, String> map) {
        super(ModelName.OVM_VDIFF);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        tauDefault = tau = Double.parseDouble(map.get("tau"));
        lenInteractionDefault = lenInteraction = Double.parseDouble(map.get("l_int"));
        betaDefault = beta = Double.parseDouble(map.get("beta"));
        lambdaDefault = lambda = Double.parseDouble(map.get("lambda"));
        s0Default = s0 = Double.parseDouble(map.get("s0"));
        variantDefault = variant = Integer.parseInt(map.get("variant"));

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
        if (s0 < 0 || v0 < 0 || tau < 0 || lenInteraction < 0 || beta < 0 || lambda < 0 || variant < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName().name());
            System.exit(-1);
        }

        // TODO further param check
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
        tau = tauDefault;
        lenInteraction = lenInteractionDefault;
        beta = betaDefault;
        lambda = lambdaDefault;
        s0 = s0Default;
        variant = variantDefault;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
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
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #getTauDefault()
     */
    @Override
    public double getTauDefault() {
        return tauDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #getLenInteractionDefault()
     */
    @Override
    public double getLenInteractionDefault() {
        return lenInteractionDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #getBetaDefault()
     */
    @Override
    public double getBetaDefault() {
        return betaDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #getLambdaDefault()
     */
    @Override
    public double getLambdaDefault() {
        return lambdaDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
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
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #getVariantDefault()
     */
    @Override
    public int getVariantDefault() {
        return variantDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
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
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #setTau(double)
     */
    @Override
    public void setTau(double tau) {
        this.tau = tau;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #setLenInteraction(double)
     */
    @Override
    public void setLenInteraction(double lenInteraction) {
        this.lenInteraction = lenInteraction;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #setBeta(double)
     */
    @Override
    public void setBeta(double beta) {
        this.beta = beta;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #setLambda(double)
     */
    @Override
    public void setLambda(double lambda) {
        this.lambda = lambda;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
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
     * org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF
     * #setVariant(int)
     */
    @Override
    public void setVariant(int variant) {
        this.variant = variant;
        parametersUpdated();
    }

}
