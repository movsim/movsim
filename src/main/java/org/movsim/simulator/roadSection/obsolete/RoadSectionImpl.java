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
package org.movsim.simulator.roadSection.obsolete;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.movsim.input.InputData;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.simulation.DetectorInput;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.output.LoopDetector;
import org.movsim.output.LoopDetectors;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.FlowConservingBottlenecks;
import org.movsim.simulator.roadnetwork.InitialConditionsMacro;
import org.movsim.simulator.roadnetwork.SpeedLimits;
import org.movsim.simulator.roadnetwork.TrafficLights;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.simulator.vehicles.obsolete.VehicleContainer;
import org.movsim.simulator.vehicles.obsolete.VehicleContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class RoadSectionImpl.
 */
public class RoadSectionImpl extends AbstractRoadSection implements RoadSection {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(RoadSectionImpl.class);

    /** The detectors. */
    private LoopDetectors detectors = null;

    private int countVehiclesToOfframp;

    
    private List<SimpleOnrampImpl> simpleOnramps;
    /**
     * Instantiates a new road section impl.
     * @param inputData 
     * 
     * @param roadinput
     *            the input data
     * @param vehGenerator
     *            the veh generator
     */
    public RoadSectionImpl(InputData inputData, final RoadInput roadinput, final VehicleGenerator vehGenerator) {
        super(inputData, roadinput, vehGenerator);
        logger.info("Cstr. RoadSectionImpl");

        initialize(inputData, roadinput);

       

    }

    /**
     * @param roadinput
     */
    private void initialize(InputData inputData, RoadInput roadInput) {
        countVehiclesToOfframp = 0;

        vehContainers = new ArrayList<VehicleContainer>();
        for (int laneIndex = 0; laneIndex < nLanes; laneIndex++) {
            vehContainers.add(new VehicleContainerImpl(id, laneIndex));
        }

        initSimpleOnramps(inputData);

//        upstreamBoundary = new UpstreamBoundary(id, vehGenerator, vehContainers, roadInput.getTrafficSourceData(),
//                inputData.getProjectMetaData().getProjectName());

        flowConsBottlenecks = new FlowConservingBottlenecks(roadInput.getFlowConsBottleneckInputData());
        speedlimits = new SpeedLimits(roadInput.getSpeedLimitInputData());

        trafficLights = new TrafficLights(roadInput.getTrafficLightsInput());

        final DetectorInput detInput = roadInput.getDetectorInput();
        if (detInput.isWithDetectors()) {
            detectors = new LoopDetectors(roadInput.getId(), detInput);
        }

        initialConditions(inputData.getSimulationInput(), roadInput);
    }

    private void initSimpleOnramps(final InputData inputData) {

        simpleOnramps = new LinkedList<SimpleOnrampImpl>();
        final String projectName = inputData.getProjectMetaData().getProjectName();

        // add simple onramps (with dropping mechanism)
        final List<SimpleRampData> simpleOnrampData = inputData.getSimulationInput().getSingleRoadInput()
                .getSimpleRamps();
        int rampIndex = 1;
        for (final SimpleRampData rmpSimpl : simpleOnrampData) {
            // merging from onramp only to most-right lane (shoulder lane)
            simpleOnramps.add(new SimpleOnrampImpl(id, rmpSimpl, vehGenerator, vehContainers.get(MovsimConstants.MOST_RIGHT_LANE), projectName,
                    rampIndex));
            rampIndex++;
        }

      
    }


