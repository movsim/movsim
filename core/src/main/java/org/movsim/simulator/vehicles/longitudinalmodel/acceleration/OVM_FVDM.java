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

import org.movsim.autogen.OptimalVelocityFunctionEnum;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterOVMFVDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OVM_FVDM. OVM = Optimal-Velocity Model and FVDM = Full-Velocity-Difference Model
 * 
 * Model parameters:
 * The tau. Speed adaptation time.
 * The transition width
 * The form factor beta.
 * The sensitivity gamma
 * 
 * The choice opt function variant. Variants: 0=fullVD original, 1=fullVD,secBased, 2=threePhase.
 * 
 */
class OVM_FVDM extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger logger = LoggerFactory.getLogger(OVM_FVDM.class);

    enum OptimalVelocityFunction {
        BANDO, TRIANGULAR, THREEPHASE
    }

    private final IModelParameterOVMFVDM param;

    /**
     * Instantiates a new OVM = Optimal-Velocity Model or FVDM = Full-Velocity-Difference Model
     * 
     * @param modelParameter
     */
    public OVM_FVDM(IModelParameterOVMFVDM modelParameter) {
        super(ModelName.OVM_FVDM);
        this.param = modelParameter;

    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {

        // Local dynamic variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle); // only needed for VDIFF

        // speed limit: OVM causes accidents due to immediate braking reaction
        final double v0Local = Math.min(alphaV0 * getDesiredSpeed(), me.getSpeedlimit());
        // System.out.println("Test: accSimple(...)="+accSimple(700.,3.6664,3.6664));System.exit(1);
        return acc(s, v, dv, alphaT, v0Local);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        final double alphaT = 1;
        return acc(s, v, dv, alphaT, getDesiredSpeed());
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
     * @param alphaT
     *            the alpha t
     * @param v0Local
     *            the v0loc
     * @return the double
     */
    private double acc(double s, double v, double dv, double alphaT, double v0Local) {

        final double transitionWidthLoc = Math.max(1e-6, param.getTransitionWidth() * alphaT);

        // final double betaLoc=beta*alpha_T;
        final double betaLoc = param.getBeta();

        double vOptimal = 0;// optimal velocity

        final double s0 = getMinimumGap();

        OptimalVelocityFunctionEnum variant = param.getOptimalSpeedFunction();

        if (variant == OptimalVelocityFunctionEnum.BANDO) {
            // standard OVM function (Bando model)
            // scale OVM/VDIFF so that v0 represents actual desired speed
            final double v0Prev = v0Local / (1.0 + Math.tanh(betaLoc));
            vOptimal = Math.max(v0Prev * (Math.tanh((s - s0) / transitionWidthLoc - betaLoc) - Math.tanh(-betaLoc)), 0.);
            // LOG.debug("s = {}, vOpt = {}", s, vOpt);
        } else if (variant == OptimalVelocityFunctionEnum.TRIANGULAR) {
            // triangular OVM function
            final double T = param.getBeta(); // interpret this as "time headway"
            vOptimal = Math.max(Math.min((s - s0) / T, v0Local), 0.0);
        } else if (variant == OptimalVelocityFunctionEnum.THREEPHASE) {
            // "Three-phase" OVM function
            final double diffT = 0.0 * Math.pow(Math.max(1 - v / v0Local, 0.0001), 0.5);
            final double Tmin = transitionWidthLoc + diffT; // minimum time headway
            final double Tmax = betaLoc + diffT; // maximum time headway
            final double Tdyn = (s - s0) / Math.max(v, MovsimConstants.SMALL_VALUE);
            vOptimal = (Tdyn > Tmax) ? Math.min((s - s0) / Tmax, v0Local) : (Tdyn > Tmin) ? Math.min(v, v0Local)
                    : (Tdyn > 0) ? Math.min((s - s0) / Tmin, v0Local) : 0;
        } else {
            logger.error("cannot map to optimal velocity variant = {}", param.getOptimalSpeedFunction());
            // System.exit(-1); // TODO throw exception
        }

        // calc acceleration
        double aWanted = 0; // return value
        final double tau = param.getTau();
        final double gamma = param.getGamma();
        if (variant == OptimalVelocityFunctionEnum.BANDO) {
            // original VDIFF model, OVM: lambda == 0
            aWanted = (vOptimal - v) / tau - gamma * dv;
        } else if (variant == OptimalVelocityFunctionEnum.TRIANGULAR) {
            aWanted = (vOptimal - v) / tau - gamma * v * dv / Math.max(s - 1.0 * s0, MovsimConstants.SMALL_VALUE);
        } else if (variant == OptimalVelocityFunctionEnum.THREEPHASE) {
            aWanted = (vOptimal - v) / tau - gamma * ((dv > 0) ? dv : 0);
        }

        if (aWanted > 100) {
            logger.error(" acc > 100! vopt = {}, v = {}", vOptimal, v);
            logger.error(" tau = {}, dv = {}", tau, dv);
            logger.error(" lambda = {} ", gamma);
            System.exit(-1);
        }
        return aWanted;
    }

    @Override
    protected IModelParameterOVMFVDM getParameter() {
        return param;
    }

}
