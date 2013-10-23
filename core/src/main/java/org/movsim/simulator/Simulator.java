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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.movsim.autogen.CrossSection;
import org.movsim.autogen.InitialConditions;
import org.movsim.autogen.MacroIC;
import org.movsim.autogen.MicroIC;
import org.movsim.autogen.Movsim;
import org.movsim.autogen.Road;
import org.movsim.autogen.Simulation;
import org.movsim.autogen.TrafficSinkType;
import org.movsim.autogen.TrafficSourceType;
import org.movsim.input.ProjectMetaData;
import org.movsim.input.network.OpenDriveReader;
import org.movsim.output.FileTrafficSinkData;
import org.movsim.output.FileTrafficSourceData;
import org.movsim.output.SimulationOutput;
import org.movsim.simulator.observer.ServiceProviders;
import org.movsim.simulator.roadnetwork.InitialConditionsMacro;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.boundaries.AbstractTrafficSource;
import org.movsim.simulator.roadnetwork.boundaries.InflowTimeSeries;
import org.movsim.simulator.roadnetwork.boundaries.MicroInflowFileReader;
import org.movsim.simulator.roadnetwork.boundaries.SimpleRamp;
import org.movsim.simulator.roadnetwork.boundaries.TrafficSourceMacro;
import org.movsim.simulator.roadnetwork.boundaries.TrafficSourceMicro;
import org.movsim.simulator.roadnetwork.controller.FlowConservingBottleneck;
import org.movsim.simulator.roadnetwork.controller.LoopDetector;
import org.movsim.simulator.roadnetwork.controller.RoadObject;
import org.movsim.simulator.roadnetwork.controller.TrafficLight;
import org.movsim.simulator.roadnetwork.controller.TrafficLights;
import org.movsim.simulator.roadnetwork.controller.VariableMessageSignDiversion;
import org.movsim.simulator.roadnetwork.regulator.Regulators;
import org.movsim.simulator.roadnetwork.routing.Routing;
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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Simulator.class);

    private long startTimeMillis;

    private final ProjectMetaData projectMetaData;
    private String projectName;

    private final Movsim inputData;

    private VehicleFactory vehicleFactory;
    private TrafficCompositionGenerator defaultTrafficComposition;
    private TrafficLights trafficLights;
    private Regulators regulators;

    private ServiceProviders serviceProviders;

    private SimulationOutput simOutput;
    private final RoadNetwork roadNetwork;
    private Routing routing;
    private final SimulationRunnable simulationRunnable;
    private int obstacleCount;
    private long timeOffsetMillis;

    /**
     * Constructor.
     * 
     * @param inputData
     * 
     * @throws SAXException
     * @throws JAXBException
     */
    public Simulator(Movsim inputData) {
        this.projectMetaData = ProjectMetaData.getInstance();
	this.inputData = Preconditions.checkNotNull(inputData);
        roadNetwork = new RoadNetwork();
        simulationRunnable = new SimulationRunnable(this);
        simulationRunnable.setCompletionCallback(this);
    }

    public void initialize() throws JAXBException, SAXException {
        LOG.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and Martin Budden (2011-2013)");

        projectName = projectMetaData.getProjectName();
        timeOffsetMillis = 0;
        if (inputData.getScenario().getSimulation().isSetTimeOffset()) {
            DateTime dateTime = LocalDateTime.parse(inputData.getScenario().getSimulation().getTimeOffset(),
                    DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZ")).toDateTime(DateTimeZone.UTC);
            timeOffsetMillis = dateTime.getMillis();
            LOG.info("global time offset set={} --> {} milliseconds.", dateTime, timeOffsetMillis);
            ProjectMetaData.getInstance().setTimeOffsetMillis(timeOffsetMillis);
        }
        projectMetaData.setXodrNetworkFilename(inputData.getScenario().getNetworkFilename()); // TODO

        Simulation simulationInput = inputData.getScenario().getSimulation();

        parseOpenDriveXml(roadNetwork, projectMetaData);
        routing = new Routing(inputData.getScenario().getRoutes(), roadNetwork);

        vehicleFactory = new VehicleFactory(simulationInput.getTimestep(), inputData.getVehiclePrototypes(),
                inputData.getConsumption(), routing);

        roadNetwork.setWithCrashExit(simulationInput.isCrashExit());

        simulationRunnable.setTimeStep(simulationInput.getTimestep());

        // TODO better handling of case "duration = INFINITY"
        double duration = simulationInput.isSetDuration() ? simulationInput.getDuration() : -1;

        simulationRunnable.setDuration(duration < 0 ? Double.MAX_VALUE : duration);

        if (simulationInput.isWithSeed()) {
            MyRandom.initializeWithSeed(simulationInput.getSeed());
        }

        defaultTrafficComposition = new TrafficCompositionGenerator(simulationInput.getTrafficComposition(),
                vehicleFactory);

        trafficLights = new TrafficLights(inputData.getScenario().getTrafficLights(), roadNetwork);

        regulators = new Regulators(inputData.getScenario().getRegulators(), roadNetwork);

        checkTrafficLightseInitialized();

	serviceProviders = new ServiceProviders(inputData.getServiceProviders(), routing, roadNetwork);

        // For each road in the MovSim XML input data, find the corresponding roadSegment and
        // set its input data accordingly
        matchRoadSegmentsAndRoadInput(simulationInput.getRoad());

        reset();
        startTimeMillis = System.currentTimeMillis();
    }

    public Iterable<String> getVehiclePrototypeLabels() {
        return vehicleFactory.getLabels();
    }

    public TrafficCompositionGenerator getVehicleGenerator() {
        return defaultTrafficComposition;
    }

    public ProjectMetaData getProjectMetaData() {
        return projectMetaData;
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
     * @throws ParserConfigurationException
     */
    public void loadScenarioFromXml(String scenario, String path) throws JAXBException, SAXException {
        roadNetwork.clear();
        projectMetaData.setProjectName(scenario);
        projectMetaData.setPathToProjectXmlFile(path);
        initialize();
    }

    private void matchRoadSegmentsAndRoadInput(List<Road> roads) {
        for (Road roadInput : roads) {
            LOG.info("roadInput.getId()={}", roadInput.getId());
            RoadSegment roadSegment = Preconditions.checkNotNull(roadNetwork.findByUserId(roadInput.getId()),
                    "cannot find roadId=\"" + roadInput.getId() + "\" in road network.");
            addInputToRoadSegment(roadSegment, roadInput);
        }
        createSignalPoints();
    }

    private void checkTrafficLightseInitialized() {
        for (RoadSegment roadSegment : roadNetwork) {
            for (TrafficLight trafficLight : roadSegment.trafficLights()) {
                if (trafficLight.status() == null) {
                    throw new IllegalArgumentException("trafficLight=" + trafficLight.signalId() + " on road="
                            + roadSegment.userId() + " hat not been initialized. Check movsim regulator input.");
                }
            }
        }
    }

    private void createSignalPoints() {
        // adding of RoadObjects to RoadSegment must be finished here
        for (RoadSegment roadSegment : roadNetwork) {
            for (RoadObject roadObject : roadSegment.roadObjects()) {
                roadObject.createSignalPositions();
            }
        }
    }

    /**
     * Parse the OpenDrive (.xodr) file to load the network topology and road layout.
     * 
     * @param projectMetaData
     * @return
     * @throws SAXException
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    private static boolean parseOpenDriveXml(RoadNetwork roadNetwork, ProjectMetaData projectMetaData)
            throws JAXBException, SAXException {
        final String xodrFileName = projectMetaData.getXodrNetworkFilename();
        final String xodrPath = projectMetaData.getPathToProjectFile();
        final String fullXodrFileName = xodrPath + xodrFileName;
        LOG.info("try to load {}", fullXodrFileName);
        final boolean loaded = OpenDriveReader.loadRoadNetwork(roadNetwork, fullXodrFileName);
        LOG.info("done with parsing road network {}. Success: {}", fullXodrFileName, loaded);
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
            LOG.info("road with id={} has its own vehicle composition generator.", roadSegment.id());
        }

        // set up the traffic source
        if (roadInput.isSetTrafficSource()) {
            TrafficSourceType trafficSourceData = roadInput.getTrafficSource();
            AbstractTrafficSource trafficSource = null;
            if (trafficSourceData.isSetInflow()) {
                InflowTimeSeries inflowTimeSeries = new InflowTimeSeries(trafficSourceData.getInflow());
                trafficSource = new TrafficSourceMacro(composition, roadSegment, inflowTimeSeries);
            } else if (trafficSourceData.isSetInflowFromFile()) {
                trafficSource = new TrafficSourceMicro(composition, roadSegment);
                MicroInflowFileReader reader = new MicroInflowFileReader(trafficSourceData.getInflowFromFile(),
                        roadSegment.laneCount(), timeOffsetMillis, routing, (TrafficSourceMicro) trafficSource);
                reader.readData();
            }
            if (trafficSource != null) {
                if (trafficSourceData.isLogging()) {
                    trafficSource.setRecorder(new FileTrafficSourceData(roadSegment.userId()));
                }
                roadSegment.setTrafficSource(trafficSource);
            }
        }

        // set up the traffic sink
        if (roadInput.isSetTrafficSink()) {
            configureTrafficSink(roadInput.getTrafficSink(), roadSegment);
        }

        // set up simple ramp with dropping mechanism
        if (roadInput.isSetSimpleRamp()) {
            org.movsim.autogen.SimpleRamp simpleRampData = roadInput.getSimpleRamp();
            InflowTimeSeries inflowTimeSeries = new InflowTimeSeries(simpleRampData.getInflow());
            SimpleRamp simpleRamp = new SimpleRamp(composition, roadSegment, simpleRampData, inflowTimeSeries);
            if (simpleRampData.isLogging()) {
                simpleRamp.setRecorder(new FileTrafficSourceData(roadSegment.userId()));
            }
            roadSegment.setSimpleRamp(simpleRamp);
        }

        // set up the detectors
        if (roadInput.isSetDetectors()) {
            boolean log = roadInput.getDetectors().isLogging();
            boolean logLanes = roadInput.getDetectors().isLoggingLanes();
            double sampleDt = roadInput.getDetectors().getSampleInterval();
            for (CrossSection crossSection : roadInput.getDetectors().getCrossSection()) {
                LoopDetector det = new LoopDetector(roadSegment, crossSection.getPosition(), sampleDt, log, logLanes);
                roadSegment.roadObjects().add(det);
            }
        }
        // set up the flow conserving bottlenecks
        if (roadInput.isSetFlowConservingInhomogeneities()) {
            for (org.movsim.autogen.Inhomogeneity inhomogeneity : roadInput.getFlowConservingInhomogeneities()
                    .getInhomogeneity()) {
                FlowConservingBottleneck flowConservingBottleneck = new FlowConservingBottleneck(inhomogeneity,
                        roadSegment);
                roadSegment.roadObjects().add(flowConservingBottleneck);
            }
        }

        if (roadInput.isSetVariableMessageSignDiversions()) {
            for (org.movsim.autogen.VariableMessageSignDiversion diversion : roadInput
                    .getVariableMessageSignDiversions().getVariableMessageSignDiversion()) {
                VariableMessageSignDiversion variableMessageSignDiversion = new VariableMessageSignDiversion(
                        diversion.getPosition(), diversion.getValidLength(), roadSegment);
                roadSegment.roadObjects().add(variableMessageSignDiversion);
            }
        }

        if (roadInput.isSetInitialConditions()) {
            initialConditions(roadSegment, roadInput.getInitialConditions(), composition);
        }
    }

    private static void configureTrafficSink(TrafficSinkType trafficSinkType, RoadSegment roadSegment) {
        if(!roadSegment.hasSink()){
            throw new IllegalArgumentException("roadsegment=" + roadSegment.userId() + " does not have a TrafficSink.");
        }
        if (trafficSinkType.isLogging()) {
            roadSegment.sink().setRecorder(new FileTrafficSinkData(roadSegment.userId()));
        }
    }

    private static void initialConditions(RoadSegment roadSegment, InitialConditions initialConditions,
            TrafficCompositionGenerator vehGenerator) {
        Preconditions.checkNotNull(initialConditions);
        if (initialConditions.isSetMacroIC()) {
            setMacroInitialConditions(roadSegment, initialConditions.getMacroIC(), vehGenerator);
        } else if (initialConditions.isSetMicroIC()) {
            setMicroInitialConditions(roadSegment, initialConditions.getMicroIC(), vehGenerator);
        } else {
            LOG.info("no initial conditions defined");
        }
    }

    /**
     * Determine vehicle positions on all relevant lanes while considering minimum gaps to avoid accidents. Gaps are
     * left at the beginning and the end of the road segment on purpose. However, the consistency check is not complete
     * and other segments are not considered.
     * 
     * @param roadSegment
     * @param macroInitialConditions
     * @param vehGenerator
     */
    private static void setMacroInitialConditions(RoadSegment roadSegment, List<MacroIC> macroInitialConditions,
            TrafficCompositionGenerator vehGenerator) {

        LOG.info("choose macro initial conditions: generate vehicles from macro-localDensity ");
        final InitialConditionsMacro icMacro = new InitialConditionsMacro(macroInitialConditions);

        for (LaneSegment laneSegment : roadSegment.laneSegments()) {
            if (laneSegment.type() != Lanes.Type.TRAFFIC) {
                LOG.debug("no macroscopic initial conditions for non-traffic lanes (slip roads etc).");
                continue;
            }

            double position = roadSegment.roadLength(); // start at end of segment
            while (position > 0) {
                final TestVehicle testVehicle = vehGenerator.getTestVehicle();

                final double rhoLocal = icMacro.rho(position);
                double speedInit = icMacro.hasUserDefinedSpeeds() ? icMacro.vInit(position) : testVehicle
                        .getEquilibriumSpeed(rhoLocal);
                if (LOG.isDebugEnabled() && !icMacro.hasUserDefinedSpeeds()) {
                    LOG.debug("use equilibrium speed={} in macroscopic initial conditions.", speedInit);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String
                            .format("macroscopic init conditions from input: roadId=%s, x=%.3f, rho(x)=%.3f/km, speed=%.2fkm/h",
                                    roadSegment.id(), position, Units.INVM_TO_INVKM * rhoLocal, Units.MS_TO_KMH
                                            * speedInit));
                }

                if (rhoLocal <= 0) {
                    LOG.debug("no vehicle added at x={} for vanishing initial localDensity={}.", position, rhoLocal);
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
                    LOG.debug("leave minimum gap at origin of road segment and start with next lane, pos={}", position);
                    break;
                }
                final Vehicle leader = laneSegment.rearVehicle();
                final double gapToLeader = (leader == null) ? MovsimConstants.GAP_INFINITY : leader.getRearPosition()
                        - position;

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(
                            "meanDistance=%.3f, minimumGap=%.2f, posDecrement=%.3f, gapToLeader=%.3f\n",
                            meanDistanceInLane, minimumGap, posDecrement, gapToLeader));
                }

                if (gapToLeader > 0) {
                    veh.setFrontPosition(position);
                    veh.setSpeed(speedInit);
                    veh.setLane(laneSegment.lane());
                    LOG.debug("add vehicle from macroscopic initial conditions at pos={} with speed={}.", position,
                            speedInit);
                    roadSegment.addVehicle(veh);
                } else {
                    LOG.debug("cannot add vehicle due to gap constraints at pos={} with speed={}.", position, speedInit);
                }

            }
        }
    }

    private static void setMicroInitialConditions(RoadSegment roadSegment, List<MicroIC> initialMicroConditions,
            TrafficCompositionGenerator vehGenerator) {
        LOG.debug(("choose micro initial conditions"));
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
            final int lane = ic.getLane();
            if (lane < Lanes.MOST_INNER_LANE || lane > roadSegment.laneCount()) {
                throw new IllegalArgumentException("lane=" + lane
                        + " given in initial condition does not exist for road=" + roadSegment.id()
                        + " which has a laneCount of " + roadSegment.laneCount());
            }
            veh.setLane(lane);
            roadSegment.addVehicle(veh);
            LOG.info(String.format("set vehicle with label = %s on lane=%d with front at x=%.2f, speed=%.2f",
                    veh.getLabel(), veh.lane(), veh.getFrontPosition(), veh.getSpeed()));
            if (veh.getLongitudinalModel().isCA()) {
                LOG.info(String.format("and for the CA in physical quantities: front position at x=%.2f, speed=%.2f",
                        veh.physicalQuantities().getFrontPosition(), veh.physicalQuantities().getSpeed()));
            }
        }
    }

    public void reset() {
        simulationRunnable.reset();
        if (inputData.getScenario().isSetOutputConfiguration()) {
            simOutput = new SimulationOutput(simulationRunnable.timeStep(),
                    projectMetaData.isInstantaneousFileOutput(), inputData.getScenario().getOutputConfiguration(),
                    roadNetwork, routing, vehicleFactory);
        }
        obstacleCount = roadNetwork.obstacleCount();
    }

    public void runToCompletion() {
        LOG.info("Simulator.run: start simulation at {} seconds of simulation project={}",
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
        LOG.info(String.format("Simulator.run: stop after time = %.2fs = %.2fh of simulation project=%s",
                simulationTime, simulationTime / 3600., projectName));

        regulators.simulationCompleted(simulationTime);
        
        LOG.info("total traveltime={} seconds", roadNetwork.totalVehicleTravelTime());
        LOG.info("total distance traveled={} meters", roadNetwork.totalVehicleTravelDistance());
        
        long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format(
                    "time elapsed=%d milliseconds --> simulation time warp = %.2f, time per 1000 update steps=%.3fs",
                    elapsedTimeMillis, simulationTime / TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis),
                    (1000. * TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) / simulationRunnable.iterationCount())));
            LOG.info("remaining vehicles in simulation after completion:\n {}", showAllVehicles());
        }
    }

    private String showAllVehicles() {
        int counter = 0;
        StringBuilder sb = new StringBuilder();
        for (RoadSegment roadSegment : roadNetwork) {
            Iterator<Vehicle> iterator = roadSegment.iterator();
            while (iterator.hasNext()) {
                Vehicle vehicle = iterator.next();
                if (vehicle.type() == Vehicle.Type.OBSTACLE) {
                    continue;
                }
                counter++;
                sb.append(counter).append(": ").append(vehicle.toString());
                sb.append(" on segment: ").append(roadSegment.toString());
                sb.append("\n");
            }
        }
        sb.append("total vehicles remaining in network after completion: ").append(counter).append(" vehicles");
        return sb.toString();
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (LOG.isInfoEnabled() && iterationCount % 1000 == 0) {
            int numberOfVehicles = roadNetwork.vehicleCount() - roadNetwork.getObstacleCount();
            LOG.info(String.format("Simulator.update :time = %.2fs = %.2fh, dt = %.2fs, vehicles=%d, projectName=%s",
                    simulationTime, simulationTime / 3600, dt, numberOfVehicles, projectName));
        }

        trafficLights.timeStep(dt, simulationTime, iterationCount);
        regulators.timeStep(dt, simulationTime, iterationCount);
        roadNetwork.timeStep(dt, simulationTime, iterationCount);

        if (simOutput != null) {
            simOutput.timeStep(dt, simulationTime, iterationCount);
        }
    }

    public Regulators getRegulators() {
        return regulators;
    }
}
