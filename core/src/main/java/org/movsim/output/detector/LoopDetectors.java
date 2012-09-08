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
package org.movsim.output.detector;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.model.simulation.DetectorInput;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LoopDetectors.
 */
public class LoopDetectors implements SimulationTimeStep {
    final static Logger logger = LoggerFactory.getLogger(LoopDetectors.class);

    /** The detectors. */
    private final List<LoopDetector> detectors;

    /**
     * Constructor.
     * 
     * @param input
     *            the input
     */
    public LoopDetectors(RoadSegment roadSegment, DetectorInput input) {

        detectors = new ArrayList<LoopDetector>();

        if (input.isWithDetectors()) {
            final double dtSample = input.getSampleInterval();
            final List<Double> positions = input.getPositions();
            for (final Double detPosition : positions) {
                detectors.add(new LoopDetector(roadSegment, detPosition, dtSample, input.isWithLogging()));
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        for (final LoopDetector detector : detectors) {
            detector.timeStep(dt, simulationTime, iterationCount);
        }
    }

}
