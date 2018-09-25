/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim.org@gmail.com>
 * ----------------------------------------------------------------------------------------- This file is part of MovSim - the
 * multi-model open-source vehicular-traffic simulator. MovSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator;

import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.movsim.autogen.*;
import org.movsim.input.ProjectMetaData;
import org.movsim.input.network.OpenDriveReader;
import org.movsim.output.FileTrafficSinkData;
import org.movsim.output.FileTrafficSourceData;
import org.movsim.output.SimulationOutput;
import org.movsim.scenario.boundary.autogen.BoundaryConditionsType;
import org.movsim.scenario.vehicle.autogen.MovsimExternalVehicleControl;
import org.movsim.shutdown.ShutdownHooks;
import org.movsim.simulator.observer.ServiceProviders;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.RoadTypeSpeeds;
import org.movsim.simulator.roadnetwork.boundaries.*;
import org.movsim.simulator.roadnetwork.boundaries.SimpleRamp;
import org.movsim.simulator.roadnetwork.controller.*;
import org.movsim.simulator.roadnetwork.controller.TrafficLights;
import org.movsim.simulator.roadnetwork.controller.VariableMessageSignDiversion;
import org.movsim.simulator.roadnetwork.regulator.Regulators;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.movsim.simulator.vehicles.ExternalVehiclesController;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleFactory;
import org.movsim.utilities.MyRandom;
import org.movsim.xml.InputLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class Simulator implements SimulationTimeStep, SimulationRun.CompletionCallback {

    private static final Logger LOG = LoggerFactory.getLogger(Simulator.class);

    private long startTimeMillis;

    private final ProjectMetaData projectMetaData;

    private String projectName;

    private Movsim movsimInput;

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
     */
    public Simulator(Movsim inputData) {
        this.projectMetaData = ProjectMetaData.getInstance();
        ShutdownHooks.INSTANCE.clear(); // TODO move to better place
        this.movsimInput = Preconditions.checkNotNull(inputData);
        if (movsimInput.isSetRoadTypeSpeedMappings()) {
            RoadTypeSpeeds.INSTANCE.init(inputData.getRoadTypeSpeedMappings());
        }
        roadNetwork = new RoadNetwork();
        simulationRunnable = new SimulationRunnable(this);
        simulationRunnable.setCompletionCallback(this);
    }

    public void initialize() {
        LOG.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and Martin Budden (2011-2013)");

        projectName = projectMetaData.getProjectName();
        movsimInput = InputLoader.unmarshallMovsim(projectMetaData.getInputFile());

        timeOffsetMillis = 0;
        if (movsimInput.getScenario().getSimulation().isSetTimeOffset()) {
            DateTime dateTime = LocalDateTime.parse(movsimInput.getScenario().getSimulation().getTimeOffset(),
                    DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZ")).toDateTime(DateTimeZone.UTC);
            timeOffsetMillis = dateTime.getMillis();
            LOG.info("global time offset set={} --> {} milliseconds.", dateTime, timeOffsetMillis);
            ProjectMetaData.getInstance();
            ProjectMetaData.getInstance().setTimeOffsetMillis(timeOffsetMillis);
        }
        projectMetaData.setXodrNetworkFilename(movsimInput.getScenario().getNetworkFilename()); // TODO

        Simulation simulationInput = movsimInput.getScenario().getSimulation();

        parseOpenDriveXml(roadNetwork, projectMetaData);
        routing = new Routing(movsimInput.getScenario().getRoutes(), roadNetwork);

        if (movsimInput.isSetServiceProviders()) {
            serviceProviders = new ServiceProviders(movsimInput.getServiceProviders(), routing, roadNetwork);
        }

        vehicleFactory = new VehicleFactory(simulationInput.getTimestep(), movsimInput.getVehiclePrototypes(),
                movsimInput.getConsumption(), routing, serviceProviders);

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

        trafficLights = new TrafficLights(movsimInput.getScenario().getTrafficLights(), roadNetwork);

        regulators = new Regulators(movsimInput.getScenario().getRegulators(), roadNetwork);

        ExternalVehiclesController externalVehicleController = createExternalVehicleController();
        roadNetwork.setExternalVehicleController(externalVehicleController);

        checkTrafficLightBeingInitialized();

        MicroscopicBoundaryConditions microBoundaryConditions = null;
        if (movsimInput.getScenario().isSetMicroBoundaryConditionsFilename()) {
            String filename = movsimInput.getScenario().getMicroBoundaryConditionsFilename();
            File microBCFile = projectMetaData.getFile(filename);
            microBoundaryConditions = new MicroscopicBoundaryConditions(microBCFile);
        }

        // For each road in the MovSim XML input data, find the corresponding roadSegment and
        // set its input data accordingly
        matchRoadSegmentsAndRoadInput(simulationInput.getRoad(), microBoundaryConditions);

        if (movsimInput.getScenario().isSetInitialConditionsFilename()) {
            String filename = movsimInput.getScenario().getInitialConditionsFilename();
            File icFile = projectMetaData.getFile(filename);
            InitialConditions initialConditions = new InitialConditions(icFile);
            initialConditions.setInitialConditions(roadNetwork, defaultTrafficComposition);
        }

        reset();
        startTimeMillis = System.currentTimeMillis();
    }

    private ExternalVehiclesController createExternalVehicleController() {
        ExternalVehiclesController externalVehicleController = new ExternalVehiclesController();
        if (movsimInput.getScenario().isSetExternalVehicleControlFilename()) {
            String filename = movsimInput.getScenario().getExternalVehicleControlFilename();
            File file = projectMetaData.getFile(filename);
            Preconditions.checkArgument(file.exists(), "external vehicle control file " + file + " not found");
            MovsimExternalVehicleControl input = InputLoader.unmarshallExternalVehicleControl(file);
            LOG.info("loaded external vehicle control from file={}", file);
            externalVehicleController.setInput(input);
        }
        return externalVehicleController;
    }

    public Iterable<String> getVehiclePrototypeLabels() {
        return vehicleFactory.getLabels();
    }

    public TrafficCompositionGenerator getVehicleGenerator() {
        return defaultTrafficComposition;
    }

    public ProjectMetaData getProjectMetaData() {
        return ProjectMetaData.getInstance();
    }

    public RoadNetwork getRoadNetwork() {
        return roadNetwork;
    }

    public SimulationRunnable getSimulationRunnable() {
        return simulationRunnable;
    }

    private void matchRoadSegmentsAndRoadInput(List<Road> roads,
            MicroscopicBoundaryConditions microBoundaryConditions) {
        for (Road roadInput : roads) {
            LOG.info("roadInput.getId()={}", roadInput.getId());
            RoadSegment roadSegment = Preconditions.checkNotNull(roadNetwork.findByUserId(roadInput.getId()),
                    "cannot find roadId=\"" + roadInput.getId() + "\" in road network.");
            addInputToRoadSegment(roadSegment, roadInput, microBoundaryConditions);
        }

        createSignalPoints();
    }

    private void checkTrafficLightBeingInitialized() {
        for (RoadSegment roadSegment : roadNetwork) {
            for (TrafficLight trafficLight : roadSegment.trafficLights()) {
                if (trafficLight.status() == null) {
                    throw new IllegalArgumentException(
                            "trafficLight=" + trafficLight.signalId() + " on road=" + roadSegment.userId()
                                    + " hat not been initialized. Check movsim regulator input.");
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
     */
    private static boolean parseOpenDriveXml(RoadNetwork roadNetwork, ProjectMetaData projectMetaData) {
        File networkFile = projectMetaData.getFile(projectMetaData.getXodrNetworkFilename());
        LOG.info("try to load {}", networkFile);
        final boolean loaded = OpenDriveReader.loadRoadNetwork(roadNetwork, networkFile);
        LOG.info("done with parsing road network {}. Success: {}", networkFile, loaded);
        return loaded;
    }

    /**
     * Add input data to road segment. Note by rules of encapsulation this function is NOT a member of RoadSegment, since
     * RoadSegment should not be aware of form of XML file or RoadInput data structure.
     *
     * @param roadSegment
     * @param roadInput
     */
    private void addInputToRoadSegment(RoadSegment roadSegment, Road roadInput,
            MicroscopicBoundaryConditions microBoundaryConditions) {
        // setup own vehicle generator for roadSegment: needed for trafficSource and initial conditions
        TrafficCompositionGenerator composition = defaultTrafficComposition;

        if (roadInput.isSetTrafficComposition()) {
            composition = new TrafficCompositionGenerator(roadInput.getTrafficComposition(), vehicleFactory);
            roadSegment.setTrafficComposition(composition);
            LOG.info("road with id={} has its own vehicle composition generator.", roadSegment.id());
        }

        // set up the traffic source
        if (roadInput.isSetTrafficSource()) {
            TrafficSourceType trafficSourceData = roadInput.getTrafficSource();
            AbstractTrafficSource trafficSource;
            if (trafficSourceData.isSetInflow()) {
                // macroscopic boundary conditions
                InflowTimeSeries inflowTimeSeries = new InflowTimeSeries(trafficSourceData.getInflow());
                trafficSource = new TrafficSourceMacro(composition, roadSegment, inflowTimeSeries);
            } else if (microBoundaryConditions != null) {
                // microscopic boundary conditions
                BoundaryConditionsType boundaryConditions = microBoundaryConditions
                        .getBoundaryConditions(roadSegment.userId());
                MicroscopicBoundaryInputData inputData = new MicroscopicBoundaryInputData(boundaryConditions,
                        microBoundaryConditions.getTimeFormat(), timeOffsetMillis, routing);
                trafficSource = new TrafficSourceMicro(composition, roadSegment);
                addVehiclesToSource((TrafficSourceMicro) trafficSource, inputData);
            } else {
                throw new IllegalStateException(
                        "no micro nor macro boundary condition data provided for traffic source on roadSegment="
                                + roadSegment.userId());
            }

            if (trafficSourceData.isLogging()) {
                trafficSource.setRecorder(new FileTrafficSourceData(roadSegment.userId()));
            }
            roadSegment.setTrafficSource(trafficSource);
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

    }

    private void addVehiclesToSource(TrafficSourceMicro trafficSource, MicroscopicBoundaryInputData inputData) {
        Map<Long, Vehicle> vehicles = inputData.createVehicles((TrafficSourceMicro) trafficSource);
        for (Entry<Long, Vehicle> veh : vehicles.entrySet()) {
            trafficSource.addVehicleToQueue(veh.getKey(), veh.getValue());
        }
    }

    private static void configureTrafficSink(TrafficSinkType trafficSinkType, RoadSegment roadSegment) {
        if (!roadSegment.hasSink()) {
            throw new IllegalArgumentException("roadsegment=" + roadSegment.userId() + " does not have a TrafficSink.");
        }
        if (trafficSinkType.isLogging()) {
            roadSegment.sink().setRecorder(new FileTrafficSinkData(roadSegment.userId()));
        }
    }

    public void reset() {
        simulationRunnable.reset();
        if (movsimInput.getScenario().isSetOutputConfiguration()) {
            simOutput = new SimulationOutput(simulationRunnable.timeStep(), projectMetaData.isInstantaneousFileOutput(),
                    movsimInput.getScenario().getOutputConfiguration(), roadNetwork, routing, serviceProviders);
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
        return simulationRunnable.simulationTime() > 60.0 && roadNetwork.vehicleCount() == obstacleCount;
    }

    @Override
    public void simulationComplete(double simulationTime) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Simulator.run: stop after time = %.2fs = %.2fh of simulation project=%s",
                    simulationTime, simulationTime / 3600., projectName));
        }

        regulators.simulationCompleted(simulationTime);

        LOG.info("total traveltime={} seconds", (int) roadNetwork.totalVehicleTravelTime());
        LOG.info("total distance traveled={} meters", (int) roadNetwork.totalVehicleTravelDistance());

        long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format(
                    "time elapsed=%d milliseconds --> simulation time warp = %.2f, time per 1000 update steps=%.3fs",
                    elapsedTimeMillis, simulationTime / TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis),
                    (1000. * TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) / simulationRunnable
                            .iterationCount())));
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
