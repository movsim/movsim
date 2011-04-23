/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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
package org.movsim.output.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.model.output.DetectorInput;
import org.movsim.output.LoopDetector;
import org.movsim.simulator.vehicles.VehicleContainer;

// TODO: Auto-generated Javadoc
/**
 * The Class LoopDetectors.
 */
public class LoopDetectors {
    // final static Logger logger =
    // LoggerFactory.getLogger(LoopDetectors.class);

    private List<LoopDetector> detectors = new ArrayList<LoopDetector>();

    /**
     * Instantiates a new loop detectors.
     * 
     * @param projectName
     *            the project name
     * @param writeOutput
     *            the write output
     * @param input
     *            the input
     */
    public LoopDetectors(String projectName, boolean writeOutput, DetectorInput input) {

        detectors = new ArrayList<LoopDetector>();

        final double dtSample = input.getSampleInterval();
        final List<Double> positions = input.getPositions();

        for (final Double detPosition : positions) {
            detectors.add(new LoopDetectorImpl(projectName, writeOutput, detPosition, dtSample));
        }

    }

    /**
     * Update.
     * 
     * @param itime
     *            the itime
     * @param time
     *            the time
     * @param timestep
     *            the timestep
     * @param vehContainer
     *            the veh container
     */
    public void update(int itime, double time, double timestep, VehicleContainer vehContainer) {
        for (final LoopDetector det : detectors) {
            det.update(time, vehContainer);
        }
    }

    /**
     * Close files.
     */
    public void closeFiles() {
        for (final LoopDetector det : detectors) {
            det.closeFiles();
        }
    }

}
