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
import org.movsim.input.impl.InputDataImpl;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.simulation.DetectorInput;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.RampData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.output.LoopDetector;
import org.movsim.output.impl.LoopDetectors;
import org.movsim.simulator.Constants;
import org.movsim.simulator.impl.MyRandom;
import org.movsim.simulator.roadSection.InitialConditionsMacro;
import org.movsim.simulator.roadSection.OfframpImpl;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.SpeedLimits;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.simulator.vehicles.impl.VehicleContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class RoadSectionImpl.
 */
public class RoadSectionImpl extends AbstractRoadSection implements RoadSection {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(RoadSectionImpl.class);

    private TrafficLightsImpl trafficLights;

    /** The speedlimits. */
    private SpeedLimits speedlimits;

    /** The detectors. */
    private LoopDetectors detectors = null;

    private int countVehiclesToOfframp;

    /**
     * Instantiates a new road section impl.
     * @param inputData 
     * 
     * @param roadinput
     *            the input data
     * @param vehGenerator
     *            the veh generator
     */
    public RoadSectionImpl(InputDataImpl inputData, final RoadInput roadinput, final VehicleGenerator vehGenerator) {
        super(inputData, roadinput, vehGenerator);
        logger.info("Cstr. RoadSectionImpl");

        initialize(inputData, roadinput);

        // TODO cross-check --> testing for correct dt setup .... concept
        // between Simulator, VehGenerator and this roadSection
        if (Math.abs(dt - vehGenerator.requiredTimestep()) > Constants.SMALL_VALUE) {
            this.dt = vehGenerator.requiredTimestep();
            logger.info("model requires specific integration timestep. sets to dt={}", dt);
        }

    }

    /**
     * @param roadinput
     */
    private void initialize(InputDataImpl inputData, RoadInput roadInput) {
        countVehiclesToOfframp = 0;

        vehContainers = new ArrayList<VehicleContainer>();
        for (int laneIndex = 0; laneIndex < nLanes; laneIndex++) {
            vehContainers.add(new VehicleContainerImpl(id, laneIndex));
        }


        upstreamBoundary = new UpstreamBoundaryImpl(vehGenerator, vehContainers, roadInput.getTrafficSourceData(),
                inputData.getProjectMetaData().getProjectName());

        flowConsBottlenecks = new FlowConservingBottlenecksImpl(roadInput.getFlowConsBottleneckInputData());
        speedlimits = new SpeedLimitsImpl(roadInput.getSpeedLimitInputData());

        trafficLights = new TrafficLightsImpl(inputData.getProjectMetaData().getProjectName(),
                roadInput.getTrafficLightsInput());

        final DetectorInput detInput = roadInput.getDetectorInput();
        if (detInput.isWithDetectors()) {
            detectors = new LoopDetectors(inputData.getProjectMetaData().getProjectName(), detInput);
        }

        initialConditions(inputData.getSimulationInput(), roadInput);
    }

    /**
     * Initialize.
     * 
     * @param inputData
     *            the input data
     */
    private void initialize(InputData inputData) { //TODO delete

        countVehiclesToOfframp = 0;

        vehContainers = new ArrayList<VehicleContainer>();
        for (int laneIndex = 0; laneIndex < nLanes; laneIndex++) {
            vehContainers.add(new VehicleContainerImpl(id, laneIndex));
        }

        final RoadInput roadInput = inputData.getSimulationInput().getSingleRoadInput();

        upstreamBoundary = new UpstreamBoundaryImpl(vehGenerator, vehContainers, roadInput.getTrafficSourceData(),
                inputData.getProjectMetaData().getProjectName());

        flowConsBottlenecks = new FlowConservingBottlenecksImpl(roadInput.getFlowConsBottleneckInputData());
        speedlimits = new SpeedLimitsImpl(roadInput.getSpeedLimitInputData());

        trafficLights = new TrafficLightsImpl(inputData.getProjectMetaData().getProjectName(),
                roadInput.getTrafficLightsInput());

        final DetectorInput detInput = roadInput.getDetectorInput();
        if (detInput.isWithDetectors()) {
            detectors = new LoopDetectors(inputData.getProjectMetaData().getProjectName(), detInput);
        }

//        initialConditions(inputData.getSimulationInput());

    }

