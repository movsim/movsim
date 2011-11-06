/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.simulator;

/**
 * TimeStep interface used to run iteration.
 * 
 */
public interface SimulationTimeStep {
    /**
     * Perform a single timestep.
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations executed
     */
    public void timeStep(double dt, double simulationTime, long iterationCount);
}
