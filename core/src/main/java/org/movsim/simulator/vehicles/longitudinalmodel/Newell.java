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
package org.movsim.simulator.vehicles.longitudinalmodel;

import org.movsim.input.model.vehicle.longitudinalmodel.impl.LongitudinalModelInputDataNewellImpl;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO paper reference ...
/**
 * The Class Newell.
 */
public class Newell extends LongitudinalModelBase {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Newell.class);

    /** The dt. */
    private double dt;

    /** The v0. */
    private double v0;

    /** The s0. */
    private double s0;

    /**
     * Instantiates a new newell.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public Newell(double dt, LongitudinalModelInputDataNewellImpl parameters) {
        super(ModelName.NEWELL, parameters);
        initParameters(dt);
    }

    private void initParameters(double dt) {
        this.dt = dt;
        initParameters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl. LongitudinalModel#initParameters()
     */
    @Override
    protected void initParameters() {
        logger.debug("init model parameters");
        this.v0 = ((LongitudinalModelInputDataNewellImpl) parameters).getV0();
        this.s0 = ((LongitudinalModelInputDataNewellImpl) parameters).getS0();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double calcAcc(Vehicle me, LaneSegment vehContainer, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final Vehicle vehFront = vehContainer.frontVehicle(me);
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);

        // TODO check modeling of parameter dt=T (dt is the constant update time and cannot be changed)
        final double dtLocal = alphaT * dt;
        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * v0, me.getSpeedlimit());

        // actual Newell formula
        return acc(s, v, dv, dtLocal, v0Local);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel#calcAcc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.Vehicle)
     */
    @Override
    public double calcAcc(final Vehicle me, final Vehicle vehFront) {
        // Local dynamic variables
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);

        final double dtLocal = dt;
        final double v0Local = Math.min(v0, me.getSpeedlimit());

        return acc(s, v, dv, dtLocal, v0Local);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel #accSimple(double, double, double)
     */
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

        // workaround to avoid crash
        if (s / v < dt) {
            aWanted = -10000000;
        }

        return aWanted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl. LongitudinalModel#parameterV0()
     */
    @Override
    public double getDesiredSpeedParameterV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.AccelerationModelAbstract#setDesiredSpeedV0(double)
     */
    @Override
    protected void setDesiredSpeedV0(double v0) {
        this.v0 = v0;
    }

}
