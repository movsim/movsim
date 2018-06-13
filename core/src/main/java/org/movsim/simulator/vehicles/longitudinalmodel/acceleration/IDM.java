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
package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.autogen.ModelParameterIDM;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterIDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class IDM.
 *
 * <p>
 * Implementation of the 'intelligent driver model'(IDM). <a href="http://en.wikipedia.org/wiki/Intelligent_Driver_Model">Wikipedia article
 * IDM.</a>
 * </p>
 * <p>
 * Treiber/Kesting: Traffic Flow Dynamics, 2013, chapter 11.3
 * </p>
 * <p>
 * see <a href="https://arxiv.org/abs/cond-mat/0002177"> M. Treiber, A. Hennecke, and D. Helbing, Congested Traffic States in
 * Empirical Observations and Microscopic Simulations, Phys. Rev. E 62, 1805 (2000)].</a>
 * </p>
 *
 * Model parameters:
 * <ul>
 * <li>safe time headway T (s)</li>
 * <li>minimum gap in standstill s0 (m)</li>
 * <li>maximum desired acceleration a (m/s^2)</li>
 * <li>comfortable (desired) deceleration (m/s^2)</li>
 * <li>acceleration exponent delta (1)</li>
 * <li>gap parameter s1 (m).</li>
 * </ul>
 */
// TODO reduce visibility
public class IDM extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(IDM.class);

    private final IModelParameterIDM param;

    IDM(IModelParameterIDM parameters) {
        super(ModelName.IDM);
        this.param = parameters;
    }

    /**
     * Constructor.
     *
     * @param v0
     *         desired velocity, m/s
     * @param a
     *         maximum acceleration, m/s^2
     * @param b
     *         desired deceleration (comfortable braking), m/s^2
     * @param T
     *         safe time headway, seconds
     * @param s0
     *         bumper to bumper vehicle distance in stationary traffic, meters
     * @param s1
     *         gap parameter, meters
     */
    public IDM(double v0, double a, double b, double T, double s0, double s1) {
        super(ModelName.IDM);
        this.param = create(v0, a, b, T, s0, s1);
    }

    private static ModelParameterIDM create(double v0, double a, double b, double T, double s0, double s1) {
        ModelParameterIDM modelParameterIDM = new ModelParameterIDM();
        modelParameterIDM.setV0(v0);
        modelParameterIDM.setA(a);
        modelParameterIDM.setB(b);
        modelParameterIDM.setT(T);
        modelParameterIDM.setS0(s0);
        modelParameterIDM.setS1(s1);
        modelParameterIDM.setDelta(modelParameterIDM.getDelta());
        return modelParameterIDM;
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        // space dependencies modeled by speedlimits, alpha's

        final double localT = alphaT * param.getT();
        // consider external speedlimit
        final double localV0;
        if (me.getEffectiveSpeedlimit() != 0.0) {
            localV0 = Math.min(alphaV0 * getDesiredSpeed(), me.getEffectiveSpeedlimit());
        } else {
            localV0 = alphaV0 * getDesiredSpeed();
        }
        final double localA = alphaA * param.getA();

        return acc(s, v, dv, localT, localV0, localA);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, param.getT(), param.getV0(), param.getA());
    }

    /**
     * Acc.
     *
     * @param s
     *         the s
     * @param v
     *         the v
     * @param dv
     *         the dv
     * @param TLocal
     *         the t local
     * @param v0Local
     *         the v0 local
     * @param aLocal
     *         the a local
     * @return the double
     */
    private double acc(double s, double v, double dv, double TLocal, double v0Local, double aLocal) {
        // treat special case of v0=0 (standing obstacle)
        if (v0Local == 0.0) {
            return 0.0;
        }

        final double s0 = getMinimumGap();
        double sstar = s0 + TLocal * v + param.getS1() * Math.sqrt((v + 0.0001) / v0Local) + (0.5 * v * dv) / Math
                .sqrt(aLocal * param.getB());

        if (sstar < s0) {
            sstar = s0;
        }

        final double aWanted = aLocal * (1.0 - Math.pow((v / v0Local), param.getDelta()) - (sstar / s) * (sstar / s));

        LOG.debug("aWanted = {}", aWanted);
        return aWanted; // limit to -bMax in Vehicle
    }

    @Override
    protected IModelParameterIDM getParameter() {
        return param;
    }

}
