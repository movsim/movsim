/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.output;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.model.simulation.DetectorInput;
import org.movsim.output.fileoutput.FileDetector;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LoopDetectors.
 */
public class LoopDetectors {
    final static Logger logger = LoggerFactory.getLogger(LoopDetectors.class);

    /** The detectors. */
    private final List<LoopDetector> detectors;

    private List<FileDetector> fileDetectors;

    /**
     * Instantiates a new loop detectors.
     * 
     * @param input
     *            the input
     * @param lanes 
     */
    public LoopDetectors(long roadId, DetectorInput input, int laneCount) {

        detectors = new ArrayList<LoopDetector>();

        if (input.isWithDetectors()) {
            final double dtSample = input.getSampleInterval();

            final List<Double> positions = input.getPositions();

            for (final Double detPosition : positions) {
                detectors.add(new LoopDetector(detPosition, dtSample, laneCount));
            }

            if (input.isWithLogging()) {
                fileDetectors = new ArrayList<FileDetector>();
                for (final LoopDetector det : detectors) {
                    fileDetectors.add(new FileDetector(roadId, det, laneCount));
                }
            }
        }
    }

    /**
     * Update.
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     * @param iterationCount
     * @param roadSegment
     */
    public void update(double dt, double simulationTime, long iterationCount, RoadSegment roadSegment) {
        for (final LoopDetector detector : detectors) {
            detector.update(simulationTime, roadSegment);
        }
    }

    // for View
    /**
     * Gets the detectors.
     * 
     * @return the detectors
     */
    public List<LoopDetector> getDetectors() {
        final List<LoopDetector> loopDetectors = new ArrayList<LoopDetector>();
        for (final LoopDetector det : detectors) {
            loopDetectors.add(det);
        }
        return loopDetectors;
    }
}
