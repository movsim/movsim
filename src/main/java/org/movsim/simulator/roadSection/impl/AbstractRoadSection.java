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

import java.util.LinkedList;
import java.util.List;

import org.movsim.input.impl.InputDataImpl;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
import org.movsim.simulator.roadSection.FlowConservingBottlenecks;
import org.movsim.simulator.roadSection.SpeedLimit;
import org.movsim.simulator.roadSection.SpeedLimits;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.roadSection.UpstreamBoundary;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.impl.VehicleGeneratorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractRoadSection.
 */
public abstract class AbstractRoadSection {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AbstractRoadSection.class);
    
    /** The road length. */
    protected final double roadLength;

    /** The n lanes. */
    protected final int nLanes;

    /** The dt. */
    protected double dt;

    /** The id. */
    protected final long id;
    
    protected final long fromId;
    protected final long toId;
    
    
   // protected final boolean withCrashExit;
    
    protected final boolean instantaneousFileOutput;

    /** The veh generator. */
    protected final VehicleGenerator vehGenerator;
    
    /** The veh container list (for each lane). */
    protected List<VehicleContainer> vehContainers;
    
    protected List<Vehicle> stagedVehicles;

    /** The upstream boundary. */
    protected UpstreamBoundary upstreamBoundary;
    

    /** The flow cons bottlenecks. */
    protected FlowConservingBottlenecks flowConsBottlenecks;
    
    /** The speedlimits. */
    protected SpeedLimits speedlimits;
    
    protected TrafficLightsImpl trafficLights;
    
    // TODO same constructor for onramp and mainroad (and offramp) 
    /**
     * Instantiates a new abstract road section.
     *
     * @param inputData the input data
     * @param vehGenerator the veh generator
     */
