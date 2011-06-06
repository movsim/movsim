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

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataACC;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM;
import org.movsim.simulator.vehicles.Moveable;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;
import org.movsim.utilities.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc


// Reference for constant-acceleration heuristic:
// Arne Kesting, Martin Treiber, Dirk Helbing
// Enhanced Intelligent Driver Model to access the impact of driving strategies on traffic capacity
// Philosophical Transactions of the Royal Society A 368, 4585-4605 (2010)

// Reference for improved intelligent driver extension:
// book ... 

/**
 * The Class ACC.
 */
public class ACC extends LongitudinalModelImpl implements AccelerationModel, Observer {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ACC.class);
    
    /** The v0. 
     * desired velocity (m/s) */
    private double v0; 
    
    /** The T. 
     * time headway (s)*/
    private double T; 
    
    /** The s0. 
     * bumper-to-bumper distance (m)*/
    private double s0; 
    
    /** The s1. */
    private double s1;
    
    /** The a. 
     * acceleration (m/s^2) */
    private double a; 
    
    /** The b. 
     * comfortable (desired) deceleration (m/s^2) */
    private double b; 
    
    /** The delta. 
     * acceleration exponent (1) */
    private double delta; 
    
    /** The coolness. 
     * coolness=0: acc1=IIDM (without constant-acceleration heuristic, CAH), coolness=1 CAH 
     * factor in range [0, 1]*/
    private double coolness;  

    /**
     * Instantiates a new aCC.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public ACC(String modelName, AccelerationModelInputDataACC parameters) {
        super(modelName, AccelerationModelCategory.CONTINUOUS_MODEL, parameters);
        initParameters();
    }

    @Override
    protected void initParameters() {
        logger.debug("init model parameters");
        this.v0 = ((AccelerationModelInputDataACC) parameters).getV0();
        this.T = ((AccelerationModelInputDataACC) parameters).getT();
        this.s0 = ((AccelerationModelInputDataACC) parameters).getS0();
        this.s1 = ((AccelerationModelInputDataACC) parameters).getS1();
        this.a = ((AccelerationModelInputDataACC) parameters).getA();
        this.b = ((AccelerationModelInputDataACC) parameters).getB();
        this.delta = ((AccelerationModelInputDataACC) parameters).getDelta();
        this.coolness = ((AccelerationModelInputDataACC) parameters).getCoolness();   
    }


    // copy constructor
    /**
     * Instantiates a new aCC.
     * 
     * @param accToCopy
     *            the acc to copy
     */
