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

import java.util.Iterator;

import org.movsim.output.LoopDetectors;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A RoadSegment is a unidirectional stretch of road that contains a number of lane segments. A bidirectional stretch of
 * road may be created by combining two road segments running in opposite directions.
 * </p>
 * <p>
 * RoadSegments may be combined to form a road network.
 * </p>
 * <p>
 * A RoadSegment is normally connected to two other road segments: a source road from which vehicles enter the road
 * segment and a sink road to which vehicles exit. RoadSegments at the edge of the network will normally be connected to
 * only one other road segment: traffic inflow and outflow will be controlled directly by source and sink objects.
 * </p>
 * <p>
 * RoadSegments are connected to each other on a lane-wise basis: each sink (outgoing) lane of a road segment may be
 * connected to a source (incoming) lane of another road segment. This allows the forking and merging of road segments,
 * the creation of on-ramps and off-ramps. By connecting the lanes of a number of road segments in this way, complex
 * junctions and interchanges may be created.
 * </p>
 * <p>
 * A RoadSegment is a logical entity, not a physical one. That is a RoadSegment does not know if it is straight or
 * winding, it just knows about the vehicles it contains and what it is connected to. A vehicle's coordinates on a
 * RoadsSegment are given by the vehicle's position relative to the start of the RoadSegment and the vehicle's lane.
 * </p>
 * <p>
 * A RoadSegment has <code>laneCount</code> lanes. Lanes within a RoadSegment are represented by the LaneSegment class.
 * </p>
 * <p>
 * The mapping from a position on a RoadSegment to coordinates in physical space is determined by a RoadSegment's
 * RoadMapping. Although the RoadMapping is primarily used by software that draws the road network and the vehicles upon
 * it, elements of the RoadMapping may influence vehicle behavior, in particular a road's curvature and its gradient.
 * </p>
 */
public class RoadSegment implements Iterable<Vehicle> {

    final static Logger logger = LoggerFactory.getLogger(RoadSegment.class);

    public static final int ID_NOT_SET = -1;
    public static final int INITIAL_ID = 1;
    private static final boolean DEBUG = false;
    private static int nextId = INITIAL_ID;

    public static final int MAX_LANE_COUNT = 8;
    public static final int MAX_LANE_PAIR_COUNT = 12;

    private final int id; // the id is an internally used unique identifier for the road
    private String userId; // the userId is the id specified in the .xodr and .xml files
    private final double roadLength;
    private final double cumulativeRoadLength = -1.0; // total length of road up to start of segment
    private final int laneCount;
    private final LaneSegment laneSegments[];
    private LoopDetectors loopDetectors;
    private FlowConservingBottlenecks flowConservingBottlenecks;
    private TrafficLights trafficLights;
    private SpeedLimits speedLimits;
    private Slopes slopes;

    // Sources and Sinks
    private TrafficSource trafficSource;
    private TrafficSink sink;
    private RoadMapping roadMapping;

    public static class TestCar {
        public double s = 0.0; // distance
        public double vdiff = 0.0; // approaching rate
        public double vel = 0.0; // velocity
        public double acc = 0.0; // acceleration
    }

    /**
     * Resets the next id.
     */
    public static void resetNextId() {
        nextId = INITIAL_ID;
    }

    /**
     * Returns the number of road segments that have been created. Used for instrumentation.
     * 
     * @return the number of road segment that have been created
     */
    public static int count() {
        return nextId - INITIAL_ID;
    }

    /**
     * Constructor.
     * 
     * @param roadLength
     *            road length, in meters.
     * @param laneCount
     *            number of lanes in this road segment
     */
    public RoadSegment(double roadLength, int laneCount) {
        assert roadLength > 0.0;
        assert laneCount >= 1 && laneCount <= MAX_LANE_COUNT;
        laneSegments = new LaneSegment[laneCount];
        for (int i = 0; i < laneCount; ++i) {
            laneSegments[i] = new LaneSegment(this, i);
        }
        id = nextId++;
        this.roadLength = roadLength;
        this.laneCount = laneCount;
    }

