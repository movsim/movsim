/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

import org.movsim.autogen.NoiseParameter;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acceleration noise for microscopic traffic models with (random process) models for white noise or correlated noise
 * (Wiener process).
 * 
 * <p>
 * Paper for reference:
 * </p>
 * <p>
 * <a href="http://arxiv.org/abs/physics/0508222"> M. Treiber, A. Kesting, D. Helbing, Understanding widely scattered traffic flows, the
 * capacity drop, and platoons as effects of variance-driven time gaps. Phys. Rev. E 74, 016123 (2006).</a>
 * </p>
 */

// TODO formulate noise in more general terms. not only applicable to acceleration noise. Standard wiener with
// fluctStrenth 1
public class Noise {

    private static final Logger LOG = LoggerFactory.getLogger(Noise.class);

    /** constant for uniform distribution calculation. */
    static final double SQRT12 = Math.sqrt(12.);

    /** Flag variable for wiener process or not. */
    private final boolean isWienerProcess;

    /** The tau relax acc in seconds (parameter). */
    private final double tauRelaxAcc;

    /**
     * The fluctuation strength in (m^2/s^3) of dv/dt=a_det+xi(t). In case of Wieder process the standard deviation
     * (parameter).
     */
    private final double fluctStrength;

    /** The xi acc as dynamic state variable (output) */
    private double xiAcc;

    public Noise(NoiseParameter parameters) {
        xiAcc = 0;
        fluctStrength = parameters.getFluctStrength();
        tauRelaxAcc = parameters.getTau();

        isWienerProcess = (tauRelaxAcc != 0) ? true : false;
        LOG.debug("tauRelaxAcc = {}, isWienerProcess = {}", tauRelaxAcc, isWienerProcess);
    }

    /**
     * Update. Calculates the acceleration noise {code xiAcc} modelled by a Wiener process or as delta-correlated random
     * process.
     * 
     * @param dt
     *            simulation time interval, seconds
     */
    public void update(double dt) {

        final double randomMu0Sigma1 = getUniformlyDistributedRealization();

        if (isWienerProcess) {
            final double betaAcc = Math.exp(-dt / tauRelaxAcc);
            xiAcc = betaAcc * xiAcc + fluctStrength * Math.sqrt(2 * dt / tauRelaxAcc) * randomMu0Sigma1;
            LOG.debug("Wiener process: betaAcc={}, stdDevAcc*Math.sqrt(2*dt/tauRelaxAcc)*randomMu0Sigma1= {}", betaAcc,
                    (fluctStrength * Math.sqrt(2 * dt / tauRelaxAcc) * randomMu0Sigma1));
        } else {
            // delta-correlated acc noise.
            xiAcc = Math.sqrt(fluctStrength / dt) * randomMu0Sigma1;
        }

    }

    /**
     * calculates uniform distribution with mean=0 and variance=1.
     * 
     * @return random variable realization
     */
    private static double getUniformlyDistributedRealization() {
        final double randomVar = MyRandom.nextDouble();
        final double randomMu0Sigma1 = SQRT12 * (randomVar - 0.5);
        return randomMu0Sigma1;
    }

    /**
     * Gets the acc error.
     * 
     * @return the modelled acc error
     */
    public double getAccError() {
        return xiAcc;
    }

}
