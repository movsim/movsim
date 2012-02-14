/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNewell;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Newell.
 */
public class Newell extends LongitudinalModelBase {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Newell.class);

    /** The dt. */
    private final double dt;

    /**
     * Instantiates a new newell.
     * 
     * @param parameters
     *            the parameters
     */
    public Newell(double dt, LongitudinalModelInputDataNewell parameters) {
        super(ModelName.NEWELL, parameters);
        this.dt = dt;
        logger.debug("init model parameters");
        this.v0 = parameters.getV0();
        this.s0 = parameters.getS0();
    }

    @Override
    public double calcAcc(Vehicle me, LaneSegment laneSegment, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final Vehicle frontVehicle = laneSegment.frontVehicle(me);
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        // TODO check modeling of parameter dt=T (dt is the constant update time and cannot be changed)
        final double dtLocal = alphaT * dt;
        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * v0, me.getSpeedlimit());

        // actual Newell formula
        return acc(s, v, dv, dtLocal, v0Local);
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle) {
        // Local dynamic variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        final double dtLocal = dt;
        final double v0Local = Math.min(v0, me.getSpeedlimit());

        return acc(s, v, dv, dtLocal, v0Local);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, dt, v0);
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
     * @param dtLocal
     *            the dt local
     * @return the double
     */
    private double acc(double s, double v, double dv, double dtLocal, double v0Local) {

        final double vNew = Math.min(Math.max((s - s0) / dtLocal, 0), v0Local);

        double aWanted = (vNew - v) / dtLocal;

        return aWanted;
    }
}
