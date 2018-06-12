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
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterKKW;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class KKW.
 * 
 * The k. Multiplikator fuer sync-Abstand D=lveh+k*v*tau
 * The pb0. "Troedelwahrsch." for standing vehicles
 * The pb1. "Troedelwahrsch." for moving vehicles
 * The pa1. "Beschl.=Anti-Troedelwahrsch." falls v &lt; vp
 * The pa2. "Beschl.=Anti-Troedelwahrsch." falls v &gt;= vp
 * The vp. Geschw., ab der weniger "anti-getroedelt" wird
 */
class KKW extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(KKW.class);

    private final IModelParameterKKW param;

    /**
     * The Constant dtCA. constant update timestep for CA
     */
    private static final double dtCA = 1;

    /** The vehicle length. */
    private final double length;

    /**
     * Instantiates a new KKW model
     * 
     * @param modelParameter
     * @param vehLength
     */
    public KKW(IModelParameterKKW modelParameter, double vehLength) {
        super(ModelName.KKW);
        this.length = vehLength; // model parameter!
        this.param = modelParameter;
    }

    @Override
    public void setRelativeRandomizationV0(double relRandomizationFactor, DistributionTypeEnum distributionType) {
        // no modification of desired speed by randomization.
    }

    @Override
    public double getMinimumGap() {
        throw new UnsupportedOperationException("getS0 not applicable for KKW model.");
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {
        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        return acc(s, v, dv, alphaT, alphaV0);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, 1.0, 1.0);
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

        final int v0Loc = (int) (alphaV0 * getDesiredSpeed() + 0.5); // adapt v0 spatially
        final int vLoc = (int) (v + 0.5);

        final double kLoc = alphaT * param.getK();
        // cell length/dt^2 with dt=1 s and length 0.5 m => 0.5 m/s^2
        final int a = 1;

        final double pa = (vLoc < param.getVp()) ? param.getPa1() : param.getPa2();
        final double pb = (vLoc < 1) ? param.getPb0() : param.getPb1();
        // double in Kerner's book since k is float
        final double D = length + kLoc * vLoc * dtCA;

        // dynamic part: (Delta x-d)/tau mit s=Delta x-d und tau=1 (s)
        final int vSafe = (int) s;
        final int dvSign = (dv < -0.5) ? 1 : (dv > 0.5) ? -1 : 0;
        final int vC = (s > D - length) ? vLoc + a * (int) dtCA : vLoc + a * (int) dtCA * dvSign;
        int vtilde = Math.min(Math.min(v0Loc, vSafe), vC);
        vtilde = Math.max(0, vtilde);

        // stochastic part
        final double r1 = MyRandom.nextDouble(); // noise terms ~ G(0,1)
        final int xi = (r1 < pb) ? -1 : (r1 < pb + pa) ? 1 : 0;

        int vNew = 0;
        vNew = Math.min(vtilde + a * (int) dtCA * xi, vLoc + a * (int) dtCA);
        vNew = Math.min(Math.min(v0Loc, vSafe), vNew);
        vNew = Math.max(0, vNew);

        return ((vNew - vLoc) / dtCA);
    }

    @Override
    protected IModelParameterKKW getParameter() {
        return param;
    }

}