//    public AbstractRoadSection(final InputData inputData, final VehicleGenerator vehGenerator){ //TODO get rid of it
//        this.vehGenerator = vehGenerator;
//        final SimulationInput simInput = inputData.getSimulationInput();
//        this.dt = simInput.getTimestep();
//       // this.withCrashExit = simInput.isWithCrashExit();
//        this.roadLength = simInput.getSingleRoadInput().getRoadLength();
//        this.nLanes = simInput.getSingleRoadInput().getLanes();
//        this.id = simInput.getSingleRoadInput().getId();
//        this.fromId = simInput.getSingleRoadInput().getTrafficSourceData().getSourceId();
//        this.toId = simInput.getSingleRoadInput().getTrafficSinkData().getSinkId();
//        this.instantaneousFileOutput = inputData.getProjectMetaData().isInstantaneousFileOutput();
//        init();
//    }

    
    // onramp
    /**
     * Instantiates a new abstract road section.
     *
     * @param rampData the ramp data
     * @param vehGenerator the veh generator
     */
    public AbstractRoadSection(final RoadInput rampData, final VehicleGenerator vehGenerator){
        // TODO also ramp can have an individual vehicle generator
        this.fromId = rampData.getTrafficSourceData().getSourceId();
        this.toId = rampData.getTrafficSinkData().getSinkId();
        this.vehGenerator = vehGenerator;
        this.roadLength = rampData.getRoadLength();
        this.nLanes = 1;
        this.id = rampData.getId();
        this.instantaneousFileOutput = false;  // TODO
        init();
    }
    
    // offramp
    /**
     * Instantiates a new abstract road section.
     *
     * @param rampData the ramp data
     */
    public AbstractRoadSection(final RoadInput rampData){
        this.fromId = rampData.getTrafficSourceData().getSourceId();
        this.toId = rampData.getTrafficSinkData().getSinkId();
        this.vehGenerator = null;
        this.roadLength = rampData.getRoadLength();
        this.nLanes = 1;
        this.id = rampData.getId();
        this.instantaneousFileOutput = false;  
        init();
    }

    /**
     * @param inputData 
     * @param roadInputMap
     * @param vehGenerator2
     */
    public AbstractRoadSection(InputDataImpl inputData, RoadInput roadInput, VehicleGenerator vehGenerator) {
        
        this.dt = inputData.getSimulationInput().getTimestep();
        this.roadLength = roadInput.getRoadLength();
        this.nLanes = roadInput.getLanes();
        this.id = roadInput.getId();
        this.fromId = roadInput.getTrafficSourceData().getSourceId();
        this.toId = roadInput.getTrafficSinkData().getSinkId();
        this.instantaneousFileOutput = false; // TODO instantfileouput
        
        // generate individual vehicle generator for specific road
        final List<TrafficCompositionInputData> heterogenInputData = roadInput.getTrafficCompositionInputData();
        if(heterogenInputData.size()>0){
            logger.info("create *individual* vehicle generator for road with id={}", id);
            final boolean isWithFundamentalDiagramOutput = roadInput.isWithWriteFundamentalDiagrams();
            this.vehGenerator = new VehicleGeneratorImpl(inputData, heterogenInputData, isWithFundamentalDiagramOutput);
        }
        else{
            logger.info("use *default* vehicle generator for road with id={}", id);
            this.vehGenerator = vehGenerator;
        }
        
        init();
    }

    /**
     * Inits the.
     */
    private void init(){
        stagedVehicles = new LinkedList<Vehicle>();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#roadLength()
     */
  
    /**
     * Gets the road length.
     *
     * @return the road length
     */
    public double getRoadLength() {
        return roadLength;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#id()
     */
    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#nLanes()
     */
    /**
     * Gets the number of lanes.
     *
     * @return the number of lanes
     */
    public int getNumberOfLanes() {
        return nLanes;
    }
    
    /**
     * Gets the veh containers.
     *
     * @return the veh containers
     */
    public List<VehicleContainer> getVehContainers() {
        return vehContainers;
    }

    /**
     * Gets the veh container.
     *
     * @param laneIndex the lane index
     * @return the veh container
     */
    public VehicleContainer getVehContainer(int laneIndex) {
        return vehContainers.get(laneIndex);
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
    public void accelerate(long iterationCount, double dt, double time) {
        
//        for (VehicleContainer vehContainerLane : vehContainers) {
//            final int leftLaneIndex = vehContainerLane.getLaneIndex()+MovsimConstants.TO_LEFT;
//            final VehicleContainer vehContainerLeftLane = ( leftLaneIndex < vehContainers.size() ) ? vehContainers.get(leftLaneIndex) : null;
//            final List<Vehicle> vehiclesOnLane = vehContainerLane.getVehicles();
//            //for (int i = 0, N = vehiclesOnLane.size(); i < N; i++) {
//            for(final Vehicle veh : vehiclesOnLane){
//                //final Vehicle veh = vehiclesOnLane.get(i);
//                final double x = veh.getPosition();
//                // TODO treat null case 
//                final double alphaT = (flowConsBottlenecks==null) ? 1 : flowConsBottlenecks.alphaT(x);
//                final double alphaV0 = (flowConsBottlenecks==null) ? 1 : flowConsBottlenecks.alphaV0(x);
//                // logger.debug("i={}, x_pos={}", i, x);
//                // logger.debug("alphaT={}, alphaV0={}", alphaT, alphaV0);
//                veh.calcAcceleration(dt, vehContainerLane, vehContainerLeftLane, alphaT, alphaV0);
//            }
//        }
    }

    /**
     * Update position and speed.
     *
     * @param iterationCount the iteration count
     * @param dt the dt
     * @param time the time
     */
    public void updatePositionAndSpeed(long iterationCount, double dt, double time) {
        for (VehicleContainer vehContainerLane : vehContainers) {
            for (final Vehicle veh : vehContainerLane.getVehicles()) {
                veh.updatePostionAndSpeed(dt);
            }
        }
    }
    
    
    // general lane-changing update also for one-lane roads for updating vehicle's lane-changing process 
    /**
     * Lane changing.
     *
     * @param iterationCount the iteration count
     * @param dt the dt
     * @param time the time
     */
    
    public void laneChanging(long iterationCount, double dt, double time) {
//        for (final VehicleContainer vehContainerLane : vehContainers) {
//
//            stagedVehicles.clear();
//
//            final List<Vehicle> vehiclesOnLane = vehContainerLane.getVehicles();
//            for (final Vehicle veh : vehiclesOnLane) {
//                if (veh.considerLaneChanging(dt, vehContainers)) {
//                    stagedVehicles.add(veh);
//                }
//            }
//
//            // assign staged vehicles to new lanes
//            // necessary update of new situation *after* lane-changing decisions
//
//            for (final Vehicle veh : stagedVehicles) {
//                vehContainers.get(veh.getLane()).removeVehicle(veh);
//                vehContainers.get(veh.getTargetLane()).add(veh);
//            }
//
//        }
    }
    

    /**
     * Update road conditions.
     * 
     * @param iterationCount
     *            the iteration count
     * @param time
     *            the time
     */
    public void updateRoadConditions(long iterationCount, double time) {

        //trafficLights.update(iterationCount, time, vehContainers);

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

    /**
     * Update upstream boundary.
     *
     * @param iterationCount the iteration count
     * @param dt the dt
     * @param time the time
     */
    public void updateUpstreamBoundary(long iterationCount, double dt, double time) {
        upstreamBoundary.update(iterationCount, dt, time);
    }
    
    /**
     * Check for inconsistencies.
     *
     * @param iterationCount the iteration count
     * @param time the time
     * @param isWithCrashExit the is with crash exit
     */
    public void checkForInconsistencies(long iterationCount, double time, boolean isWithCrashExit) {
        // crash test, iterate over all lanes separately
        for (int laneIndex = 0, laneIndexMax = vehContainers.size(); laneIndex < laneIndexMax; laneIndex++) {
            final VehicleContainer vehContainerLane = vehContainers.get(laneIndex);
            final List<Vehicle> vehiclesOnLane = vehContainerLane.getVehicles();
            for (int i = 0, N = vehiclesOnLane.size(); i < N; i++) {
                final Vehicle egoVeh = vehiclesOnLane.get(i);
                final Vehicle vehFront = vehContainerLane.getLeader(egoVeh);
                final double netDistance = egoVeh.getNetDistance(vehFront);
                if (netDistance < 0) {
                    logger.error("#########################################################");
                    logger.error("Crash of Vehicle i = {} at x = {}m", i, egoVeh.getPosition());
                    if (vehFront != null) {
                        logger.error("with veh in front at x = {} on lane = {}", vehFront.getPosition(), egoVeh.getLane());
                    }
                    logger.error("roadID = {}", getId());
                    logger.error("net distance  = {}", netDistance);
                    logger.error("lane index    = {}", laneIndex);
                    logger.error("container.size = {}", vehiclesOnLane.size());
                    final StringBuilder msg = new StringBuilder("\n");
                    for (int j = Math.max(0, i - 8), M = vehiclesOnLane.size(); j <= Math.min(i + 8, M - 1); j++) {
                        final Vehicle veh = vehiclesOnLane.get(j);
                        msg.append(String.format(
                                "veh=%d, pos=%6.2f, speed=%4.2f, accModel=%4.3f, length=%3.1f, lane=%d, id=%d%n", j,
                                veh.getPosition(), veh.getSpeed(), veh.accModel(), veh.getLength(), veh.getLane(), veh.getId()));
                    }
                    logger.error(msg.toString());
                    if (isWithCrashExit) {
                	logger.error(" !!! exit after crash !!! ");
                	System.exit(-99);
                    }
                }
            }
        }
    }
    
    
    
    public void updateBoundaryVehicles(long iterationCount, double time){
        for (final VehicleContainer vehContainerLane : vehContainers) {
            vehContainerLane.updateBoundaryVehicles();
        }
    }
    
    
    public long getFromId() {
        return fromId;
    }
    
    
    public long getToId() {
        return toId;
    }

    public UpstreamBoundary getUpstreamBoundary(){
        return upstreamBoundary;
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#getTrafficLights()
     */
    public List<TrafficLight> getTrafficLights() {
        if(trafficLights==null){
            return new LinkedList<TrafficLight>();
        }
        return trafficLights.getTrafficLights();
    }
    
    public List<SpeedLimit> getSpeedLimits(){
        if(speedlimits== null){
            return new LinkedList<SpeedLimit>();
        }
        return speedlimits.getSpeedLimits();
    }
}
