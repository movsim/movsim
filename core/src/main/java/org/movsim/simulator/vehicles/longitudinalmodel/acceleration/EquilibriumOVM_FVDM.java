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


/**
 * The Class EquilibriumOVM_FVDM.
 */
class EquilibriumOVM_FVDM extends EquilibriumPropertiesImpl {

    /**
     * Instantiates a new equilibrium OVM or FVDM .
     * 
     * @param length
     *            the length
     * @param vDiffModel
     *            the velocity difference model
     */
    EquilibriumOVM_FVDM(double length, OVM_FVDM vDiffModel) {
        super(length);
        calcEquilibrium(vDiffModel);
        calcRhoQMax();
    }

    /**
     * Calc equilibrium.
     * 
     * @param model
     *            the model
     */
    private void calcEquilibrium(OVM_FVDM model) {

        double vIterate = model.getDesiredSpeed(); // variable of the relaxation equation
        final int iterMax = 100; // number of iteration steps in each relaxation
        final double dtMax = 0.3 * model.getParameter().getTau(); // iteration time step (in s) changes from
        final double dtMin = 0.1 * model.getParameter().getTau(); // dtmin (rho=rhoMax) to dtMax (rho=0)

        vEqTab[0] = model.getDesiredSpeed(); // start with rho=0

        final int length = vEqTab.length;
        for (int ir = 1; ir < length; ir++) {

            final double rho = getRho(ir);
            final double s = getNetDistance(rho);

            // start iteration with equilibrium velocity for the previous localDensity
            vIterate = vEqTab[ir - 1];
            for (int it = 1; it <= iterMax; it++) {
                final double acc = model.calcAccSimple(s, vIterate, 0);
                // interation step in [dtmin, dtmax]
                final double dtLoc = dtMax * vIterate / model.getDesiredSpeed() + dtMin;
                // actual relaxation
                vIterate += dtLoc * acc;
                if ((vIterate < 0) || (s < model.getMinimumGap())) {
                    vIterate = 0;
                }
            }
            vEqTab[ir] = vIterate;
        }
    }
}
