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
package org.movsim.simulator.vehicles.longmodel.impl;

import org.movsim.input.model.vehicle.behavior.MemoryInputData;
import org.movsim.simulator.vehicles.longmodel.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// Resignation or Memory effect, see paper:
// M. Treiber, D. Helbing:
// Memory effects in microscopic traffic models and wide scattering in flow-density data
// Physical Review E 68, 046119 (2003)

/**
 * The Class MemoryImpl.
 */
public class MemoryImpl implements Memory {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(MemoryImpl.class);

    /** The tau. 
     * relaxation time */
    private final double tau;

    /** The resignation min alpha a. */
    private final double resignationMinAlphaA;

    /** The resignation min alpha v0. */
    private final double resignationMinAlphaV0;

    /** The resignation max alpha t. */
    private final double resignationMaxAlphaT;

    /** The alpha a. 
     * dynamic state variable */
    private double alphaA;

    /** The alpha v0. 
     * dynamic state variable */
    private double alphaV0;

    /** The alpha t. 
     * dynamic state variable */
    private double alphaT;

    /**
     * Instantiates a new memory impl.
     * 
     * @param parameters
     *            the parameters
     */
    public MemoryImpl(MemoryInputData parameters) {
        // parameters
        tau = parameters.getTau();
        resignationMaxAlphaT = parameters.getResignationMaxAlphaT();
        resignationMinAlphaV0 = parameters.getResignationMinAlphaV0();
        resignationMinAlphaA = parameters.getResignationMinAlphaA();

        // initialize dynamic state variables
        alphaA = 1;
        alphaV0 = 1;
        alphaT = 1;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.Memory#update(double,
     * double, double)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.Memory#alphaA()
     */
    @Override
    public double alphaA() {
        return alphaA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.Memory#alphaV0()
     */
    @Override
    public double alphaV0() {
        return alphaV0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.Memory#alphaT()
     */
    @Override
    public double alphaT() {
        return alphaT;
    }

}
