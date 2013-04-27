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

import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterKrauss;
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
class Krauss extends LongitudinalModelBase {

    /** The Constant LOG. */
    final static Logger logger = LoggerFactory.getLogger(Krauss.class);

    /**
     * The parameter T is given by the update timestep dt: dt = T = Tr = tau_relax
     */
    private final double T;

    private final IModelParameterKrauss param;

    /**
     * The dimensionless epsilon has similar effects as the braking probability of the Nagel-Schreckenberg cellular
     * automaton default value 0.4 (PRE) or 1 (EPJB)
     */

    /**
     * Instantiates a new Krauss instance.
     * 
     * @param parameters
     *            the parameters
     */
    Krauss(double simulationTimestep, IModelParameterKrauss parameters) {
        super(ModelName.KRAUSS);
        this.T = simulationTimestep;
        this.param = parameters;
    }



    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        final double localT = alphaT * T;
        final double localV0 = Math.min(alphaV0 * getDesiredSpeed(), me.getSpeedlimit());

        return acc(s, v, dv, localT, localV0);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, T, getDesiredSpeed());
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
        final double b = param.getB();
        final double vSafe = -b * TLocal
                + Math.sqrt(b * b * TLocal * TLocal + vp * vp + 2 * b * Math.max(s - getMinimumGap(), 0.));

        /**
         * vUpper =upper limit of new speed (denoted v1 in PRE) corresponds to vNew of the Gipps model
         */
        final double vUpper = Math.min(vSafe, Math.min(v + param.getA() * TLocal, v0Local));

        // The Krauss model is essentially the Gipps model with the following
        // three additional code lines

        /**
         * vLower = lower limit of new speed (denoted v0 in PRE) some modifications due to dimensional units were
         * applied. Notice that vLower may be > vUpper in some cut-in situations: these inconsistencies were not
         * recognized/treated in the PRE publication
         */
        final double vLower = (1 - param.getEpsilon()) * vUpper + param.getEpsilon() * Math.max(0, (v - b * TLocal));
        final double r = MyRandom.nextDouble(); // instance of uniform(0,1) distribution
        final double vNew = vLower + r * (vUpper - vLower);
        final double aWanted = (vNew - v) / TLocal;

        return aWanted;
    }

    @Override
    protected IModelParameterKrauss getParameter() {
        return param;
    }

}
