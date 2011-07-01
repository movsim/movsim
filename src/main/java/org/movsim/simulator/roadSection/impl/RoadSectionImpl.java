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
package org.movsim.simulator.roadSection.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.simulation.DetectorInput;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.output.LoopDetector;
import org.movsim.output.impl.LoopDetectors;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.FlowConservingBottlenecks;
import org.movsim.simulator.roadSection.InitialConditionsMacro;
import org.movsim.simulator.roadSection.Onramp;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.SpeedLimits;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.roadSection.UpstreamBoundary;
import org.movsim.simulator.vehicles.Moveable;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.simulator.vehicles.impl.VehicleContainerImpl;
import org.movsim.simulator.vehicles.impl.VehicleGeneratorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class RoadSectionImpl.
 */
public class RoadSectionImpl implements RoadSection {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(RoadSectionImpl.class);

    /** The road length. */
    private final double roadLength;

    /** The n lanes. */
    private final int nLanes;

    /** The dt. */
    private double dt;
    
    /** The id. */
    private long id;

    
    private final boolean withCrashExit;
    
    private boolean instantaneousFileOutput;

    /** The veh container. */
    private VehicleContainer vehContainer;

    /** The veh generator. */
    private VehicleGenerator vehGenerator;

    /** The upstream boundary. */
    private UpstreamBoundary upstreamBoundary;

    /** The flow cons bottlenecks. */
    private FlowConservingBottlenecks flowConsBottlenecks;
    
    private TrafficLightsImpl trafficLights;

    /** The speedlimits. */
    private SpeedLimits speedlimits;
    

    /** The detectors. */
    private LoopDetectors detectors = null;


    /** The simple onramps. */
    private List<Onramp> simpleOnramps = null;

    /**
     * Instantiates a new road section impl.
     * 
     * @param isWithGUI
     *            the is with gui
     * @param inputData
     *            the input data
     */
    public RoadSectionImpl(InputData inputData) {
        logger.info("Cstr. RoadSectionImpl");
        this.instantaneousFileOutput = inputData.getProjectMetaData().isInstantaneousFileOutput();
        final SimulationInput simInput = inputData.getSimulationInput();
        this.dt = simInput.getTimestep();
        this.withCrashExit = simInput.isWithCrashExit();
        this.roadLength = simInput.getSingleRoadInput().getRoadLength();
        this.nLanes = simInput.getSingleRoadInput().getLanes();
        this.id = simInput.getSingleRoadInput().getId();

        initialize(inputData);

        // TODO cross-check --> testing for correct dt setup .... concept
        // between Simulator, VehGenerator and this roadSection
        if (Math.abs(dt - vehGenerator.requiredTimestep()) > Constants.SMALL_VALUE) {
            this.dt = vehGenerator.requiredTimestep();
            logger.info("model requires specific integration timestep. sets to dt={}", dt);
        }

    }

