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

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// Nagel-Schreckenberg or Barlovic-Model
// paper reference
/**
 * The Class NSM.
 */
public class NSM extends AccelerationModelAbstract implements AccelerationModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(NSM.class);

    // unit time for CA:
    /** The Constant dtCA. */
    private static final double dtCA = 1; // update timestep for CA !!
    
    /** The v0. */
    private double v0;

    /** The p slowdown. */
    private double pSlowdown;

    /** The p slow to start. */
    private double pSlowToStart; // slow-to-start rule for Barlovic model

    
    
    
    /**
     * Instantiates a new nSM.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public NSM(AccelerationModelInputDataNSM parameters) {
        super(ModelName.NSM, parameters);
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
        this.v0 = ((AccelerationModelInputDataNSM) parameters).getV0();
        this.pSlowdown = ((AccelerationModelInputDataNSM) parameters).getSlowdown();
        this.pSlowToStart = ((AccelerationModelInputDataNSM) parameters).getSlowToStart();
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
        final Vehicle vehFront = vehContainer.getLeader(me);
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);
        return acc(s, v, dv, alphaT, alphaV0);
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
        
        final double alphaT = 1;
        final double alphaV0 = 1;

        return acc(s, v, dv, alphaT, alphaV0);
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
        return acc(s, v, dv, 1, 1);
    }

    /**
     * Acc simple.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param alphaT
     *            the alpha t
     * @param alphaV0
     *            the alpha v0
     * @return the double
     */
    private double acc(double s, double v, double dv, double alphaT, double alphaV0) {
        final int v0Loc = (int) (alphaV0 * v0 + 0.5); // adapt v0 spatially
        final int vLoc = (int) (v + 0.5);
        int vNew = 0;

        final double r1 = MyRandom.nextDouble();
        final double pb = (vLoc < 1) ? pSlowToStart : pSlowdown;
        final int slowdown = (r1 < pb) ? 1 : 0;

        final int sLoc = (int) (s + 0.5);
        vNew = Math.min(vLoc + 1, v0Loc);
        vNew = Math.min(vNew, sLoc);
        vNew = Math.max(0, vNew - slowdown);

        return ((vNew - vLoc) / dtCA);
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
     * Gets the v0.
     * 
     * @return the v0
     */
    public double getV0() {
        return v0;
    }

    /**
     * Gets the slowdown.
     * 
     * @return the slowdown
     */
    public double getSlowdown() {
        return pSlowdown;
    }

    /**
     * Gets the slow to start.
     * 
     * @return the slow to start
     */
    public double getSlowToStart() {
        return pSlowToStart;
    }

    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.AccelerationModelAbstract#setDesiredSpeedV0(double)
     */
    @Override
    protected void setDesiredSpeedV0(double v0) {
        this.v0 = (int)v0;
    }


}
