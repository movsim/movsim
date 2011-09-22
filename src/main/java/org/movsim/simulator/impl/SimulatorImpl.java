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
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
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

/**
 * The Class SimulatorImpl.
 */
public class SimulatorImpl implements Simulator, Runnable {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulatorImpl.class);

    // singleton pattern
    private static SimulatorImpl instance = null; 
    
    private double time;

    private long iterationCount;

    private double timestep;

    /** The duration of the simulation. */
    private double tMax;

    /** The road sections. */
    private List<RoadSection> roadSections;

    /** The sim output. */
    private SimOutput simOutput;

    /** The sim input. */
    private InputDataImpl inputData;
    
    /** The vehicle generator. */
    private VehicleGenerator vehGenerator;
    
    private boolean isWithCrashExit;

    private String projectName;
    
    private RoadNetwork roadNetwork;
     
    
    /**
     * Instantiates a new simulator impl.
     */
    private SimulatorImpl() {
        inputData = new InputDataImpl();  // accesses static reference ProjectMetaData 
    }

    public static SimulatorImpl getInstance(){
        if(instance == null){
            instance = new SimulatorImpl();
        }
        return instance;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#initialize()
     */
    @Override
    public void initialize() {
        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and Martin Budden (2011)");
        
        // parse xmlFile and set values
        final XmlReaderSimInput xmlReader = new XmlReaderSimInput(inputData);
        final SimulationInput simInput = inputData.getSimulationInput();
        this.timestep = simInput.getTimestep(); // can be modified by certain
                                                // models
        this.tMax = simInput.getMaxSimTime();

        MyRandom.initialize(simInput.isWithFixedSeed(), simInput.getRandomSeed());
        
        roadSections = new ArrayList<RoadSection>();
        
        final List<TrafficCompositionInputData> heterogenInputData = simInput.getTrafficCompositionInputData();
                
//                simInput.getSimulationInput().getSingleRoadInput()
//                .getTrafficCompositionInputData();
        
        // this is the default vehGenerator for *all* roadsections
        // if an individual vehicle composition is defined for a specific road
        vehGenerator = new VehicleGeneratorImpl(inputData, heterogenInputData);
        isWithCrashExit = inputData.getSimulationInput().isWithCrashExit();

        reset();
    }

   
    
    /**
     * Reset.
     */
    @Override
    public void reset() {
        time = 0;
        iterationCount = 0;
        roadSections.clear();
        System.out.println("roadsections: "+inputData.getSimulationInput().getRoadInput().size()); // TODO Adapt Roadinput/RoadSection
        for (RoadInput roadinput : inputData.getSimulationInput().getRoadInput()) {
            roadSections.add(new RoadSectionImpl(inputData, roadinput, vehGenerator));
        }
        
        // TODO quick hack for pulling out onramps from mainroads
        final List<RoadSection> onramps = roadSections.get(0).rampFactory(inputData);
        for(RoadSection onramp : onramps){
            roadSections.add(onramp);
        }
        
        
        final RoadNetwork roadNetwork = RoadNetwork.getInstance();
        for (RoadSection roadSection : roadSections) {
            roadNetwork.add(roadSection);
        }
        
        logger.info(roadNetwork.toString());
        
//        for (RoadSection roadSection : roadSections) {
//            final long toId = roadSection.getToId();
//            final RoadSection roadSectionDown = findRoadById(toId);
//            if( roadSectionDown !=null ){
//                final List<VehicleContainer> lanes = roadSection.getVehContainers();
//                for(int laneIndex=0, N=lanes.size(); laneIndex<N; laneIndex++){
//                    lanes.get(laneIndex).setDownstreamConnection(roadSectionDown.getVehContainers().get(laneIndex));
//                }
//            }
//        }
        
        
        // TODO quick hack for connecting offramp with onramp
        // more general concept here !!!
        final long idOfframp = -1;
        final long idOnramp = 1;
        if (findRoadById(idOfframp) != null && findRoadById(idOnramp) != null) {
            final RoadSection onramp = findRoadById(idOnramp);
            findRoadById(idOfframp).getVehContainer(Constants.MOST_RIGHT_LANE).setDownstreamConnection(
                    onramp.getVehContainer(Constants.MOST_RIGHT_LANE));
            logger.info("connect offramp with id={} to onramp with id={}", idOfframp, idOnramp);
        }

        
        projectName = inputData.getProjectMetaData().getProjectName();

        
        // model requires specific update time depending on its category !!

        // TODO: check functionality
        if (roadSections.get(0).getTimestep() > Constants.SMALL_VALUE) {
            this.timestep = roadSections.get(0).getTimestep();
            logger.info("model sets simulation integration timestep to dt={}", timestep);
        }

        simOutput = new SimOutput(inputData, roadSections);
    }

    
    /* (non-Javadoc)
     * @see org.movsim.simulator.Simulator#getRoadSections()
     */
    @Override
    public List<RoadSection> getRoadSections() {
        return roadSections;
    }
    
    /* (non-Javadoc)
     * @see org.movsim.simulator.Simulator#findRoadById(long)
     */
    public RoadSection findRoadById(long id) {
        for (final RoadSection roadSection : roadSections) {
            if (roadSection.getId() == id) {
                return roadSection;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#run()
     */
    @Override
    public void run() {
        logger.info("Simulator.run: start simulation at {} seconds of simulation project={}", time, projectName);

        // TODO check if first output update has to be called in update for external call!!
        simOutput.update(iterationCount, time, timestep);

        while (!isSimulationRunFinished()) {
            update();
        }

        logger.info("Simulator.run: stop after time = {} seconds of simulation project={}", time, projectName);
    }

    /**
     * Stop this run.
     *
     * @return true, if successful
     */
    public boolean isSimulationRunFinished() {
        return (time > tMax);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#update()
     */
    @Override
    public void update() {

        time += timestep;
        iterationCount++;

        if (iterationCount % 100 == 0) {
            logger.info("Simulator.update : time={} seconds, dt={}", time, timestep);
        }
        
        // parallel update of all roadSections 
        
        // TODO book-keeping all *all* roadSections in one Collection for iteration
        // onramps are part of mainroad section in current implementation  
        
        final double dt = this.timestep;   // TODO
        
      
        for (RoadSection roadSection : roadSections) {
            roadSection.updateRoadConditions(iterationCount, time);
        }

        for (RoadSection roadSection : roadSections) {
            roadSection.updateBoundaryVehicles(iterationCount, time);
        }
        
        // check for crashes
        for (RoadSection roadSection : roadSections) {
            roadSection.checkForInconsistencies(iterationCount, time, isWithCrashExit);
        }


        
        // lane changes and merges from onramps/ to offramps
        for (RoadSection roadSection : roadSections) {
            roadSection.laneChanging(iterationCount, dt, time);
        }

        // lane changes and merges from onramps/ to offramps
        for (RoadSection roadSection : roadSections) {
            roadSection.laneChangingToOfframps(roadSections, iterationCount, dt, time);
        }
        
        // vehicle accelerations
        for (RoadSection roadSection : roadSections) {
            roadSection.accelerate(iterationCount, dt, time);
        }

        // vehicle pos/speed
        for (RoadSection roadSection : roadSections) {
            roadSection.updatePositionAndSpeed(iterationCount, dt, time);
        }

        for (RoadSection roadSection : roadSections) {
            roadSection.updateDownstreamBoundary();
        }

        for (RoadSection roadSection : roadSections) {
            roadSection.updateUpstreamBoundary(iterationCount, dt, time);
        }


        for (RoadSection roadSection : roadSections) {
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

   

}