    /**
     * Convenience constructor, creates road segment based on a given road mapping.
     * 
     * @param roadMapping
     */
    public RoadSegment(RoadMapping roadMapping) {
        this(roadMapping.roadLength(), roadMapping.laneCount());
        assert roadMapping.trafficLaneMin() == Lane.LANE1;
        assert roadMapping.trafficLaneMax() == laneCount;
        this.roadMapping = roadMapping;
    }

    /**
     * Sets a default sink for this road segment.
     */
    public final void addDefaultSink() {
        sink = new TrafficSink(this);
    }

    /**
     * Returns this road segment's id
     * 
     * @return this road segment's id
     */
    public final int id() {
        return id;
    }

    /**
     * Set this road segment's userId
     * 
     * @param userId
     * 
     */
    public final void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns this road segment's userId. The userId is the road's id as set in the .xodr and .xml files.
     * 
     * @return this road segment's userId
     */
    public final String userId() {
        return userId == null ? Integer.toString(id) : userId;
    }

    /**
     * Returns this road segment's road mapping.
     * 
     * @return this road segment's road mapping
     */
    public final RoadMapping roadMapping() {
        assert roadMapping != null;
        return roadMapping;
    }

    /**
     * Sets this road segment's road mapping.
     * 
     * @param roadMapping
     */
    public final void setRoadMapping(RoadMapping roadMapping) {
        this.roadMapping = roadMapping;
    }

    /**
     * Returns the traffic source (upstream boundary) for this road segment.
     * 
     * @return the traffic source
     */
    public final TrafficSource getTrafficSource() {
        return trafficSource;
    }

    /**
     * Sets the traffic source (upstream boundary) for this road segment.
     * 
     * @param trafficSource
     *            the traffic source
     */
    public final void setTrafficSource(TrafficSource trafficSource) {
        assert trafficSource != null;
        this.trafficSource = trafficSource;
    }

    /**
     * Returns the traffic sink for this road segment.
     * 
     * @return the traffic sink
     */
    public final TrafficSink sink() {
        return sink;
    }

    /**
     * Sets the traffic sink for this road segment.
     * 
     * @param sink
     *            the traffic sink
     */
    public final void setSink(TrafficSink sink) {
        this.sink = sink;
    }

    /**
     * Returns this road segment's length.
     * 
     * @return road segment length in meters
     */
    public final double roadLength() {
        return roadLength;
    }

    public final double cumulativeRoadLength() {
        // if (cumulativeRoadLength >= 0.0) {
        // return cumulativeRoadLength;
        // }
        // final RoadSegment sourceRoadSegment = sourceRoadSegment(trafficLaneMax() - 1);
        // cumulativeRoadLength = sourceRoadSegment == null ? 0.0 : sourceRoadSegment.cumulativeRoadLength() +
        // sourceRoadSegment.roadLength();
        return cumulativeRoadLength;
    }

    /**
     * Returns the number of lanes in this road segment.
     * 
     * @return number of lanes
     */
    public final int laneCount() {
        return laneCount;
    }

    /**
     * Sets the type of the given lane.
     * 
     * @param lane
     * @param laneType
     */
    public void setLaneType(int lane, Lane.Type laneType) {
        laneSegments[lane].setType(laneType);
        if (roadMapping != null) {
            roadMapping.setTrafficLaneMin(trafficLaneMin());
            roadMapping.setTrafficLaneMax(trafficLaneMax());
        }
    }

    /**
     * Returns the type of the given lane.
     * 
     * @param lane
     * 
     * @return type of lane
     */
    public Lane.Type laneType(int lane) {
        return laneSegments[lane].type();
    }

    /**
     * Returns the minimum traffic lane.
     * 
     * @return the minimum traffic lane
     */
    public int trafficLaneMin() {
        int trafficLaneMin = 0;
        while (laneSegments[trafficLaneMin].type() != Lane.Type.TRAFFIC) {
            ++trafficLaneMin;
        }
        return trafficLaneMin;
    }