    /**
     * Initial conditions.
     * 
     * @param simInput
     *            the sim input
     * @param roadInputMap 
     */
    private void initialConditions(SimulationInput simInput, RoadInput roadInput) {

        // TODO: consider multi-lane case !!!
        final List<ICMacroData> icMacroData = roadInput.getIcMacroData();
        if (!icMacroData.isEmpty()) {
            logger.debug("choose macro initial conditions: generate vehicles from macro-density ");

            final InitialConditionsMacro icMacro = new InitialConditionsMacro(icMacroData);
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
                final int laneEnter = MovsimConstants.MOST_RIGHT_LANE;
                final Vehicle veh = vehGenerator.createVehicle(vehPrototype);
                vehContainers.get(MovsimConstants.MOST_RIGHT_LANE).add(veh, xLocal, speedInit);
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
                vehContainers.get(MovsimConstants.MOST_RIGHT_LANE).add(veh, posInit, speedInit);
                logger.info("set vehicle with label = {}", veh.getLabel());
            }
        }
    }
    
    
   
    /**
     * Update downstream boundary.
     */
    @Override
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
    @Override
	public void laneChangingToOfframpsAndFromOnramps(RoadSection offramp, long iterationCount, double dt, double time) {

        // in this case the connection is to the offramp (or null if no offramp)
        // lane changes and merges from *simple* onramps
        for (SimpleOnrampImpl simpleOnramp : simpleOnramps) {
            simpleOnramp.mergeToMainroad(iterationCount, dt, time);
        }

        // and lane changes to *single* offramp
        // for (final RoadSection rmp : ramps) {
        if (offramp != null) {
            
            assert offramp instanceof OfframpImpl;

            stagedVehicles.clear();
            final VehicleContainer vehContainerRightLane = vehContainers.get(MovsimConstants.MOST_RIGHT_LANE);
            // most left lane
            final VehicleContainer rmpContainer = offramp.getVehContainer(offramp.getNumberOfLanes() - 1);

            // tricky here: change iteration order to ensure that
            // vehicles take the offramp *most upstream* as possible!
            List<Vehicle> vehicles = vehContainerRightLane.getVehicles();
            ListIterator<Vehicle> myIter = vehicles.listIterator(vehicles.size());
            while (myIter.hasPrevious()) {
                final Vehicle veh = myIter.previous();
                final double pos = veh.getPosition();
                // TODO quick hack: no planning horizon for merging to
                // off-ramp
                // allow merging only in first half !!!
                final double mergingZone = 0.7;
                if (veh.getLane() == MovsimConstants.MOST_RIGHT_LANE && !veh.inProcessOfLaneChanging() &&
                        pos > offramp.getRampPositionToMainroad() &&
                        pos < offramp.getRampPositionToMainroad() + mergingZone * offramp.getRampMergingLength()) {
                    // logger.debug("in merging to offramp: veh pos={}",
                    // veh.getPosition());
                    // check if lane change is possible
                    final double oldPos = veh.getPosition();
                    final double newPos = veh.getPosition() - offramp.getRampPositionToMainroad();
                    veh.setPosition(newPos); // mapping to coordinate system
                                             // of offramp
//                    final boolean isSafeChange = veh.getLaneChangingModel().isMandatoryLaneChangeSafe(rmpContainer);
//                    veh.setPosition(oldPos);
//                    // two steps
//                    if (isSafeChange) {
//                        // local decision to change to offramp
//                        final double fractionOfLeavingVehicles = calcFractionOfLeavingVehicles();
//                        final boolean isDesired = fractionOfLeavingVehicles < fractionToOfframpParameter;
//
//                        if (isDesired) {
//                            stagedVehicles.add(veh);
//                            countVehiclesToOfframp++;
//                        }
//                    }
                }
                if (stagedVehicles.size() > 0) {
                    break; // allow only one vehicle to leave to offramp per update step
                }
            }
            // assign staged vehicles to offrmp
            for (final Vehicle veh : stagedVehicles) {
                final double xInit = veh.getPosition() - offramp.getRampPositionToMainroad();
                final double vInit = veh.getSpeed();
                vehContainers.get(MovsimConstants.MOST_RIGHT_LANE).removeVehicle(veh);
                rmpContainer.addFromToRamp(veh, xInit, vInit, MovsimConstants.TO_LEFT);
                // System.exit(-1);
                // rmpContainer.add(veh, xInit, vInit);
            }

            stagedVehicles.clear();
        }

    }
    
    private double fractionToOfframpParameter = 0.1;
    private int offsetVehicleUpstreamCounter = 0;
    private double calcFractionOfLeavingVehicles(){
        double vehiclesFromUpstream = upstreamBoundary.getEnteringVehCounter()-offsetVehicleUpstreamCounter; 
        final double frac = ( vehiclesFromUpstream == 0 ) ? 0 : countVehiclesToOfframp / vehiclesFromUpstream;
        logger.debug("fraction of leaving vehicles={}, upstreamCounter={}", frac, vehiclesFromUpstream);
        return frac;
    }

    @Override
    public void setFractionOfLeavingVehicles(double newFraction) {
        this.fractionToOfframpParameter = newFraction;
        offsetVehicleUpstreamCounter = upstreamBoundary.getEnteringVehCounter();
        countVehiclesToOfframp = 0;
        logger.info("set new fractionToOfframpParameter={}. Reset counter and new upstream vehicle offset={}", fractionToOfframpParameter, offsetVehicleUpstreamCounter);
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
        //detectors.update(iterationCount, time, dt, vehContainers);
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
