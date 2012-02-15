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

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataKrauss;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// paper reference and modifications ...

/**
 * The Class Krauss.
 * 
 * @author Martin Treiber, Ralph Germ
 */
public class Krauss extends LongitudinalModelBase {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Krauss.class);

    /**
     * The parameter T is given by the update timestep dt: dt = T = Tr = tau_relax
     */
    private final double T;

    /** The a. */
    private final double a;

    /** The b. */
    private final double b;

    /**
     * The dimensionless epsilon has similar effects as the braking probability of the Nagel-Schreckenberg cellular
     * automaton default value 0.4 (PRE) or 1 (EPJB)
     */
    private double epsilon;

    /**
     * Instantiates a new Krauss instance.
     * 
     * @param parameters
     *            the parameters
     */
    public Krauss(double dt, LongitudinalModelInputDataKrauss parameters) {
        super(ModelName.KRAUSS, parameters);
        this.T = dt;
        logger.debug("init model parameters");
        this.v0 = parameters.getV0();
        this.a = parameters.getA();
        this.b = parameters.getB();
        this.s0 = parameters.getS0();
        this.epsilon = parameters.getEpsilon();
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

    /**
     * Gets the epsilon.
     * 
     * @return the epsilon
     */
    public double getEpsilon() {
        return epsilon;
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        final double localT = alphaT * T;
        final double localV0 = Math.min(alphaV0 * v0, me.getSpeedlimit());

        return acc(s, v, dv, localT, localV0);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, T, v0);
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
     *            the local time gap. Notice that inconsistencies may arise for nontrivial values since then no longer
     *            dt=T=tau_relax making the vSafe formula possibly inconsistent
     * @param v0Local
     *            the v0 local
     * 
     * @return the double
     */
    private double acc(double s, double v, double dv, double TLocal, double v0Local) {
        final double vp = v - dv;
        /**
         * safe speed; complicated formula in PRE 55, 5601 (1997) is essentially the vSafe formula for the simple Gipps
         * model. The complicated formula considers effects of finite dt; this is treated uniformly for all models in
         * our update routine, so it is not necessary here. Therefore the simple Gipps vSafe formula is chosen
         */
        final double vSafe = -b * TLocal + Math.sqrt(b * b * TLocal * TLocal + vp * vp + 2 * b * Math.max(s - s0, 0.));

        /**
         * vUpper =upper limit of new speed (denoted v1 in PRE) corresponds to vNew of the Gipps model
         */
        final double vUpper = Math.min(vSafe, Math.min(v + a * TLocal, v0Local));

        // The Krauss model is essentially the Gipps model with the following
        // three additional code lines

        /**
         * vLower = lower limit of new speed (denoted v0 in PRE) some modifications due to dimensional units were
         * applied. Notice that vLower may be > vUpper in some cut-in situations: these inconsistencies were not
         * recognized/treated in the PRE publication
         */
        final double vLower = (1 - epsilon) * vUpper + epsilon * Math.max(0, (v - b * TLocal));
        final double r = MyRandom.nextDouble(); // instance of uniform(0,1) distribution
        final double vNew = vLower + r * (vUpper - vLower);
        final double aWanted = (vNew - v) / TLocal;

        return aWanted;
    }
}
