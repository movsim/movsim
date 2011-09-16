/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ---------------------------------------------------------------------- This file is part of MovSim - the multi-model
 * open-source vehicular-traffic simulator MovSim is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataACC;
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

// Reference for improved intelligent driver extension: book 

/**
 * The Class ACC.
 */
public class ACC extends AccelerationModelAbstract implements AccelerationModel, Observer {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ACC.class);

    /**
     * The v0. desired velocity (m/s)
     */
    private double v0;

    /**
     * The T. time headway (s)
     */
    private double T;

    /**
     * The s0. bumper-to-bumper distance (m)
     */
    private double s0;

    /** The s1. */
    private double s1;

    /**
     * The a. acceleration (m/s^2)
     */
    private double a;

    /**
     * The b. comfortable (desired) deceleration (m/s^2)
     */
    private double b;

    /**
     * The delta. acceleration exponent (1)
     */
    private double delta;

    /**
     * The coolness. coolness=0: acc1=IIDM (without constant-acceleration
     * heuristic, CAH), coolness=1 CAH factor in range [0, 1]
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModel#initParameters()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double calcAcc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final Moveable vehFront = vehContainer.getLeader(me);
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);

        final double aLead = (vehFront == null) ? me.getAcc() : vehFront.getAcc();

        // space dependencies modeled by speedlimits, alpha's

        final double Tloc = alphaT * T;
        // consider external speedlimit
        final double v0Loc = Math.min(alphaV0 * v0, me.getSpeedlimit());
        final double aLoc = alphaA * a;

        return acc(s, v, dv, aLead, Tloc, v0Loc, aLoc);

    }
    
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel#calcAcc(org.movsim.simulator.vehicles.Vehicle, org.movsim.simulator.vehicles.Vehicle)
     */
    @Override
    public double calcAcc(final Vehicle me, final Vehicle vehFront){
        // Local dynamical variables
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);
        final double aLead = (vehFront == null) ? me.getAcc() : vehFront.getAcc();
        
        final double TLocal = T;;
        final double v0Local =  Math.min(v0, me.getSpeedlimit());
        final double aLocal = a;

        return acc(s, v, dv, aLead, TLocal, v0Local, aLocal);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #accSimple(double, double, double)
     */
    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, 0, T, v0, a);
    }

    // Implementation of ACC model with improved IDM (IIDM)
    /**
     * Acc.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param aLead
     *            the a lead
     * @param TLocal
     *            the t local
     * @param v0Local
     *            the v0 local
     * @param aLocal
     *            the a local
     * @return the double
     */
    private double acc(double s, double v, double dv, double aLead, double TLocal, double v0Local, double aLocal) {
        // treat special case of v0=0 (standing obstacle)
        if (v0Local == 0) {
            return 0;
        }
        // IIDM

        final double sstar = s0
                + Math.max(TLocal * v + s1 * Math.sqrt((v + 0.00001) / v0Local) + 0.5 * v * dv / Math.sqrt(a * b), 0.);
        final double z = sstar / Math.max(s, 0.01);
        final double accEmpty = (v <= v0) ? a * (1 - Math.pow((v / v0), delta)) : -b
                * (1 - Math.pow((v0 / v), a * delta / b));
        final double accPos = accEmpty * (1. - Math.pow(z, Math.min(2 * a / accEmpty, 100.)));
        final double accInt = a * (1 - z * z);

        final double accIIDM = (v < v0) ? (z < 1) ? accPos : accInt : (z < 1) ? accEmpty : accInt + accEmpty;

        // constant-acceleration heurstic (CAH)

        final double aLeadRestricted = Math.min(aLead, a);
        final double dvp = Math.max(dv, 0.0);
        final double vLead = v - dvp;
        final double denomCAH = vLead * vLead - 2 * s * aLeadRestricted;

        final double accCAH = ((vLead * dvp < -2 * s * aLeadRestricted) && (denomCAH != 0)) ? v * v * aLeadRestricted
                / denomCAH : aLeadRestricted - 0.5 * dvp * dvp / Math.max(s, 0.0001);

        // ACC with IIDM

        final double accACC_IIDM = (accIIDM > accCAH) ? accIIDM : (1 - coolness) * accIIDM + coolness
                * (accCAH + b * Math.tanh((accIIDM - accCAH) / b));

        return accACC_IIDM;
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
     * LongitudinalModel#parameterV0()
     */
    @Override
    public double getDesiredSpeedParameterV0() {
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
     * LongitudinalModel#getRequiredUpdateTime()
     */
    @Override
    public double getRequiredUpdateTime() {
        return 0; // continuous model requires no specific timestep
    }

    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.AccelerationModelAbstract#setDesiredSpeedV0(double)
     */
    @Override
    protected void setDesiredSpeedV0(double v0) {
        this.v0 = v0;
    }

   

  

   

}