//    public ACC(ACC accToCopy) {
//        super(accToCopy.modelName(), accToCopy.getModelCategory());
//        this.v0 = accToCopy.getV0();
//        this.T = accToCopy.getT();
//        this.s0 = accToCopy.getS0();
//        this.s1 = accToCopy.getS1();
//        this.a = accToCopy.getA();
//        this.b = accToCopy.getB();
//        this.delta = accToCopy.getDelta();
//        this.coolness = accToCopy.getCoolness();
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

        // TODO kommentare checken/loeschen ... Martin!

        // Local dynamical variables
        final Moveable vehFront = vehContainer.getLeader(me);
        final double s = me.netDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.relSpeed(vehFront);
        final double aLeadDummy = 0;
        final double a_lead = (vehFront == null) ? aLeadDummy : vehFront.getAcc();

        // space dependencies modelled by speedlimits, alpha's
        // space dependencies modeled by speedlimits, alpha's

        final double Tloc = alphaT * T;
        final double v0Loc = Math.min(alphaV0 * v0, me.speedlimit()); // consider
                                                                      // external
                                                                      // speedlimit
        final double aLoc = alphaA * a;

        // double sstar = s0 + Tloc*v + s1*Math.sqrt((v+0.0001)/v0loc) +
        // (0.5*v*dv)/Math.sqrt(a*b);
        //
        // // if(sstar<s0+0.2*v*Tloc){
        // // sstar=s0+0.2*v*Tloc;
        // // }
        //
        //
        // if(sstar<s0){ sstar=s0;}
        //
        //
        // double aWanted = a*( 1.- Math.pow((v/v0loc), delta) -
        // (sstar/s)*(sstar/s));
        //
        // //return Math.max(aWanted, -BMAX);
        // return aWanted;

        // -------------------------------------------------------------

        // IDM: effectives Tmin/T selbst wenn Vorderfz s. schnell entfernt
        final double sstarmin_vT = 0.1; // 0.1 ... 0.3 bisher 0.2

        // ########################################################
        // IDM acceleration
        // ########################################################

        final double v0_sstar = 20; // lediglich Normierung, dass s1 Einheit m
        double sstar = s0 + Tloc * v + s1 * Math.sqrt((v + 0.01) / v0_sstar) + (0.5 * v * dv) / Math.sqrt(aLoc * b);

        // CAH und IDM: Kleinster Wert von (s-s0)/s0 (seff=s-s0 im Nenner)
        // "Nicht-glattes" Verhalten falls seffmin_s0 > 0.5
        final double seffmin_s0 = 0.2; // 0.1 ... 0.5
        final double sstarmin = seffmin_s0 * s0 + sstarmin_vT * v * Tloc;
        if (sstar < sstarmin) {
            sstar = sstarmin;
        }

        final double tau_relax = 20.; // physiolog. Relaxationszeit, falls v>v0
        double accFree = (v < v0Loc) ? aLoc * (1. - Math.pow((v / v0Loc), delta)) : (v0Loc - v) / tau_relax;

        // Unterdruecke accFree, falls dv^2/2s Bremsverzoeg. >=b impliziert
        // (Das FD bleibt unveraendert)
        final double b_kin = 0.5 * Math.max(dv, 0.0) * dv / s;
        accFree *= Math.max(1 - b_kin / b, 0.0);

        final double accIDM = accFree - aLoc * sstar * sstar / (s * s);

        // ########################################################
        // CAH deceleration (constant-acceleration heuristics)
        // ########################################################

        // Unterdruecke CAH- "Mitzieheffekte", falls Vordermann staerker
        // als gewuenscht beschleunigt

        // CAH

        // final double dvp = Math.max(dv, 0.0);
        // final double v_lead = v-dvp;
        // final double sEff = Math.max(s-s0, seffmin_s0*s0);
        // final boolean smin_at_stop = (v_lead*dvp < - 2 * sEff*a_lead_eff);
        // final double denom = v_lead*v_lead - 2 * sEff*a_lead_eff;
        //
        // final double accCAH = ( smin_at_stop && ( denom!=0 ))
        // ? v*v*a_lead_eff/denom
        // : a_lead_eff - 0.5*dvp*dvp/sEff;

        // ######################################################
        // Mischen von IDM, CAH ("Beste aller Welten abzueglich b")
        // ######################################################

        // coolness=0: acc1=acc2=accIDM; coolness=1 (VLA): acc1=accCAH
        // final double acc1 = coolness*accCAH + (1.-coolness)*accIDM;

        // // folgende 3 Zeilen erlauben schnelle (IDM-) Beschl. am Stauende
        // final double db1 = 0.2*aLoc;
        // final double shiftb1 = 0.0*aLoc; // shift to negative values of
        // accIDM
        // final double delta_b = b*0.5*(Math.tanh((-accIDM-shiftb1)/db1)+1);
        // //delta_b = b; //!!! Deaktivieren der "delta" Manipulation
        //
        // double acc2 = - delta_b + maxSmooth(accIDM, acc1, delta_b);

        // ######################################################
        // Philosophical Transactions of the Royal Society A 368, 4585-4605
        // (2010)
        // ######################################################

        final double accLeadMax = Math.max(0.0, 1.1 * accIDM);
        final double a_lead_eff = Math.min(accLeadMax, a_lead);

        final double dvp = Math.max(dv, 0.0);
        final double v_lead = v - dvp;

        final double denomNew = v_lead * v_lead - 2 * s * a_lead;

        final double accCAHNew = ((v_lead * dvp < -2 * s * a_lead_eff) && (denomNew != 0)) ? v * v * a_lead / denomNew
                : Math.min(a, a_lead) - 0.5 * dvp * dvp / Math.max(s, 0.0001);

        double acc2 = (accIDM > accCAHNew) ? accIDM : (1 - coolness) * accIDM + coolness
                * (accCAHNew + b * Math.tanh((accIDM - accCAHNew) / b));

        // ######################################################
        // Verallgemeinerung der vereinfachten Formulierung auf exaktes
        // Einhalten
        // von s0+vT im Gleichgewicht fuer v<=v0fuer acc2=acc ohne Jerk
        // IIDM --> Book
        // ######################################################

        final double z = sstar / Math.max(s, 0.01);
        final double accEmpty = (v <= v0) ? a * (1 - Math.pow((v / v0), delta)) : -b
                * (1 - Math.pow((v0 / v), a * delta / b));
        final double accPos = accEmpty * (1. - Math.pow(z, Math.min(2 * a / accEmpty, 100.)));
        final double accInt = a * (1 - z * z);

        final double accIDMnew = (v < v0) ? (z < 1) ? accPos : accInt : (z < 1) ? accEmpty : accInt + accEmpty;

        final double acc2New2 = (accIDMnew > accCAHNew) ? accIDMnew : (1 - coolness) * accIDMnew + coolness
                * (accCAHNew + b * Math.tanh((accIDMnew - accCAHNew) / b));

        acc2 = acc2New2;

        return (acc2);
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
        double sstar = s0 + T * v + 0.5 * v * dv / Math.sqrt(a * b); // desired
                                                                     // distance
        sstar += s1 * Math.sqrt((v + 0.000001) / v0);
        if (sstar < s0) {
            sstar = s0;
        }
        final double aWanted = a * (1. - Math.pow((v / v0), delta) - (sstar / s) * (sstar / s));
        return aWanted;
    }

    // private double maxSmooth(double x1, double x2, double dx){
    // return 0.5*(x1+x2) + Math.sqrt(0.25*(x1-x2)*(x1-x2) + dx*dx);
    // }
    //
    // private double minSmooth(double x1, double x2, double dx){
    // return 0.5*(x1+x2) - Math.sqrt(0.25*(x1-x2)*(x1-x2) + dx*dx);
    // }

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    public double getV0() {
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
     * Gets the s0.
     * 
     * @return the s0
     */
    public double getS0() {
        return s0;
    }

    /**
     * Gets the s1.
     * 
     * @return the s1
     */
    public double getS1() {
        return s1;
    }

    /**
     * Gets the delta.
     * 
     * @return the delta
     */
    public double getDelta() {
        return delta;
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
     * Gets the coolness.
     * 
     * @return the coolness
     */
    public double getCoolness() {
        return coolness;
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
