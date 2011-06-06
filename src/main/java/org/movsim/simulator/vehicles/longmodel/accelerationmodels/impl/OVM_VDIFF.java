/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF;
import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.Moveable;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// paper references Bando Model and velocity-difference model
// Variants of OVM function need documentation !!!
/**
 * The Class OVM_VDIFF.
 */
public class OVM_VDIFF extends LongitudinalModelImpl implements AccelerationModel {

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

    /** The choice opt func variant. 
     * variants: 0=fullVD orig, 1=fullVD,secBased, 2=threePhase */
    private int choiceOptFuncVariant; 

    /**
     * Instantiates a new oV m_ vdiff.
     * 
     * @param modelName
     *            the model name
     * @param parameter
     *            the parameter
     */
    public OVM_VDIFF(String modelName, AccelerationModelInputDataOVM_VDIFF parameters) {
        super(modelName, AccelerationModelCategory.CONTINUOUS_MODEL, parameters);
        initParameters();
    }
    
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

    /**
     * Instantiates a new oV m_ vdiff.
     * 
     * @param vdiffToCopy
     *            the vdiff to copy
     */
//    public OVM_VDIFF(OVM_VDIFF vdiffToCopy) {
//        super(vdiffToCopy.modelName(), vdiffToCopy.getModelCategory());
//        this.s0 = vdiffToCopy.getS0();
//        this.v0 = vdiffToCopy.getV0();
//        this.tau = vdiffToCopy.getTau();
//        this.lenInteraction = vdiffToCopy.getLenInteraction();
//        this.beta = vdiffToCopy.getBeta();
//        this.lambda = vdiffToCopy.getLambda();
//        this.choiceOptFuncVariant = vdiffToCopy.getOptFuncVariant();
//    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {

        // TODO: reaction time ?!
        // final double T_react_local = T_react;
        // final double s=cyclicBuf->get_s(iveh,it,T_react_local);
        // final double v=cyclicBuf->get_v(iveh,it,T_react_local);

        // Local dynamic variables
        final Moveable vehFront = vehContainer.getLeader(me);
        final double s = me.netDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.relSpeed(vehFront); // only needed for VDIFF

        // speed limit --> OVM causes accidents due to immediate braking reaction  
        final double v0loc = Math.min(alphaV0 * v0, me.speedlimit()); // consider
                                                                      // external
                                                                      // speedlimit

        return acc(s, v, dv, alphaT, v0loc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #accSimple(double, double, double)
     */
    @Override
    public double accSimple(double s, double v, double dv) {
        final double alphaT = 1;
        // final double alphaV0 = 1;
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
     * @param v0loc
     *            the v0loc
     * @return the double
     */
    private double acc(double s, double v, double dv, double alphaT, double v0loc) {

        // logger.debug("alphaT = {}", alphaT);
        // logger.debug("v0loc = {}", v0loc);

        double lenInteractionLoc = lenInteraction * alphaT;
        if (lenInteractionLoc < 1e-6) {
            lenInteractionLoc = 1e-6;
        }

        // final double betaLoc=beta*alpha_T;
        final double betaLoc = beta;

        double vOpt = 0;// optimal velocity

        if (choiceOptFuncVariant == 0 || choiceOptFuncVariant == 3) {
            // standard OVM function (Bando model)
            // vopt = max( 0.5*v0*( tanh((s-s0)/l_intLoc-betaLoc) -
            // tanh(-betaLoc)), 0.);
            // OVM/VDIFF nun so skaliert, dass v0 tatsaechlicih
            // Wunschgeschwindigkeit
            final double v0Prev = v0loc / (1. + Math.tanh(betaLoc));
            vOpt = Math.max(v0Prev * (Math.tanh((s - s0) / lenInteractionLoc - betaLoc) - Math.tanh(-betaLoc)), 0.);
            // logger.debug("s = {}, vOpt = {}", s, vOpt);
        } else if (choiceOptFuncVariant == 1) {
            // Triangular OVM function
            final double T = beta; // "time headway" // TODO muss alles noch dokumentiert werden!!!
            vOpt = Math.max(Math.min((s - s0) / T, v0loc), 0.);
        } else if (choiceOptFuncVariant == 2) {
            // "Three-phase" OVM function
            final double diffT = 0. * Math.pow(Math.max(1 - v / v0loc, 0.0001), 0.5);
            final double Tmin = lenInteractionLoc + diffT; // min time headway
            final double Tmax = betaLoc + diffT; // max time headway
            final double Tdyn = (s - s0) / Math.max(v, Constants.SMALL_VALUE);
            vOpt = (Tdyn > Tmax) ? Math.min((s - s0) / Tmax, v0loc) : (Tdyn > Tmin) ? Math.min(v + 0., v0loc)
                    : (Tdyn > 0) ? Math.min((s - s0) / Tmin, v0loc) : 0;
        } else {
            // logger.error("optimal velocity variant = {} not implemented. exit.",
            // choiceOptFuncVariant);
            System.exit(-1);
        }

        // calc acceleration
        double aWanted = 0; // return value
        if (choiceOptFuncVariant <= 1) {
            aWanted = (vOpt - v) / tau - lambda * dv; // OVM: lambda == 0
            // original VDIFF model

        } else if (choiceOptFuncVariant == 2) {
            aWanted = (vOpt - v) / tau - lambda * v * dv / Math.max(s - 1.0 * s0, Constants.SMALL_VALUE);
            // aWanted = Math.min(aWanted, 5.); // limit max acceleration
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
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModelImpl#parameterV0()
     */
    @Override
    public double parameterV0() {
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
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModelImpl#getRequiredUpdateTime()
     */
    @Override
    public double getRequiredUpdateTime() {
        return 0; // continuous model requires no specific timestep
    }

}
