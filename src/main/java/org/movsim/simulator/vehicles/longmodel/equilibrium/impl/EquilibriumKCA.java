/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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
package org.movsim.simulator.vehicles.longmodel.equilibrium.impl;

import org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.KCA;

// TODO: Auto-generated Javadoc
/**
 * The Class EquilibriumKCA.
 */
public class EquilibriumKCA extends EquilibriumPropertiesImpl {

    /**
     * Instantiates a new equilibrium kca.
     * 
     * @param length
     *            the length
     * @param kcaModel
     *            the kca model
     */
    public EquilibriumKCA(double length, KCA kcaModel) {
        super(length);

        calcEquilibrium(kcaModel);
        calcRhoQMax();

    }

    /**
     * Calc equilibrium.
     * 
     * @param model
     *            the model
     */
    private void calcEquilibrium(KCA model) {
        double vIter = model.getV0(); // variable of the relaxation equation
        final int itMax = 100; // number of iteration steps in each relaxation
        final double dtMax = 2; // iteration time step (in s) changes from
        final double dtMin = 0.01; // dtmin (rho=rhomax) to dtmax (rho=0)

        vEqTab[0] = model.getV0(); // start with rho=0
        final int length = vEqTab.length;
        for (int ir = 1; ir < length; ir++) {
            final double rho = getRho(ir);
            final double s = getNetDistance(rho);
            // start iteration with equilibrium velocity for the previous
            // density
            vIter = vEqTab[ir - 1];
            for (int it = 1; it <= itMax; it++) {
                final double acc = model.accSimple(s, vIter, 0.);
                final double dtloc = dtMax * vIter / model.getV0() + dtMin; // it.
                                                                            // step
                                                                            // in
                                                                            // [dtmin,dtmax]
                // actual relaxation
                vIter += dtloc * acc;
                if (vIter < 0) {
                    vIter = 0;
                }
            }
            vEqTab[ir] = vIter;
        }

    }

}
