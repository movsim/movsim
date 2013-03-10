package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;


class EquilibriumCCS extends EquilibriumPropertiesImpl {

    public EquilibriumCCS(double length, CCS model) {
        super(length);

//        calcEquilibrium(model);
//        calcRhoQMax();
    }

    /**
     * Calc equilibrium.
     * 
     * @param model
     *            the model
     */
    private void calcEquilibrium(CCS model) {
        // Find equilibrium velocities vEqTab[ir] with simple relaxation
        // method: Just model for homogeneous traffic solved for
        // the velocity v_it of one arbitrary vehicle
        // (no brain, but stable and simple method...)

        double vIter = model.getDesiredSpeed(); // variable of the relaxation equation
        final int itMax = 100; // number of iteration steps in each relaxation
        final double dtMax = 2; // iteration time step (in s) changes from
        final double dtMin = 0.01; // dtmin (rho=rhomax) to dtmax (rho=0)

        vEqTab[0] = model.getDesiredSpeed(); // start with rho=0
        final int length = vEqTab.length;

        for (int ir = 1; ir < length; ir++) {
            final double rho = getRho(ir);
            final double s = getNetDistance(rho);
            // start iteration with equilibrium velocity for the previous localDensity
            vIter = vEqTab[ir - 1];
            for (int it = 1; it <= itMax; it++) {
                final double acc = model.calcAccSimple(s, vIter, 0.);
                // interation step in [dtmin, dtmax]
                final double dtloc = dtMax * vIter / model.getDesiredSpeed() + dtMin;
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
