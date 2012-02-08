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

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataOVM_FVDM;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OVM_FVDM. OVM = Optimal-Velocity Model and FVDM = Full-Velocity-Difference Model
 */
public class OVM_FVDM extends LongitudinalModelBase {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(OVM_FVDM.class);

    /** The s0. Minimum distance gap*/
    private double s0;

    /** The v0. Desired velocity*/
    private double v0;

    /** The tau. Speed adaptation time*/
    private double tau;

    /** The transition width */
    private double transitionWidth;

    /** The beta. Form factor */
    private double beta;

    /** The lambda. Sensitivity. */ // TODO rg 12/19/2011: called gamma in the book
    private double lambda;

    /**
     * The choice opt function variant. Variants: 0=fullVD original, 1=fullVD,secBased, 2=threePhase.
     */
    private int choiceOptFuncVariant;

    /**
     * Instantiates a new OVM = Optimal-Velocity Model or FVDM = Full-Velocity-Difference Model
     * 
     * @param parameters
     *            the parameters
     */
    public OVM_FVDM(LongitudinalModelInputDataOVM_FVDM parameters) {
        super(ModelName.OVM_FVDM, parameters);
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
        this.s0 = ((LongitudinalModelInputDataOVM_FVDM) parameters).getS0();
        this.v0 = ((LongitudinalModelInputDataOVM_FVDM) parameters).getV0();
        this.tau = ((LongitudinalModelInputDataOVM_FVDM) parameters).getTau();
        this.transitionWidth = ((LongitudinalModelInputDataOVM_FVDM) parameters).getTransitionWidth();
        this.beta = ((LongitudinalModelInputDataOVM_FVDM) parameters).getBeta();
        this.lambda = ((LongitudinalModelInputDataOVM_FVDM) parameters).getLambda();
        choiceOptFuncVariant = ((LongitudinalModelInputDataOVM_FVDM) parameters).getVariant();
    }

    @Override
    public double calcAcc(Vehicle me, LaneSegment laneSegment, double alphaT, double alphaV0, double alphaA) {

        // Local dynamic variables
        final Vehicle frontVehicle = laneSegment.frontVehicle(me);
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle); // only needed for VDIFF

        // speed limit: OVM causes accidents due to immediate braking reaction
        final double v0Local = Math.min(alphaV0 * v0, me.getSpeedlimit());
        // System.out.println("Test: accSimple(...)="+accSimple(700.,3.6664,3.6664));System.exit(1);
        return acc(s, v, dv, alphaT, v0Local);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel#calcAcc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.Vehicle)
     */
    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle) {
        // Local dynamic variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        final double alphaT = 1;
        final double v0Local = Math.min(v0, me.getSpeedlimit());

        return acc(s, v, dv, alphaT, v0Local);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel #accSimple(double, double, double)
     */
    @Override
    public double calcAccSimple(double s, double v, double dv) {
        final double alphaT = 1;
        return acc(s, v, dv, alphaT, v0);
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

        final double transitionWidthLoc = Math.max(1e-6, transitionWidth * alphaT);

        // final double betaLoc=beta*alpha_T;
        final double betaLoc = beta;

        double vOptimal = 0;// optimal velocity

        if (choiceOptFuncVariant == 0 || choiceOptFuncVariant == 3) {
            // standard OVM function (Bando model)
            // scale OVM/VDIFF so that v0 represents actual desired speed
            final double v0Prev = v0Local / (1. + Math.tanh(betaLoc));
            vOptimal = Math.max(v0Prev * (Math.tanh((s - s0) / transitionWidthLoc - betaLoc) - Math.tanh(-betaLoc)), 0.);
            // logger.debug("s = {}, vOpt = {}", s, vOpt);
        } else if (choiceOptFuncVariant == 1) {
            // triangular OVM function
            final double T = beta; // "time headway"
            vOptimal = Math.max(Math.min((s - s0) / T, v0Local), 0.);
        } else if (choiceOptFuncVariant == 2) {
            // "Three-phase" OVM function
            final double diffT = 0. * Math.pow(Math.max(1 - v / v0Local, 0.0001), 0.5);
            final double Tmin = transitionWidthLoc + diffT; // minimum time headway
            final double Tmax = betaLoc + diffT; // maximum time headway
            final double Tdyn = (s - s0) / Math.max(v, MovsimConstants.SMALL_VALUE);
            vOptimal = (Tdyn > Tmax) ? Math.min((s - s0) / Tmax, v0Local) : (Tdyn > Tmin) ? Math.min(v, v0Local)
                    : (Tdyn > 0) ? Math.min((s - s0) / Tmin, v0Local) : 0;
        } else {
            logger.error("optimal velocity variant = {} not implemented. exit.", choiceOptFuncVariant);
            System.exit(-1);
        }

        // calc acceleration
        double aWanted = 0; // return value
        if (choiceOptFuncVariant <= 1) {
            // original VDIFF model, OVM: lambda == 0
            aWanted = (vOptimal - v) / tau - lambda * dv;
        } else if (choiceOptFuncVariant == 2) {
            aWanted = (vOptimal - v) / tau - lambda * v * dv / Math.max(s - 1.0 * s0, MovsimConstants.SMALL_VALUE);
        } else if (choiceOptFuncVariant == 3) {
            aWanted = (vOptimal - v) / tau - lambda * ((dv > 0) ? dv : 0);
        }

        if (aWanted > 100) {
            logger.error(" acc > 100! vopt = {}, v = {}", vOptimal, v);
            logger.error(" tau = {}, dv = {}", tau, dv);
            logger.error(" lambda = {} ", lambda);
            System.exit(-1);
        }
        return aWanted;
    }

    /**
     * Gets the s0.
     * 
     * @return the s0
     */
    public double getS0() {
        return s0;
    }

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    public double getV0() {
        return v0;
    }

    /**
     * Gets the tau.
     * 
     * @return the tau
     */
    public double getTau() {
        return tau;
    }

    /**
     * Gets the len interaction.
     * 
     * @return the len interaction
     */
    public double getTransitionWidth() {
        return transitionWidth;
    }

    /**
     * Gets the beta.
     * 
     * @return the beta
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Gets the lambda.
     * 
     * @return the lambda
     */
    public double getLambda() {
        return lambda;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl. LongitudinalModel#parameterV0()
     */
    @Override
    public double getDesiredSpeed() {
        return v0;
    }

    /**
     * Gets the opt func variant.
     * 
     * @return the opt func variant
     */
    public int getOptFuncVariant() {
        return choiceOptFuncVariant;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.AccelerationModelAbstract#setDesiredSpeedV0(double)
     */
    @Override
    protected void setDesiredSpeed(double v0) {
        this.v0 = v0;
    }
}
