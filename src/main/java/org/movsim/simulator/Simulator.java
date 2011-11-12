/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ---------------------------------------------------------------------- This file is part of MovSim - the multi-model
 * open-source vehicular-traffic simulator MovSim is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.movsim.input.InputData;
import org.movsim.input.file.opendrive.OpenDriveReader;
import org.movsim.input.impl.InputDataImpl;
import org.movsim.input.impl.XmlReaderSimInput;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
import org.movsim.output.SimObservables;
import org.movsim.output.SimOutput;
import org.movsim.simulator.impl.RoadNetworkDeprecated;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.impl.OfframpImpl;
import org.movsim.simulator.roadSection.impl.RoadSectionFactory;
import org.movsim.simulator.roadsegment.RoadSegment;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.impl.VehicleGeneratorImpl;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Interface Simulator.
 */

public class Simulator implements Runnable {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Simulator.class);

    // singleton pattern
    private static Simulator instance = new Simulator();

    private double time;

    private long iterationCount;

    private double timestep; // fix for one simulation !!

    /** The duration of the simulation. */
    private double tMax;

    /** The road sections. */
    private List<RoadSection> roadSections;

    private Map<Long, RoadSection> roadSectionsMap;

    /** The sim output. */
    private SimOutput simOutput;

    /** The sim input. */
    private InputDataImpl inputData;

    /** The vehicle generator. */
    private VehicleGenerator vehGenerator;

    private boolean isWithCrashExit;

    private String projectName;

    private long startTimeMillis;

    RoadNetwork roadNetwork;

    /**
     * Instantiates a new simulator impl.
     */
    private Simulator() {
        inputData = new InputDataImpl(); // accesses static reference ProjectMetaData

    }

    public static Simulator getInstance() {
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#initialize()
     */
    public void initialize() {
        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and Martin Budden (2011)");

        roadNetwork = new RoadNetwork();

        String scenario = "onramp"; //TODO cmdline parser and ProjectmetaData
        // String xmlFileName = "/home/kesting/workspace/movsim/file/src/test/resources/" + scenario + ".xodr"; //TODO remove

        String xmlFileName = "/roadnetwork/" + scenario + ".xodr";
        logger.info("try to load ", xmlFileName);
        OpenDriveReader.loadRoadNetwork(roadNetwork, xmlFileName);
        logger.info("done with road network parsing");

        // parse xmlFile and set values
        final XmlReaderSimInput xmlReader = new XmlReaderSimInput(inputData);
        final SimulationInput simInput = inputData.getSimulationInput();
        this.timestep = inputData.getSimulationInput().getTimestep(); // fix

        this.tMax = simInput.getMaxSimTime();

        MyRandom.initialize(simInput.isWithFixedSeed(), simInput.getRandomSeed());

        // this is the default vehGenerator for *all* roadsections
        // if an individual vehicle composition is defined for a specific road
        final List<TrafficCompositionInputData> heterogenInputData = simInput.getTrafficCompositionInputData();
        final boolean isWithFundDiagramOutput = inputData.getSimulationInput().isWithWriteFundamentalDiagrams();
        vehGenerator = new VehicleGeneratorImpl(inputData, heterogenInputData, isWithFundDiagramOutput);

        isWithCrashExit = inputData.getSimulationInput().isWithCrashExit();

        
        reset();
    }

    /**
     * Reset.
     */
    public void reset() {
        time = 0;
        iterationCount = 0;

        System.out.println("roadsections: " + inputData.getSimulationInput().getRoadInput().size()); // TODO Adapt
        // Roadinput/RoadSection

        // TODO roadsections
        // connect physical roads from network input to input from movsim 
        for(RoadSegment roadSegment : roadNetwork){
            long roadSegmentId = roadSegment.id();
            final RoadInput roadInput = inputData.getSimulationInput().getRoadInput().get(roadSegmentId);
            if(roadInput!=null){
                roadSegment.addInput(roadInput);
            }
            else{
                logger.debug("no additional input for road id={} provided in movsim configuration file.");
            }
        }
        
        
        roadSections = new LinkedList<RoadSection>();
        for (RoadInput roadinput : inputData.getSimulationInput().getRoadInput().values()) {
            final RoadSection roadSection = RoadSectionFactory.create(inputData, roadinput, vehGenerator);
            roadSections.add(roadSection);
        }

        
        createMap();

        // ---------------------------------------------
        // TODO work in progress, 
        // only needed in vehicle for calculating net distance but this concept not yet sufficiently worked-out 
        final RoadNetworkDeprecated roadNetworkDeprecated = RoadNetworkDeprecated.getInstance();
        for (RoadSection roadSection : roadSections) {
            roadNetworkDeprecated.add(roadSection);
        }
        logger.info(roadNetworkDeprecated.toString());
        // ---------------------------------------------
        

        // TODO quick hack for connecting offramp with onramp
        // more general concept needed here
        final long idOfframp = -1;
        final long idOnramp = 1;
        if (findRoadById(idOfframp) != null && findRoadById(idOnramp) != null) {
            final RoadSection onramp = findRoadById(idOnramp);
            findRoadById(idOfframp).getVehContainer(Constants.MOST_RIGHT_LANE).setDownstreamConnection(
                    onramp.getVehContainer(Constants.MOST_RIGHT_LANE));
            logger.info("connect offramp with id={} to onramp with id={}", idOfframp, idOnramp);
        }
        // ---------------------------------------------
        
        
        projectName = inputData.getProjectMetaData().getProjectName();

        // model requires specific update time depending on its category !!

        simOutput = new SimOutput(inputData, roadSections, this.roadSectionsMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#getRoadSections()
     */
    public List<RoadSection> getRoadSections() {
        return roadSections;
    }

    
    // helper function
    private void createMap() {
        roadSectionsMap = new HashMap<Long, RoadSection>();
        for (final RoadSection roadSection : roadSections) {
            roadSectionsMap.put(roadSection.getId(), roadSection);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#findRoadById(long)
     */
    public RoadSection findRoadById(long id) {
        if (roadSectionsMap.containsKey(id)) {
            return roadSectionsMap.get(id);
        }
        return null;
    }


//    // TODO just hack
//    private RoadSection findFirstOfframp() {
//        for (final RoadSection roadSection : roadSections) {
//            if (roadSection instanceof OfframpImpl) {
//                return roadSection;
//            }
//        }
//        return null;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#run()
     */
    @Override
    public void run() {
        logger.info("Simulator.run: start simulation at {} seconds of simulation project={}", time, projectName);

        startTimeMillis = System.currentTimeMillis();
        // TODO check if first output update has to be called in update for external call!!
        simOutput.update(iterationCount, time, timestep);

        while (!isSimulationRunFinished()) {
            update();
        }

        logger.info(String.format("Simulator.run: stop after time = %.2fs = %.2fh of simulation project=%s", time,
                time / 3600, projectName));
        final double elapsedTime = 0.001 * (System.currentTimeMillis() - startTimeMillis);
        logger.info(String.format(
                "time elapsed = %.3fs --> simulation time warp = %.2f, time per 1000 update steps=%.3fs", elapsedTime,
                time / elapsedTime, 1000 * elapsedTime / iterationCount));
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
    public void update() {

        time += timestep;
        iterationCount++;

        if (iterationCount % 100 == 0) {
            logger.info(String.format("Simulator.update :time = %.2fs = %.2fh, dt = %.2fs, projectName=%s", time,
                    time / 3600, timestep, projectName));
        }

        // parallel update of all roadSections

        // TODO book-keeping all *all* roadSections in one Collection for iteration
        // onramps are part of mainroad section in current implementation

        final double dt = this.timestep; // TODO

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

        // lane changes
        for (RoadSection roadSection : roadSections) {
            roadSection.laneChanging(iterationCount, dt, time);
        }

        // // merges from onramps/ to offramps (and also simpleRamp)
        // for (RoadSection roadSection : roadSections) {
        // // TODO: network information
        // RoadSection connectedRoadSection = null;
        // if(roadSection instanceof RoadSectionImpl){
        // // mainroad gets reference to first offramp (or null)
        // // TODO allow more than one offramp !!!
        // connectedRoadSection = findFirstOfframp();
        // }
        // if(roadSection instanceof OnrampMobilImpl){
        // connectedRoadSection = findRoadById(roadSection.getToId());
        // }
        //
        // roadSection.laneChangingToOfframpsAndFromOnramps(connectedRoadSection, iterationCount, dt, time);
        // }
        //

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
    public long iterationCount() {
        return iterationCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#time()
     */
    public double time() {
        return time;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#timestep()
     */
    public double timestep() {
        return timestep;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#getSiminput()
     */
    public InputData getSimInput() {
        return inputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.Simulator#getSimObservables()
     */
    public SimObservables getSimObservables() {
        return simOutput;
    }

    public RoadNetwork getRoadNetwork() {
        return roadNetwork;
    }

}
