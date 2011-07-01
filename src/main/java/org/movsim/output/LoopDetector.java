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
package org.movsim.output;

import org.movsim.utilities.ObservableInTime;

// TODO: Auto-generated Javadoc
/**
 * The Interface LoopDetector.
 */
public interface LoopDetector extends ObservableInTime {

    /**
     * Gets the det position.
     * 
     * @return the det position
     */
    double getDetPosition();

    /**
     * Gets the mean speed.
     * 
     * @return the mean speed
     */
    double getMeanSpeed();

    /**
     * Gets the density arithmetic.
     * 
     * @return the density arithmetic
     */
    double getDensityArithmetic();

    /**
     * Gets the flow.
     * 
     * @return the flow
     */
    double getFlow();

    /**
     * Gets the occupancy.
     * 
     * @return the occupancy
     */
    double getOccupancy();

    /**
     * Gets the veh count output.
     * 
     * @return the veh count output
     */
    int getVehCountOutput();

    /**
     * Gets the mean speed harmonic.
     * 
     * @return the mean speed harmonic
     */
    double getMeanSpeedHarmonic();

    /**
     * Gets the mean timegap harmonic.
     * 
     * @return the mean timegap harmonic
     */
    double getMeanTimegapHarmonic();

    /**
     * Gets the dt sample.
     * 
     * @return the dt sample
     */
    double getDtSample();
}