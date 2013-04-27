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
package org.movsim.simulator.vehicles.longitudinalmodel;

import org.movsim.autogen.MemoryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// Resignation or Memory effect, see paper:
// M. Treiber, D. Helbing:
// Memory effects in microscopic traffic models and wide scattering in flow-localDensity data
// Physical Review E 68, 046119 (2003)

/**
 * The Class MemoryImpl.
 */
public class Memory {

    /** The Constant LOG. */
    final static Logger logger = LoggerFactory.getLogger(Memory.class);

    /**
     * The tau. relaxation time
     */
    private final double tau;

    /** The resignation min alpha a. Unitless. */
    private final double resignationMinAlphaA;

    /** The resignation min alpha v0. Unitless. */
    private final double resignationMinAlphaV0;

    /** The resignation max alpha t. Unitless. */
    private final double resignationMaxAlphaT;

    /**
     * The alpha a. dynamic state variable
     */
    private double alphaA;

    /**
     * The alpha v0. dynamic state variable
     */
    private double alphaV0;

    /**
     * The alpha t. dynamic state variable
     */
    private double alphaT;

    /**
     * Instantiates a new memory impl.
     * 
     * @param parameters
     *            the parameters
     */
    public Memory(MemoryParameter parameters) {
        // parameters
        tau = parameters.getTau();
        resignationMaxAlphaT = parameters.getAlphaT();
        resignationMinAlphaV0 = parameters.getAlphaV0();
        resignationMinAlphaA = parameters.getAlphaA();

        // initialize dynamic state variables
        alphaA = 1;
        alphaV0 = 1;
        alphaT = 1;

    }

    /**
     * Update.
     * 
     * @param dt
     *            the dt
     * @param v
     *            the v
     * @param v0
     *            the v0
     */
    public void update(double dt, double v, double v0) {
        // exponential moving average
        final double gamma = Math.exp(-dt / tau);

        // level of service function
        final double vRel = v / v0;

        // integration of alpha-factors
        alphaT = gamma * alphaT + (1 - gamma) * (resignationMaxAlphaT + vRel * (1. - resignationMaxAlphaT));
        alphaV0 = gamma * alphaV0 + (1 - gamma) * (resignationMinAlphaV0 + vRel * (1. - resignationMinAlphaV0));
        alphaA = gamma * alphaA + (1 - gamma) * (resignationMinAlphaA + vRel * (1. - resignationMinAlphaA));

        logger.debug("vRel = {}, v0 = {}", vRel, v0);
        logger.debug("alphaT = {}, alphaV0 = {}", alphaT, alphaV0);

    }

    /**
     * Alpha a.
     * 
     * @return the double
     */
    public double alphaA() {
        return alphaA;
    }

    /**
     * Alpha v0.
     * 
     * @return the double
     */
    public double alphaV0() {
        return alphaV0;
    }

    /**
     * Alpha t.
     * 
     * @return the double
     */
    public double alphaT() {
        return alphaT;
    }
}
