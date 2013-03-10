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


// TODO: Auto-generated Javadoc
/**
 * The Class EquilibriumACC.
 */
class EquilibriumACC extends EquilibriumPropertiesImpl {

    /**
     * Instantiates a new equilibrium acc.
     * 
     * @param length
     *            the length
     * @param model
     *            the acc model
     */
    EquilibriumACC(double length, ACC model) {
        super(length);

        calcEquilibrium(model);
        calcRhoQMax();
    }

    // Calculates equilibrium velocity of IDM and IDM with finite s0
    // and free-acc exponent delta
    // uses numeric iteration procedure and
    // !! calculates THE WHOLE FIELD veq

    // output: Arhomax, Atab[], vfactab[] (for calc_rhs! vfactab only hdcorr)
    // veqtab[] (only needed for output),
    // Qmax, rhoQmax (only needed for BC),
    // rho_vtab, rho_freetab, rho_congtab (only needed for BC)

    // Find equilibrium velocities veqtab[ir] with simple relaxation
    // method: Just model for homogeneous traffic solved for
    // the velocity v_it of one arbitrary vehicle
    // (no brain, but stable and simple method...)

    /**
     * Calc equilibrium.
     * 
     * @param model
     *            the acc model
     */
    private void calcEquilibrium(ACC model) {

        double v0 = model.getDesiredSpeed();
        double vIter = v0; // variable of the relaxation equation
        final int itMax = 100; // number of iteration steps in each relaxation
        final double dtMax = 2; // iteration time step (in s) changes from
        final double dtMin = 0.01; // dtmin (rho=rhomax) to dtmax (rho=0)

        vEqTab[0] = v0; // start with rho=0
        final int length = vEqTab.length;

        for (int ir = 1; ir < length; ir++) {
            final double rho = getRho(ir);
            final double s = getNetDistance(rho);
            // start iteration with equilibrium velocity for the previous localDensity
            vIter = vEqTab[ir - 1];
            for (int it = 1; it <= itMax; it++) {
                final double acc = model.calcAccSimple(s, vIter, 0.);
                // interation step in [dtmin, dtmax]
                final double dtloc = dtMax * vIter / v0 + dtMin;
                // TODO: direkter source code
                // for (int it=1; it<=itmax; it++){
                // double dtloc = dtmax*v_it/v0 + dtmin; // it. step in
                // [dtmin,dtmax]
                // double sstar = s0 + T * v_it + s1*sqrt((v_it+0.000001)/v0);
                //
                // // acceleration for various variants
                //
                // double acc = (s>s0)
                // ? a * (1-pow(v_it/v0, 4) - sstar*sstar/(s*s) )
                // :0;

                // actual relaxation
                vIter += dtloc * acc;
                if ((vIter < 0) || (s < model.getMinimumGap())) {
                    vIter = 0;
                }
            }
            vEqTab[ir] = vIter;
        }
    }
}
