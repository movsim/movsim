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
package org.movsim.simulator.roadnetwork;

import java.util.List;

import org.movsim.autogen.MacroIC;
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.Tables;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The Class InitialConditionsMacro.
 */
public class InitialConditionsMacro {

    private static final Logger LOG = LoggerFactory.getLogger(InitialConditionsMacro.class);

    /** the positions along the road segment in m */
    double[] pos;

    /** The density profile in 1/m */
    double[] rho;

    /** the speeds along the road segment in m/s. Only initialized when initial speeds are provided. */
    double[] speed;

    /**
     * Instantiates a new initial conditions macro.
     * 
     * @param icData
     *            the ic data
     */
    public InitialConditionsMacro(List<MacroIC> macroIC) {

        final int size = macroIC.size();

        pos = new double[size];
        rho = new double[size];
        if (useUserDefinedSpeeds(macroIC)) {
            speed = new double[size];
        }
        // case speed = 0 --> set vehicle ast equilibrium speed

        // generateMacroFields: rho guaranteed to be > RHOMIN, v to be < VMAX

        for (int i = 0; i < size; i++) {
            MacroIC localMacroIC = macroIC.get(i);
            final double rhoLocal = localMacroIC.getDensityPerKm() * Units.INVKM_TO_INVM;
            if (rhoLocal > MovsimConstants.SMALL_VALUE) {
                pos[i] = localMacroIC.getPosition();
                rho[i] = rhoLocal;
                if (hasUserDefinedSpeeds()) {
                    speed[i] = Math.min(localMacroIC.getSpeed(), MovsimConstants.MAX_VEHICLE_SPEED);
                    LOG.debug("speed={}", speed[i]);
                }
            }
        }
    }

    private static boolean useUserDefinedSpeeds(List<MacroIC> macroIC) {
        boolean userDefinedSpeed = true;
        for (int i = 0, N = macroIC.size(); i < N; i++) {
            if (i == 0) {
                // set initial value
                userDefinedSpeed = macroIC.get(i).isSetSpeed();
            }
            if (macroIC.get(i).isSetSpeed() != userDefinedSpeed) {
                throw new IllegalArgumentException(
                        "decide whether equilibrium speed or user-defined speeds should be used. Do not mix the speed input!");
            }
        }
        return userDefinedSpeed;
    }

    public boolean hasUserDefinedSpeeds() {
        return speed != null;
    }

    /**
     * initial speed.
     * 
     * @param x
     *            the x
     * @return the double
     */
    public double vInit(double x) {
        Preconditions.checkNotNull(speed, "expected usage of equilibrium speeds, check with hasUserDefinedSpeeds");
        return Tables.intpextp(pos, speed, x);
    }

    /**
     * Density
     * 
     * @param x
     *            the x
     * @return the double
     */
    public double rho(double x) {
        return Tables.intpextp(pos, rho, x);
    }
}