    /**
     * Returns the maximum traffic lane.
     * 
     * @return the maximum traffic lane
     */
    public int trafficLaneMax() {
        int trafficLaneMax = laneCount - 1;
        while (laneSegments[trafficLaneMax].type() != Lane.Type.TRAFFIC) {
            --trafficLaneMax;
        }
        return trafficLaneMax + 1;
    }

    public final LaneSegment laneSegment(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        return laneSegments[lane];
    }

    public final void setSourceLaneSegmentForLane(LaneSegment sourceLaneSegment, int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        laneSegments[lane].setSourceLaneSegment(sourceLaneSegment);
    }

    public final LaneSegment sourceLaneSegment(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        return laneSegments[lane].sourceLaneSegment();
    }

    public final RoadSegment sourceRoadSegment(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        if (laneSegments[lane].sourceLaneSegment() == null) {
            return null;
        }
        return laneSegments[lane].sourceLaneSegment().roadSegment();
    }

    public final int sourceLane(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        if (laneSegments[lane].sourceLaneSegment() == null) {
            return Lane.NONE;
        }
        return laneSegments[lane].sourceLaneSegment().lane();
    }

    public final void setSinkLaneSegmentForLane(LaneSegment sinkLaneSegment, int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_COUNT;
        laneSegments[lane].setSinkLaneSegment(sinkLaneSegment);
    }

    public final LaneSegment sinkLaneSegment(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_COUNT;
        return laneSegments[lane].sinkLaneSegment();
    }

    public final RoadSegment sinkRoadSegment(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_COUNT;
        if (laneSegments[lane].sinkLaneSegment() == null) {
            return null;
        }
        return laneSegments[lane].sinkLaneSegment().roadSegment();
    }

    public final int sinkLane(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_COUNT;
        if (laneSegments[lane].sinkLaneSegment() == null) {
            return Lane.NONE;
        }
        return laneSegments[lane].sinkLaneSegment().lane();
    }

