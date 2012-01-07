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

/**
 * The Interface LongitudinalModelInputDataOVM_FVDM.
 */
public interface LongitudinalModelInputDataOVM_FVDM extends LongitudinalModelInputData {

    /**
     * Gets the s0.
     * 
     * @return the s0
     */
    double getS0();

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    double getV0();

    /**
     * Gets the tau.
     * 
     * @return the tau
     */
    double getTau();

    /**
     * Gets the transition width
     * 
     * @return the transition width
     */
    double getTransitionWidth();

    /**
     * Gets the beta.
     * 
     * @return the beta
     */
    double getBeta();

    /**
     * Gets the lambda.
     * 
     * @return the lambda
     */
    double getLambda();

    /**
     * Gets the variant.
     * 
     * @return the variant
     */
    int getVariant();

    /**
     * Gets the v0 default.
     * 
     * @return the v0 default
     */
    double getV0Default();

    /**
     * Gets the tau default.
     * 
     * @return the tau default
     */
    double getTauDefault();

    /**
     * Gets the transition width default.
     * 
     * @return the transition width default
     */
    double getTransitionWidthDefault();

    /**
     * Gets the beta default.
     * 
     * @return the beta default
     */
    double getBetaDefault();

    /**
     * Gets the lambda default.
     * 
     * @return the lambda default
     */
    double getLambdaDefault();

    /**
     * Gets the s0 default.
     * 
     * @return the s0 default
     */
    double getS0Default();

    /**
     * Gets the variant default.
     * 
     * @return the variant default
     */
    int getVariantDefault();

    /**
     * Sets the v0.
     * 
     * @param v0
     *            the new v0
     */
    void setV0(double v0);

    /**
     * Sets the tau.
     * 
     * @param tau
     *            the new tau
     */
    void setTau(double tau);

    /**
     * Sets the len interaction.
     * 
     * @param lenInteraction
     *            the new len interaction
     */
    void setLenInteraction(double lenInteraction);

    /**
     * Sets the beta.
     * 
     * @param beta
     *            the new beta
     */
    void setBeta(double beta);

    /**
     * Sets the lambda.
     * 
     * @param lambda
     *            the new lambda
     */
    void setLambda(double lambda);

    /**
     * Sets the s0.
     * 
     * @param s0
     *            the new s0
     */
    void setS0(double s0);

    /**
     * Sets the variant.
     * 
     * @param variant
     *            the new variant
     */
    void setVariant(int variant);

}