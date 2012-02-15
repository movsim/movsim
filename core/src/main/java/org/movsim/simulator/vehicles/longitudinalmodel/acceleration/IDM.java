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
package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataIDM;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class IDM.
 * <p>
 * Implementation of the 'intelligent driver model'(IDM). <a
 * href="http://en.wikipedia.org/wiki/Intelligent_Driver_Model">Wikipedia article IDM.</a>
 * </p>
 * <p>
 * Treiber/Kesting: Verkehrsdynamik und -simulation, 2010, chapter 11.3
 * </p>
 * <p>
 * see <a href="http://xxx.uni-augsburg.de/abs/cond-mat/0002177"> M. Treiber, A. Hennecke, and D. Helbing, Congested
 * Traffic States in Empirical Observations and Microscopic Simulations, Phys. Rev. E 62, 1805 (2000)].</a>
 * </p>
 */
public class IDM extends LongitudinalModelBase {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(IDM.class);

    /** safe time headway (s). */
    private final double T;

    /** gap parameter (m). */
    private final double s1;

    /** acceleration (m/s^2). */
    private final double a;

    /** comfortable (desired) deceleration (braking), (m/s^2). */
    private final double b;

    /** acceleration exponent. */
    private final double delta;

    /**
     * Instantiates a new IDM.
     * 
     * @param parameters
     *            the parameters: v0, T, s0, s1, a, b, delta
     */
    public IDM(LongitudinalModelInputDataIDM parameters) {
        super(ModelName.IDM, parameters);
        logger.debug("init model parameters");
        this.v0 = parameters.getV0();
        this.T = parameters.getT();
        this.s0 = parameters.getS0();
        this.s1 = parameters.getS1();
        this.a = parameters.getA();
        this.b = parameters.getB();
        this.delta = parameters.getDelta();
    }

    /**
     * Constructor.
     * 
     * @param v0
     *            desired velocity, m/s
     * @param a
     *            maximum acceleration, m/s^2
     * @param b
     *            desired deceleration (comfortable braking), m/s^2
     * @param T
     *            safe time headway, seconds
     * @param s0
     *            bumper to bumper vehicle distance in stationary traffic, meters
     * @param s1
     *            gap parameter, meters
     */
    public IDM(double v0, double a, double b, double T, double s0, double s1) {
        super(ModelName.IDM, null);
        this.v0 = v0;
        this.a = a;
        this.b = b;
        this.T = T;
        this.s0 = s0;
        this.s1 = s1;
        this.delta = 4.0;
    }

    /**
     * Gets the s1.
     * 
     * @return the s1
     */
    public double getS1() {
        return s1;
    }

    /**
     * Gets the t.
     * 
     * @return the t
     */
    public double getT() {
        return T;
    }

    /**
     * Gets the delta.
     * 
     * @return the delta
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Gets the a.
     * 
     * @return the a
     */
    public double getA() {
        return a;
    }

    /**
     * Gets the b.
     * 
     * @return the b
     */
    public double getB() {
        return b;
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        // space dependencies modeled by speedlimits, alpha's

        final double localT = alphaT * T;
        // consider external speedlimit
        final double localV0;
        if (me.getSpeedlimit() != 0.0) {
            localV0 = Math.min(alphaV0 * v0, me.getSpeedlimit());
        } else {
            localV0 = alphaV0 * v0;
        }
        final double localA = alphaA * a;

        return acc(s, v, dv, localT, localV0, localA);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, T, v0, a);
    }

    /**
     * Acc.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param TLocal
     *            the t local
     * @param v0Local
     *            the v0 local
     * @param aLocal
     *            the a local
     * @return the double
     */
    private double acc(double s, double v, double dv, double TLocal, double v0Local, double aLocal) {
        // treat special case of v0=0 (standing obstacle)
        if (v0Local == 0.0) {
            return 0.0;
        }

        double sstar = s0 + TLocal * v + s1 * Math.sqrt((v + 0.0001) / v0Local) + (0.5 * v * dv)
                / Math.sqrt(aLocal * b);

        if (sstar < s0) {
            sstar = s0;
        }

        final double aWanted = aLocal * (1.0 - Math.pow((v / v0Local), delta) - (sstar / s) * (sstar / s));

        logger.debug("aWanted = {}", aWanted);
        return aWanted; // limit to -bMax in Vehicle
    }
}
