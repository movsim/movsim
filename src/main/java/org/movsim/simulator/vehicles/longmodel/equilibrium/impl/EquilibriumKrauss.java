/**
 * 
 * Copyright (C) 2010 by Ralph Germ (http://www.ralphgerm.de)
 * 
 */
package org.movsim.simulator.vehicles.longmodel.equilibrium.impl;

import org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.Gipps;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.Krauss;

/**
 * @author ralph
 *
 */
public class EquilibriumKrauss extends EquilibriumPropertiesImpl{
    /**
     * Instantiates a new equilibrium gipps.
     * 
     * @param length
     *            the length
     * @param gippsModel
     *            the gipps model
     */
    public EquilibriumKrauss(double length, Krauss model) {
        super(length);

        calcEquilibrium(model);
        calcRhoQMax();

    }

    // Calculates equilibrium velocity of Gipps and Gipps with finite s0
    // and free-acc exponent delta
    // uses numeric iteration procedure

    /**
     * Calc equilibrium.
     * 
     * @param model
     *            the gipps model
     */
    private void calcEquilibrium(Krauss model) {

        // Find equilibrium velocities veqtab[ir] with simple relaxation
        // method: Just model for homogeneous traffic solved for
        // the velocity v_it of one arbitrary vehicle
        // (no brain, but stable and simple method...)

        double v_it = model.getV0(); // variable of the relaxation equation
        final int itmax = 100; // number of iteration steps in each relaxation
        final double dtmax = 2; // iteration time step (in s) changes from
        final double dtmin = 0.01; // dtmin (rho=rhomax) to dtmax (rho=0)

        // start with rho=0
        vEqTab[0] = model.getV0();

        for (int ir = 1; ir < vEqTab.length; ir++) {
            final double rho = rhoMax * ir / vEqTab.length;
            final double s = 1. / rho - 1. / rhoMax;

            // start iteration with equilibrium speed for previous density
            v_it = vEqTab[ir - 1];

            for (int it = 1; it <= itmax; it++) {
                final double acc = model.accSimple(s, v_it, 0.);
                // iteration step in [dtmin,dtmax]
                final double dtloc = dtmax * v_it / model.getV0() + dtmin;

                // actual relaxation
                v_it += dtloc * acc;
                if (v_it < 0) {
                    v_it = 0;
                }

            }
            vEqTab[ir] = v_it;

        }
    }
}
