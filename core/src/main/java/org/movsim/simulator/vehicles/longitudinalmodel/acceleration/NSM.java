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

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNSM;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// Nagel-Schreckenberg or Barlovic-Model
// paper reference
/**
 * The Class NSM.
 */
public class NSM extends LongitudinalModelBase {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(NSM.class);

    /** The constant unit time */
    private static final double dtCA = 1; 

    /** The v0. */
    private double v0;

    /** The p slowdown. */
    private double pSlowdown;

    /** slow-to-start rule for Barlovic model */
    private double pSlowToStart; 

    /**
     * Instantiates a new Nagel-Schreckenberg or Barlovic cellular automaton.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public NSM(LongitudinalModelInputDataNSM parameters) {
        super(ModelName.NSM, parameters);
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
        this.v0 = ((LongitudinalModelInputDataNSM) parameters).getV0();
        this.pSlowdown = ((LongitudinalModelInputDataNSM) parameters).getSlowdown();
        this.pSlowToStart = ((LongitudinalModelInputDataNSM) parameters).getSlowToStart();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double calcAcc(Vehicle me, LaneSegment vehContainer, double alphaT, double alphaV0, double alphaA) {
        // local dynamical variables
        final Vehicle vehFront = vehContainer.frontVehicle(me);
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);
        
        // consider external speedlimit
        final double localV0 = Math.min(alphaV0 * v0, me.getSpeedlimit() / me.physicalQuantities().getvScale());
        if (logger.isDebugEnabled()) {
            if (localV0 < v0) {
                logger.debug(String.format("CA v0=%.2f, localV0=%.2f, external speedlimit=%.2f, v-scaling=%.2f\n", v0,
                        localV0, me.getSpeedlimit(), me.physicalQuantities().getvScale()));
            }
        }

        return acc(s, v, dv, localV0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel#calcAcc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.Vehicle)
     */
    @Override
    public double calcAcc(final Vehicle me, final Vehicle vehFront) {
        // local dynamical variables
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);

        // consider external speedlimit
        final double localV0 = Math.min(v0, me.getSpeedlimit() / me.physicalQuantities().getvScale());
        if (logger.isDebugEnabled()) {
            if (localV0 < v0) {
                logger.debug(String.format("CA v0=%.2f, localV0=%.2f, external speedlimit=%.2f, v-scaling=%.2f\n", v0,
                        localV0, me.getSpeedlimit(), me.physicalQuantities().getvScale()));
            }
        }

        return acc(s, v, dv, localV0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel #accSimple(double, double, double)
     */
    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, v0);
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
     * @param localT
     *            the local T
     * @param localV0
     *            the local v0
     * @return the double
     */
    private double acc(double s, double v, double dv, double localV0) {
        final int localIntegerV0 = (int) (localV0 + 0.5);
        if (localIntegerV0 <= 0) {
            logger.warn(
                    "local desired speed localVO={} is mapped to CA integer v0={} probably due to a speed limit. Cannot move forward.",
                    localV0, localIntegerV0);
        }
        
        final int vLocal = (int) (v + 0.5);
        int vNew = 0;

        final double r1 = MyRandom.nextDouble();
        final double pb = (vLocal < 1) ? pSlowToStart : pSlowdown;
        final int slowdown = (r1 < pb) ? 1 : 0;

        final int sLoc = (int) (s + 0.5);
        vNew = Math.min(vLocal + 1, localIntegerV0);
        vNew = Math.min(vNew, sLoc);
        vNew = Math.max(0, vNew - slowdown);

        return ((vNew - vLocal) / dtCA);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.AccelerationModelAbstract#setDesiredSpeedV0(double)
     */
    @Override
    protected void setDesiredSpeedV0(double v0) {
        this.v0 = (int) v0;
    }

}
