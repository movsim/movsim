/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

import org.movsim.autogen.DistributionTypeEnum;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterNSM;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class NSM: Nagel-Schreckenberg or Barlovic-Model
 * Parameters:
 * The p slowdown.
 * slow-to-start rule for Barlovic model
 */
class NSM extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger logger = LoggerFactory.getLogger(NSM.class);

    /** The constant unit time */
    private static final double dtCA = 1;

    private final IModelParameterNSM param;

    /**
     * Instantiates a new Nagel-Schreckenberg or Barlovic cellular automaton.
     * 
     * @param modelParameter
     */
    public NSM(IModelParameterNSM modelParameter) {
        super(ModelName.NSM);
        this.param = modelParameter;
    }

    @Override
    public void setRelativeRandomizationV0(double relRandomizationFactor, DistributionTypeEnum distributionType) {
        // no modification of desired speed by randomization.
    }

    @Override
    public double getMinimumGap() {
        throw new UnsupportedOperationException("minimum gap not applicable for NSM model.");
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {
        // local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        // consider external speedlimit
        final double localV0 = Math.min(alphaV0 * getDesiredSpeed(), me.getEffectiveSpeedlimit()
                / me.physicalQuantities().getvScale());
        if (logger.isDebugEnabled()) {
            if (localV0 < getDesiredSpeed()) {
                logger.debug(String.format("CA v0=%.2f, localV0=%.2f, external speedlimit=%.2f, v-scaling=%.2f\n",
                        getDesiredSpeed(), localV0, me.getEffectiveSpeedlimit(), me.physicalQuantities().getvScale()));
            }
        }

        return acc(s, v, dv, localV0);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, getDesiredSpeed());
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
     * @param localV0
     *            the local v0
     * @return the double
     */
    private double acc(double s, double v, double dv, double localV0) {
        final int localIntegerV0 = (int) (localV0 + 0.5);
        final int vLocal = (int) (v + 0.5);
        int vNew = 0;

        final double r1 = MyRandom.nextDouble();
        final double pb = (vLocal < 1) ? param.getPSlowStart() : param.getPSlowdown();
        final int slowdown = (r1 < pb) ? 1 : 0;

        final int sLoc = (int) (s + 0.5);
        vNew = Math.min(vLocal + 1, localIntegerV0);
        vNew = Math.min(vNew, sLoc);
        vNew = Math.max(0, vNew - slowdown);

        return (vNew - vLocal) / dtCA;
    }

    @Override
    protected IModelParameterNSM getParameter() {
        return param;
    }

}
