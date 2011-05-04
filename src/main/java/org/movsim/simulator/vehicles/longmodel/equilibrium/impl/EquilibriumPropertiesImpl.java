/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
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

import java.io.PrintWriter;

import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumProperties;
import org.movsim.utilities.FileUtils;
import org.movsim.utilities.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class EquilibriumPropertiesImpl.
 */
public abstract class EquilibriumPropertiesImpl implements EquilibriumProperties {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(EquilibriumPropertiesImpl.class);

    /** The Constant NRHO. */
    final static int NRHO = 51; // time critical

    /** The rho max. */
    final double rhoMax;
    
    /** The length. */
    final double length;

    /** The q max. */
    double qMax;
    
    /** The rho q max. */
    double rhoQMax;

    /** The v eq tab. */
    double[] vEqTab;

    /**
     * Instantiates a new equilibrium properties impl.
     * 
     * @param length
     *            the length
     */
    public EquilibriumPropertiesImpl(double length) {
        this.length = length;
        vEqTab = new double[NRHO];
        rhoMax = 1. / length;
    }

    /**
     * Gets the q max.
     * 
     * @return the q max
     */
    public double getQMax() {
        return qMax;
    }

    /**
     * Gets the rho max.
     * 
     * @return the rho max
     */
    public double getRhoMax() {
        return rhoMax;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumProperties
     * #getRhoQMax()
     */
    @Override
    public double getRhoQMax() {
        return rhoQMax;
    }

    /**
     * Gets the net distance.
     * 
     * @param rho
     *            the rho
     * @return the net distance
     */
    protected double getNetDistance(double rho) {
        return rho != 0 ? (1. / rho - 1. / rhoMax) : 0;
    }

    // calculate Qmax, and abszissa rhoQmax from veqtab (necessary for BC)
    /**
     * Calc rho q max.
     */
    protected void calcRhoQMax() {
        int ir = 1;
        qMax = -1.;
        while (vEqTab[ir] * rhoMax * ir / vEqTab.length > qMax) {
            qMax = vEqTab[ir] * rhoMax * ir / vEqTab.length;
            ir++;
        }
        rhoQMax = rhoMax * ir / vEqTab.length;
        logger.debug("rhoQMax = {} = {}/km", rhoQMax, rhoQMax * 1000);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumProperties
     * #getVEq(double)
     */
    @Override
    public double getVEq(double rho) {
        return Tables.intp(vEqTab, rho, 0, rhoMax);
    }

    /**
     * Gets the rho.
     * 
     * @param i
     *            the i
     * @return the rho
     */
    protected double getRho(int i) {
        return rhoMax * i / (vEqTab.length - 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumProperties
     * #writeOutput(java.lang.String)
     */
    @Override
    public void writeOutput(String filename) {
        final PrintWriter fstr = FileUtils.getWriter(filename);
        fstr.printf(Constants.COMMENT_CHAR + " rho at max Q = %8.3f%n", 1000 * rhoQMax);
        fstr.printf(Constants.COMMENT_CHAR + " max Q        = %8.3f%n", 3600 * qMax);
        fstr.printf(Constants.COMMENT_CHAR + " rho[1/km],  s[m],vEq[km/h], Q[veh/h]%n");
        for (int i = 0; i < vEqTab.length; i++) {
            final double rho = getRho(i);
            final double s = getNetDistance(rho);
            fstr.printf("%8.2f, %8.2f, %8.2f, %8.2f%n", 1000 * rho, s, 3.6 * vEqTab[i], 3600 * rho * vEqTab[i]);
        }
        fstr.close();
    }

}
