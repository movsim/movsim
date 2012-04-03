/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.movsim.input.InputData;
import org.movsim.input.ProjectMetaData;
import org.movsim.input.XmlReaderSimInput;
import org.movsim.input.file.opendrive.OpenDriveReader;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.VehiclesInput;
import org.movsim.input.model.output.RouteInput;
import org.movsim.input.model.output.RoutesInput;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.VehicleTypeInput;
import org.movsim.input.model.simulation.TrafficLightsInput;
import org.movsim.input.model.simulation.TrafficSourceData;
import org.movsim.output.LoopDetectors;
import org.movsim.output.SimulationOutput;
import org.movsim.output.fileoutput.FileTrafficLightRecorder;
import org.movsim.output.fileoutput.FileTrafficSourceData;
import org.movsim.roadmappings.RoadMappingPolyS;
import org.movsim.simulator.roadnetwork.FlowConservingBottlenecks;
import org.movsim.simulator.roadnetwork.InflowTimeSeries;
import org.movsim.simulator.roadnetwork.InitialConditionsMacro;
import org.movsim.simulator.roadnetwork.Lane;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadMapping;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.movsim.simulator.roadnetwork.Slopes;
import org.movsim.simulator.roadnetwork.SpeedLimits;
import org.movsim.simulator.roadnetwork.TrafficLights;
import org.movsim.simulator.roadnetwork.TrafficSource;
import org.movsim.simulator.vehicles.FuelConsumptionModelPool;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.utilities.ConversionUtilities;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simulator implements SimulationTimeStep, SimulationRun.CompletionCallback {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Simulator.class);

    private long startTimeMillis;

    private final ProjectMetaData projectMetaData;
    private String projectName;
    private final InputData inputData;
    private FuelConsumptionModelPool fuelConsumptionModelPool;
    private VehicleGenerator vehGenerator;
    private SimulationOutput simOutput;
    private final RoadNetwork roadNetwork;
    private Map<String, Route> routes;
    private final SimulationRunnable simulationRunnable;

    /**
     * Constructor.
     */
    public Simulator(ProjectMetaData projectMetaData) {
        this.projectMetaData = projectMetaData;
        inputData = new InputData();
        roadNetwork = new RoadNetwork();
        simulationRunnable = new SimulationRunnable(this);
        simulationRunnable.setCompletionCallback(this);
    }

    public void initialize() {
        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and Martin Budden (2011, 2012)");

        projectName = projectMetaData.getProjectName();
        // TODO temporary handling of Variable Message Sign until added to XML
        roadNetwork.setHasVariableMessageSign(projectName.equals("routing"));

        final SimulationInput simInput = parseMovSimXml(projectMetaData, inputData);

        fuelConsumptionModelPool = new FuelConsumptionModelPool(inputData.getFuelConsumptionInput());

        final boolean loadedRoadNetwork = parseOpenDriveXml(roadNetwork, projectMetaData);

        roadNetwork.setWithCrashExit(simInput.isWithCrashExit());

        simulationRunnable.setTimeStep(simInput.getTimestep());
        simulationRunnable.setDuration(simInput.getMaxSimTime());

        MyRandom.initialize(simInput.isWithFixedSeed(), simInput.getRandomSeed());

        vehGenerator = createVehicleGenerator(simInput);

        // Routes
        final RoutesInput routesInput = simInput.getRoutesInput();
        routes = new HashMap<String, Route>();
        if (routesInput != null) {
            for (final RouteInput routeInput : routesInput.getRoutes()) {
                final Route route = new Route(routeInput.getName());
                final List<String> roadIds = routeInput.getRoadIds();
                for (final String roadId : roadIds) {
                    route.add(roadNetwork.findByUserId(roadId));
                }
                routes.put(route.getName(), route);
            }
        }

        // For each road in the MovSim XML input data, find the corresponding roadSegment and
        // set its input data accordingly
        final Map<String, RoadInput> roadInputMap = simInput.getRoadInput();
        if (loadedRoadNetwork == false && roadInputMap.size() == 1) {
            defaultTestingRoadMapping(roadInputMap);
        } else {
            matchRoadSegmentsAndRoadInput(roadInputMap);
        }

        reset();
    }

    public VehiclesInput getVehiclesInput(){
        return inputData.getVehiclesInput();
    }
    
    public VehicleGenerator getVehicleGenerator() {
        return vehGenerator;
    }

    public ProjectMetaData getProjectMetaData() {
        return projectMetaData;
    }

    public InputData getSimInput() {
        return inputData;
    }

    public SimulationOutput getSimOutput() {
        return simOutput;
    }

    public RoadNetwork getRoadNetwork() {
        return roadNetwork;
    }

    public SimulationRunnable getSimulationRunnable() {
        return simulationRunnable;
    }

    public List<Double> getTravelTimeDataEMAs(double time) {
        final double tauEMA = 40;
        return simOutput.getTravelTimes().getTravelTimesEMA(time, tauEMA);
    }

    /**
     * Load scenario from xml.
     * 
     * @param scenario
     *            the scenario
     */
    public void loadScenarioFromXml(String scenario, String path) {
        roadNetwork.clear();
        projectMetaData.setProjectName(scenario);
        projectMetaData.setPathToProjectXmlFile(path);
        initialize();
    }

    /**
     * @param roadInputMap
     */
    private void matchRoadSegmentsAndRoadInput(Map<String, RoadInput> roadInputMap) {
        for (final RoadInput roadInput : roadInputMap.values()) {
            final RoadSegment roadSegment = roadNetwork.findByUserId(roadInput.getId());
            if (roadSegment != null) {
                addInputToRoadSegment(roadSegment, roadInput, vehGenerator);
            } else {
                // at least warn user that roadId cannot be matched to xodr roadnetwork
                // TODO add option to exit here if user sets option. Such input errors are just annoying.
                logger.warn("cannot find roadId={} from input in constructed roadNetwork. IGNORE DATA!!!", roadInput.getId());
            }
        }
    }

    /**
     * This is the default vehGenerator for all roadSegments as long as no individual vehicle composition of a roadSegment is defined
     * 
     * @param simInput
     */
    private VehicleGenerator createVehicleGenerator(SimulationInput simInput) {
        final List<VehicleTypeInput> vehicleTypeInputs = simInput.getTrafficCompositionInputData();
        final VehicleGenerator vehGenerator = new VehicleGenerator(simulationRunnable.timeStep(),
                inputData.getVehiclesInput(), vehicleTypeInputs, fuelConsumptionModelPool, routes);
        return vehGenerator;

    }

    /**
     * There was no xodr file and there is only one road segment in the MovSimXML file so set up a default s-shaped road
     * mapping.
     * 
     * @param roadInputMap
     */
    private void defaultTestingRoadMapping(Map<String, RoadInput> roadInputMap) {
        logger.warn("Simulation with test network");
        final RoadInput roadinput = roadInputMap.values().iterator().next();
        final int laneCount = 1;
        final double roadLength = 1500;
        final RoadMapping roadMapping = new RoadMappingPolyS(laneCount, 10, 50, 50, 100.0 / Math.PI, roadLength);
        final RoadSegment roadSegment = new RoadSegment(roadMapping);
        addInputToRoadSegment(roadSegment, roadinput, vehGenerator);
        roadSegment.setUserId("1");
        roadSegment.addDefaultSink();
        roadNetwork.add(roadSegment);
    }

    /**
     * Parse the OpenDrive (.xodr) file to load the network topology and road layout.
     * 
     * @param projectMetaData
     * @return
     */
    private static boolean parseOpenDriveXml(RoadNetwork roadNetwork, ProjectMetaData projectMetaData) {
        final String xodrFileName = projectMetaData.getXodrFilename();
        final String xodrPath = projectMetaData.getPathToProjectXmlFile();
        final String fullXodrFileName = xodrPath + xodrFileName;
        logger.info("try to load {}", fullXodrFileName);
        final boolean loaded = OpenDriveReader.loadRoadNetwork(roadNetwork, fullXodrFileName);
        logger.info("done with parsing road network {}. Success: {}", fullXodrFileName, loaded);
        return loaded;
    }

    /**
     * Parse the MovSim XML file to add the simulation components eg network filename, vehicles and vehicle models,
     * traffic composition, traffic sources etc.
     * 
     * @return
     */
    private static SimulationInput parseMovSimXml(ProjectMetaData projectMetaData, InputData inputData) {
        final XmlReaderSimInput xmlReader = new XmlReaderSimInput(projectMetaData, inputData);
        final SimulationInput simInput = inputData.getSimulationInput();
        return simInput;
    }

    /**
     * Add input data to road segment.
     * 
     * Note by rules of encapsulation this function is NOT a member of RoadSegment, since RoadSegment should not be
     * aware of form of XML file or RoadInput data structure.
     * 
     * @param roadSegment
     * @param roadInput
     */
    private void addInputToRoadSegment(RoadSegment roadSegment, RoadInput roadInput,
            VehicleGenerator defaultVehGenerator) {

        VehicleGenerator roadVehGenerator = defaultVehGenerator;
        // set up vehicle generator for roadElement
        final List<VehicleTypeInput> roadVehicleTypeInputs = roadInput.getTrafficCompositionInputData();
        if(roadVehicleTypeInputs != null){
            // setup own vehicle generator for roadSegment: needed for trafficSource and initial conditions
            roadVehGenerator = new VehicleGenerator(simulationRunnable.timeStep(), inputData.getVehiclesInput(),
                    roadVehicleTypeInputs, fuelConsumptionModelPool, routes);
            logger.info("road with id={} has its own vehicle composition generator.", roadSegment.userId());
        }

        // set up the traffic source
        final TrafficSourceData trafficSourceData = roadInput.getTrafficSourceData();
        if (trafficSourceData.getInflowTimeSeries().size() != 0) {
            final InflowTimeSeries inflowTimeSeries = new InflowTimeSeries(trafficSourceData.getInflowTimeSeries());
            final TrafficSource trafficSource = new TrafficSource(roadVehGenerator, roadSegment, inflowTimeSeries);
            if (trafficSourceData.withLogging()) {
                trafficSource.setRecorder(new FileTrafficSourceData(roadSegment.userId()));
            }
            roadSegment.setTrafficSource(trafficSource);
        }

        // set up the traffic lights
        final TrafficLightsInput trafficLightsInput = roadInput.getTrafficLightsInput();
        final TrafficLights trafficLights = new TrafficLights(trafficLightsInput);
        if (trafficLightsInput.isWithLogging()) {
            final int nDt = trafficLightsInput.getnDtSample();
            trafficLights.setRecorder(new FileTrafficLightRecorder(nDt, trafficLights, roadSegment));
        }
        roadSegment.setTrafficLights(trafficLights);

        // set up the speed limits
        final SpeedLimits speedLimits = new SpeedLimits(roadInput.getSpeedLimitInputData());
        roadSegment.setSpeedLimits(speedLimits);

        // set up the slopes
        final Slopes slopes = new Slopes(roadInput.getSlopesInputData());
        roadSegment.setSlopes(slopes);

        // set up the detectors
        final LoopDetectors loopDetectors = new LoopDetectors(roadSegment, roadInput.getDetectorInput());
        roadSegment.setLoopDetectors(loopDetectors);

        // set up the flow conserving bottlenecks
        final FlowConservingBottlenecks flowConservingBottlenecks = new FlowConservingBottlenecks(
                roadInput.getFlowConsBottleneckInputData());
        roadSegment.setFlowConservingBottlenecks(flowConservingBottlenecks);

        initialConditions(roadSegment, roadInput, roadVehGenerator);

        // final TrafficSinkData trafficSinkData = roadinput.getTrafficSinkData();
    }

    private static void initialConditions(RoadSegment roadSegment, RoadInput roadInput, VehicleGenerator vehGenerator) {
        final List<ICMacroData> icMacroData = roadInput.getIcMacroData();
        if (!icMacroData.isEmpty()) {
            setMacroInitialConditions(roadSegment, roadInput, vehGenerator, icMacroData);
        } else {
            final List<ICMicroData> icSingle = roadInput.getIcMicroData();
            setMicroInitialConditions(roadSegment, roadInput, vehGenerator, icSingle);
        }  
    }

    /**
     * Determine vehicle positions on all relevant lanes while considering minimum gaps to avoid accidents. Gaps are
     * left at the beginning and the end of the road segment on purpose. However, the consistency check is not complete
     * and other segments are not considered.
     * 
     * @param roadSegment
     * @param roadInput
     * @param vehGenerator
     * @param icMacroData
     */
    private static void setMacroInitialConditions(RoadSegment roadSegment, RoadInput roadInput,
            VehicleGenerator vehGenerator, final List<ICMacroData> icMacroData) {

        logger.info("choose macro initial conditions: generate vehicles from macro-density ");
        final InitialConditionsMacro icMacro = new InitialConditionsMacro(icMacroData);

        final Iterator<LaneSegment> laneSegmentIterator = roadSegment.laneSegmentIterator();
        while (laneSegmentIterator.hasNext()) {
            LaneSegment lane = laneSegmentIterator.next();
            if (lane.type() != Lane.Type.TRAFFIC) {
                logger.debug("no macroscopic initial conditions for non-traffic lanes (slip roads etc).");
                continue;
            }

            double position = roadSegment.roadLength(); // start at end of segment
            while (position > 0) {
                final VehiclePrototype vehPrototype = vehGenerator.getVehiclePrototype();

                final double rhoLocal = icMacro.rho(position);
                double speedInit = icMacro.vInit(position);
                if (speedInit < 0) {
                    speedInit = vehPrototype.getEquilibriumSpeed(rhoLocal);
                    logger.debug("use equilibrium speed={} in macroscopic initial conditions.", speedInit);
                }

                if(logger.isDebugEnabled()){
                logger.debug(String.format(
                        "macroscopic init conditions from input: roadId=%s, x=%.3f, rho(x)=%.3f/km, speed=%.2fkm/h",
                        roadInput.getId(), position, ConversionUtilities.INVM_TO_INVKM * rhoLocal,
                        ConversionUtilities.MS_TO_KMH * speedInit));
                }

                if (rhoLocal <= 0) {
                    logger.debug("no vehicle added at x={} for vanishing initial density={}.", position, rhoLocal);
                    position -= 50;  // move on in upstream direction 
                    continue;
                }

                final Vehicle veh = vehGenerator.createVehicle(vehPrototype);
                final double meanDistanceInLane = 1. / (rhoLocal + MovsimConstants.SMALL_VALUE);
                final double minimumGap = veh.getLength() + veh.getLongitudinalModel().getS0();
                final double posDecrement = Math.max(meanDistanceInLane, minimumGap);
                position -= posDecrement;
                
                if(position <= posDecrement){
                    logger.debug("leave minimum gap at origin of road segment and start with next lane, pos={}", position);
                    break; 
                }
                final Vehicle leader = lane.rearVehicle();
                final double gapToLeader = (leader == null) ? MovsimConstants.GAP_INFINITY : leader.getRearPosition()
                        - position;
                
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(
                            "meanDistance=%.3f, minimumGap=%.2f, posDecrement=%.3f, gapToLeader=%.3f\n",
                            meanDistanceInLane, minimumGap, posDecrement, gapToLeader));
                }
                
                if (gapToLeader > 0) {
                    veh.setFrontPosition(position);
                    veh.setSpeed(speedInit);
                    veh.setLane(lane.lane());
                    logger.debug("add vehicle from macroscopic initial conditions at pos={} with speed={}.", position,
                            speedInit);
                    roadSegment.addVehicle(veh);
                }
                else{
                    logger.debug("cannot add vehicle due to gap constraints at pos={} with speed={}.", position,
                            speedInit);
                }
                
            }
        }
    }
    
    private static void setMicroInitialConditions(RoadSegment roadSegment, RoadInput roadInput,
            VehicleGenerator vehGenerator, List<ICMicroData> icSingle) {
        logger.debug(("choose micro initial conditions"));
        int vehicleNumber = 1;
        for (final ICMicroData ic : icSingle) {
            // TODO counter
            final String vehTypeFromFile = ic.getLabel();
            final Vehicle veh = (vehTypeFromFile.length() == 0) ? vehGenerator.createVehicle() : vehGenerator
                    .createVehicle(vehTypeFromFile);
            veh.setVehNumber(vehicleNumber);
            ++vehicleNumber;
            // testwise:
            veh.setFrontPosition(Math.round(ic.getX() / veh.physicalQuantities().getxScale()));
            veh.setSpeed(Math.round(ic.getSpeed() / veh.physicalQuantities().getvScale()));
            final int lane = ic.getInitLane();
            if (lane <= 0 || lane > roadSegment.laneCount()) {
                logger.error("Error: lane=" + lane + " on road id=" + roadSegment.userId()
                        + " does not exist. Choose as initial condition a lane between 1 and "
                        + roadSegment.laneCount());
                System.exit(-1);
            }
            veh.setLane(lane - 1);
            roadSegment.addVehicle(veh);
            logger.info(String.format("set vehicle with label = %s on lane=%d with front at x=%.2f, speed=%.2f",
                    veh.getLabel(), veh.getLane(), veh.getFrontPosition(), veh.getSpeed()));
            if (veh.getLongitudinalModel().isCA()) {
                logger.info(String.format(
                        "and for the CA in physical quantities: front position at x=%.2f, speed=%.2f", veh
                                .physicalQuantities().getFrontPosition(), veh.physicalQuantities().getSpeed()));
            }
        }
    }

    public void reset() {
        simulationRunnable.reset();
        simOutput = new SimulationOutput(simulationRunnable.timeStep(), projectMetaData.isInstantaneousFileOutput(), inputData, roadNetwork, routes);
    }

    public void runToCompletion() {
        logger.info("Simulator.run: start simulation at {} seconds of simulation project={}",
                simulationRunnable.simulationTime(), projectName);

        startTimeMillis = System.currentTimeMillis();
        // TODO check if first output update has to be called in update for external call!!
        // TODO FloatingCars do not need this call. First output line for t=0 is written twice to file
        simOutput.timeStep(simulationRunnable.timeStep(), simulationRunnable.simulationTime(),
                simulationRunnable.iterationCount());
        simulationRunnable.runToCompletion();
    }

    /**
     * Returns true if the simulation has finished.
     */
    public boolean isFinished() {
        return false;
    }

    @Override
    public void simulationComplete(double simulationTime) {
        logger.info(String.format("Simulator.run: stop after time = %.2fs = %.2fh of simulation project=%s",
                simulationTime, simulationTime / 3600, projectName));
        final double elapsedTime = 0.001 * (System.currentTimeMillis() - startTimeMillis);
        logger.info(String.format(
                "time elapsed = %.3fs --> simulation time warp = %.2f, time per 1000 update steps=%.3fs", elapsedTime,
                simulationTime / elapsedTime, 1000 * elapsedTime / simulationRunnable.iterationCount()));
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (iterationCount % 100 == 0) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("Simulator.update :time = %.2fs = %.2fh, dt = %.2fs, projectName=%s",
                        simulationTime, simulationTime / 3600, dt, projectName));
            }
        }
        roadNetwork.timeStep(dt, simulationTime, iterationCount);
        simOutput.timeStep(dt, simulationTime, iterationCount);
    }
}
