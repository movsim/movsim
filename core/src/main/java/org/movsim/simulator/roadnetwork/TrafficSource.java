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
package org.movsim.simulator.roadnetwork;

import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TrafficSource.
 */
public class TrafficSource implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TrafficSource.class);
    
    private static final double MEASURING_INTERVAL_S = 60.0;
    
    private double measuredInflow;
    
    private double measuredTime;
    
    private int measuredInflowCount;

    private double nWait;

    private final VehicleGenerator vehGenerator;

    private final RoadSegment roadSegment;

    private final InflowTimeSeries inflowTimeSeries;

    private int enteringVehCounter;

    /** The x enter last. status of last merging vehicle for logging to file */
    private double xEnterLast;

    private double vEnterLast;

    private int laneEnterLast;

    public interface RecordDataCallback {
        /**
         * Callback to allow the application to process or record the traffic source data.
         * 
         */
        public void recordData(double simulationTime, int laneEnter, double xEnter, double vEnter, double totalInflow,
                int enteringVehCounter, double nWait);
    }

    private RecordDataCallback recordDataCallback;

    /**
     * Instantiates a new upstream boundary .
     * 
     * @param vehGenerator
     *            the vehicle generator
     */
    public TrafficSource(VehicleGenerator vehGenerator, RoadSegment roadSegment, InflowTimeSeries inflowTimeSeries) {
        this.vehGenerator = vehGenerator;
        this.roadSegment = roadSegment;
        nWait = 0;
        measuredInflow = 0;
        measuredTime = 0;
        measuredInflowCount = 0;

        this.inflowTimeSeries = inflowTimeSeries;
    }

    /**
     * Sets the traffic source recorder.
     * 
     * @param recordDataCallback
     */
    public void setRecorder(RecordDataCallback recordDataCallback) {
        enteringVehCounter = 1;
        this.recordDataCallback = recordDataCallback;
    }

    /**
     * Gets the entering veh counter.
     * 
     * @return the entering veh counter
     */
    public int getEnteringVehCounter() {
        return enteringVehCounter;
    }

    /**
     * Gets the new cyclic lane index for entering.
     * 
     * @param iLane
     *            the i lane
     * @return the new cyclic lane index for entering
     */
    private int getNewCyclicLaneIndexForEntering(int iLane) {
        return iLane == roadSegment.laneCount() - 1 ? 0 : iLane + 1;
    }

    /**
     * Gets the total inflow.
     * 
     * @param time
     *            the time
     * @return the total inflow
     */
    public double getTotalInflow(double time) {
        // inflow over all lanes
        final double qBC = inflowTimeSeries.getFlowPerLane(time);
        final int nLanes = roadSegment.laneCount();
        return nLanes * qBC;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // integrate inflow demand
        final double totalInflow = getTotalInflow(simulationTime);
        nWait += totalInflow * dt;
        
        measuredTime += dt;
        if (measuredTime > MEASURING_INTERVAL_S) {
            measuredInflow = measuredInflowCount /MEASURING_INTERVAL_S; // vehicles per second 
            measuredTime = 0.0;
            measuredInflowCount = 0;
            logger.debug(String.format("source=%d with measured inflow Q=%.1f/h over all lanes and queue length %d of waiting vehicles", 
                    roadSegment.id(), measuredInflow*Units.INVS_TO_INVH, getQueueLength()));
        }
        
        if (nWait >= 1.0) {
            // try to insert new vehicle at inflow
            // iterate periodically over n lanes
            int iLane = laneEnterLast;
            for (int i = 0, N = roadSegment.laneCount(); i < N; i++) {
                iLane = getNewCyclicLaneIndexForEntering(iLane);
                // final VehicleContainer vehContainerLane = vehContainers.get(iLane);
                final LaneSegment laneSegment = roadSegment.laneSegment(iLane);
                // lane index is identical to vehicle's lane number
                final boolean isEntered = tryEnteringNewVehicle(laneSegment, simulationTime, totalInflow);
                if (isEntered) {
                    nWait--;
                    measuredInflowCount++;
                    if (recordDataCallback != null) {
                        recordDataCallback.recordData(simulationTime, laneEnterLast, xEnterLast, vEnterLast,
                                totalInflow, enteringVehCounter, nWait);
                    }
                    return; // only one insert per simulation update
                }
            }
        }
    }

    /**
     * <p>
     * Try entering new vehicle.
     * </p>
     * 
     * <p>
     * If the inflow is near capacity, it is crucial to avoid initial perturbations as much as possible. Otherwise, one
     * would activate an "inflow bottleneck", and less vehicles can be entered as one would like to. The crux is that
     * vehicles can be introduced only at times given by the simulation time step which is generalaly incommensurate
     * with the inverse of the inflow. For example, if the simulation time step is 0.4s, capacity is 2400 veh/h, and the
     * prescribed inflow is 2260 veh/h or one vehicle every 1.59s, you insert one vehicle every 1.6s, most of the time.
     * However, at some instances, two vehicles are inserted at time headway of 1.2s corresponding macroscopically to
     * 3000 veh/h, far above capacity. Typical time gaps for this situation are 1.2s most of the time but 0.8s
     * occasionally. This introduces a perturbation which may "activate" the "inflow bottleneck", unless a flow of 2260
     * veh/h is absolutely stable which is not always the case. Since the time of insertion cannot be changed, one can
     * homogenize the inflow by allowing the insertion point to vary by a maximum of one vehicle-vehicle distance.
     * </p>
     * 
     * @param laneSegment
     * @param time
     *            the time
     * @param qBC
     *            the q bc
     * @return true, if successful
     */
    private boolean tryEnteringNewVehicle(LaneSegment laneSegment, double time, double qBC) {

        // type of new vehicle
        final VehiclePrototype vehPrototype = vehGenerator.getVehiclePrototype();
        final Vehicle leader = laneSegment.rearVehicle();

        // (1) empty road
        if (leader == null) {
            enterVehicleOnEmptyRoad(laneSegment, time, vehPrototype);
            return true;
        }
        // (2) check if gap to leader is sufficiently large origin of road section is assumed to be zero
        final double netGapToLeader = leader.getRearPosition(); 
        final double gapAtQMax = 1. / vehPrototype.getRhoQMax();

        // minimal distance set to 80% of 1/rho at flow maximum in fundamental diagram
        double minRequiredGap = 0.8 * gapAtQMax;
        if (vehPrototype.getLongModel().isCA()) {
            minRequiredGap = leader.getSpeed();
        }
        if (netGapToLeader > minRequiredGap) {
            enterVehicle(laneSegment, time, minRequiredGap, vehPrototype, leader);
            return true;
        }
        // no entering possible
        return false;
    }

    /**
     * Enter vehicle on empty road.
     * 
     * @param laneSegment
     * @param time
     *            the time
     * @param vehPrototype
     *            the vehicle prototype
     */
    private void enterVehicleOnEmptyRoad(LaneSegment laneSegment, double time, VehiclePrototype vehPrototype) {
        final double xEnter = 0;
        final double vEnter = inflowTimeSeries.getSpeed(time);
        addVehicle(laneSegment, vehPrototype, xEnter, vEnter);
        logger.debug("add vehicle from upstream boundary to empty road: xEnter={}, vEnter={}", xEnter, vEnter);
    }

    /**
     * Enter vehicle.
     * 
     * @param laneSegment
     * @param time
     * @param sFreeMin
     * @param vehPrototype
     * @param leader
     */
    private void enterVehicle(LaneSegment laneSegment, double time, double sFreeMin, VehiclePrototype vehPrototype,
            Vehicle leader) {

        final double speedDefault = inflowTimeSeries.getSpeed(time);

        final double sFree = leader.getMidPosition() - leader.getLength();
        final double xLast = leader.getMidPosition();
        final double vLast = leader.getSpeed();
        final double aLast = leader.getAcc();

        final double vEnterTest = Math.min(speedDefault, 1.5 * vLast);
        final double lengthLast = leader.getLength();

        final double qBC = inflowTimeSeries.getFlowPerLane(time);
        final double xEnter = Math.min(vEnterTest * nWait / Math.max(qBC, 0.001), xLast - sFreeMin - lengthLast);
        final double rhoEnter = 1. / (xLast - xEnter);
        final double vMaxEq = vehPrototype.getEquilibriumSpeed(0.5 * rhoEnter);
        final double bMax = 4; // max. kinematic deceleration at boundary
        final double bEff = Math.max(0.1, bMax + aLast);
        final double vMaxKin = vLast + Math.sqrt(2 * sFree * bEff);
        final double vEnter = Math.min(Math.min(vEnterTest, vMaxEq), vMaxKin);

        addVehicle(laneSegment, vehPrototype, xEnter, vEnter);
    }

    /**
     * Adds the vehicle.
     * 
     * @param vehContainer
     *            the veh container
     * @param vehPrototype
     *            the veh prototype
     * @param xEnter
     *            the x enter
     * @param vEnter
     *            the v enter
     */
    private void addVehicle(LaneSegment laneSegment, VehiclePrototype vehPrototype, double xEnter, double vEnter) {
        final Vehicle vehicle = vehGenerator.createVehicle(vehPrototype);
        vehicle.setFrontPosition(xEnter);
        vehicle.setSpeed(vEnter);
        vehicle.setLane(laneSegment.lane());
        vehicle.setRoadSegment(roadSegment.id(), roadSegment.roadLength());
        laneSegment.addVehicle(vehicle);
        // status variables of entering vehicle for logging
        enteringVehCounter++;
        xEnterLast = xEnter;
        vEnterLast = vEnter;
        laneEnterLast = laneSegment.lane();
    }

    public void setFlowPerLane(double newFlowPerLane) {
        logger.info("set new flow per lane={} per second and reset queue of waiting vehicles={}", newFlowPerLane, nWait);
        inflowTimeSeries.setConstantFlowPerLane(newFlowPerLane);
        nWait = 0;
    }

    public double getFlowPerLane(double time) {
        return inflowTimeSeries.getFlowPerLane(time);
    }
    
    /**
     * Returns the measured inflow in vehicles per second, averaged over the measuring interval.
     * 
     * @return measured inflow over all lanes in vehicles per seconds
     * 
     */
    public double measuredInflow() {
        return measuredInflow;
    }

    /**
     * Returns the number of vehicles in the queue.
     * 
     * @return integer queue length over all lanes
     */
    public int getQueueLength() {
        return (int)nWait;
    }

}
