/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.simulator;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Iterable collection of the road segments in the road network.
 */
public class RoadNetwork implements SimulationTimeStep, Iterable<RoadSegment> {

    private final ArrayList<RoadSegment> roadSegments = new ArrayList<RoadSegment>();
    private String name;

    /**
     * Set the name of the road network.
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
     * Clear the road network so that it is empty and ready to accept new RoadSegments, Vehicles,
     * sources, sinks and junctions.
     */
    public void clear() {
        name = null;
//        LaneChangeModel.resetCount();
//        LongitudinalDriverModel.resetNextId();
        RoadSegment.resetNextId();
//        TrafficFlowBase.resetNextId();
//        Vehicle.resetNextId();
        roadSegments.clear();
    }

    /**
     * Called when the system is running low on memory, and would like actively running process to
     * try to tighten their belts.
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
     * The main timestep of the simulation.
     * </p>
     * 
     * <p>
     * Iterate over every roadSegment in the road network, calling that roadSegment's timestep.
     * </p>
     * <p>
     * Each timestep all the vehicles' lanes, positions and velocities and are updated. Then the
     * outflow is performed for each road segment, moving vehicles onto the next road segment (or
     * removing them entirely from the road network) when required. Then the inflow is performed for
     * each road segment, adding any new vehicles supplied by any traffic sources. Finally the
     * vehicle detectors are updated.
     * </p>
     * 
     * @param dt
     *            simulation time interval, seconds. Typically 0.25 seconds or less.
     * @param simulationTime
     *            the current logical time in the simulation
     */
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // Make each type of update for each road segment, this avoids problems with vehicles
        // being updated twice (for example when a vehicle moves of the end of a road segment
        // onto the next road segment.
        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.makeLaneChanges(dt, simulationTime, iterationCount);
        }
        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
        }
        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.outFlow(dt, simulationTime, iterationCount);
        }
        for (final RoadSegment roadSegment : roadSegments) {
            roadSegment.inFlow(dt, simulationTime, iterationCount);
        }
    }


    /**
     * Asserts the road network's class invariant. Used for debugging.
     */
    public void assertInvariant() {
        for (final RoadSegment roadSegment : roadSegments) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            if (roadMapping != null) {
                assert roadMapping.laneCount() == roadSegment.laneCount();
                assert roadMapping.trafficLaneMax() == roadSegment.trafficLaneMax();
                assert roadMapping.trafficLaneMin() == roadSegment.trafficLaneMin();
                assert Math.abs(roadMapping.roadLength() - roadSegment.roadLength()) < 0.1;
            }
        }
    }
}
