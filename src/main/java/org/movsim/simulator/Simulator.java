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
package org.movsim.simulator;

import java.util.List;

import org.movsim.input.InputData;
import org.movsim.output.SimObservables;
import org.movsim.simulator.roadSection.RoadSection;

// TODO: Auto-generated Javadoc
/**
 * The Interface Simulator.
 */
public interface Simulator {

    /**
     * The total number of updates.
     * 
     * @return the long
     */
    long iterationCount();

    /**
     * Time.
     * 
     * @return the double
     */
    double time();

    /**
     * Timestep.
     * 
     * @return the double which represents the timestep of the next update.
     */
    double timestep();

    /**
     * Run.
     */
    void run();

    /**
     * Calls the update for the Roadsection and SimulationOutput
     */
    void update();
    
    boolean isSimulationRunFinished();

    /**
     * Restart.
     */
    void reset();

    /**
     * Gets the sim input. All simulation input data.
     * 
     * @return the sim input
     */
    InputData getSimInput();

    /**
     * Gets the sim observables. Floating cars, Virtual Detectors, SpatioTemporal. 
     * 
     * @return the sim observables
     */
    SimObservables getSimObservables();

    /**
     * Initializes from xml. Calls restart.
     */
    void initialize();
    
    List<RoadSection> getRoadSections();
    
    public RoadSection findRoadById(long id);
    
}
