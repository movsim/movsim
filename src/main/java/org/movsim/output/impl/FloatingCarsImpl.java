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
package org.movsim.output.impl;

import java.util.List;

import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.output.FloatingCars;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.utilities.impl.ObservableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FloatingCarsImpl.
 */
public class FloatingCarsImpl extends ObservableImpl implements FloatingCars {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FloatingCarsImpl.class);

    private List<Integer> fcdList;

    /** The n dt out. */
    private final int nDtOut;

    private RoadSegment roadSegment;

    /**
     * Instantiates a new floating cars impl.
     *
     * @param roadSegment the road segment
     * @param input the input
     */
    public FloatingCarsImpl(RoadSegment roadSegment, final FloatingCarInput input) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.output.FloatingCars#getFcdList()
     */
    @Override
    public List<Integer> getFcdList() {
        return fcdList;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public RoadSegment getRoadSegment(){
        return roadSegment;
    }
    
    /* (non-Javadoc)
     * @see org.movsim.output.FloatingCars#getMoveableContainer()
     */
//    @Deprecated
//    public MoveableContainer getMoveableContainer(){
//        return vehContainers.get(MovsimConstants.MOST_RIGHT_LANE);
//    }
}
