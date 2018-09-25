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

package org.movsim.simulator.roadnetwork.boundaries;

import com.google.common.base.Preconditions;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;
import java.util.TreeMap;

public class TrafficSourceMicro extends AbstractTrafficSource {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficSourceMicro.class);

    private final SortedMap<Long, Vehicle> vehicleQueue = new TreeMap<>();

    public TrafficSourceMicro(TrafficCompositionGenerator vehGenerator, RoadSegment roadSegment) {
        super(vehGenerator, roadSegment);
    }

    public void addVehicleToQueue(long time, Vehicle vehicle) {
        Preconditions.checkArgument(vehicleQueue.put(time, vehicle) == null,
                "cannot add vehicle to queue with same time=" + time);
        LOG.debug("added vehicle with (re)entering-time={}, queueSize={}", time, vehicleQueue.size());
        showQueue();
    }

    private void showQueue() {
        for (Long entryTime : vehicleQueue.keySet()) {
            LOG.debug("entryTime={}", entryTime);
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        calcApproximateInflow(dt);
        if (vehicleQueue.isEmpty()) {
            return;
        }
        Long entryTime = vehicleQueue.firstKey();
        if (simulationTime >= entryTime.longValue()) {
            Vehicle vehicle = vehicleQueue.get(entryTime);
            int testLane = (vehicle.lane() != Vehicle.LANE_NOT_SET) ?
                    vehicle.lane() :
                    getNewCyclicLaneForEntering(laneEnterLast);
            LaneSegment laneSegment = roadSegment.laneSegment(testLane);
            final boolean isEntered = tryEnteringNewVehicle(vehicle, laneSegment);
            if (isEntered) {
                vehicleQueue.remove(entryTime);
                incrementInflowCount(1);
                recordData(simulationTime, 0);
            }
        }
    }

    private boolean tryEnteringNewVehicle(Vehicle vehicle, LaneSegment laneSegment) {
        Vehicle leader = laneSegment.rearVehicle();
        double vEnter = vehicle.getSpeed();
        if (leader == null) {
            enterVehicle(laneSegment, vEnter, vehicle);
            return true;
        }
        vEnter = Math.min(vEnter, leader.getSpeed());
        // check if gap to leader is sufficiently large (xEnter of road section is assumed to be zero)
        final double netGapToLeader = leader.getRearPosition();
        // very crude approximation for minimum gap
        double minRequiredGap = vehicle.getEffectiveLength() + 2 * vehicle.getLongitudinalModel().getDesiredSpeed();
        if (vehicle.getLongitudinalModel().isCA()) {
            minRequiredGap = leader.getSpeed();
        }
        if (netGapToLeader > minRequiredGap) {
            enterVehicle(laneSegment, vEnter, vehicle);
            return true;
        }
        return false;
    }

    private void enterVehicle(LaneSegment laneSegment, double vEnter, Vehicle vehicle) {
        double xEnter = 0;
        initVehicle(laneSegment, xEnter, vEnter, vehicle);
        LOG.debug("add vehicle from upstream boundary to empty road: xEnter={}, vEnter={}", xEnter, vEnter);
    }

    @Override
    public double getTotalInflow(double time) {
        return 0; // no flow-based input
    }

}
