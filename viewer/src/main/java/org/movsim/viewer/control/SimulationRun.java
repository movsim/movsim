/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                             <movsim.org@gmail.com>
 * ---------------------------------------------------------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MovSim.
 *  If not, see <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 *  
 * ---------------------------------------------------------------------------------------------------------------------
 */

package org.movsim.viewer.control;

import org.movsim.facades.MovsimViewerFacade;
import org.movsim.simulator.Simulator;

public class SimulationRun {

    protected long totalSimulationTime;
    protected final Simulator simulator;

    /**
     * Constructor, sets the simulation object.
     * 
     * @param movsimSimulatorCore
     */
    public SimulationRun() {
        this.simulator = MovsimViewerFacade.getInstance().getSimulator();
        assert simulator != null;
    }

    /**
     * Returns the simulation timestep object.
     * 
     * @return the simulation timestep object
     */
    public final Simulator getSimulation() {
        return simulator;
    }

    /**
     * Returns the simulation timestep
     * 
     * @return simulation timestep in seconds
     */
    public final double timeStep() {
        return simulator.timestep();
    }

    /**
     * Returns the number of iterations executed.
     * 
     * @return number of iterations
     */
    public final long iterationCount() {
        return simulator.iterationCount();
    }

    /**
     * <p>
     * Returns the time for which the simulation has been running.
     * </p>
     * 
     * <p>
     * This is the logical time in the simulation (that is the sum of the deltaTs), not the amount of real time that has been required to do
     * the simulation calculations.
     * </p>
     * 
     * @return the simulation time
     */
    public final double simulationTime() {
        return simulator.time();
    }

    /**
     * Returns the total execution time of the simulation. Useful for order of magnitude benchmarking.
     * 
     * @return total execution time of the simulation
     */
    public final long totalSimulationTime() {
        return totalSimulationTime;
    }

    /**
     * Resets the simulation instrumentation data.
     */
    public void reset() {
        totalSimulationTime = 0;
    }

}
