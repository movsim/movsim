/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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
package org.movsim.output.floatingcars;

import java.util.Collection;

import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FloatingCars.
 */
public class FloatingCars implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FloatingCars.class);

    private final Collection<Integer> floatingCarVehicleNumbers;
    private final int nDtOut;

    private final RoadNetwork roadNetwork;
    
    private final FileFloatingCars fileWriter;
    /**
     * Constructor.
     * 
     * @param roadSegment
     *            the road segment
     * @param input
     *            the input
     */
    public FloatingCars(FloatingCarInput input, RoadNetwork roadNetwork, boolean writeFileOutput) {
        this.roadNetwork = roadNetwork;
        this.nDtOut = input.getNDt();
        this.floatingCarVehicleNumbers = input.getFloatingCars();
        fileWriter = (writeFileOutput) ? new FileFloatingCars(this) : null;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (iterationCount % nDtOut == 0) {
            if(fileWriter != null){
                fileWriter.writeOutput(simulationTime, roadNetwork);
            }
            logger.debug("update FloatingCars: iterationCount={}", iterationCount);
        }
    }

    public Collection<Integer> getFloatingCarVehicleNumbers() {
        return floatingCarVehicleNumbers;
    }
}
