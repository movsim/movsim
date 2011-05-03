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

import org.movsim.input.model.vehicle.longModel.ModelInputDataGipps;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;

// TODO: Auto-generated Javadoc
// paper reference and modifications ...

/**
 * The Class Gipps.
 */
public class Gipps extends LongitudinalModelImpl implements AccelerationModel {

    /** The T. */
    private final double T; // ergibt sich aus dt !!
    
    /** The v0. */
    private final double v0;
    
    /** The a. */
    private final double a;
    
    /** The b. */
    private final double b;
    
    /** The s0. */
    private final double s0;

    /**
     * Instantiates a new gipps.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public Gipps(String modelName, ModelInputDataGipps parameters) {
        super(modelName, AccelerationModelCategory.INTERATED_MAP_MODEL);
        this.T = parameters.getDt(); // Gipps: dt=T=Tr=tau_relax!
        this.v0 = parameters.getV0();
        this.a = parameters.getA();
        this.b = parameters.getB();
        this.s0 = parameters.getS0();
    }

    // copy constructor
    /**
     * Instantiates a new gipps.
     * 
     * @param modelToCopy
     *            the model to copy
     */
    public Gipps(Gipps modelToCopy) {
        super(modelToCopy.modelName(), modelToCopy.getModelCategory());
        this.T = modelToCopy.getT();
        this.v0 = modelToCopy.getV0();
        this.a = modelToCopy.getA();
        this.b = modelToCopy.getB();
        this.s0 = modelToCopy.getS0();
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
     * Gets the v0.
     * 
     * @return the v0
     */
    public double getV0() {
        return v0;
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

    /**
     * Gets the s0.
     * 
     * @return the s0
     */
    public double getS0() {
        return s0;
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
        return acc(s, v, dv, v0, T);
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
     * @param v0Loc
     *            the v0 loc
     * @param aLoc
     *            the a loc
     * @return the double
     */
    public double acc(double s, double v, double dv, double v0Loc, double aLoc) {
        final double vp = v - dv;
        final double vSafe = -b * T + Math.sqrt(b * b * T * T + vp * vp + 2 * b * Math.max(s - s0, 0.)); // safe
                                                                                                         // velocity
        final double vNew = Math.min(vSafe, Math.min(v + aLoc * T, v0Loc));
        final double aWanted = (vNew - v) / T;
        return aWanted;
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
    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {

        // // Local dynamical variables
        // double s = cyclicBuf->get_s(iveh); //cyclicBuf->get_x(iveh-1) -
        // cyclicBuf->get_l(iveh-1) - cyclicBuf->get_x(iveh);
        // //xveh[iveh-1]-length[iveh-1]-xveh[iveh];
        // double v= cyclicBuf->get_v(iveh); //vveh[iveh];
        // double dv= v - cyclicBuf->get_v(iveh-1); //vveh[iveh];

        // Local dynamical variables
        final Vehicle vehFront = vehContainer.getLeader(me);
        final double s = me.netDistance(vehFront);
        final double v = me.speed();
        final double dv = (vehFront == null) ? 0 : v - vehFront.speed();

        // space dependencies modeled by speedlimits, alpha's
        // TODO check
        // final double Tloc = alphaT*T;
        final double v0Loc = Math.min(alphaV0 * v0, me.speedlimit()); // consider
                                                                      // external
                                                                      // speedlimit
        final double aLoc = alphaA * a;

        // actual Gipps formula
        return acc(s, v, dv, v0Loc, aLoc);

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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModelImpl#getRequiredUpdateTime()
     */
    @Override
    public double getRequiredUpdateTime() {
        return this.T; // iterated map requires specific timestep!!
    }

}
