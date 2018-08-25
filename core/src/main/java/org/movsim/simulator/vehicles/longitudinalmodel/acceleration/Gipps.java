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
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterGipps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// paper reference and modifications ...

/**
 * The Class Gipps.
 * 
 * 
 * Model parameters:
 * <ul>
 * <li>T, results from update timestep dt dt = T = Tr = tau_relax</li>
 * <li>a</li>
 * <li>b</li>
 * </ul>
 */
class Gipps extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Gipps.class);

    private final IModelParameterGipps param;

    private final double parameterT;

    Gipps(double simulationTimestep, IModelParameterGipps modelParameter) {
        super(ModelName.GIPPS);
        this.parameterT = simulationTimestep;
        this.param = modelParameter;
    }

    @Override
    public void setRelativeRandomizationV0(double relRandomizationFactor, DistributionTypeEnum distributionType) {
        // no modification of desired speed by randomization.
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        // space dependencies modeled by speedlimits, alpha's

        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * getDesiredSpeed(), me.getEffectiveSpeedlimit());

        // #############################################################
        // space dependencies modelled by alpha_T
        // (!!! watch for alpha_T: dt unchanged, possibly inconsistent!)
        // #############################################################

        final double TLocal = alphaT * parameterT;

        // actual Gipps formula
        return acc(s, v, dv, v0Local, TLocal);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, getDesiredSpeed(), parameterT);
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
     * @param v0Local
     *            the v0 local
     * @param TLocal
     *            the t local
     * @return the double
     */
    private double acc(double s, double v, double dv, double v0Local, double TLocal) {
        final double vp = v - dv;
        // safe speed
        final double a = param.getA();
        final double b = param.getB();
        final double vSafe = -b * TLocal
                + Math.sqrt(b * b * TLocal * TLocal + vp * vp + 2 * b * Math.max(s - getMinimumGap(), 0.));
        final double vNew = Math.min(vSafe, Math.min(v + a * TLocal, v0Local));
        final double aWanted = (vNew - v) / TLocal;
        return aWanted;
    }

    @Override
    protected IModelParameterGipps getParameter() {
        return param;
    }

    protected double getParameterT() {
        return parameterT;
    }

}
