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

import java.util.List;
import java.util.Map;

import org.movsim.input.model.simulation.TrafficCompositionInputData;

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
     * Gets the duration of the simulation.
     * 
     * @return the max simulation time
     */
    double getMaxSimTime();

    /**
     * Checks if is with fixed seed.
     * 
     * @return true, if is with fixed seed
     */
    boolean isWithFixedSeed();

    /**
     * Checks if is with crash exit.
     * 
     * @return true, if is with crash exit
     */
    boolean isWithCrashExit();

    /**
     * Gets the random seed.
     * 
     * @return the random seed
     */
    int getRandomSeed();
    
    
    /**
     * Checks if is with write fundamental diagrams.
     * 
     * @return true, if is with write fundamental diagrams
     */
    boolean isWithWriteFundamentalDiagrams();

    /**
     * Gets the heterogeneity input data.
     * 
     * @return the heterogeneity input data
     */
    List<TrafficCompositionInputData> getTrafficCompositionInputData();


    //ArrayList<RoadInput> getRoadInput();
    Map<Long, RoadInput> getRoadInput();
    

    /**
     * Gets the single road input. Quick hack: only one single main road
     * 
     * @return the single road input
     */
    RoadInput getSingleRoadInput();

    /**
     * Gets the output input.
     * 
     * @return the output input
     */
    OutputInput getOutputInput();

}