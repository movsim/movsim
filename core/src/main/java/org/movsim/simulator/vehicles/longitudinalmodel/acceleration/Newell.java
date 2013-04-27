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

import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterNewell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Newell.
 */
class Newell extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger logger = LoggerFactory.getLogger(Newell.class);

    /** The simulation timepstep as parameter */
    private final double dt;

    private final IModelParameterNewell param;

    /**
     * Instantiates a new Newell car-following model.
     * 
     * @param simulationTimestep
     * @param modelParameter
     */
    public Newell(double simulationTimestep, IModelParameterNewell modelParameter) {
        super(ModelName.NEWELL);
        this.dt = simulationTimestep;
        this.param = modelParameter;
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        // TODO check modeling of parameter dt=T (dt is the constant update time and cannot be changed)
        final double dtLocal = alphaT * dt;
        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * getDesiredSpeed(), me.getSpeedlimit());

        // actual Newell formula
        return acc(s, v, dv, dtLocal, v0Local);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, dt, getDesiredSpeed());
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

        final double vNew = Math.min(Math.max((s - getMinimumGap()) / dtLocal, 0), v0Local);

        double aWanted = (vNew - v) / dtLocal;

        return aWanted;
    }

    @Override
    protected IModelParameterNewell getParameter() {
        return param;
    }

}
