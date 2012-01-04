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
package org.movsim.simulator;

import java.util.List;
import java.util.Map;

import org.movsim.input.InputData;
import org.movsim.input.ProjectMetaData;
import org.movsim.input.XmlReaderSimInput;
import org.movsim.input.file.opendrive.OpenDriveReader;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
import org.movsim.input.model.simulation.TrafficLightsInput;
import org.movsim.input.model.simulation.TrafficSourceData;
import org.movsim.output.LoopDetectors;
import org.movsim.output.SimObservables;
import org.movsim.output.SimOutput;
import org.movsim.output.fileoutput.FileFundamentalDiagram;
import org.movsim.output.fileoutput.FileTrafficLightRecorder;
import org.movsim.output.fileoutput.FileTrafficSourceData;
import org.movsim.roadmappings.RoadMappingPolyS;
import org.movsim.simulator.roadnetwork.FlowConservingBottlenecks;
import org.movsim.simulator.roadnetwork.InflowTimeSeries;
import org.movsim.simulator.roadnetwork.InitialConditionsMacro;
import org.movsim.simulator.roadnetwork.Lane;
import org.movsim.simulator.roadnetwork.RoadMapping;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SpeedLimits;
import org.movsim.simulator.roadnetwork.TrafficLights;
import org.movsim.simulator.roadnetwork.TrafficSource;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simulator implements Runnable {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Simulator.class);

    private final ProjectMetaData projectMetaData;
    private double time;

    private long iterationCount;

    /**
     * The timestep is a constant for one simulation run. It cannot be changed during a simulation. But of course you can run another
     * simulation with a different timestep.
     */
    private double timestep;

    /** The duration of the simulation. */
    private double tMax;

    private SimOutput simOutput;

    private final InputData inputData;

    private VehicleGenerator vehGenerator;

    private String projectName;

    private long startTimeMillis;

    private final RoadNetwork roadNetwork;

    /**
     * Instantiates a new simulator.
     */
    public Simulator(ProjectMetaData projectMetaData) {
    	this.projectMetaData = projectMetaData;
        inputData = new InputData(projectMetaData); // accesses static reference ProjectMetaData
        roadNetwork = new RoadNetwork();
    }

    public void initialize() {
        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and Martin Budden (2011)");

        projectName = projectMetaData.getProjectName();

        final SimulationInput simInput = parseMovSimXm();
        final boolean loadedRoadNetwork = parseOpenDriveXml(projectMetaData);

        roadNetwork.setWithCrashExit(simInput.isWithCrashExit());

        timestep = simInput.getTimestep();
        tMax = simInput.getMaxSimTime();

        MyRandom.initialize(simInput.isWithFixedSeed(), simInput.getRandomSeed());

        createVehicleGenerator(simInput);

        // For each road in the MovSim XML input data, find the corresponding roadSegment and
        // set its input data accordingly
        final Map<String, RoadInput> roadInputMap = inputData.getSimulationInput().getRoadInput();
        if (loadedRoadNetwork == false && roadInputMap.size() == 1) {
            defaultTestingRoadMapping(roadInputMap); // TODO rg: This has to be corrected/deleted at some point
        } else {
            matchRoadSegmentsAndRoadInput(roadInputMap);
        }

        reset();
    }

    /**
     * @param roadInputMap
     */
    private void matchRoadSegmentsAndRoadInput(final Map<String, RoadInput> roadInputMap) {
        for (final RoadInput roadinput : roadInputMap.values()) {
            final RoadSegment roadSegment = roadNetwork.findByUserId(roadinput.getId());
            if (roadSegment != null) {
                addInputToRoadSegment(roadSegment, roadinput);
            }
        }
    }

    /**
     * this is the default vehGenerator for *all* roadsections if an individual vehicle composition is defined for a specific road
     * 
     * @param simInput
     */
    private void createVehicleGenerator(final SimulationInput simInput) {
        final List<TrafficCompositionInputData> heterogenInputData = simInput.getTrafficCompositionInputData();
        vehGenerator = new VehicleGenerator(timestep, inputData, heterogenInputData);
        // output fundamental diagrams
        final boolean instantaneousFileOutput = projectMetaData.isInstantaneousFileOutput();
        final boolean isWithFundDiagramOutput = simInput.isWithWriteFundamentalDiagrams();
        if (instantaneousFileOutput && isWithFundDiagramOutput) {
            FileFundamentalDiagram.writeFundamentalDiagrams(vehGenerator.prototypes());
        }

    }

    /**
     * there was no xodr file and there is only one road segment in the MovSimXML file so set up a default s-shaped road mapping
     * 
     * @param roadInputMap
     */
    private void defaultTestingRoadMapping(final Map<String, RoadInput> roadInputMap) {
        logger.warn("Simulation with test network");
        final RoadInput roadinput = roadInputMap.values().iterator().next();
        final int laneCount = 1;// roadinput.getLanes();
        final double roadLength = 1500;// roadinput.getRoadLength();
        // final RoadMapping roadMapping = new RoadMappingLine(laneCount, 0, 0, 0, roadLength);
        final RoadMapping roadMapping = new RoadMappingPolyS(laneCount, 10, 50, 50, 100.0 / Math.PI, roadLength);
        final RoadSegment roadSegment = new RoadSegment(roadMapping);
        addInputToRoadSegment(roadSegment, roadinput);
        roadSegment.setUserId("1");
        roadSegment.addDefaultSink();
        roadNetwork.add(roadSegment);
    }

    /**
     * Parse the OpenDrive (.xodr) file to load the network topology and road layout
     * 
     * @param projectMetaData
     * @return
     */
    private boolean parseOpenDriveXml(ProjectMetaData projectMetaData) {
        final String xodrFileName = projectMetaData.getXodrFilename();
        final String xodrPath = projectMetaData.getXodrPath();
        final String fullXodrFileName = xodrPath + xodrFileName;
        logger.info("try to load {}", fullXodrFileName);
        final boolean loaded = OpenDriveReader.loadRoadNetwork(roadNetwork, fullXodrFileName);
        logger.info("done with parsing road network {}. Success: {}", fullXodrFileName, loaded);
        return loaded;
    }

    /**
     * Parse the MovSim XML file to add the simulation components eg network filename, vehicles and vehicle models, traffic composition,
     * traffic sources etc
     * 
     * @return
     */
    private SimulationInput parseMovSimXm() {
        final XmlReaderSimInput xmlReader = new XmlReaderSimInput(inputData);
        final SimulationInput simInput = inputData.getSimulationInput();
        return simInput;
    }

    public void reset() {
        time = 0;
        iterationCount = 0;
        simOutput = new SimOutput(inputData, roadNetwork);
    }

    /**
     * Add input data to road segment.
     * 
     * Note by rules of encapsulation this function is NOT a member of RoadSegment, since RoadSegment should not be aware of form of XML
     * file or RoadInput data structure.
     * 
     * @param roadSegment
     * @param roadinput
     */
    private void addInputToRoadSegment(RoadSegment roadSegment, RoadInput roadinput) {
    	
    	// set up the traffic source
        final TrafficSourceData trafficSourceData = roadinput.getTrafficSourceData();
        final InflowTimeSeries inflowTimeSeries = new InflowTimeSeries(trafficSourceData.getInflowTimeSeries());
        final TrafficSource trafficSource = new TrafficSource(vehGenerator, roadSegment, inflowTimeSeries);
        if (trafficSourceData.withLogging()) {
        	trafficSource.setRecorder(new FileTrafficSourceData(roadSegment.userId()));
        }
        roadSegment.setTrafficSource(trafficSource);

        // set up the traffic lights
        final TrafficLightsInput trafficLightsInput = roadinput.getTrafficLightsInput();
        final TrafficLights trafficLights = new TrafficLights(trafficLightsInput);
        if (trafficLightsInput.isWithLogging()) {
            final int nDt = trafficLightsInput.getnDtSample();
            trafficLights.setRecorder(new FileTrafficLightRecorder(nDt, trafficLights, roadSegment));
        }
        roadSegment.setTrafficLights(trafficLights);

        // set up the speed limits
        final SpeedLimits speedLimits = new SpeedLimits(roadinput.getSpeedLimitInputData());
        roadSegment.setSpeedLimits(speedLimits);

        // set up the detectors
        final LoopDetectors loopDetectors = new LoopDetectors(roadSegment.userId(), roadinput.getDetectorInput(),
                roadSegment.laneCount());
        roadSegment.setLoopDetectors(loopDetectors);

        final FlowConservingBottlenecks flowConservingBottlenecks = new FlowConservingBottlenecks(
                roadinput.getFlowConsBottleneckInputData());
        roadSegment.setFlowConservingBottlenecks(flowConservingBottlenecks);

        initialConditions(roadSegment, inputData.getSimulationInput(), roadinput);

        // final TrafficSinkData trafficSinkData = roadinput.getTrafficSinkData();
    }

    private void initialConditions(RoadSegment roadSegment, SimulationInput simInput, RoadInput roadInput) {

        // TODO: consider multi-lane case !!!
        final List<ICMacroData> icMacroData = roadInput.getIcMacroData();
        if (!icMacroData.isEmpty()) {
            logger.debug("choose macro initial conditions: generate vehicles from macro-density ");
            final InitialConditionsMacro icMacro = new InitialConditionsMacro(icMacroData);
            // if ringroad: set xLocalMin e.g. -SMALL_VAL
            final double xLocalMin = 0;
            double xLocal = roadSegment.roadLength(); // start from behind
            while (xLocal > xLocalMin) {
                final VehiclePrototype vehPrototype = vehGenerator.getVehiclePrototype();
                final double rhoLocal = icMacro.rho(xLocal);
                double speedInit = icMacro.vInit(xLocal);
                if (speedInit == 0) {
                    speedInit = vehPrototype.getEquilibriumSpeed(rhoLocal);
                }
                final int laneEnter = MovsimConstants.MOST_RIGHT_LANE;
                final Vehicle veh = vehGenerator.createVehicle(vehPrototype);
                veh.setFrontPosition(xLocal);
                veh.setSpeed(speedInit);
                veh.setLane(Lane.LANE1);
                roadSegment.addVehicle(veh);
                // vehContainers.get(MovsimConstants.MOST_RIGHT_LANE).add(veh, xLocal, speedInit);
                logger.debug("init conditions macro: rhoLoc={}/km, xLoc={}", 1000 * rhoLocal, xLocal);
                xLocal -= 1 / rhoLocal;
            }
        } else {
            logger.debug(("choose micro initial conditions"));
            final List<ICMicroData> icSingle = roadInput.getIcMicroData();
            int vehicleNumber = 1;
            for (final ICMicroData ic : icSingle) {
                // TODO counter
                final String vehTypeFromFile = ic.getLabel();
                final Vehicle veh = (vehTypeFromFile.isEmpty()) ? vehGenerator.createVehicle() : vehGenerator
                        .createVehicle(vehTypeFromFile);
                // TODO: consider multi-lane case, distribute over all lanes
                veh.setVehNumber(vehicleNumber);
                ++vehicleNumber;
                // testwise:
                veh.setFrontPosition(Math.round(ic.getX()/veh.physicalQuantities().getxScale()));
                veh.setSpeed(Math.round(ic.getSpeed()/veh.physicalQuantities().getvScale()));
                final int lane = ic.getInitLane();  // TODO check lane numbering in ic input
                veh.setLane(Lane.LANE1);
                roadSegment.addVehicle(veh);
                // vehContainers.get(MovsimConstants.MOST_RIGHT_LANE).add(veh, posInit, speedInit);
                logger.info(String.format("set vehicle with label = %s on lane=%d with front at x=%.2f, speed=%.2f", veh.getLabel(), veh.getLane(), veh.getFrontPosition(), veh.getSpeed()));
                if(veh.getLongitudinalModel().isCA()){
                    logger.info(String.format("and for the CA in physical quantities: front position at x=%.2f, speed=%.2f", veh.physicalQuantities().getFrontPosition(), veh.physicalQuantities().getSpeed()));
                }
            }
        }
    }

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
        simOutput.update(time, iterationCount);

        while (!isSimulationRunFinished()) {
            updateTimestep();
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

    public void updateTimestep() {
        time += timestep;
        iterationCount++;

        if (iterationCount % 100 == 0) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("Simulator.update :time = %.2fs = %.2fh, dt = %.2fs, projectName=%s", time,
                        time / 3600, timestep, projectName));
            }
        }

        roadNetwork.timeStep(timestep, time, iterationCount);
        simOutput.update(time, iterationCount);
    }

    public long iterationCount() {
        return iterationCount;
    }

    public double time() {
        return time;
    }

    public double timestep() {
        return timestep;
    }

    public InputData getSimInput() {
        return inputData;
    }

    public SimObservables getSimObservables() {
        return simOutput;
    }

    public RoadNetwork getRoadNetwork() {
        return roadNetwork;
    }
}
