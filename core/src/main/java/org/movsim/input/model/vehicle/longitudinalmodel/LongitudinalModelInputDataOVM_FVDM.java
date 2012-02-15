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

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataOVM_FVDM;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LongitudinalModelInputDataOVM_VDIFF.
 */
public class LongitudinalModelInputDataOVM_FVDM extends LongitudinalModelInputData {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModelInputDataOVM_FVDM.class);

    private double v0;
    private final double v0Default;

    private double tau;
    private final double tauDefault;

    private double transitionWidth;
    private final double transitionWidthDefault;

    private double beta;
    private final double betaDefault;

    private double lambda;
    private final double lambdaDefault;

    private double s0;
    private final double s0Default;

    private int variant;
    private final int variantDefault;

    /**
     * Instantiates a new model input data OVM or FVDM.
     * 
     * @param map
     *            the map
     */
    public LongitudinalModelInputDataOVM_FVDM(Map<String, String> map) {
        super(ModelName.OVM_FVDM);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        tauDefault = tau = Double.parseDouble(map.get("tau"));
        transitionWidthDefault = transitionWidth = Double.parseDouble(map.get("l_int"));
        betaDefault = beta = Double.parseDouble(map.get("beta"));
        lambdaDefault = lambda = Double.parseDouble(map.get("lambda"));
        s0Default = s0 = Double.parseDouble(map.get("s0"));
        variantDefault = variant = Integer.parseInt(map.get("variant"));

        checkParameters();

    }

    @Override
    protected void checkParameters() {
        if (s0 < 0 || v0 < 0 || tau < 0 || transitionWidth < 0 || beta < 0 || lambda < 0 || variant < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName().name());
            System.exit(-1);
        }

        // TODO further param check
    }

    @Override
    public void resetParametersToDefault() {
        v0 = v0Default;
        tau = tauDefault;
        transitionWidth = transitionWidthDefault;
        beta = betaDefault;
        lambda = lambdaDefault;
        s0 = s0Default;
        variant = variantDefault;
    }

    public double getS0() {
        return s0;
    }

    public double getV0() {
        return v0;
    }

    public double getTau() {
        return tau;
    }

    public double getTransitionWidth() {
        return transitionWidth;
    }

    public double getBeta() {
        return beta;
    }

    public double getLambda() {
        return lambda;
    }

    public int getVariant() {
        return variant;
    }

    public double getV0Default() {
        return v0Default;
    }

    public double getTauDefault() {
        return tauDefault;
    }

    public double getTransitionWidthDefault() {
        return transitionWidthDefault;
    }

    public double getBetaDefault() {
        return betaDefault;
    }

    public double getLambdaDefault() {
        return lambdaDefault;
    }

    public double getS0Default() {
        return s0Default;
    }

    public int getVariantDefault() {
        return variantDefault;
    }

    public void setV0(double v0) {
        this.v0 = v0;
        parametersUpdated();
    }

    public void setTau(double tau) {
        this.tau = tau;
        parametersUpdated();
    }

    public void setLenInteraction(double lenInteraction) {
        this.transitionWidth = lenInteraction;
        parametersUpdated();
    }

    public void setBeta(double beta) {
        this.beta = beta;
        parametersUpdated();
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
        parametersUpdated();
    }

    public void setS0(double s0) {
        this.s0 = s0;
        parametersUpdated();
    }

    public void setVariant(int variant) {
        this.variant = variant;
        parametersUpdated();
    }

}
