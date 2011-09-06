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
import org.movsim.input.impl.InputDataImpl;
import org.movsim.input.impl.XmlReaderSimInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.output.LoopDetector;
import org.movsim.output.SimObservables;
import org.movsim.output.SimOutput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.impl.RoadSectionImpl;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.impl.VehicleGeneratorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SimulatorImpl.
 */
public class SimulatorImpl implements Simulator, Runnable {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulatorImpl.class);

    /** The time. */
    private double time;

    /** The iterationCount. */
    private long iterationCount;

    /** The timestep. */
    private double timestep;

    /** The duration of the simulation. */
    private double tMax;

    /** The road section. */
    private List<RoadSection> roadSections;

    /** The sim output. */
    private SimOutput simOutput;

    /** The sim input. */
    private InputDataImpl inputData;
    
    /** The veh generator. */
    protected VehicleGenerator vehGenerator;
    
    private final boolean isWithCrashExit;

    /**
     * Instantiates a new simulator impl.
     */
    public SimulatorImpl() {
        this.inputData = new InputDataImpl();
        roadSections = new ArrayList<RoadSection>();
        vehGenerator = new VehicleGeneratorImpl(inputData);
        isWithCrashExit = inputData.getSimulationInput().isWithCrashExit();
    }

    /**
     * Restart.
     */
    @Override
    public void restart() {
        time = 0;
        iterationCount = 0;
        roadSections.clear();
        roadSections.add(new RoadSectionImpl(inputData, vehGenerator));

        // model requires specific update time depending on its category !!

        // TODO: check functionality
        if (roadSections.get(0).getTimestep() > Constants.SMALL_VALUE) {
            this.timestep = roadSections.get(0).getTimestep();
            logger.info("model sets simulation integration timestep to dt={}", timestep);
        }

        simOutput = new SimOutput(inputData, roadSections);
    }

    
    @Override
    public List<RoadSection> getRoadSections() {
        return roadSections;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#run()
     */
    @Override
    public void run() {
        logger.info("Simulator.run: start simulation at {} seconds", time);

        simOutput.update(iterationCount, time, timestep);

        while (!stopThisRun(time)) {
            time += timestep;
            iterationCount++;
            update();
        }

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
        if (iterationCount % 100 == 0) {
            logger.info("Simulator.update: time={} seconds, dt={}", time, timestep);
        }
        
        // roadSection.update(iterationCount, time);
        // parallel update of all roadSections 
        
        
        final double dt = this.timestep; // TODO
        // check for crashes
        for(RoadSection roadSection : roadSections){
            roadSection.checkForInconsistencies(iterationCount, time, isWithCrashExit);
        }
        
        for(RoadSection roadSection : roadSections){
            roadSection.updateRoadConditions(iterationCount, time);
        }

        // vehicle accelerations
        for(RoadSection roadSection : roadSections){
            roadSection.accelerate(iterationCount, dt, time);
        }

        // vehicle pos/speed
        for(RoadSection roadSection : roadSections){
            roadSection.updatePositionAndSpeed(iterationCount, dt, time);
        }

        for(RoadSection roadSection : roadSections){
            roadSection.updateDownstreamBoundary();
        }

        for(RoadSection roadSection : roadSections){
            roadSection.updateUpstreamBoundary(iterationCount, dt, time);
        }

        for(RoadSection roadSection : roadSections){
            roadSection.updateOnramps(iterationCount, dt, time);
        }

        for(RoadSection roadSection : roadSections){
            roadSection.updateDetectors(iterationCount, dt, time);
        }
        
        simOutput.update(iterationCount, time, timestep);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#iTime()
     */
    @Override
    public long iterationCount() {
        return iterationCount;
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
    public InputData getSimInput() {
        return inputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#getSimObservables()
     */
    @Override
    public SimObservables getSimObservables() {
        return simOutput;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#initialize()
     */
    @Override
    public void initialize() {

        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and  Martin Budden (2011)");

        // parse xmlFile and set values
        final XmlReaderSimInput xmlReader = new XmlReaderSimInput(inputData);
        final SimulationInput simInput = inputData.getSimulationInput();
        this.timestep = simInput.getTimestep(); // can be modified by certain
                                                // models
        this.tMax = simInput.getMaxSimTime();

        MyRandom.initialize(simInput.isWithFixedSeed(), simInput.getRandomSeed());

        restart();
    }

   

}
