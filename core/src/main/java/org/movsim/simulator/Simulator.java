/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

import javax.xml.bind.JAXBException;

import org.movsim.core.autogen.InitialConditions;
import org.movsim.core.autogen.MacroIC;
import org.movsim.core.autogen.MicroIC;
import org.movsim.core.autogen.MovsimScenario;
import org.movsim.core.autogen.Road;
import org.movsim.core.autogen.Routes;
import org.movsim.core.autogen.Simulation;
import org.movsim.input.ProjectMetaData;
import org.movsim.input.network.opendrive.OpenDriveReader;
import org.movsim.output.SimulationOutput;
import org.movsim.output.detector.LoopDetectors;
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
import org.movsim.simulator.roadnetwork.SimpleRamp;
import org.movsim.simulator.roadnetwork.Slopes;
import org.movsim.simulator.roadnetwork.SpeedLimits;
import org.movsim.simulator.roadnetwork.TrafficLights;
import org.movsim.simulator.roadnetwork.TrafficSource;
import org.movsim.simulator.vehicles.TestVehicle;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleFactory;
import org.movsim.utilities.MyRandom;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class Simulator implements SimulationTimeStep, SimulationRun.CompletionCallback {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Simulator.class);

    private long startTimeMillis;

    private final ProjectMetaData projectMetaData;
    private String projectName;
    private MovsimScenario inputData; // cannot be final, parsing in init TODO
    // private FuelConsumptionModelPool fuelConsumptionModelPool; TODO

    private VehicleFactory vehicleFactory;
    private TrafficCompositionGenerator defaultTrafficComposition;
    private SimulationOutput simOutput;
    private final RoadNetwork roadNetwork;
    private Map<String, Route> routes;
    private final SimulationRunnable simulationRunnable;
    private int obstacleCount;

    /**
     * Constructor.
     */
    public Simulator(ProjectMetaData projectMetaData) {
        this.projectMetaData = projectMetaData;
        // inputData = projectMetaData.getInputData();
        roadNetwork = new RoadNetwork();
        simulationRunnable = new SimulationRunnable(this);
        simulationRunnable.setCompletionCallback(this);
    }

    public void initialize() throws JAXBException, SAXException {
        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and Martin Budden (2011, 2012)");

        projectName = projectMetaData.getProjectName();
        // TODO temporary handling of Variable Message Sign until added to XML
        roadNetwork.setHasVariableMessageSign(projectName.startsWith("routing"));

        inputData = projectMetaData.getInputData();
        Simulation simulationInput = inputData.getSimulation();

        // TODO one level higher?
        vehicleFactory = new VehicleFactory(simulationInput.getTimestep(), inputData.getVehiclePrototypes());

        final boolean loadedRoadNetwork = parseOpenDriveXml(roadNetwork, projectMetaData);

        roadNetwork.setWithCrashExit(simulationInput.isCrashExit());

        simulationRunnable.setTimeStep(simulationInput.getTimestep());
        
        
        // TODO better handling of case "duration = INFINITY"
        double duration = simulationInput.isSetDuration() ? simulationInput.getDuration() : -1;
        
        simulationRunnable.setDuration(duration < 0 ? Double.MAX_VALUE : duration);

        if (simulationInput.isSetSeed()) {
            MyRandom.initialize(simulationInput.getSeed());
        } else {
            MyRandom.initialize();
        }

        createRoutes(inputData.getRoutes());

        defaultTrafficComposition = new TrafficCompositionGenerator(simulationInput.getTrafficComposition(),
                vehicleFactory);

        // For each road in the MovSim XML input data, find the corresponding roadSegment and
        // set its input data accordingly
        // final Map<String, RoadInput> roadInputMap = simulationInput.get.getRoadInput();
        if (loadedRoadNetwork == false && simulationInput.getRoad().size() == 1) {
            defaultTestingRoadMapping(simulationInput.getRoad().get(0));
        } else {
            matchRoadSegmentsAndRoadInput(simulationInput.getRoad());
        }

        reset();
    }

    private void createRoutes(Routes routesInput) {
        routes = new HashMap<>();
        if (routesInput != null) {
            for (org.movsim.core.autogen.Route routeInput : routesInput.getRoute()) {
                final Route route = new Route(routeInput.getLabel());
                for (org.movsim.core.autogen.Road roadInput : routeInput.getRoad()) {
                    route.add(roadNetwork.findByUserId(roadInput.getId()));
                }
                if (route.size() == 0) {
                    logger.error("route with name \"{}\" does not contain any roadSegments. Ignore route!",
                            route.getName());
                    continue;
                }
                Route r = routes.put(route.getName(), route);
                if (r != null) {
                    logger.error("route with name \"{}\" already defined. Overwrite existing route.", r.getName());
                }
            }
        }
    }

    // public VehiclesInput getVehiclesInput() {
    // return inputData.getVehiclesInput();
    // }

    public TrafficCompositionGenerator getVehicleGenerator() {
        return defaultTrafficComposition;
    }

    public ProjectMetaData getProjectMetaData() {
        return projectMetaData;
    }

    // public InputData getSimInput() {
    // return inputData;
    // }

    public SimulationOutput getSimOutput() {
        return simOutput;
    }

    public RoadNetwork getRoadNetwork() {
        return roadNetwork;
    }

    public SimulationRunnable getSimulationRunnable() {
        return simulationRunnable;
    }

    /**
     * Load scenario from xml.
     * 
     * @param scenario
     * @param path
     * @throws JAXBException
     * @throws SAXException
     */
    public void loadScenarioFromXml(String scenario, String path) throws JAXBException, SAXException {
        roadNetwork.clear();
        
//        String scenario = scenarioWithEnding.substring(0, scenarioWithEnding.length() - 4);
//        System.out.println("scenario = " + scenario);
        
        projectMetaData.setProjectName(scenario);
        projectMetaData.setPathToProjectXmlFile(path);
        initialize();
    }

    /**
     * @param roads
     */
    private void matchRoadSegmentsAndRoadInput(List<Road> roads) {
        for (final Road roadInput : roads) {
            final RoadSegment roadSegment = roadNetwork.findByUserId(roadInput.getId());
            if (roadSegment != null) {
                addInputToRoadSegment(roadSegment, roadInput);
            } else {
                // at least warn user that roadId cannot be matched to xodr roadnetwork
                // TODO add option to exit here if user sets option. Such input errors are just annoying.
                logger.warn("cannot find roadId={} from input in constructed roadNetwork. IGNORE DATA!!!",
                        roadInput.getId());
            }
        }
    }

    // /**
    // * This is the default defaultTrafficComposition for all roadSegments as long as no individual vehicle composition
    // of a
    // * roadSegment is defined
    // *
    // * @param inputData2
    // */
    // private VehicleGeneratorOld createVehicleGenerator() {
    // Preconditions.checkNotNull(inputData);
    // return new VehicleGeneratorOld(simulationRunnable.timeStep(), inputData.getVehiclePrototypes(), inputData
    // .getSimulation().getTrafficComposition(), fuelConsumptionModelPool, routes);
    // }

    /**
     * There was no xodr file and there is only one road segment in the MovSimXML file so set up a default s-shaped road
     * mapping.
     * 
     * @param road
     */
    private void defaultTestingRoadMapping(Road roadInput) {
        logger.warn("Simulation with test network");
        final int laneCount = 1;
        final double roadLength = 1500;
        final RoadMapping roadMapping = new RoadMappingPolyS(laneCount, 10, 50, 50, 100.0 / Math.PI, roadLength);
        final RoadSegment roadSegment = new RoadSegment(roadMapping);
        addInputToRoadSegment(roadSegment, roadInput);
        roadSegment.setUserId("1");
        roadSegment.addDefaultSink();
        roadNetwork.add(roadSegment);
    }

    /**
     * Parse the OpenDrive (.xodr) file to load the network topology and road layout.
     * 
     * @param projectMetaData
     * @return
     * @throws SAXException
     * @throws JAXBException
     */
    private static boolean parseOpenDriveXml(RoadNetwork roadNetwork, ProjectMetaData projectMetaData)
            throws JAXBException, SAXException {
        final String xodrFileName = projectMetaData.getXodrNetworkFilename();
        final String xodrPath = projectMetaData.getPathToProjectXmlFile();
        final String fullXodrFileName = xodrPath + xodrFileName;
        logger.info("try to load {}", fullXodrFileName);
        final boolean loaded = OpenDriveReader.loadRoadNetwork(roadNetwork, fullXodrFileName);
        logger.info("done with parsing road network {}. Success: {}", fullXodrFileName, loaded);
        return loaded;
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
    private void addInputToRoadSegment(RoadSegment roadSegment, Road roadInput) {
        // setup own vehicle generator for roadSegment: needed for trafficSource and initial conditions
        TrafficCompositionGenerator composition = roadInput.isSetTrafficComposition() ? new TrafficCompositionGenerator(
                roadInput.getTrafficComposition(), vehicleFactory) : defaultTrafficComposition;
        if (roadInput.isSetTrafficComposition()) {
            logger.info("road with id={} has its own vehicle composition generator.", roadSegment.userId());
        }

        // set up the traffic source
        if (roadInput.isSetTrafficSource()) {
            final org.movsim.core.autogen.TrafficSource trafficSourceData = roadInput.getTrafficSource();
            if (trafficSourceData.isSetInflow()) {
                final InflowTimeSeries inflowTimeSeries = new InflowTimeSeries(trafficSourceData.getInflow());
                final TrafficSource trafficSource = new TrafficSource(composition, roadSegment, inflowTimeSeries);
                if (trafficSourceData.isLogging()) {
                    trafficSource.setRecorder(new FileTrafficSourceData(roadSegment.userId()));
                }
                roadSegment.setTrafficSource(trafficSource);
            }
        }

        // set up simple ramp with dropping mechanism
        if (roadInput.isSetSimpleRamp()) {
            org.movsim.core.autogen.SimpleRamp simpleRampData = roadInput.getSimpleRamp();
            InflowTimeSeries inflowTimeSeries = new InflowTimeSeries(simpleRampData.getInflow());
            SimpleRamp simpleRamp = new SimpleRamp(defaultTrafficComposition, roadSegment, simpleRampData,
                    inflowTimeSeries);
            if (simpleRampData.isLogging()) {
                simpleRamp.setRecorder(new FileTrafficSourceData(roadSegment.userId()));
            }
            roadSegment.setSimpleRamp(simpleRamp);
        }

        // set up the traffic lights
        final org.movsim.core.autogen.TrafficLights trafficLightsInput = roadInput.getTrafficLights();
        final TrafficLights trafficLights = new TrafficLights(roadSegment.roadLength(), trafficLightsInput);
        if (trafficLightsInput.isLogging()) {
            final int nDt = trafficLightsInput.getNDt().intValue();
            trafficLights.setRecorder(new FileTrafficLightRecorder(nDt, trafficLights, roadSegment));
        }
        roadSegment.setTrafficLights(trafficLights);

        // set up the speed limits
        final SpeedLimits speedLimits = new SpeedLimits(roadInput.getSpeedLimits().getSpeedLimit());
        roadSegment.setSpeedLimits(speedLimits);

        // set up the slopes
        final Slopes slopes = new Slopes(roadInput.getSlopes().getSlope());
        roadSegment.setSlopes(slopes);

        // set up the detectors
        if (roadInput.isSetDetectors()) {
            roadSegment.setLoopDetectors(new LoopDetectors(roadSegment, roadInput.getDetectors()));
        }
        // set up the flow conserving bottlenecks
        if (roadInput.isSetFlowConservingInhomogeneities()) {
            roadSegment.setFlowConservingBottlenecks(new FlowConservingBottlenecks(roadInput
                    .getFlowConservingInhomogeneities()));
        }

        if (roadInput.isSetInitialConditions()) {
            initialConditions(roadSegment, roadInput.getInitialConditions(), defaultTrafficComposition);
        }

        // final TrafficSinkData trafficSinkData = roadinput.getTrafficSinkData();
    }

    private static void initialConditions(RoadSegment roadSegment, InitialConditions initialConditions,
            TrafficCompositionGenerator vehGenerator) {
        Preconditions.checkNotNull(initialConditions);

        if (initialConditions.isSetMacroIC()) {
            setMacroInitialConditions(roadSegment, initialConditions.getMacroIC(), vehGenerator);
        } else if (initialConditions.isSetMicroIC()) {
            setMicroInitialConditions(roadSegment, initialConditions.getMicroIC(), vehGenerator);
        }
 else {
            throw new IllegalStateException();
        }
    }

    /**
     * Determine vehicle positions on all relevant lanes while considering minimum gaps to avoid accidents. Gaps are
     * left at the beginning and the end of the road segment on purpose. However, the consistency check is not complete
     * and other segments are not considered.
     * 
     * @param roadSegment
     * @param roadInput
     * @param defaultTrafficComposition
     * @param icMacroData
     */
    private static void setMacroInitialConditions(RoadSegment roadSegment, List<MacroIC> macroInitialConditions,
            TrafficCompositionGenerator vehGenerator) {

        logger.info("choose macro initial conditions: generate vehicles from macro-localDensity ");
        final InitialConditionsMacro icMacro = new InitialConditionsMacro(macroInitialConditions);

        final Iterator<LaneSegment> laneSegmentIterator = roadSegment.laneSegmentIterator();
        while (laneSegmentIterator.hasNext()) {
            LaneSegment lane = laneSegmentIterator.next();
            if (lane.type() != Lane.Type.TRAFFIC) {
                logger.debug("no macroscopic initial conditions for non-traffic lanes (slip roads etc).");
                continue;
            }

            double position = roadSegment.roadLength(); // start at end of segment
            while (position > 0) {
                final TestVehicle testVehicle = vehGenerator.getTestVehicle();

                final double rhoLocal = icMacro.rho(position);
                double speedInit =  icMacro.hasUserDefinedSpeeds() ? icMacro.vInit(position) : testVehicle.getEquilibriumSpeed(rhoLocal);
                if (logger.isDebugEnabled() && !icMacro.hasUserDefinedSpeeds()) {
                    logger.debug("use equilibrium speed={} in macroscopic initial conditions.", speedInit);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(String
                            .format("macroscopic init conditions from input: roadId=%s, x=%.3f, rho(x)=%.3f/km, speed=%.2fkm/h",
                                    roadSegment.id(), position, Units.INVM_TO_INVKM * rhoLocal, Units.MS_TO_KMH
                                            * speedInit));
                }

                if (rhoLocal <= 0) {
                    logger.debug("no vehicle added at x={} for vanishing initial localDensity={}.", position, rhoLocal);
                    position -= 50; // move on in upstream direction
                    continue;
                }

                final Vehicle veh = vehGenerator.createVehicle(testVehicle);
                final double meanDistanceInLane = 1. / (rhoLocal + MovsimConstants.SMALL_VALUE);
                // TODO icMacro for ca
                // final double minimumGap = veh.getLongitudinalModel().isCA() ? veh.getLength() : veh.getLength() +
                // veh.getLongitudinalModel().getS0();
                final double minimumGap = veh.getLength() + veh.getLongitudinalModel().getMinimumGap();
                final double posDecrement = Math.max(meanDistanceInLane, minimumGap);
                position -= posDecrement;

                if (position <= posDecrement) {
                    logger.debug("leave minimum gap at origin of road segment and start with next lane, pos={}",
                            position);
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
                } else {
                    logger.debug("cannot add vehicle due to gap constraints at pos={} with speed={}.", position,
                            speedInit);
                }

            }
        }
    }

    private static void setMicroInitialConditions(RoadSegment roadSegment,
 List<MicroIC> initialMicroConditions,
            TrafficCompositionGenerator vehGenerator) {
        logger.debug(("choose micro initial conditions"));
        int vehicleNumber = 1;
        for (final MicroIC ic : initialMicroConditions) {
            // TODO counter
            final String vehTypeFromFile = ic.getLabel();
            final Vehicle veh = (vehTypeFromFile.length() == 0) ? vehGenerator.createVehicle() : vehGenerator
                    .createVehicle(vehTypeFromFile);
            veh.setVehNumber(vehicleNumber);
            vehicleNumber++;
            // testwise:
            veh.setFrontPosition(Math.round(ic.getPosition() / veh.physicalQuantities().getxScale()));
            veh.setSpeed(Math.round(ic.getSpeed() / veh.physicalQuantities().getvScale()));
            final int lane = ic.getLane().intValue();
            if (lane <= 0 || lane > roadSegment.laneCount()) {
                logger.error("Error: lane=" + lane + " on road id=" + roadSegment.userId()
                        + " does not exist. Choose as initial condition a lane between 1 and "
                        + roadSegment.laneCount());
                throw new IllegalArgumentException("lane=" + lane
                        + " given in initial condition does not exist for road=" + roadSegment.id());
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
        simOutput = new SimulationOutput(simulationRunnable.timeStep(), projectMetaData.isInstantaneousFileOutput(),
                inputData, roadNetwork, routes);
        obstacleCount = roadNetwork.obstacleCount();
    }

    public void runToCompletion() {
        logger.info("Simulator.run: start simulation at {} seconds of simulation project={}",
                simulationRunnable.simulationTime(), projectName);

        startTimeMillis = System.currentTimeMillis();
        // TODO check if first output update has to be called in update for external call!!
        // TODO FloatingCars do not need this call. First output line for t=0 is written twice to file
        // simOutput.timeStep(simulationRunnable.timeStep(), simulationRunnable.simulationTime(),
        // simulationRunnable.iterationCount());
        simulationRunnable.runToCompletion();
    }

    /**
     * Returns true if the simulation has finished.
     */
    public boolean isFinished() {
        if (simulationRunnable.simulationTime() > 60.0 && roadNetwork.vehicleCount() == obstacleCount) {
            return true;
        }
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