    /**
     * Initial conditions.
     * 
     * @param simInput
     *            the sim input
     * @param roadInput 
     */
    private void initialConditions(SimulationInput simInput, RoadInput roadInput) {

        // TODO: consider multi-lane case !!!
        final List<ICMacroData> icMacroData = roadInput.getIcMacroData();
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
                vehContainers.get(Constants.MOST_RIGHT_LANE).add(veh, xLocal, speedInit);
                logger.debug("init conditions macro: rhoLoc={}/km, xLoc={}", 1000 * rhoLocal, xLocal);

                xLocal -= 1 / rhoLocal;

            }
        } else {
            logger.debug(("choose micro initial conditions"));
            final List<ICMicroData> icSingle = roadInput.getIcMicroData();
            for (final ICMicroData ic : icSingle) {
                // TODO counter
                final double posInit = ic.getX();
                final double speedInit = ic.getSpeed();
                final String vehTypeFromFile = ic.getLabel();
                final int laneInit = ic.getInitLane();
                final Vehicle veh = (vehTypeFromFile.isEmpty()) ? vehGenerator.createVehicle() : vehGenerator
                        .createVehicle(vehTypeFromFile);
                // TODO: consider multi-lane case, distribute over all lanes
                vehContainers.get(Constants.MOST_RIGHT_LANE).add(veh, posInit, speedInit);
                logger.info("set vehicle with label = {}", veh.getLabel());
            }
        }
    }

    // just hack for "pulling out" the onramps contructed in the mainroad
    // roadsection
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.roadSection.RoadSection#rampFactory(org.movsim.input
     * .InputData)
     */
    public List<RoadSection> rampFactory(final InputData inputData) {

        List<RoadSection> ramps = new ArrayList<RoadSection>();

        final String projectName = inputData.getProjectMetaData().getProjectName();

        // add simple onramps (with dropping mechanism)
        final List<SimpleRampData> simpleOnrampData = inputData.getSimulationInput().getSingleRoadInput()
                .getSimpleRamps();
        int rampIndex = 1;
        for (final SimpleRampData rmpSimpl : simpleOnrampData) {
            // merging from onramp only to most-right lane (shoulder lane)
            ramps.add(new OnrampImpl(rmpSimpl, vehGenerator, vehContainers.get(Constants.MOST_RIGHT_LANE), projectName,
                    rampIndex));
            rampIndex++;
        }

        // and simply add the new onramp with lane-changing decision and true
        // merging

        final List<RampData> rampData = inputData.getSimulationInput().getSingleRoadInput().getRamps();
        for (final RampData rmp : rampData) {
            if ((rmp.getId() > 0)) {
                // merging from onramp only to most-right lane (shoulder lane)
                ramps.add(new OnrampMobilImpl(rmp, vehGenerator, vehContainers.get(Constants.MOST_RIGHT_LANE),
                        projectName, rampIndex));
                rampIndex++;
            }
            // quick hack for considering offramp (identified by negative id)
            if ((rmp.getId() < 0)) {
                ramps.add(new OfframpImpl(rmp));
                rampIndex++;
            }
        }
        return ramps;
    }

    /**
     * Update downstream boundary.
     */
    public void updateDownstreamBoundary() {
        for (VehicleContainer vehContainerLane : vehContainers) {
            vehContainerLane.removeVehiclesDownstream(roadLength);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.roadSection.RoadSection#laneChangingToOfframps(java
     * .util.List, long, double, double)
     */
    public void laneChangingToOfframps(List<RoadSection> ramps, long iterationCount, double dt, double time) {

        // TODO extract as parameter to xml configuration
        // TODO treat each offramp separately for correct book-keeping
        final double fractionToOfframp = 0.3;

        for (final RoadSection rmp : ramps) {
            // TODO quick hack -> identify offramp by class name
            if (rmp instanceof OfframpImpl) {
                stagedVehicles.clear();
                final VehicleContainer vehContainerRightLane = vehContainers.get(Constants.MOST_RIGHT_LANE);
                final VehicleContainer rmpContainer = rmp.getVehContainer(rmp.getNumberOfLanes() - 1); // most
                                                                                                       // left
                                                                                                       // lane
                for (final Vehicle veh : vehContainerRightLane.getVehicles()) {
                    final double pos = veh.getPosition();
                    // TODO quick hack: no planning horizon for merging to
                    // off-ramp
                    // allow merging only in first half !!!
                    final double mergingZone = 0.5;
                    if (veh.getLane()==Constants.MOST_RIGHT_LANE 
                            && !veh.inProcessOfLaneChanging() 
                            && pos > rmp.getRampPositionToMainroad()
                            && pos < rmp.getRampPositionToMainroad() + mergingZone * rmp.getRampMergingLength()) {
                        // logger.debug("in merging to offramp: veh pos={}",
                        // veh.getPosition());
                        // check if lane change is possible
                        final double oldPos = veh.getPosition();
                        final double newPos = veh.getPosition() - rmp.getRampPositionToMainroad();
                        veh.setPosition(newPos); // mapping to coordinate system
                                                 // of offramp
                        final boolean isSafeChange = veh.getLaneChangingModel().isMandatoryLaneChangeSafe(rmpContainer);
                        veh.setPosition(oldPos);
                        // local decision to change to offramp
                        final double fractionOfLeavingVehicles = upstreamBoundary.getEnteringVehCounter() == 0 ? 0
                                : countVehiclesToOfframp / (double) upstreamBoundary.getEnteringVehCounter();
                        final boolean isDesired = fractionOfLeavingVehicles < fractionToOfframp;
                        logger.debug("fraction of leaving vehicles={}, upstreamCounter={}", fractionOfLeavingVehicles,
                                upstreamBoundary.getEnteringVehCounter());
                        if (isSafeChange && isDesired) {
                            stagedVehicles.add(veh);
                            countVehiclesToOfframp++;
                        }
                    }
                }
                // assign staged vehicles to offrmp
                for (final Vehicle veh : stagedVehicles) {
                    final double xInit = veh.getPosition() - rmp.getRampPositionToMainroad();
                    final double vInit = veh.getSpeed();
                    vehContainers.get(Constants.MOST_RIGHT_LANE).removeVehicle(veh);
                    rmpContainer.addFromToRamp(veh, xInit, vInit, Constants.TO_LEFT);
                    // System.exit(-1);
                    // rmpContainer.add(veh, xInit, vInit);
                }
            }
        }

        stagedVehicles.clear();

    }

    // /**
    // * Accelerate.
    // *
    // * @param iterationCount
    // * the i time
    // * @param dt
    // * the dt
    // * @param time
    // * the time
    // */
    // public void accelerate(int iterationCount, double dt, double time) {
    // for (VehicleContainer vehContainerLane : vehContainers) {
    // final List<Vehicle> vehiclesOnLane = vehContainerLane.getVehicles();
    // for (int i = 0, N = vehiclesOnLane.size(); i < N; i++) {
    // final Vehicle veh = vehiclesOnLane.get(i);
    // final double x = veh.getPosition();
    // final double alphaT = flowConsBottlenecks.alphaT(x);
    // final double alphaV0 = flowConsBottlenecks.alphaV0(x);
    // // logger.debug("i={}, x_pos={}", i, x);
    // // logger.debug("alphaT={}, alphaV0={}", alphaT, alphaV0);
    // veh.calcAcceleration(dt, vehContainerLane, alphaT, alphaV0);
    // }
    // }
    // }

    /**
     * Update position and speed.
     * 
     * @param iterationCount
     *            the iteration count
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    public void updatePositionAndSpeed(int iterationCount, double dt, double time) {
        for (VehicleContainer vehContainerLane : vehContainers) {
            for (final Vehicle veh : vehContainerLane.getVehicles()) {
                veh.updatePostionAndSpeed(dt);
            }
        }
    }

    // traffic lights haben eigene Phasen-Dynamik !
    /**
     * Update road conditions.
     * 
     * @param iterationCount
     *            the iteration count
     * @param time
     *            the time
     */
    public void updateRoadConditions(long iterationCount, double time) {

        trafficLights.update(iterationCount, time, vehContainers);

        updateSpeedLimits(vehContainers);
    }

    /**
     * Update speed limits.
     * 
     * @param vehContainers
     *            the veh containers
     */
    private void updateSpeedLimits(List<VehicleContainer> vehContainers) {
        if (!speedlimits.isEmpty()) {
            for (final VehicleContainer vehContainerLane : vehContainers) {
                for (final Vehicle veh : vehContainerLane.getVehicles()) {
                    final double pos = veh.getPosition();
                    veh.setSpeedlimit(speedlimits.calcSpeedLimit(pos));
                    logger.debug("pos={} --> speedlimit in km/h={}", pos, 3.6 * speedlimits.calcSpeedLimit(pos));
                }
            }
        }
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
     * @see org.movsim.simulator.roadSection.RoadSection#getLoopDetectors()
     */
    @Override
    public List<LoopDetector> getLoopDetectors() {
        return detectors.getDetectors();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#updateDetectors(long,
     * double, double)
     */
    @Override
    public void updateDetectors(long iterationCount, double dt, double time) {
        detectors.update(iterationCount, time, dt, vehContainers);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.roadSection.AbstractRoadSection#getRampMergingLength
     * ()
     */
    @Override
    public double getRampMergingLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.AbstractRoadSection#
     * getRampPositionToMainroad()
     */
    @Override
    public double getRampPositionToMainroad() {
        // TODO Auto-generated method stub
        return 0;
    }


}
