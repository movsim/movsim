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
import org.movsim.simulator.vehicles.MoveableContainer;
import org.movsim.simulator.vehicles.VehicleContainer;
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

    private VehicleContainer vehContainer;

    /**
     * Instantiates a new floating cars impl.
     * 
     * @param vehContainer
     *            the veh container
     * @param input
     *            the input
     */
    public FloatingCarsImpl(VehicleContainer vehContainer, FloatingCarInput input) {
        logger.debug("Cstr. FloatingCars");

        this.vehContainer = vehContainer;
        this.nDtOut = input.getNDt();

        this.fcdList = input.getFloatingCars();

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
     */
    public void update(int itime, double time, double timestep) {

        if (itime % nDtOut == 0) {
            notifyObservers(time);
            logger.debug("update FloatingCars: itime={}", itime);
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
     * @see org.movsim.output.FloatingCars#getMoveableContainer()
     */
    @Override
    public MoveableContainer getMoveableContainer() {
        return vehContainer;
    }

}
