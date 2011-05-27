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
package org.movsim.simulator.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.SimulationInput;
import org.movsim.output.LoopDetector;
import org.movsim.output.LoopDetectorObserver;
import org.movsim.output.Macro3DObserver;
import org.movsim.output.SimOutput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.impl.RoadSectionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SimulatorImpl.
 */
public class SimulatorImpl implements Simulator {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulatorImpl.class);

    /** The time. */
    private double time;

    /** The itime. */
    private int itime;

    /** The timestep. */
    private double timestep;

    /** The duration of the simulation. */
    private final double tMax;

    /** The road section. */
    private RoadSection roadSection;

    /** The sim output. */
    private SimOutput simOutput;

    /** The is with gui. */
    private final boolean isWithGUI;

    /** The sim input. */
    private InputData simInput; // dynamisch, kann von GUI veraendert
                                // werden

    private List<LoopDetectorObserver> listLoopDetectorObserver = new ArrayList<LoopDetectorObserver>();
    private List<Macro3DObserver> listMacro3DObserver = new ArrayList<Macro3DObserver>();

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
        this.tMax = simInput.getMaxSimTime();

        MyRandom.initialize(simInput.isWithFixedSeed(), simInput.getRandomSeed());

        restart();
    }

    /**
     * Restart.
     */
    public void restart() {
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
            logger.info("Simulator.update: time={} seconds, dt={}", time, timestep);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#getSiminput()
     */
    @Override
    public InputData getSiminput() {
        return simInput;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.Simulator#setSimInput(org.movsim.input.InputData)
     */
    @Override
    public void setSimInput(InputData simInput) {
        this.simInput = simInput;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#registerObserver(org.movsim.output.
     * LoopDetectorObserver)
     */
    @Override
    public void registerObserver(LoopDetectorObserver o) {
        listLoopDetectorObserver.add(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#removeObserver(org.movsim.output.
     * LoopDetectorObserver)
     */
    @Override
    public void removeObserver(LoopDetectorObserver o) {
        int i = listLoopDetectorObserver.indexOf(o);
        if (i >= 0) {
            listLoopDetectorObserver.remove(i);
        }
    }

    public void notifyLoopDetectorObservers() {
        for (LoopDetectorObserver observer : listLoopDetectorObserver) {
            List<LoopDetector> det = simOutput.getDetectors().getDetectors();
            for (LoopDetector d : det) {
                observer.update(timestep, d.flow(), d.meanSpeed(), d.rhoArithmetic());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#registerObserver(org.movsim.output.
     * Macro3DObserver)
     */
    @Override
    public void registerObserver(Macro3DObserver o) {
        listMacro3DObserver.add(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#removeObserver(org.movsim.output.
     * Macro3DObserver)
     */
    @Override
    public void removeObserver(Macro3DObserver o) {
        int i = listMacro3DObserver.indexOf(o);
        if (i >= 0) {
            listMacro3DObserver.remove(i);
        }
    }

    public void notifyMacro3DObservers() {
        for (Macro3DObserver o : listMacro3DObserver) {
            o.updateMacro3D();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#getSimOutput()
     */
    @Override
    public SimOutput getSimOutput() {
        return simOutput;
    }

}
