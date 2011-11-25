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
package org.movsim.simulator.roadnetwork;

import java.util.List;

import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.impl.Tables;

/**
 * The Class InitialConditionsMacro.
 */
public class InitialConditionsMacro {

    // final static double SMALL_VAL = 1e-7;

    /** The pos. */
    double[] pos;

    /** The rho. */
    double[] rho;

    /** The speed. */
    double[] speed;

    /**
     * Instantiates a new initial conditions macro impl.
     * 
     * @param icData
     *            the ic data
     */
    public InitialConditionsMacro(List<ICMacroData> icData) {

        final int size = icData.size();

        pos = new double[size];
        rho = new double[size];
        speed = new double[size];

        // case speed = 0 --> set vehicle ast equilibrium speed

        // generateMacroFields: rho guaranteed to be > RHOMIN, v to be < VMAX

        for (int i = 0; i < size; i++) {
            final double rhoLocal = icData.get(i).getRho();
            if (rhoLocal > MovsimConstants.SMALL_VALUE) {
                pos[i] = icData.get(i).getX();
                rho[i] = rhoLocal;
                final double speedLocal = icData.get(i).getSpeed();
                speed[i] = (speedLocal <= MovsimConstants.MAX_VEHICLE_SPEED) ? speedLocal : 0;
            }
        }
    }

    /**
     * V init.
     * 
     * @param x
     *            the x
     * @return the double
     */
    public double vInit(double x) {
        return Tables.intpextp(pos, speed, x);
    }

    /**
     * Rho.
     * 
     * @param x
     *            the x
     * @return the double
     */
    public double rho(double x) {
        return Tables.intpextp(pos, rho, x);
    }
}
