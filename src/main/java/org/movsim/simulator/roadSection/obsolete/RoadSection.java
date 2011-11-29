/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.roadSection.obsolete;

import java.util.List;

import org.movsim.output.LoopDetector;
import org.movsim.simulator.roadnetwork.SpeedLimit;
import org.movsim.simulator.roadnetwork.TrafficLight;
import org.movsim.simulator.roadnetwork.UpstreamBoundary;
import org.movsim.simulator.vehicles.obsolete.VehicleContainer;

// TODO: Auto-generated Javadoc
/**
 * The Interface RoadSection.
 */
public interface RoadSection {
    /**
     * Road length.
     * 
     * @return the double
     */
    double getRoadLength();

    /**
     * N lanes.
     * 
     * @return the int
     */
    int getNumberOfLanes();

    /**
     * Id.
     * 
     * @return the long
     */
    long getId();

    /**
     * Gets the veh container.
     * 
     * @param laneIndex
     *            the lane index
     * @return the veh container
     */
    VehicleContainer getVehContainer(int laneIndex);

    /**
     * Gets the veh containers.
     * 
     * @return the veh containers
     */
    List<VehicleContainer> getVehContainers();

    /**
     * Check for inconsistencies.
     * 
     * @param iterationCount
     *            the iteration count
     * @param time
     *            the time
     * @param isWithCrashExit
     *            the is with crash exit
     */
    void checkForInconsistencies(long iterationCount, double time, boolean isWithCrashExit);

    void laneChanging(long iterationCount, double dt, double time);

    /**
     * Accelerate.
     * 
     * @param iterationCount
     *            the iteration count
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    void accelerate(long iterationCount, double dt, double time);

    /**
     * Update road conditions.
     * 
     * @param iterationCount
     *            the iteration count
     * @param time
     *            the time
     */
    void updateRoadConditions(long iterationCount, double time);

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
    void updatePositionAndSpeed(long iterationCount, double dt, double time);

    /**
     * Update downstream boundary.
     */
    void updateDownstreamBoundary();

    /**
     * Update upstream boundary.
     * 
     * @param iterationCount
     *            the iteration count
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    void updateUpstreamBoundary(long iterationCount, double dt, double time);

    /**
     * Gets the ramp merging length.
     * 
     * @return the ramp merging length
     */
    double getRampMergingLength(); // TODO redesign for network view

    /**
     * Gets the ramp position to mainroad.
     * 
     * @return the ramp position to mainroad
     */
    double getRampPositionToMainroad(); // TODO redesign for network view

    /**
     * Gets the traffic lights.
     * 
     * @return the traffic lights
     */
    List<TrafficLight> getTrafficLights();

    /**
     * Gets the loop detectors.
     * 
     * @return the loop detectors
     */
    List<LoopDetector> getLoopDetectors();

    // void updateOnramps(long iterationCount, double dt, double time);

    /**
     * Update detectors.
     * 
     * @param iterationCount
     *            the iteration count
     * @param dt
     *            the dt
     * @param simulationTime
     *            the simulation time
     */
    void updateDetectors(long iterationCount, double dt, double simulationTime);

    // TODO hack here:
    // connected road section is mainroad when called for onramp and offramp when called from mainroad
    void laneChangingToOfframpsAndFromOnramps(RoadSection connectedRoadSection, long iterationCount, double dt,
            double time);

    void updateBoundaryVehicles(long iterationCount, double time);

    long getFromId();

    long getToId();

    UpstreamBoundary getUpstreamBoundary();

    void setFractionOfLeavingVehicles(double newFraction);

    List<SpeedLimit> getSpeedLimits();

}
