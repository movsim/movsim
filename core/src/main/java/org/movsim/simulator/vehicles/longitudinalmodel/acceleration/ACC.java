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

import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterACC;
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
 * 
 * <p>
 * See {@link IModelParameterACC} for the model parameters.
 * </p>
 */
class ACC extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger logger = LoggerFactory.getLogger(ACC.class);

    private final IModelParameterACC param;

    public ACC(IModelParameterACC modelParameter) {
        super(ModelName.ACC);
        this.param = modelParameter;
    }

    @Override
    protected IModelParameterACC getParameter() {
        return param;
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        final double aLead = frontVehicle == null ? me.getAcc() : frontVehicle.getAcc();

        // space dependencies modeled by speedlimits, alpha's

        final double Tlocal = alphaT * param.getT();
        // if(alphaT!=1){
        // System.out.printf("calcAcc: pos=%.2f, speed=%.2f, alphaT=%.3f, alphaV0=%.3f, T=%.3f, Tlocal=%.3f \n",
        // me.getPosition(), me.getSpeed(), alphaT, alphaV0, T, Tlocal);
        // }
        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * getDesiredSpeed(), me.getSpeedlimit());
        final double aLocal = alphaA * param.getA();

        return acc(s, v, dv, aLead, Tlocal, v0Local, aLocal);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, 0, param.getT(), getDesiredSpeed(), param.getA());
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

        final double sstar = getMinimumGap()
                + Math.max(
                        TLocal * v + param.getS1() * Math.sqrt((v + 0.00001) / v0Local) + 0.5 * v * dv
                                / Math.sqrt(aLocal * param.getB()), 0.);
        final double z = sstar / Math.max(s, 0.01);
        final double accEmpty = (v <= v0Local) ? aLocal * (1 - Math.pow((v / v0Local), param.getDelta())) : -param
                .getB() * (1 - Math.pow((v0Local / v), aLocal * param.getDelta() / param.getB()));
        final double accPos = accEmpty * (1. - Math.pow(z, Math.min(2 * aLocal / accEmpty, 100.)));
        final double accInt = aLocal * (1 - z * z);

        final double accIIDM = (v < v0Local) ? (z < 1) ? accPos : accInt : (z < 1) ? accEmpty : accInt + accEmpty;

        // constant-acceleration heuristic (CAH)

        final double aLeadRestricted = Math.min(aLead, aLocal);
        final double dvp = Math.max(dv, 0.0);
        final double vLead = v - dvp;
        final double denomCAH = vLead * vLead - 2 * s * aLeadRestricted;

        final double accCAH = ((vLead * dvp < -2 * s * aLeadRestricted) && (denomCAH != 0)) ? v * v * aLeadRestricted
                / denomCAH : aLeadRestricted - 0.5 * dvp * dvp / Math.max(s, 0.0001);

        // ACC with IIDM

        final double accACC_IIDM = (accIIDM > accCAH) ? accIIDM : (1 - param.getCoolness()) * accIIDM
                + param.getCoolness() * (accCAH + param.getB() * Math.tanh((accIIDM - accCAH) / param.getB()));

        return accACC_IIDM;
    }
}
