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

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataGipps;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// paper reference and modifications ...

/**
 * The Class Gipps.
 */
public class Gipps extends LongitudinalModelBase {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Gipps.class);

    /**
     * The T. results from update timestep dt dt = T = Tr = tau_relax
     */
    private final double T;

    /** The a. */
    private final double a;

    /** The b. */
    private final double b;

    /**
     * Instantiates a new gipps.
     * 
     * @param parameters
     *            the parameters
     */
    public Gipps(double dt, LongitudinalModelInputDataGipps parameters) {
        super(ModelName.GIPPS, parameters);
        this.T = dt;
        logger.debug("init model parameters");
        this.v0 = parameters.getV0();
        this.a = parameters.getA();
        this.b = parameters.getB();
        this.s0 = parameters.getS0();
    }

    @Override
    protected void setDesiredSpeed(double v0) {
        this.v0 = v0;
    }
    @Override
    public double getDesiredSpeed() {
        return v0;
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

    @Override
    public double calcAcc(Vehicle me, LaneSegment laneSegment, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final Vehicle vehFront = laneSegment.frontVehicle(me);
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = (vehFront == null) ? 0 : v - vehFront.getSpeed();

        // space dependencies modeled by speedlimits, alpha's

        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * v0, me.getSpeedlimit());

        // #############################################################
        // space dependencies modelled by alpha_T
        // (!!! watch for alpha_T: dt unchanged, possibly inconsistent!)
        // #############################################################

        final double TLocal = alphaT * T;

        // actual Gipps formula
        return acc(s, v, dv, v0Local, TLocal);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel#calcAcc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.Vehicle)
     */
    @Override
    public double calcAcc(final Vehicle me, final Vehicle vehFront) {
        // Local dynamical variables
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);

        final double TLocal = T;
        final double v0Local = Math.min(v0, me.getSpeedlimit());

        return acc(s, v, dv, v0Local, TLocal);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, v0, T);
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
     * @param v0Local
     *            the v0 local
     * @param TLocal
     *            the t local
     * @return the double
     */
    private double acc(double s, double v, double dv, double v0Local, double TLocal) {
        final double vp = v - dv;
        // safe speed
        final double vSafe = -b * TLocal + Math.sqrt(b * b * TLocal * TLocal + vp * vp + 2 * b * Math.max(s - s0, 0.));
        final double vNew = Math.min(vSafe, Math.min(v + a * TLocal, v0Local));
        final double aWanted = (vNew - v) / TLocal;
        return aWanted;
    }
}
