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

import org.movsim.autogen.ModelParameterOVMFVDM;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase.ModelName;
import org.movsim.utilities.LinearInterpolatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The Class EquilibriumPropertiesImpl.
 */
public class EquilibriumPropertiesImpl implements EquilibriumProperties {

    private static final double TINY_VALUE = 0.0001;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EquilibriumPropertiesImpl.class);

    /**
     * Discretization steps for tabulated function. Time-critical.
     */
    private static final int NRHO = 51;

    /**
     * The maximum density
     */
    private final double rhoMax;

    /**
     * The maximum equilibrium flow
     */
    double qMax = 0;

    /**
     * The density at maximum flow
     */
    double rhoQMax = 0;

    private final LinearInterpolatedFunction vEqFunction;

    public EquilibriumPropertiesImpl(double vehicleLength, LongitudinalModelBase model) {
        this.rhoMax = 1.0 / Math.max(vehicleLength, TINY_VALUE);
        if (vehicleLength < TINY_VALUE) {
            LOG.warn("vehicle length is artificially small={}, assume finite length {}", vehicleLength, TINY_VALUE);
        }

        if (model.hasDesiredSpeed()) {
            vEqFunction = calcEquilibriumSpeedFunction(model);
            calcRhoQMax();
        } else {
            double[] xDummy = new double[]{0};
            vEqFunction = new LinearInterpolatedFunction(xDummy, xDummy);
        }
    }

    @Override
    public double getQMax() {
        return qMax;
    }

    @Override
    public double getRhoMax() {
        return rhoMax;
    }

    @Override
    public double getRhoQMax() {
        return rhoQMax;
    }

    @Override
    public double getNetDistance(double rho) {
        return rho != 0.0 ? (1. / rho - 1. / rhoMax) : 0;
    }

    @Override
    public double getVEq(double rho) {
        // return Tables.intp(vEqTab, Math.min(rho, rhoMax), 0, rhoMax);
        return vEqFunction.value(rho);
    }

    @Override
    public double getRho(int i) {
        return rhoMax * i / (NRHO - 1.);
    }

    @Override
    public int getVEqCount() {
        return vEqFunction.getNumberOfDataPoints();
    }

    /**
     * Calculates equilibrium velocity {@literal vEq} as a function of the density {@literal rho}.
     * <p>
     * <p>
     * Finds equilibrium velocities with simple relaxation method: Model for homogeneous traffic solved for the velocity v_it of one
     * arbitrary vehicle.
     */
    private LinearInterpolatedFunction calcEquilibriumSpeedFunction(LongitudinalModelBase model) {
        LOG.info("calc equilibrium speed as function of density for model={}", model.modelName());
        if (!model.hasDesiredSpeed()) {
            throw new IllegalArgumentException("longitudinal model " + model.modelName()
                    + " has no desired speed; vEq(rho) cannot be calculated ");
        }

        double v0 = model.getDesiredSpeed();
        if (v0 < TINY_VALUE) {
            LOG.warn("desired speed is artifically small for model={}, assume finite value={}", model.modelName(),
                    TINY_VALUE);
        }
        double vIteration = v0; // variable of the relaxation equation
        final int itMax = 100; // number of iteration steps in each relaxation
        double dtMax = 2; // iteration time step (in s) changes from
        double dtMin = 0.01; // dtmin (rho=rhomax) to dtmax (rho=0)

        if (model.modelName == ModelName.OVM_FVDM) {
            ModelParameterOVMFVDM parameter = (ModelParameterOVMFVDM) model.getParameter();
            dtMax = 0.3 * parameter.getTau();
            dtMin = 0.1 * parameter.getTau();
        }

        double[] vEqTab = new double[NRHO];
        double[] rhoTab = new double[NRHO];

        vEqTab[0] = v0; // start with rho=0
        rhoTab[0] = 0;
        for (int ir = 1; ir < vEqTab.length; ir++) {
            final double rho = getRho(ir);
            final double s = getNetDistance(rho);
            // start iteration with equilibrium velocity for the previous localDensity
            vIteration = vEqTab[ir - 1];
            for (int it = 1; it <= itMax; it++) {
                final double acc = model.calcAccSimple(s, vIteration, 0.);
                // integration step in [dtmin, dtmax]
                final double dtLocal = dtMax * vIteration / Math.max(v0, TINY_VALUE) + dtMin;
                // actual relaxation
                vIteration += dtLocal * acc;
                if ((vIteration < 0) || (model.hasMinimumGap() && s < model.getMinimumGap())) {
                    vIteration = 0;
                }
            }
            vEqTab[ir] = vIteration;
            rhoTab[ir] = rho;
        }

        return new LinearInterpolatedFunction(rhoTab, vEqTab);
    }

    // calculate Qmax, and abscissa rhoQmax from veqtab
    private void calcRhoQMax() {
        Preconditions.checkNotNull(vEqFunction, "first calc equlibrium funcion vEq");
        final double incr = rhoMax / (vEqFunction.getNumberOfDataPoints() - 1);
        qMax = -1.;
        double rho = 0;
        while (vEqFunction.value(rho) * rho > qMax) {
            qMax = vEqFunction.value(rho) * rho;
            rho += incr;
        }
        rhoQMax = rho - incr;
        LOG.info("rhoQMax = {}/km, qMax={}/h", rhoQMax * 1000, qMax * 3600);
    }

}