    /**
     * Initialize.
     * 
     * @param inputData
     *            the input data
     */
    private void initialize(InputData inputData) {
        vehContainer = new VehicleContainerImpl();

        vehGenerator = new VehicleGeneratorImpl(inputData);

        final RoadInput roadInput = inputData.getSimulationInput().getSingleRoadInput();
        upstreamBoundary = new UpstreamBoundaryImpl(vehGenerator, vehContainer, roadInput.getUpstreamBoundaryData(),
                inputData.getProjectMetaData().getProjectName());

        flowConsBottlenecks = new FlowConservingBottlenecksImpl(roadInput.getFlowConsBottleneckInputData());
        speedlimits = new SpeedLimitsImpl(roadInput.getSpeedLimitInputData());
        
        trafficLights = new TrafficLightsImpl(inputData.getProjectMetaData().getProjectName(), roadInput.getTrafficLightsInput());
        
        final DetectorInput detInput = roadInput.getDetectorInput();
        if (detInput.isWithDetectors()) {
            detectors = new LoopDetectors(inputData.getProjectMetaData().getProjectName(), detInput);
        }

        initialConditions(inputData.getSimulationInput());
        
        initOnramps(inputData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#roadLength()
     */
    @Override
    public double roadLength() {
        return roadLength;
    }
    
    
    //TODO documentation
    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.RoadSection#id()
     */
    public long id() {
        return id;
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#vehContainer()
     */
    @Override
    public VehicleContainer vehContainer() {
        return vehContainer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#update(int, double)
     */
    @Override
    public void update(int iTime, double time) {

        // check for crashes
        checkForInconsistencies(iTime, time);

        updateRoadConditions(iTime, time);

        // vehicle accelerations
        accelerate(iTime, dt, time);

        // vehicle pos/speed
        updatePositionAndSpeed(iTime, dt, time);

        updateDownstreamBoundary();

        updateUpstreamBoundary(iTime, dt, time);

        updateOnramps(iTime, dt, time);
        
        detectors.update(iTime, time, dt, vehContainer);

    }

    /**
     * Initial conditions.
     * 
     * @param simInput
     *            the sim input
     */
    private void initialConditions(SimulationInput simInput) {
        final List<ICMacroData> icMacroData = simInput.getSingleRoadInput().getIcMacroData();
        if (!icMacroData.isEmpty()) {
            logger.debug("choose macro initial conditions: generate vehicles from macro-density ");

            final InitialConditionsMacro icMacro = new InitialConditionsMacroImpl(icMacroData);
            final double xLocalMin = 0; // if ringroad: set xLocalMin e.g.
                                        // -SMALL_VAL

            double xLocal = roadLength; // start from behind
            while (xLocal > xLocalMin) {
                final VehiclePrototype vehPrototype = vehGenerator.getVehiclePrototype();
                final double rhoLocal = icMacro.rho(xLocal);
                double speedInit = icMacro.vInit(xLocal);
                if (speedInit == 0) {
                    speedInit = vehPrototype.getEquilibriumSpeed(rhoLocal); // equil
                                                                            // speed
                }
                final int laneEnter = Constants.MOST_RIGHT_LANE;
                final Vehicle veh = vehGenerator.createVehicle(vehPrototype);
                vehContainer.add(veh, xLocal, speedInit, laneEnter);
                logger.debug("init conditions macro: rhoLoc={}/km, xLoc={}", 1000 * rhoLocal, xLocal);

                xLocal -= 1 / rhoLocal;

            }
        } else {
            logger.debug(("choose micro initial conditions"));
            final List<ICMicroData> icSingle = simInput.getSingleRoadInput().getIcMicroData();
            for (final ICMicroData ic : icSingle) {
                // TODO counter 
                final double posInit = ic.getX();
                final double speedInit = ic.getSpeed();
                final String vehTypeFromFile = ic.getLabel();
                final int laneInit = ic.getInitLane();
                final Vehicle veh = (vehTypeFromFile.isEmpty()) ? vehGenerator.createVehicle() : vehGenerator
                        .createVehicle(vehTypeFromFile);
                vehContainer.add(veh, posInit, speedInit, laneInit);
                logger.info("set vehicle with label = {}", veh.getLabel());
            }
        }
    }

   

    /**
     * Inits the onramps.
     * 
     * @param inputData
     *            the input data
     */
    private void initOnramps(InputData inputData) {
        simpleOnramps = new ArrayList<Onramp>();
        final List<SimpleRampData> onrampData = inputData.getSimulationInput().getSingleRoadInput().getSimpleRamps();
        final String projectName = inputData.getProjectMetaData().getProjectName();
        int rampIndex = 1;
        for (final SimpleRampData onrmp : onrampData) {
            simpleOnramps.add(new OnrampImpl(onrmp, vehGenerator, vehContainer, projectName, rampIndex));
            rampIndex++;
        }
    }

    /**
     * Update downstream boundary.
     */
    private void updateDownstreamBoundary() {
        vehContainer.removeVehiclesDownstream(roadLength);
    }

    /**
     * Update upstream boundary.
     * 
     * @param itime
     *            the itime
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    private void updateUpstreamBoundary(int itime, double dt, double time) {
        upstreamBoundary.update(itime, dt, time);
    }

    /**
     * Check for inconsistencies.
     * 
     * @param iTime
     *            the i time
     * @param time
     *            the time
     */
    private void checkForInconsistencies(int iTime, double time) {
        // crash test
        final List<Vehicle> vehicles = vehContainer.getVehicles();
        for (int i = 0, N = vehicles.size(); i < N; i++) {
            final Moveable egoVeh = vehicles.get(i);
            final Moveable vehFront = vehContainer.getLeader(egoVeh);
            final double netDistance = egoVeh.netDistance(vehFront);
            if (netDistance < 0) {
                logger.error("#########################################################");
                logger.error("Crash of Vehicle i = {} at x = {}m", i, egoVeh.getPosition());
                if(vehFront!=null){
                    logger.error("with veh in front at x = {} on lane = {}", vehFront.getPosition(), egoVeh.getLane());
                }
                logger.error("net distance  = {}", netDistance);
                logger.error("container.size = {}", vehicles.size());
                final StringBuilder msg = new StringBuilder("\n");
                for (int j = Math.max(0, i - 8), M = vehicles.size(); j <= Math.min(i + 8, M - 1); j++) {
                    final Moveable veh = vehicles.get(j);
                    msg.append(String
                            .format("veh=%d, pos=%6.2f, speed=%4.2f, accModel=%4.3f, length=%3.1f, lane=%d, id=%d%n",
                                    j, veh.getPosition(), veh.getSpeed(), veh.accModel(), veh.length(), veh.getLane(), veh.id()));
                } 
                logger.error(msg.toString());
                if (instantaneousFileOutput) {
                    if(withCrashExit){
                        logger.error(" !!! exit after crash !!! ");
                        System.exit(-99);
                    }
                }
            }
        }
    }

    /**
     * Accelerate.
     * 
     * @param iTime
     *            the i time
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    private void accelerate(int iTime, double dt, double time) {
        final List<Vehicle> vehicles = vehContainer.getVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            final Vehicle veh = vehicles.get(i);
            final double x = veh.getPosition();
            final double alphaT = flowConsBottlenecks.alphaT(x);
            final double alphaV0 = flowConsBottlenecks.alphaV0(x);
            // logger.debug("i={}, x_pos={}", i, x);
            // logger.debug("alphaT={}, alphaV0={}", alphaT, alphaV0);
            veh.calcAcceleration(dt, vehContainer, alphaT, alphaV0);
        }
    }

    /**
     * Update position and speed.
     * 
     * @param iTime
     *            the i time
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    private void updatePositionAndSpeed(int iTime, double dt, double time) {
        for (final Vehicle veh : vehContainer.getVehicles()) {
            veh.updatePostionAndSpeed(dt);
        }
    }

    // traffic lights haben eigene Phasen-Dynamik !
    /**
     * Update road conditions.
     * 
     * @param time
     *            the time
     */
    private void updateRoadConditions(int iTime, double time) {
        
        trafficLights.update(iTime, time, vehContainer.getVehicles());
        
        updateSpeedLimits(vehContainer.getVehicles());
    }

    private void updateSpeedLimits(List<Vehicle> vehicles) {
        if (!speedlimits.isEmpty()) {
            for (final Vehicle veh : vehContainer.getVehicles()) {
                final double pos = veh.getPosition();
                veh.setSpeedlimit(speedlimits.calcSpeedLimit(pos));
            }
        }
    }

    /**
     * Update onramps.
     * 
     * @param itime
     *            the itime
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    private void updateOnramps(int itime, double dt, double time) {
        if (simpleOnramps.isEmpty())
            return;
        for (final Onramp onramp : simpleOnramps) {
            onramp.update(itime, dt, time);
        }
    }

//    public double firstRampFlow() {
//        // TODO Auto-generated method stub
//        return 0;
//    }
//
//    public double upstreamInflow() {
//        // TODO Auto-generated method stub
//        return 0;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#getTimestep()
     */
    @Override
    public double getTimestep() {
        return dt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#getTrafficLights()
     */
    @Override
    public List<TrafficLight> getTrafficLights() {
        return trafficLights.getTrafficLights();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#nLanes()
     */
    @Override
    public int nLanes() {
        return nLanes;
    }

    @Override
    public List<LoopDetector> getLoopDetectors() {
        return detectors.getDetectors();
    }

}
