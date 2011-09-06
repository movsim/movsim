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
import org.movsim.input.model.simulation.RampData;
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
public class RoadSectionImpl extends AbstractRoadSection implements RoadSection {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(RoadSectionImpl.class);

    private TrafficLightsImpl trafficLights;

    /** The speedlimits. */
    private SpeedLimits speedlimits;

    /** The detectors. */
    private LoopDetectors detectors = null;

    /** The simple onramps. */
    private List<Onramp> onramps = null;

    
    /**
     * Instantiates a new road section impl.
     * 
     * @param inputData
     *            the input data
     */
    public RoadSectionImpl(final InputData inputData, final VehicleGenerator vehGenerator) {
        super(inputData, vehGenerator);
        logger.info("Cstr. RoadSectionImpl");
        
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
        
        vehContainers = new ArrayList<VehicleContainer>();
        for(int laneIndex = 0; laneIndex < nLanes; laneIndex++){
            vehContainers.add(new VehicleContainerImpl(laneIndex));
        }

        final RoadInput roadInput = inputData.getSimulationInput().getSingleRoadInput();
        upstreamBoundary = new UpstreamBoundaryImpl(vehGenerator, vehContainers, roadInput.getUpstreamBoundaryData(),
                inputData.getProjectMetaData().getProjectName());

        flowConsBottlenecks = new FlowConservingBottlenecksImpl(roadInput.getFlowConsBottleneckInputData());
        speedlimits = new SpeedLimitsImpl(roadInput.getSpeedLimitInputData());

        trafficLights = new TrafficLightsImpl(inputData.getProjectMetaData().getProjectName(),
                roadInput.getTrafficLightsInput());

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
     * @see org.movsim.simulator.roadSection.RoadSection#update(int, double)
     */
//    public void update(long iterationCount, double time) {
//
//        // check for crashes
//        checkForInconsistencies(iterationCount, time);
//
//        updateRoadConditions(iterationCount, time);
//
//        laneChanging(iterationCount, dt, time);  
//    
//        // vehicle accelerations
//        accelerate(iterationCount, dt, time);
//
//        // vehicle pos/speed
//        updatePositionAndSpeed(iterationCount, dt, time);
//
//        updateDownstreamBoundary();
//
//        updateUpstreamBoundary(iterationCount, dt, time);
//
//        updateOnramps(iterationCount, dt, time);
//
//        detectors.update(iterationCount, time, dt, vehContainers);
//
//    }

    /**
     * Initial conditions.
     * 
     * @param simInput
     *            the sim input
     */
    private void initialConditions(SimulationInput simInput) {
        
        // TODO: consider multi-lane case !!!
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
                vehContainers.get(Constants.MOST_RIGHT_LANE).add(veh, xLocal, speedInit);  
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
                // TODO: consider multi-lane case, distribute over all lanes
                vehContainers.get(Constants.MOST_RIGHT_LANE).add(veh, posInit, speedInit);  
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
        
        onramps = new ArrayList<Onramp>();
        
        final String projectName = inputData.getProjectMetaData().getProjectName();
        
        // add simple onramps (with dropping mechanism)
        final List<SimpleRampData> simpleOnrampData = inputData.getSimulationInput().getSingleRoadInput().getSimpleRamps();
        int rampIndex = 1;
        for (final SimpleRampData onrmp : simpleOnrampData) {
            // merging from onramp only to most-right lane (shoulder lane)
            onramps.add(new OnrampImpl(onrmp, vehGenerator, vehContainers.get(Constants.MOST_RIGHT_LANE), projectName, rampIndex));
            rampIndex++;
        }
        
        // and simply add the new onramp with lane-changing decision and true merging 
        final List<RampData> onrampData = inputData.getSimulationInput().getSingleRoadInput().getRamps();
        for (final RampData onrmp : onrampData) {
            // merging from onramp only to most-right lane (shoulder lane)
            onramps.add(new OnrampMobilImpl(onrmp, vehGenerator, vehContainers.get(Constants.MOST_RIGHT_LANE), projectName, rampIndex));
            rampIndex++;
        }
    }

    /**
     * Update downstream boundary.
     */
    public void updateDownstreamBoundary() {
        for(VehicleContainer vehContainerLane : vehContainers){
            vehContainerLane.removeVehiclesDownstream(roadLength);
        }
    }

   
   
    /**
     * Accelerate.
     * 
     * @param iterationCount
     *            the i time
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    public void accelerate(int iterationCount, double dt, double time) {
        for (VehicleContainer vehContainerLane : vehContainers) {
            final List<Vehicle> vehiclesOnLane = vehContainerLane.getVehicles();
            for (int i = 0, N = vehiclesOnLane.size(); i < N; i++) {
                final Vehicle veh = vehiclesOnLane.get(i);
                final double x = veh.getPosition();
                final double alphaT = flowConsBottlenecks.alphaT(x);
                final double alphaV0 = flowConsBottlenecks.alphaV0(x);
                // logger.debug("i={}, x_pos={}", i, x);
                // logger.debug("alphaT={}, alphaV0={}", alphaT, alphaV0);
                veh.calcAcceleration(dt, vehContainerLane, alphaT, alphaV0);
            }
        }
    }

    /**
     * Update position and speed.
     * 
     * @param iterationCount
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
     * @param vehicles
     *            the vehicles
     */
    private void updateSpeedLimits(List<VehicleContainer> vehContainers) {
        if (!speedlimits.isEmpty()) {
            for (VehicleContainer vehContainerLane : vehContainers) {
                for (final Vehicle veh : vehContainerLane.getVehicles()) {
                    final double pos = veh.getPosition();
                    veh.setSpeedlimit(speedlimits.calcSpeedLimit(pos));
                }
            }
        }
    }

    /**
     * Update onramps.
     * 
     * @param iterationCount
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    
    
    public void updateOnramps(long iterationCount, double dt, double time) {
        if (onramps.isEmpty())
            return;
        for (final Onramp onramp : onramps) {
            onramp.update(iterationCount, dt, time);
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

    @Override
    public void updateDetectors(long iterationCount, double dt, double time){
        detectors.update(iterationCount, time, dt, vehContainers);
    }
   
    @Override
    public OnrampMobilImpl getMobilRampHack(){
        return (OnrampMobilImpl)onramps.get(0); 
    }

    @Override
    public void laneChanging(long iterationCount, double dt, double time) {
        // TODO Auto-generated method stub
    }
    
}
