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
package org.movsim.simulator.vehicles.longmodel.accelerationmodels;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// paper references Bando Model and velocity-difference model
// Variants of OVM function need documentation !!!
/**
 * The Class OVM_VDIFF.
 */
public class OVM_VDIFF extends AccelerationModelAbstract implements AccelerationModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(OVM_VDIFF.class);

    /** The s0. */
    private double s0;

    /** The v0. */
    private double v0;

    /** The tau. */
    private double tau;

    /** The len interaction. */
    private double lenInteraction;

    /** The beta. */
    private double beta;

    /** The lambda. */
    private double lambda;

    /**
     * The choice opt func variant. variants: 0=fullVD orig, 1=fullVD,secBased, 2=threePhase
     */
    private int choiceOptFuncVariant;

    /**
     * Instantiates a new oV m_ vdiff.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public OVM_VDIFF(AccelerationModelInputDataOVM_VDIFF parameters) {
        super(ModelName.OVM_VDIFF, parameters);
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
        this.s0 = ((AccelerationModelInputDataOVM_VDIFF) parameters).getS0();
        this.v0 = ((AccelerationModelInputDataOVM_VDIFF) parameters).getV0();
        this.tau = ((AccelerationModelInputDataOVM_VDIFF) parameters).getTau();
        this.lenInteraction = ((AccelerationModelInputDataOVM_VDIFF) parameters).getLenInteraction();
        this.beta = ((AccelerationModelInputDataOVM_VDIFF) parameters).getBeta();
        this.lambda = ((AccelerationModelInputDataOVM_VDIFF) parameters).getLambda();
        choiceOptFuncVariant = ((AccelerationModelInputDataOVM_VDIFF) parameters).getVariant();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double calcAcc(Vehicle me, LaneSegment vehContainer, double alphaT, double alphaV0, double alphaA) {

        // Local dynamic variables
        final Vehicle vehFront = vehContainer.frontVehicle(me);
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront); // only needed for VDIFF

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
    public double calcAcc(final Vehicle me, final Vehicle vehFront) {
        // Local dynamic variables
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);

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

        final double lenInteractionLoc = Math.max(1e-6, lenInteraction * alphaT);

        // final double betaLoc=beta*alpha_T;
        final double betaLoc = beta;

        double vOpt = 0;// optimal velocity

        if (choiceOptFuncVariant == 0 || choiceOptFuncVariant == 3) {
            // standard OVM function (Bando model)
            // scale OVM/VDIFF so that v0 represents actual desired speed
            final double v0Prev = v0Local / (1. + Math.tanh(betaLoc));
            vOpt = Math.max(v0Prev * (Math.tanh((s - s0) / lenInteractionLoc - betaLoc) - Math.tanh(-betaLoc)), 0.);
            // logger.debug("s = {}, vOpt = {}", s, vOpt);
        } else if (choiceOptFuncVariant == 1) {
            // Triangular OVM function
            final double T = beta; // "time headway"
            vOpt = Math.max(Math.min((s - s0) / T, v0Local), 0.);
        } else if (choiceOptFuncVariant == 2) {
            // "Three-phase" OVM function
            final double diffT = 0. * Math.pow(Math.max(1 - v / v0Local, 0.0001), 0.5);
            final double Tmin = lenInteractionLoc + diffT; // minimum time headway
            final double Tmax = betaLoc + diffT; // maximum time headway
            final double Tdyn = (s - s0) / Math.max(v, MovsimConstants.SMALL_VALUE);
            vOpt = (Tdyn > Tmax) ? Math.min((s - s0) / Tmax, v0Local) : (Tdyn > Tmin) ? Math.min(v + 0., v0Local)
                    : (Tdyn > 0) ? Math.min((s - s0) / Tmin, v0Local) : 0;
        } else {
            logger.error("optimal velocity variant = {} not implemented. exit.", choiceOptFuncVariant);
            System.exit(-1);
        }

        // calc acceleration
        double aWanted = 0; // return value
        if (choiceOptFuncVariant <= 1) {
            // original VDIFF model, OVM: lambda == 0
            aWanted = (vOpt - v) / tau - lambda * dv;
        } else if (choiceOptFuncVariant == 2) {
            aWanted = (vOpt - v) / tau - lambda * v * dv / Math.max(s - 1.0 * s0, MovsimConstants.SMALL_VALUE);
        } else if (choiceOptFuncVariant == 3) {
            aWanted = (vOpt - v) / tau - lambda * ((dv > 0) ? dv : 0);
        }

        if (aWanted > 100) {
            logger.error(" acc > 100! vopt = {}, v = {}", vOpt, v);
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
    public double getLenInteraction() {
        return lenInteraction;
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
    public double getDesiredSpeedParameterV0() {
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
    protected void setDesiredSpeedV0(double v0) {
        this.v0 = v0;
    }

}
