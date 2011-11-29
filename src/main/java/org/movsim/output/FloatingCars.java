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

import java.util.List;

import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.utilities.impl.ObservableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FloatingCarsImpl.
 */
public class FloatingCars extends ObservableImpl {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FloatingCars.class);

    private final List<Integer> fcdList;

    /** The n dt out. */
    private final int nDtOut;

    private final RoadSegment roadSegment;

    /**
     * Instantiates a new floating cars.
     * 
     * @param roadSegment
     *            the road segment
     * @param input
     *            the input
     */
    public FloatingCars(RoadSegment roadSegment, final FloatingCarInput input) {
        logger.debug("Cstr. FloatingCars");

        this.roadSegment = roadSegment;
        this.nDtOut = input.getNDt();

        this.fcdList = input.getFloatingCars();

    }

    /**
     * Update.
     * 
     * @param iterationCount
     *            the itime
     * @param time
     *            the time
     * @param timestep
     *            the timestep
     */
    public void update(long iterationCount, double time, double timestep) {

        if (iterationCount % nDtOut == 0) {
            notifyObservers(time);
            logger.debug("update FloatingCars: iterationCount={}", iterationCount);
        }
    }

    /**
     * Gets the fcd list.
     * 
     * @return the fcd list
     */
    public List<Integer> getFcdList() {
        return fcdList;
    }

    /**
     * Gets the road segment.
     * 
     * @return the road segment
     */
    public RoadSegment getRoadSegment() {
        return roadSegment;
    }
}
