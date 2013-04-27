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

package org.movsim.simulator.roadnetwork;

import java.util.ArrayList;
import java.util.Iterator;

import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterable collection of the road segments in the road network.
 */
public class RoadNetwork implements SimulationTimeStep, Iterable<RoadSegment> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(RoadNetwork.class);

    private final ArrayList<RoadSegment> roadSegments = new ArrayList<>();
    private String name;

    private boolean isWithCrashExit;
    private boolean hasVariableMessageSign;

    /**
     * Sets the name of the road network.
     * 
     * @param name
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the road network.
     * 
     * @return the name of the road network
     */
    public final String name() {
        return name;
    }

    /**
     * Given its id, find a road segment in the road network.
     * 
     * @param id
     * @return the road segment with the given id
     */
    public RoadSegment findById(int id) {
        for (final RoadSegment roadSegment : roadSegments) {
            if (roadSegment.id() == id) {
                return roadSegment;
            }
        }
        return null;
    }

    /**
     * Given its userId, find a road segment in the road network.
     * 
     * @param userId
     * @return the road segment with the given userId
     */
    public RoadSegment findByUserId(String userId) {
        for (final RoadSegment roadSegment : roadSegments) {
            if (roadSegment.userId() != null && roadSegment.userId().equals(userId)) {
                return roadSegment;
            }
        }
        return null;
    }

    /**
     * Clear the road network so that it is empty and ready to accept new RoadSegments, Vehicles, sources, sinks and
     * junctions.
     */
    public void clear() {
        name = null;
        hasVariableMessageSign = false;
        // LaneChangeModel.resetCount();
        // LongitudinalDriverModel.resetNextId();
        RoadSegment.resetNextId();
        // TrafficFlowBase.resetNextId();
        // Vehicle.resetNextId();
        roadSegments.clear();
    }

    /**
     * Called when the system is running low on memory, and would like actively running process to try to tighten their
     * belts.
     */
    public void onLowMemory() {
        roadSegments.trimToSize();
    }

    /**
     * Returns the number of RoadSegments in the road network.
     * 
     * @return the number of RoadSegments in the road network
     */
    public final int size() {
        return roadSegments.size();
    }

    /**
     * Adds a road segment to the road network.
     * 
     * @param roadSegment
     * @return roadSegment for convenience
     */
    public RoadSegment add(RoadSegment roadSegment) {
        assert roadSegment != null;
        assert roadSegment.eachLaneIsSorted();
        roadSegments.add(roadSegment);
        return roadSegment;
    }

    /**
     * Returns an iterator over all the road segments in the road network.
     * 
     * @return an iterator over all the road segments in the road network
     */
    @Override
    public Iterator<RoadSegment> iterator() {
        return roadSegments.iterator();
    }

    /**
     * <p>
     * The main timestep of the simulation. Update of calculation of vehicle accelerations, movements, lane-changing decisions. Each update
     * step is applied in parallel to all vehicles <i>of the entire network</i>. Otherwise, inconsistencies would occur. In particular, the
     * complete old state (positions, lanes, speeds ...) is made available during the complete update step of one timestep. Then the outflow
     * is performed for each road segment, moving vehicles onto the next road segment (or removing them entirely from the road network) when
     * required. Then the inflow is performed for each road segment, adding any new vehicles supplied by any traffic sources. Finally the
     * vehicle detectors are updated.
     * </p>
     * 
     * <p>
     * The steps themselves are grouped into two main blocks and an auxillary block:
     * <ol type="a">
     * <li>Longitudinal update:</li>
     * <ol type="i">
     * <li>Calculate accelerations</li>
     * <li>update speeds
     * <li>update positions
     * </ol>
     * <li>Discrete Decision update:</li>
     * <ol type="i">
     * <li>Determine decisions (whether to change lanes, decide to cruise/stop at a traffic light, etc.)</li>
     * <li>perform decisions (do the lane changes, cruising/stopping at traffic light, etc.)</li>
     * </ol>
     * 
     * <li>Do the related bookkeeping (update of inflow and outflow at boundaries) and update virtual detectors</li>
     * </ol>
     * </p>
     * 
     * <p>
     * The blocks can be swapped as long as each block is done serially for the whole network in exactly the above order (i),(ii),(iii).
     * </p>
     * 
     * @param dt
     *            simulation time interval, seconds.
     * @param simulationTime
     *            the current logical time in the simulation
     * @param iterationCount
     *            the counter of performed update steps
     */
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // Make each type of update for each road segment, this avoids problems with vehicles
        // being updated twice (for example when a vehicle moves of the end of a road segment
        // onto the next road segment.

        LOG.debug("called timeStep: time={}, timestep=", simulationTime, dt);
        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.updateRoadConditions(dt, simulationTime, iterationCount);
        }

        // Note: must do lane changes before vehicle positions are updated (or after outFlow) to ensure
        // the vehicle's roadSegmentId is correctly set
        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.makeLaneChanges(dt, simulationTime, iterationCount);
        }

        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.updateVehicleAccelerations(dt, simulationTime, iterationCount);
        }

        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.updateVehiclePositionsAndSpeeds(dt, simulationTime, iterationCount);
        }

        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.checkForInconsistencies(simulationTime, iterationCount, isWithCrashExit);
        }

        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.outFlow(dt, simulationTime, iterationCount);
        }

        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.inFlow(dt, simulationTime, iterationCount);
        }

        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.updateDetectors(dt, simulationTime, iterationCount);
        }
    }

    public void setWithCrashExit(boolean isWithCrashExit) {
        this.isWithCrashExit = isWithCrashExit;
    }

    public void setHasVariableMessageSign(boolean hasVariableMessageSign) {
        this.hasVariableMessageSign = hasVariableMessageSign;
    }

    public boolean hasVariableMessageSign() {
        return hasVariableMessageSign;
    }

    /**
     * Returns the number of vehicles on this road network.
     * 
     * @return the number of vehicles on this road network
     */
    public int vehicleCount() {
        int vehicleCount = 0;
        for (final RoadSegment roadSegment : roadSegments) {
            vehicleCount += roadSegment.getVehicleCount();
        }
        return vehicleCount;
    }

    public int getStoppedVehicleCount() {
        int stoppedVehicleCount = 0;
        for (final RoadSegment roadSegment : roadSegments) {
            stoppedVehicleCount += roadSegment.getStoppedVehicleCount();
        }
        return stoppedVehicleCount;
    }

    public double vehiclesMeanSpeed() {
        double averageSpeed = 0;
        for (final RoadSegment roadSegment : roadSegments) {
            averageSpeed += roadSegment.meanSpeed();
        }
        return averageSpeed / roadSegments.size();
    }

    /**
     * Returns the number of obstacles on this road network.
     * 
     * @return the number of obstacles on this road network
     */
    public int obstacleCount() {
        int obstacleCount = 0;
        for (final RoadSegment roadSegment : roadSegments) {
            obstacleCount += roadSegment.obstacleCount();
        }
        return obstacleCount;
    }

    /**
     * Returns the number of obstacles for the given route.
     * 
     * @return the number of obstacles on the given route
     */
    public int obstacleCount(Route route) {
        int obstacleCount = 0;
        for (final RoadSegment roadSegment : roadSegments) {
            obstacleCount += roadSegment.obstacleCount();
        }
        return obstacleCount;
    }

    /**
     * Asserts the road network's class invariant. Used for debugging.
     */
    public boolean assertInvariant() {
        for (final RoadSegment roadSegment : roadSegments) {
            assert roadSegment.assertInvariant();
        }
        return true;
    }

    /**
     * Returns the number of vehicles on route.
     * 
     * @return the number of vehicles on given route.
     */
    public static int vehicleCount(Route route) {
        int vehicleCount = 0;
        for (final RoadSegment roadSegment : route) {
            vehicleCount += roadSegment.getVehicleCount();
        }
        return vehicleCount;
    }

    /**
     * Returns the total travel time of all vehicles on this road network, including those that have exited.
     * 
     * @return the total vehicle travel time
     */
    public double totalVehicleTravelTime() {
        double totalVehicleTravelTime = 0.0;
        for (RoadSegment roadSegment : roadSegments) {
            totalVehicleTravelTime += roadSegment.totalVehicleTravelTime();
            if (roadSegment.sink() != null) {
                totalVehicleTravelTime += roadSegment.sink().totalVehicleTravelTime();
            }
        }
        return totalVehicleTravelTime;
    }

    public static double totalVehicleTravelTime(Route route) {
        double totalVehicleTravelTime = 0.0;
        for (final RoadSegment roadSegment : route) {
            totalVehicleTravelTime += roadSegment.totalVehicleTravelTime();
            if (roadSegment.sink() != null) {
                totalVehicleTravelTime += roadSegment.sink().totalVehicleTravelTime();
            }
        }
        return totalVehicleTravelTime;
    }

    public static double instantaneousTravelTime(Route route) {
        double instantaneousTravelTime = 0;
        for (final RoadSegment roadSegment : route) {
            instantaneousTravelTime += roadSegment.instantaneousTravelTime();
        }
        return instantaneousTravelTime;
    }

    /**
     * Returns the total travel distance of all vehicles on this road network, including those that have exited.
     * 
     * @return the total vehicle travel distance
     */
    public double totalVehicleTravelDistance() {
        double totalVehicleTravelDistance = 0.0;
        for (RoadSegment roadSegment : roadSegments) {
            totalVehicleTravelDistance += roadSegment.totalVehicleTravelDistance();
            if (roadSegment.sink() != null) {
                totalVehicleTravelDistance += roadSegment.sink().totalVehicleTravelDistance();
            }
        }
        return totalVehicleTravelDistance;
    }

    public static double totalVehicleTravelDistance(Route route) {
        double totalVehicleTravelDistance = 0.0;
        for (final RoadSegment roadSegment : route) {
            totalVehicleTravelDistance += roadSegment.totalVehicleTravelDistance();
            if (roadSegment.sink() != null) {
                totalVehicleTravelDistance += roadSegment.sink().totalVehicleTravelDistance();
            }
        }
        return totalVehicleTravelDistance;
    }

    /**
     * Returns the total fuel used by all vehicles on this road network, including those that have exited.
     * 
     * @return the total vehicle fuel used
     */
    public double totalVehicleFuelUsedLiters() {
        double totalVehicleFuelUsedLiters = 0.0;
        for (RoadSegment roadSegment : roadSegments) {
            totalVehicleFuelUsedLiters += roadSegment.totalVehicleFuelUsedLiters();
            if (roadSegment.sink() != null) {
                totalVehicleFuelUsedLiters += roadSegment.sink().totalFuelUsedLiters();
            }
        }
        return totalVehicleFuelUsedLiters;
    }

    public static double instantaneousFuelUsedLiters(Route route) {
        double instantaneousConsumption = 0;
        for (final RoadSegment roadSegment : route) {
            instantaneousConsumption += roadSegment.instantaneousConsumptionLitersPerSecond();
        }
        return instantaneousConsumption;
    }

}
