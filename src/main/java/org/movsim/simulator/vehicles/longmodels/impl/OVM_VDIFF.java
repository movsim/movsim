/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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
package org.movsim.simulator.vehicles.longmodels.impl;

import org.movsim.input.model.vehicle.longModel.ModelInputDataOVM_VDIFF;
import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodels.LongModelCategory;
import org.movsim.simulator.vehicles.longmodels.LongitudinalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



// paper references Bando Model and velocity-difference model
public class OVM_VDIFF extends LongitudinalModelImpl implements LongitudinalModel {

    final static Logger logger = LoggerFactory.getLogger(OVM_VDIFF.class);

    private double s0;

    private double v0;

    private double tau;

    private double lenInteraction;

    private double beta;

    private double lambda;

    private int choiceOptFuncVariant; // variants: 0=fullVD orig, 1=fullVD secBased, 2=threePhase

    public OVM_VDIFF(String modelName, ModelInputDataOVM_VDIFF parameter) {
        super(modelName, LongModelCategory.CONTINUOUS_MODEL);
        this.s0 = parameter.getS0();
        this.v0 = parameter.getV0();
        this.tau = parameter.getTau();
        this.lenInteraction = parameter.getLenInteraction();
        this.beta = parameter.getBeta();
        this.lambda = parameter.getLambda(); // parameter of VDIFF variant
        choiceOptFuncVariant = parameter.getVariant();
    }

    public OVM_VDIFF(OVM_VDIFF vdiffToCopy) {
        super(vdiffToCopy.modelName(), vdiffToCopy.getModelCategory());
        this.s0 = vdiffToCopy.getS0();
        this.v0 = vdiffToCopy.getV0();
        this.tau = vdiffToCopy.getTau();
        this.lenInteraction = vdiffToCopy.getLenInteraction();
        this.beta = vdiffToCopy.getBeta();
        this.lambda = vdiffToCopy.getLambda();
        this.choiceOptFuncVariant = vdiffToCopy.getOptFuncVariant();
    }

    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {

        // TODO: reaction time ?!
        // final double T_react_local = T_react;
        // final double s=cyclicBuf->get_s(iveh,it,T_react_local);
        // final double v=cyclicBuf->get_v(iveh,it,T_react_local);

        // Local dynamic variables
        final Vehicle vehFront = vehContainer.getLeader(me);
        final double s = me.netDistance(vehFront);
        final double v = me.speed();
        final double dv = me.relSpeed(vehFront); // only needed for VDIFF
        
        // speed limit test TODO
        final double v0loc = Math.min(alphaV0*v0, me.speedlimit());  // consider external speedlimit

        return acc(s, v, dv, alphaT, v0loc);
    }

    public double accSimple(double s, double v, double dv) {
        final double alphaT = 1;
        //final double alphaV0 = 1;
        return acc(s, v, dv, alphaT, v0);
    }


    private double acc(double s, double v, double dv, double alphaT, double v0loc) {

//      logger.debug("alphaT = {}", alphaT);
//      logger.debug("v0loc = {}", v0loc);

        
        double lenInteractionLoc = lenInteraction * alphaT;
        if (lenInteractionLoc < 1e-6) {
            lenInteractionLoc = 1e-6;
        }
        
        //final double betaLoc=beta*alpha_T;
        final double betaLoc = beta;


        double vOpt = 0;// optimal velocity
        
        if (choiceOptFuncVariant == 0 || choiceOptFuncVariant == 3 ) {
            // standard OVM function (Bando model)
            // vopt = max( 0.5*v0*( tanh((s-s0)/l_intLoc-betaLoc) - tanh(-betaLoc)), 0.);
        	// OVM/VDIFF nun so skaliert, dass v0 tatsaechlicih  Wunschgeschwindigkeit
            final double v0Prev = v0loc / (1. + Math.tanh(betaLoc));
            vOpt = Math.max(v0Prev * (Math.tanh((s - s0) / lenInteractionLoc - betaLoc) - Math.tanh(-betaLoc)), 0.);
            // logger.debug("s = {}, vOpt = {}", s, vOpt);
        }
        else if (choiceOptFuncVariant == 1) {
            // Triangular OVM function
            final double T = beta; // "time headway"
            vOpt = Math.max(Math.min((s - s0) / T, v0loc), 0.); 
        }
        else if (choiceOptFuncVariant == 2) {
            // "Three-phase" OVM function
            final double diffT = 0. * Math.pow(Math.max(1 - v / v0loc, 0.0001), 0.5);
            final double Tmin = lenInteractionLoc + diffT; // min time headway
            final double Tmax = betaLoc + diffT; // max time headway
            final double Tdyn = (s - s0) / Math.max(v, Constants.SMALL_VALUE);
            vOpt = (Tdyn > Tmax) ? 
            		Math.min((s - s0) / Tmax, v0loc) : 
            			(Tdyn > Tmin) ? Math.min(v + 0., v0loc) : 
            				(Tdyn > 0) ? Math.min((s - s0) / Tmin, v0loc) : 0;
        }
        else{
           // logger.error("optimal velocity variant = {} not implemented. exit.", choiceOptFuncVariant);
            System.exit(-1);
        }

        // calc acceleration
        double aWanted = 0; // return value
        if (choiceOptFuncVariant <= 1) {
            aWanted = (vOpt - v) / tau - lambda * dv;  // OVM: lambda == 0
            // original  VDIFF model

        } 
        else if (choiceOptFuncVariant == 2) {
            aWanted = (vOpt - v) / tau - lambda * v * dv / Math.max(s - 1.0 * s0, Constants.SMALL_VALUE);
            //aWanted = Math.min(aWanted, 5.); // limit max acceleration
        }
        else if (choiceOptFuncVariant == 3){
            aWanted = (vOpt-v)/tau - lambda * ((dv>0) ? dv : 0);
        }
        
        if (aWanted > 100) {
            logger.error(" acc > 100! vopt = {}, v = {}", vOpt, v);
            logger.error(" tau = {}, dv = {}", tau, dv);
            logger.error(" lambda = {} ", lambda);
            System.exit(-1);
        }
        return aWanted;
    }

       

    
    
    public double getS0() {
        return s0;
    }

    public double getV0() {
        return v0;
    }

    public double getTau() {
        return tau;
    }

    public double getLenInteraction() {
        return lenInteraction;
    }

    public double getBeta() {
        return beta;
    }

    public double getLambda() {
        return lambda;
    }

    public double parameterV0() {
        return v0;
    }

    public int getOptFuncVariant() {
        return choiceOptFuncVariant;
    }
    
	public double getRequiredUpdateTime() {
		return 0; // continuous model requires no specific timestep
	}

}
