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
package org.movsim.simulator.impl;

import org.movsim.input.InputData;
import org.movsim.input.model.SimulationInput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.output.SimOutput;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.impl.RoadSectionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SimulatorImpl.
 */
public class SimulatorImpl implements Simulator {
    final static Logger logger = LoggerFactory.getLogger(SimulatorImpl.class);

    private double time;
    private int itime;

    private double timestep;

    private final double tMax; // sim duration

    private RoadSection roadSection;
    private SimOutput simOutput;

    private final boolean isWithGUI;

    private final InputData simInput; // dynamisch, kann von GUI veraendert
                                      // werden

    /**
     * Instantiates a new simulator impl.
     * 
     * @param isWithGUI
     *            the is with gui
     * @param inputData
     *            the input data
     */
    public SimulatorImpl(boolean isWithGUI, InputData inputData) {
        this.isWithGUI = isWithGUI;
        this.simInput = inputData;
        final SimulationInput simInput = inputData.getSimulationInput();
        this.timestep = simInput.getTimestep(); // can be modified by certain
                                                // models (see below)
        this.tMax = simInput.getMaxSimulationTime();

        MyRandom.initialize(simInput.isWithFixedSeed(), simInput.getRandomSeed());

        restart();
    }

    /**
     * Restart.
     */
    private void restart() {
        time = 0;
        itime = 0;
        roadSection = new RoadSectionImpl(isWithGUI, simInput);

        // model requires specific update time depending on its category !!

        // TODO: check functionality
        if (roadSection.getTimestep() > Constants.SMALL_VALUE) {
            this.timestep = roadSection.getTimestep();
            logger.info("model sets simulation integration timestep to dt={}", timestep);
        }

        simOutput = new SimOutput(isWithGUI, simInput, roadSection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#run()
     */
    @Override
    public void run() {
        logger.info("Simulator.run: start simulation at {} seconds", time);

        simOutput.update(itime, time, timestep, roadSection);

        while (!stopThisRun(time)) {
            time += timestep;
            itime++;
            update();
        }

        simOutput.close();
        logger.info("Simulator.run: stop after time = {} seconds", time);
    }

    /**
     * Stop this run.
     * 
     * @param time
     *            the time
     * @return true, if successful
     */
    private boolean stopThisRun(double time) {
        return (time > tMax);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#update()
     */
    @Override
    public void update() {
        if (itime % 100 == 0) {
            // logger.info("Simulator.update: itime={}", itime);
            logger.info("Simulator.update: time={}, dt={}", (time / 60.), timestep);
        }
        roadSection.update(itime, time);
        simOutput.update(itime, time, timestep, roadSection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#iTime()
     */
    @Override
    public int iTime() {
        return itime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#time()
     */
    @Override
    public double time() {
        return time;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#timestep()
     */
    @Override
    public double timestep() {
        return timestep;
    }

}
