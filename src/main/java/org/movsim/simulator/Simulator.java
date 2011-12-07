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
import org.movsim.input.model.simulation.TrafficSourceData;
import org.movsim.output.LoopDetectors;
import org.movsim.output.SimObservables;
import org.movsim.output.SimOutput;
import org.movsim.roadmappings.RoadMappingPolyS;
import org.movsim.simulator.roadnetwork.FlowConservingBottlenecks;
import org.movsim.simulator.roadnetwork.InitialConditionsMacro;
import org.movsim.simulator.roadnetwork.Lane;
import org.movsim.simulator.roadnetwork.RoadMapping;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SpeedLimits;
import org.movsim.simulator.roadnetwork.TrafficLights;
import org.movsim.simulator.roadnetwork.UpstreamBoundary;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simulator implements Runnable {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Simulator.class);

    /** singleton pattern with eager initilization */
    private static Simulator instance = new Simulator();

    private double time;

    private long iterationCount;

    private double timestep; // fix for one simulation !!

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
    private Simulator() {
        inputData = new InputData(); // accesses static reference ProjectMetaData
        roadNetwork = new RoadNetwork();
    }

    public static Simulator getInstance() {
        return instance;
    }

    public void initialize() {
        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and Martin Budden (2011)");

        final ProjectMetaData projectMetaData = inputData.getProjectMetaData();
        projectName = projectMetaData.getProjectName();
        final String path = projectMetaData.getPathToProjectXmlFile();
        final String xodrFileName = projectMetaData.getXodrFilename();
        final String xodrPath = projectMetaData.getXodrPath();

        // First parse the OpenDrive (.xodr) file to load the network topology and road layout
        final String fullXodrFileName = xodrPath + xodrFileName;
        logger.info("try to load {}", fullXodrFileName);
        final boolean loaded = OpenDriveReader.loadRoadNetwork(roadNetwork, fullXodrFileName);
        if (loaded == false) {
            logger.error("failed to load {}", fullXodrFileName);
        }
        logger.info("done with road network parsing");

        // Now parse the MovSim XML file to add the simulation components
        // eg vehicles and vehicle models, traffic composition, traffic sources etc
        final XmlReaderSimInput xmlReader = new XmlReaderSimInput(inputData);
        final SimulationInput simInput = inputData.getSimulationInput();

        this.timestep = simInput.getTimestep(); // fix

        this.tMax = simInput.getMaxSimTime();

        MyRandom.initialize(simInput.isWithFixedSeed(), simInput.getRandomSeed());

        // this is the default vehGenerator for *all* roadsections
        // if an individual vehicle composition is defined for a specific road
        final List<TrafficCompositionInputData> heterogenInputData = simInput.getTrafficCompositionInputData();
        final boolean isWithFundDiagramOutput = simInput.isWithWriteFundamentalDiagrams();
        vehGenerator = new VehicleGenerator(inputData, heterogenInputData, isWithFundDiagramOutput);

        final boolean isWithCrashExit = simInput.isWithCrashExit();
        roadNetwork.setWithCrashExit(isWithCrashExit);

        // For each road in the MovSim XML input data, find the corresponding roadSegment and
        // set its input data accordingly
        final Map<Long, RoadInput> roadInputMap = inputData.getSimulationInput().getRoadInput();
        if (loaded == false && roadInputMap.size() == 1) {
            // there was no xodr file and there is only one road segment in the MovSimXML file
            // so set up a default s-shaped road mapping
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
        } else {
            for (final RoadInput roadinput : roadInputMap.values()) {
                final RoadSegment roadSegment = roadNetwork.findById((int) roadinput.getId());
                if (roadSegment != null) {
                    addInputToRoadSegment(roadSegment, roadinput);
                }
            }
        }
        reset();
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
        // for now this is a minimal implementation, just the traffic source and traffic sink
        // need to add further data, eg initial conditions, bottlenecks etc
        final TrafficSourceData trafficSourceData = roadinput.getTrafficSourceData();
        final UpstreamBoundary upstreamBoundary = new UpstreamBoundary(roadSegment.id(), vehGenerator, roadSegment,
                trafficSourceData);
        roadSegment.setUpstreamBoundary(upstreamBoundary);

        final TrafficLights trafficLights = new TrafficLights(roadinput.getTrafficLightsInput());
        roadSegment.setTrafficLights(trafficLights);

        final SpeedLimits speedLimits = new SpeedLimits(roadinput.getSpeedLimitInputData());
        roadSegment.setSpeedLimits(speedLimits);

        final LoopDetectors loopDetectors = new LoopDetectors(roadSegment.id(), roadinput.getDetectorInput());
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
                veh.setPosition(xLocal);
                veh.setSpeed(speedInit);
                veh.setLane(Lane.LANE1);
                roadSegment.addVehicle(veh);
                //vehContainers.get(MovsimConstants.MOST_RIGHT_LANE).add(veh, xLocal, speedInit);
                logger.debug("init conditions macro: rhoLoc={}/km, xLoc={}", 1000 * rhoLocal, xLocal);
                xLocal -= 1 / rhoLocal;
            }
        } else {
            logger.debug(("choose micro initial conditions"));
            final List<ICMicroData> icSingle = roadInput.getIcMicroData();
            int vehicleNumber = 0;
            for (final ICMicroData ic : icSingle) {
                // TODO counter
                final String vehTypeFromFile = ic.getLabel();
                final Vehicle veh = (vehTypeFromFile.isEmpty()) ? vehGenerator.createVehicle() : vehGenerator
                        .createVehicle(vehTypeFromFile);
                // TODO: consider multi-lane case, distribute over all lanes
                veh.setVehNumber(vehicleNumber);
                ++vehicleNumber;
                veh.setPosition(ic.getX());
                veh.setSpeed(ic.getSpeed());
                final int lane = ic.getInitLane();
                veh.setLane(Lane.LANE1);
                roadSegment.addVehicle(veh);
                //vehContainers.get(MovsimConstants.MOST_RIGHT_LANE).add(veh, posInit, speedInit);
                logger.info("set vehicle with label = {}", veh.getLabel());
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
        simOutput.update(iterationCount, time, timestep);

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
            logger.info(String.format("Simulator.update :time = %.2fs = %.2fh, dt = %.2fs, projectName=%s", time,
                    time / 3600, timestep, projectName));
        }
        // TODO new update of roadSegments
        roadNetwork.timeStep(timestep, time, iterationCount);
        // parallel update of all roadSections
        // final double dt = this.timestep; // TODO

        simOutput.update(iterationCount, time, timestep);
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
