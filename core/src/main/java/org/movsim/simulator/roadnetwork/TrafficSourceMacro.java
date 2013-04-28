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

import org.movsim.simulator.vehicles.TestVehicle;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TrafficSourceMacro.
 */
public class TrafficSourceMacro extends AbstractTrafficSource {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TrafficSourceMacro.class);
    
    private final InflowTimeSeries inflowTimeSeries;

    private TestVehicle testVehicle;

    /**
     * Instantiates a new upstream boundary .
     * 
     * @param vehGenerator
     *            the vehicle generator
     */
    public TrafficSourceMacro(TrafficCompositionGenerator vehGenerator, RoadSegment roadSegment,
            InflowTimeSeries inflowTimeSeries) {
        super(vehGenerator, roadSegment);
        this.inflowTimeSeries = inflowTimeSeries;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        final double totalInflow = getTotalInflow(simulationTime);
        nWait += totalInflow * dt;
        
        calcApproximateInflow(dt);
        
        if (nWait >= 1.0) {
            if (testVehicle == null) {
                testVehicle = vehGenerator.getTestVehicle();
            }
            // try to insert new vehicle at inflow, iterate periodically over n lanes
            int iLane = laneEnterLast;
            for (int i = 0, N = roadSegment.laneCount(); i < N; i++) {
                iLane = getNewCyclicLaneForEntering(iLane);
                final LaneSegment laneSegment = roadSegment.laneSegment(iLane);
                // laneIndex index is identical to vehicle's lanenumber
                // type of new vehicle
                final boolean isEntered = tryEnteringNewVehicle(testVehicle, laneSegment, simulationTime, totalInflow);
                if (isEntered) {
                    testVehicle = null;
                    nWait--;
                    incrementInflowCount(1);
                    recordData(simulationTime, totalInflow);
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
     * vehicles can be introduced only at times given by the simulation time step which is generally incommensurate with
     * the inverse of the inflow. For example, if the simulation time step is 0.4s, capacity is 2400 veh/h, and the
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
    private boolean tryEnteringNewVehicle(TestVehicle testVehicle, LaneSegment laneSegment, double time, double qBC) {

        final Vehicle leader = laneSegment.rearVehicle();

        // (1) empty road
        if (leader == null) {
            enterVehicleOnEmptyRoad(laneSegment, time, testVehicle);
            return true;
        }
        // (2) check if gap to leader is sufficiently large origin of road section is assumed to be zero
        final double netGapToLeader = leader.getRearPosition(); 
        final double gapAtQMax = 1. / testVehicle.getRhoQMax();

        // minimal distance set to 80% of 1/rho at flow maximum in fundamental diagram
        double minRequiredGap = 0.8 * gapAtQMax;
        if (testVehicle.getLongitudinalModel().isCA()) {
            minRequiredGap = leader.getSpeed();
        }
        if (netGapToLeader > minRequiredGap) {
            enterVehicle(laneSegment, time, minRequiredGap, testVehicle, leader);
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
    private void enterVehicleOnEmptyRoad(LaneSegment laneSegment, double time, TestVehicle testVehicle) {
        final double xEnter = 0;
        final double vEnter = inflowTimeSeries.getSpeed(time);
        addVehicle(laneSegment, testVehicle, xEnter, vEnter);
        LOG.debug("add vehicle from upstream boundary to empty road: xEnter={}, vEnter={}", xEnter, vEnter);
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
    private void enterVehicle(LaneSegment laneSegment, double time, double sFreeMin, TestVehicle testVehicle,
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
        final double vMaxEq = testVehicle.getEquilibriumSpeed(0.5 * rhoEnter);
        final double bMax = 4; // max. kinematic deceleration at boundary
        final double bEff = Math.max(0.1, bMax + aLast);
        final double vMaxKin = vLast + Math.sqrt(2 * sFree * bEff);
        final double vEnter = Math.min(Math.min(vEnterTest, vMaxEq), vMaxKin);

        addVehicle(laneSegment, testVehicle, xEnter, vEnter);
    }

    @Override
    public double getTotalInflow(double time) {
        return inflowTimeSeries.getFlowPerLane(time) * roadSegment.laneCount();
    }

}