    /**
     * Returns the number of vehicles removed from this road segment.
     * 
     * @return the number of vehicles removed from this road segment
     */
    public int removedVehicleCount() {
        int removedVehicleCount = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            removedVehicleCount += laneSegment.getRemovedVehicleCount();
        }
        return removedVehicleCount;
    }

    /**
     * Clears the removed vehicle count.
     */
    public void clearVehicleRemovedCount() {
        for (final LaneSegment laneSegment : laneSegments) {
            laneSegment.clearVehicleRemovedCount();
        }
    }

    /**
     * Clears this road segment of any vehicles.
     */
    public void clearVehicles() {
        for (final LaneSegment laneSegment : laneSegments) {
            laneSegment.clearVehicles();
        }
    }

    /**
     * Returns the total number of vehicles on this road segment, all lanes.
     * 
     * @return the total number of vehicles on this road segment
     */
    public int totalVehicleCount() {
        int totalVehicleCount = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            totalVehicleCount += laneSegment.vehicleCount();
        }
        return totalVehicleCount;
    }

    /**
     * Returns the number of vehicles in the given lane on this road segment.
     * 
     * @param lane
     * 
     * @return the number of vehicles in the given lane on this road segment
     */
    public int getVehicleCount(int lane) {
        assert lane >= Lane.LANE1;
        assert lane < laneCount;
        return laneSegments[lane].vehicleCount();
    }

    /**
     * <p>
     * Returns the vehicle at the given index in the given lane.
     * </p>
     * 
     * <p>
     * In each lane vehicles are sorted in order of decreasing position:
     * </p>
     * 
     * <p>
     * V[n+1].pos < V[n].pos < V[n-1].pos ... < V[1].pos < V[0].pos
     * </p>
     * 
     * @param lane
     * @param index
     * 
     * @return vehicle at given index in the given lane
     */
    public Vehicle getVehicle(int lane, int index) {
        return laneSegments[lane].getVehicle(index);
    }

    /**
     * Removes the vehicle at the given index in the given lane.
     * 
     * @param lane
     * @param index
     *            index of vehicle to remove
     */
    @Deprecated
    public void removeVehicle(int lane, int index) {
        laneSegments[lane].removeVehicle(index);
    }

    /**
     * Removes the front vehicle on the given lane.
     * 
     * @param lane
     */
    public void removeFrontVehicleOnLane(int lane) {
        laneSegments[lane].removeFrontVehicleOnLane();
    }

    /**
     * Removes any vehicles that have moved past the end of this road segment.
     */
    public void removeVehiclesPastEnd() {
        for (final LaneSegment laneSegment : laneSegments) {
            laneSegment.removeVehiclesPastEnd();
        }
    }

    /**
     * Adds an obstacle to this road segment.
     * 
     * @param obstacle
     */
    public void addObstacle(Vehicle obstacle) {
        // assert obstacle.type() == Vehicle.Type.OBSTACLE;
        addVehicle(obstacle);
    }

    /**
     * Adds a vehicle to this road segment.
     * 
     * @param vehicle
     */
    public void addVehicle(Vehicle vehicle) {
        laneSegments[vehicle.getLane()].addVehicle(vehicle);
    }

    /**
     * Adds a vehicle to the start of this road segment.
     * 
     * @param vehicle
     */
    public void appendVehicle(Vehicle vehicle) {
        laneSegments[vehicle.getLane()].appendVehicle(vehicle);
    }

    /**
     * Updates the road conditions.
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    public void updateRoadConditions(double dt, double simulationTime, long iterationCount) {
        if (trafficLights != null) {
            trafficLights.update(dt, simulationTime, iterationCount, this);
        }
        updateSpeedLimits();
        updateSlopes();
    }
    
    private void updateSpeedLimits() {
        if (speedLimits != null && speedLimits.isEmpty() == false) {
            for (final LaneSegment laneSegment : laneSegments) {
                for (final Vehicle vehicle : laneSegment) {
                    assert vehicle.roadSegmentId() == id;
                    final double pos = vehicle.getFrontPosition();
                    final double speedlimit = speedLimits.calcSpeedLimit(pos);
                    vehicle.setSpeedlimit(speedlimit);
                    logger.debug("pos={} --> speedlimit in km/h={}", pos, 3.6 * speedlimit);
                }
            }
        }
    }
    
    private void updateSlopes() {
        if (slopes != null && slopes.isEmpty() == false) {
            for (final LaneSegment laneSegment : laneSegments) {
                for (final Vehicle vehicle : laneSegment) {
                    assert vehicle.roadSegmentId() == id;
                    final double pos = vehicle.getFrontPosition();
                    final double slope = slopes.calcSlope(pos);
                    vehicle.setSlope(slope);
                    logger.debug("pos={} --> slope gradient{}", pos, slope);
                }
            }
        }
    }

    /**
     * Lane change.
     * <p>
     * For each vehicle check if a lane change is desired and safe and, if so, make the lane change.
     * </p>
     * 
     * <p>
     * <code>makeLaneChanges</code> preserves the vehicle sort order, since only lateral movements of vehicles are made.
     * </p>
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    public void makeLaneChanges(double dt, double simulationTime, long iterationCount) {
        if (laneCount < 2) {
            // need at least 2 lanes for lane changing
            return;
        }
        // TODO assure priority for lane changes from slow to fast lanes
        for (final LaneSegment laneSegment : laneSegments) {
            assert laneSegment.assertInvariant();
            for (Iterator<Vehicle> vehIterator = laneSegment.iterator(); vehIterator.hasNext();) {
                Vehicle vehicle = vehIterator.next();
                assert vehicle.roadSegmentId() == id;
                if (vehicle.considerLaneChange(dt, this)) {
                    final int targetLane = vehicle.getTargetLane();
                    assert targetLane != Lane.NONE;
                    assert laneSegments[targetLane].type() != Lane.Type.ENTRANCE;
                    // iteratorRemove avoids ConcurrentModificationException
                    vehIterator.remove();
                    vehicle.setLane(targetLane);
                    laneSegments[targetLane].addVehicle(vehicle);
                }
            }
        }
    }

    /**
     * Accelerate.
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    public void updateVehicleAccelerations(double dt, double simulationTime, long iterationCount) {
        for (final LaneSegment laneSegment : laneSegments) {
            assert laneSegment.laneIsSorted();
            assert laneSegment.assertInvariant();
            // final int leftLaneIndex = laneSegment.getLaneIndex()+MovsimConstants.TO_LEFT;
            final LaneSegment leftLaneSegment = null; // TODO get left lane ( leftLaneIndex < vehContainers.size() ) ?
                                                      // vehContainers.get(leftLaneIndex) : null;
            for (final Vehicle vehicle : laneSegment) {
                final double x = vehicle.getFrontPosition();
                final double alphaT = (flowConservingBottlenecks == null) ? 1 : flowConservingBottlenecks.alphaT(x);
                final double alphaV0 = (flowConservingBottlenecks == null) ? 1 : flowConservingBottlenecks.alphaV0(x);
                // logger.debug("i={}, x_pos={}", i, x);
                // logger.debug("alphaT={}, alphaV0={}", alphaT, alphaV0);
                vehicle.updateAcceleration(dt, laneSegment, leftLaneSegment, alphaT, alphaV0);
            }
        }
    }

    /**
     * Update the vehicle positions and velocities by calling vehicle.updatePositionAndSpeed for each vehicle.
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    public void updateVehiclePositionsAndSpeeds(double dt, double simulationTime, long iterationCount) {
        for (final LaneSegment laneSegment : laneSegments) {
            assert laneSegment.laneIsSorted();
            for (final Vehicle vehicle : laneSegment) {
                vehicle.updatePositionAndSpeed(dt);
            }
        }
    }

    /**
     * If there is a traffic sink, use it to perform any traffic outflow.
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    public void outFlow(double dt, double simulationTime, long iterationCount) {
        for (final LaneSegment laneSegment : laneSegments) {
            laneSegment.outFlow(dt, simulationTime, iterationCount);
            assert laneSegment.assertInvariant();
        }
        if (sink != null) {
            sink.timeStep(dt, simulationTime, iterationCount);
        }
    }

    /**
     * If there is a traffic source, use it to perform any traffic inflow.
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    public void inFlow(double dt, double simulationTime, long iterationCount) {
        assert eachLaneIsSorted();
        if (trafficSource != null) {
            trafficSource.timeStep(dt, simulationTime, iterationCount);
            assert assertInvariant();
        }
    }

    /**
     * Updates the detectors, if there are any.
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    public void updateDetectors(double dt, double simulationTime, long iterationCount) {
        if (this.loopDetectors != null) {
            loopDetectors.timeStep(dt, simulationTime, iterationCount);
        }
    }

    /**
     * Returns the rear vehicle on the given lane.
     * 
     * @param lane
     * @return the rear vehicle on the given lane
     */
    public Vehicle rearVehicleOnLane(int lane) {
        return laneSegments[lane].rearVehicle();
    }

    /**
     * Finds the vehicle in the given lane immediately at or behind the given position.
     * 
     * @param lane
     *            lane in which to search
     * @return reference to the rear vehicle
     */
    public Vehicle rearVehicle(int lane, double vehiclePos) {
        return laneSegments[lane].rearVehicle(vehiclePos);
    }

    public Vehicle rearVehicleOnSinkLanePosAdjusted(int lane) {
        return laneSegments[lane].rearVehicleOnSinkLanePosAdjusted();
    }

    Vehicle secondLastVehicleOnSinkLanePosAdjusted(int lane) {
        return laneSegments[lane].secondLastVehicleOnSinkLanePosAdjusted();
    }

    /**
     * Returns the front vehicle on the given lane.
     * 
     * @param lane
     * @return the front vehicle on the given lane
     */
    public Vehicle frontVehicleOnLane(int lane) {
        return laneSegments[lane].frontVehicle();
    }

    /**
     * Finds the vehicle in the given lane immediately in front of the given position. That is a vehicle such that
     * vehicle.positon() > vehicePos (strictly greater than). The vehicle whose position equals vehiclePos is deemed to
     * be in the rear.
     * 
     * @param lane
     *            lane in which to search
     * @return reference to the front vehicle
     */
    public Vehicle frontVehicle(int lane, double vehiclePos) {
        return laneSegments[lane].frontVehicle(vehiclePos);
    }

    /**
     * Sets the speed limits for this road segment.
     * 
     * @param speedLimits
     */
    public void setSpeedLimits(SpeedLimits speedLimits) {
        this.speedLimits = speedLimits;
    }
    
    /**
     * Returns an iterable over all the speed limits in the road segment.
     * 
     * @return an iterable over all the speed limits in the road segment
     */
    public Iterable<SpeedLimit> speedLimits() {
        return speedLimits == null ? null : speedLimits;
    }
    
    /**
     * Sets the slopes for this road segment.
     * 
     * @param slopes
     */
    public void setSlopes(Slopes slopes) {
        this.slopes = slopes;
    }
    
    /**
     * Returns an iterable over all the slopes in the road segment.
     * 
     * @return an iterable over all the slopes in the road segment
     */
    public Iterable<Slope> slopes() {
        return slopes == null ? null : slopes;
    }

    /**
     * Sets the traffic lights for this road segment.
     * 
     * @param trafficLights
     */
    public void setTrafficLights(TrafficLights trafficLights) {
        this.trafficLights = trafficLights;
    }

    /**
     * Returns an iterable over all the traffic lights in the road segment.
     * 
     * @return an iterable over all the traffic lights in the road segment
     */
    public Iterable<TrafficLight> trafficLights() {
        return trafficLights == null ? null : trafficLights;
    }

    /**
     * Returns true if each lane in the vehicle array is sorted.
     * 
     * @return true if each lane in the vehicle array is sorted
     */
    public boolean eachLaneIsSorted() {
        for (final LaneSegment laneSegment : laneSegments) {
            if (laneSegment.laneIsSorted() == false) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("synthetic-access")
    private class VehicleIterator implements Iterator<Vehicle>, Iterable<Vehicle> {
        int lane;
        int index;
        int count;

        public VehicleIterator() {
        }

        @Override
        public boolean hasNext() {
            if (index < laneSegments[lane].vehicleCount()) {
                return true;
            }
            int nextLane = lane + 1;
            while (nextLane < laneCount) {
                if (laneSegments[nextLane].vehicleCount() > 0) {
                    return true;
                }
                ++nextLane;
            }
            final int vc = totalVehicleCount();
            if (vc != count) {
                assert false;
            }
            return false;
        }

        @Override
        public Vehicle next() {
            if (index < laneSegments[lane].vehicleCount()) {
                // get the next vehicle in the current lane
                ++count;
                return laneSegments[lane].getVehicle(index++);
            }
            int nextLane = lane + 1;
            while (nextLane < laneCount) {
                if (laneSegments[nextLane].vehicleCount() > 0) {
                    lane = nextLane;
                    index = 0;
                    ++count;
                    return laneSegments[lane].getVehicle(index++);
                }
                ++nextLane;
            }
            return null;
        }

        @Override
        public void remove() {
            // not supported
            assert false;
        }

        @Override
        public Iterator<Vehicle> iterator() {
            return new VehicleIterator();
        }
    }

    /**
     * Returns an iterator over all the vehicles in this road segment.
     * 
     * @return an iterator over all the vehicles in this road segment
     */
    @Override
    public final Iterator<Vehicle> iterator() {
        return new VehicleIterator();
    }

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
    public void checkForInconsistencies(double time, long iterationCount, boolean isWithCrashExit) {
        for (final LaneSegment laneSegment : laneSegments) {
            for (int index = 0, N = laneSegment.vehicleCount(); index < N; index++) {
                final Vehicle vehicle = laneSegment.getVehicle(index);
                final Vehicle vehFront = laneSegment.frontVehicle(vehicle);
                final double netDistance = vehicle.getNetDistance(vehFront);
                if (netDistance < 0) {
                    logger.error("Crash happened!!!");
                    final StringBuilder sb = new StringBuilder("\n");
                    sb.append(String.format("Crash of Vehicle i=%d (id=%d) at x=%.4f ", index, vehicle.getId(),
                            vehicle.getFrontPosition()));
                    if (vehFront != null) {
                        sb.append(String.format("with veh (id=%d) in front at x=%.4f on lane=%d\n", vehFront.getId(),
                                vehFront.getFrontPosition(), vehicle.getLane()));
                    }
                    sb.append("roadID=" + id);
                    sb.append(", net distance=" + netDistance);
                    sb.append(", lane index=" + laneSegment.lane());
                    sb.append(", container.size=" + laneSegment.vehicleCount());
                    sb.append("\n");

                    for (int j = Math.max(0, index - 8), M = laneSegment.vehicleCount(); j <= Math
                            .min(index + 8, M - 1); j++) {
                        final Vehicle veh = laneSegment.getVehicle(j);
                        sb.append(String
                                .format("veh=%d, pos=%6.2f, speed=%4.2f, accModel=%4.3f, acc=%4.3f, length=%3.1f, lane=%d, id=%d%n",
                                        j, veh.getFrontPosition(), veh.getSpeed(), veh.accModel(), veh.getAcc(),
                                        veh.getLength(), veh.getLane(), veh.getId()));
                    }
                    logger.error(sb.toString());
                    if (isWithCrashExit) {
                        logger.error(" !!! exit after crash !!! ");
                        System.exit(-99);
                    }
                }
            }
        }
    }

    @SuppressWarnings("synthetic-access")
    private class LaneSegmentIterator implements Iterator<LaneSegment> {
        int index;

        public LaneSegmentIterator() {
        }

        @Override
        public boolean hasNext() {
            if (index < laneCount) {
                return true;
            }
            return false;
        }

        @Override
        public LaneSegment next() {
            if (index < laneCount) {
                // get the next lane segment
                return laneSegments[index++];
            }
            return null;
        }

        @Override
        public void remove() {
            // not supported
            throw new UnsupportedOperationException("no remove possible");
        }
    }

    /**
     * Returns an iterator over all the lane segments in this road segment.
     * 
     * @return an iterator over all the lane segments in this road segment
     */
    public final Iterator<LaneSegment> laneSegmentIterator() {
        return new LaneSegmentIterator();
    }

    public final LoopDetectors getLoopDetectors() {
        return loopDetectors;
    }

    public void setLoopDetectors(LoopDetectors loopDetectors) {
        this.loopDetectors = loopDetectors;
    }

    public void setFlowConservingBottlenecks(FlowConservingBottlenecks flowConservingBottlenecks) {
        this.flowConservingBottlenecks = flowConservingBottlenecks;
    }

    /**
     * Asserts the road segment's class invariant. Used for debugging.
     */
    public boolean assertInvariant() {
        final RoadMapping roadMapping = roadMapping();
        if (roadMapping != null) {
            assert roadMapping.laneCount() == laneCount();
            assert roadMapping.trafficLaneMax() == trafficLaneMax();
            assert roadMapping.trafficLaneMin() == trafficLaneMin();
            assert Math.abs(roadMapping.roadLength() - roadLength()) < 0.1;
        }
        for (final LaneSegment laneSegment : laneSegments) {
            laneSegment.assertInvariant();
        }
        return true;
    }
}
