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

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.model.simulation.DetectorInput;
import org.movsim.output.fileoutput.FileDetector;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class LoopDetectors.
 */
public class LoopDetectors {
    final static Logger logger = LoggerFactory.getLogger(LoopDetectors.class);

    /** The detectors. */
    private List<LoopDetector> detectors;

    private List<FileDetector> fileDetectors;

    /**
     * Instantiates a new loop detectors.
     * 
     * @param projectName
     *            the project name
     * @param input
     *            the input
     */
    public LoopDetectors(long roadId, String projectName, DetectorInput input) {

        detectors = new ArrayList<LoopDetector>();

        final double dtSample = input.getSampleInterval();

        final List<Double> positions = input.getPositions();

        for (final Double detPosition : positions) {
            detectors.add(new LoopDetector(projectName, detPosition, dtSample));
        }

        if (input.isWithLogging()) {
            fileDetectors = new ArrayList<FileDetector>();
            for (final LoopDetector det : detectors) {
                fileDetectors.add(new FileDetector(roadId, projectName, det));
            }
        }

    }

    /**
     * Update.
     *
     * @param itime the itime
     * @param time the time
     * @param timestep the timestep
     * @param roadSegment
     */
    public void update(long itime, double time, double timestep, RoadSegment roadSegment) {
        for (final LoopDetector det : detectors) {
            det.update(time, roadSegment);
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
