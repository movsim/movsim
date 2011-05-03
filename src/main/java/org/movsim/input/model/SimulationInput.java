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
package org.movsim.input.model;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Interface SimulationInput.
 */
public interface SimulationInput {

    /**
     * Gets the timestep.
     * 
     * @return the timestep
     */
    double getTimestep();

    /**
     * Gets the max simulation time.
     * 
     * @return the max simulation time
     */
    double getMaxSimulationTime();

    /**
     * Checks if is with fixed seed.
     * 
     * @return true, if is with fixed seed
     */
    boolean isWithFixedSeed();

    /**
     * Gets the random seed.
     * 
     * @return the random seed
     */
    int getRandomSeed();

    /**
     * Gets the road input.
     * 
     * @return the road input
     */
    ArrayList<RoadInput> getRoadInput();

    // quick hack: only one single main road
    /**
     * Gets the single road input.
     * 
     * @return the single road input
     */
    RoadInput getSingleRoadInput();

}