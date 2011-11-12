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
import java.util.Random;

import org.movsim.input.model.RoadInput;
import org.movsim.simulator.vehicles.Vehicle;

/**
 * <p>
 * A RoadSegment is a unidirectional stretch of road that contains vehicles. A bidirectional stretch
 * of road may be created by combining two road segments running in opposite directions.
 * </p>
 * <p>
 * RoadSegments may be combined to form a road network.
 * </p>
 * 
 * <p>
 * A RoadSegment is normally connected to two other road segments: a source road from which vehicles
 * enter the road segment and a sink road to which vehicles exit. RoadSegments at the edge of the
 * network will normally be connected to only one other road segment: traffic inflow and outflow
 * will be controlled directly by source and sink objects.
 * </p>
 * <p>
 * RoadSegments are connected to each other on a lane-wise basis: each sink (outgoing) lane of a
 * road segment may be connected to a source (incoming) lane of another road segment. This allows
 * the forking and merging of road segments, the creation of on-ramps and off-ramps. By connecting
 * the lanes of a number of road segments in this way, complex junctions and interchanges may be
 * created.
 * </p>
 * <p>
 * A RoadSegment is a logical entity, not a physical one. That is a RoadSegment does not know if it
 * is straight or winding, it just knows about the vehicles it contains and what it is connected to.
 * A vehicle's coordinates on a RoadsSegment are given by the vehicle's position relative to the
 * start of the RoadSegment and the vehicle's lane.
 * </p>
 * <p>
 * A RoadSegment has <code>laneCount</code> lanes. Lanes are of three types, traffic lanes, exit
 * (deceleration) lanes and entrance (acceleration) lanes. Typically vehicle behavior (and
 * especially lane change behavior) is different in each type of lane.
 * </p>
 * <p>
 * The mapping from a position on a RoadSegment to coordinates in physical space is determined by a
 * RoadSegment's RoadMapping. The RoadMapping is not used in the traffic simulation itself and is
 * only used by software that draws the road network and the vehicles upon it.
 * </p>
 * <p>
 * The vehicles on a each lane of a road segment are stored in a sorted ArrayList. This ArrayList is
 * kept sorted so that the vehicles in front of and behind a given vehicle can be found efficiently.
 * </p>
 */

public class RoadSegment implements Iterable<Vehicle> {

    public static final int ID_NOT_SET = -1;
    public static final int INITIAL_ID = 1;
    private static final boolean DEBUG = false;
    private static int nextId = INITIAL_ID;

    public static final int MAX_LANE_COUNT = 8;
    public static final int MAX_LANE_PAIR_COUNT = 12;

    private final int id;
    private String userId;
    private final double roadLength;
    private double cumulativeRoadLength = -1.0; // total length of road up to start of segment
    final int laneCount;
    // Lane linkage
    private RoadSegment sinkRoadSegment[];
    private int sinkLane[];
    private RoadSegment sourceRoadSegment[];
    private int sourceLane[];
    private Lane.Type laneType[];
    // Sources and Sinks
    private TrafficSource source;
    // sink is of type TrafficFlowBase to allow the sink to be a TrafficFlowOnRamp
    private TrafficFlowBase sink;
    private int removedVehicleCount; // used for calculating traffic flow
    private RoadMapping roadMapping;
    private static final int VEHICLES_PER_LANE_INITIAL_SIZE = 50;
    // ArrayList of vehicles for each lane
    final ArrayList<Vehicle>[] laneVehicles;

//    static class VehiclePositionComparator implements Comparator<Vehicle> {
//        @Override
//        public int compare(Vehicle lhs, Vehicle rhs) {
//            final double left = lhs.position();
//            final double right = rhs.position();
//            if (left > right) {
//                return 1;
//            } else if (left < right) {
//                return -1;
//            }
//            return 0;
//        }
//    }
//    private final VehiclePositionComparator vehiclePositionComparator = new VehiclePositionComparator();

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
    // warnings suppressed for laneVehicles ArrayList
    @SuppressWarnings("unchecked")
    public RoadSegment(double roadLength, int laneCount) {
        assert roadLength > 0.0;
        assert laneCount >= 1 && laneCount <= MAX_LANE_COUNT;
        laneVehicles = new ArrayList[laneCount];
        laneType = new Lane.Type[laneCount];
        for (int i = 0; i < laneCount; ++i) {
            laneVehicles[i] = new ArrayList<Vehicle>(VEHICLES_PER_LANE_INITIAL_SIZE);
            laneType[i] = Lane.Type.TRAFFIC;
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
     * Convenience constructor which also sets this road segment's source.
     * 
     * @param roadMapping
     * @param source
     */
    public RoadSegment(RoadMapping roadMapping, TrafficSource source) {
        this(roadMapping);
        setSource(source);
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
     * @param userId 
     * 
     */
    public final void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns this road segment's userId
     * 
     * @return this road segment's userId
     */
    public final String userId() {
        return userId;
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
     * @param roadMapping 
     */
    public final void setRoadMapping(RoadMapping roadMapping) {
        this.roadMapping = roadMapping;
    }

    /**
     * Returns the traffic source for this road segment.
     * 
     * @return the traffic source
     */
    public final TrafficSource source() {
        return source;
    }

    /**
     * Sets the traffic source for this road segment.
     * 
     * @param source
     *            the traffic source
     */
    public final void setSource(TrafficSource source) {
        assert source != null;
        this.source = source;
        source.setRoadSegment(this);
    }

    /**
     * Returns the traffic sink for this road segment.
     * 
     * @return the traffic sink
     */
    public final TrafficFlowBase sink() {
        return sink;
    }

    /**
     * Sets the traffic sink for this road segment.
     * 
     * @param sink
     *            the traffic sink
     */
    public final void setSink(TrafficFlowBase sink) {
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
        if (cumulativeRoadLength >= 0.0) {
            return cumulativeRoadLength;
        }
        final RoadSegment sourceRoadSegment = sourceRoadSegment(trafficLaneMax() - 1);
        cumulativeRoadLength = sourceRoadSegment == null ? 0.0 : sourceRoadSegment.cumulativeRoadLength() + sourceRoadSegment.roadLength();
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
        this.laneType[lane] = laneType;
        if (laneType == Lane.Type.ENTRANCE) {
            setSinkLaneForLane(Lane.NONE, lane);
        }
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
        return laneType[lane];
    }

    /**
     * Returns the minimum traffic lane.
     * 
     * @return the minimum traffic lane
     */
    public int trafficLaneMin() {
        int trafficLaneMin = 0;
        while (laneType[trafficLaneMin] != Lane.Type.TRAFFIC) {
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
        while (laneType[trafficLaneMax] != Lane.Type.TRAFFIC) {
            --trafficLaneMax;
        }
        return trafficLaneMax + 1;
    }

    public void setSourceRoadSegmentForLane(RoadSegment sourceRoad, int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        if (this.sourceRoadSegment == null) {
            this.sourceRoadSegment = new RoadSegment[MAX_LANE_PAIR_COUNT];
        }
        this.sourceRoadSegment[lane] = sourceRoad;
    }

    public RoadSegment sourceRoadSegment(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        if (sourceRoadSegment == null) {
            return null;
        }
        return sourceRoadSegment[lane];
    }

    public void setSourceLaneForLane(int sourceLane, int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        if (this.sourceLane == null) {
            this.sourceLane = new int[MAX_LANE_PAIR_COUNT];
        }
        this.sourceLane[lane] = sourceLane;
    }

    public int sourceLane(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_PAIR_COUNT;
        if (sourceLane == null) {
            return Lane.NONE;
        }
        return sourceLane[lane];
    }

    public void setSinkRoadSegmentForLane(RoadSegment sinkRoad, int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_COUNT;
        if (sinkRoadSegment == null) {
            sinkRoadSegment = new RoadSegment[MAX_LANE_PAIR_COUNT];
        }
        sinkRoadSegment[lane] = sinkRoad;
    }

    public RoadSegment sinkRoadSegment(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_COUNT;
        if (sinkRoadSegment == null) {
            return null;
        }
        return sinkRoadSegment[lane];
    }

    public void setSinkLaneForLane(int sinkLane, int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_COUNT;
        if (this.sinkLane == null) {
            this.sinkLane = new int[MAX_LANE_PAIR_COUNT];
        }
        this.sinkLane[lane] = sinkLane;
    }

    public int sinkLane(int lane) {
        assert lane >= Lane.LANE1 && lane < MAX_LANE_COUNT;
        if (sinkLane == null) {
            return Lane.NONE;
        }
        return sinkLane[lane];
    }

    /**
     * Returns the number of vehicles removed from this road segment.
     * 
     * @return the number of vehicles removed from this road segment
     */
    public int removedVehicleCount() {
        return removedVehicleCount;
    }

    /**
     * Clears the removed vehicle count.
     */
    public void clearVehicleRemovedCount() {
        removedVehicleCount = 0;
    }

    /**
     * Clears this road segment of any vehicles.
     */
    public void clearVehicles() {
        for (final ArrayList<Vehicle> vehicles : laneVehicles) {
            vehicles.clear();
        }
    }

    /**
     * Returns the total number of vehicles on this road segment, all lanes.
     * 
     * @return the total number of vehicles on this road segment
     */
    public int totalVehicleCount() {
        int totalVehicleCount = 0;
        for (final ArrayList<Vehicle> vehicles : laneVehicles) {
            totalVehicleCount += vehicles.size();
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
    public int vehicleCount(int lane) {
        assert lane >= Lane.LANE1;
        assert lane < laneCount;
        return laneVehicles[lane].size();
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
        return laneVehicles[lane].get(index);
    }

    /**
     * Returns the number of vehicles in the given lane.
     * @param lane
     * @return the number of vehicles in the given lane
     */
    public int getVehicleCount(int lane) {
        return laneVehicles[lane].size();
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
        laneVehicles[lane].remove(index);
    }

    /**
     * Removes the front vehicle on the given lane.
     * 
     * @param lane
     */
    public void removeFrontVehicleOnLane(int lane) {
        if (laneVehicles[lane].size() > 0) {
            laneVehicles[lane].remove(0);
        }
    }

    /**
     * Removes one vehicle at random.
     */
    public void removeRandomVehicle() {
        if (totalVehicleCount() < 1) {
            return;
        }
        final long seed = 987654321L;
        final Random random = new Random(seed);
        int lane = random.nextInt(laneCount);
        ArrayList<Vehicle> vehicles = laneVehicles[lane];
        int vehicleCount = vehicles.size();
        while (vehicleCount == 0) {
            // make sure we didn't choose an empty lane
            lane = random.nextInt(laneCount);
            vehicles = laneVehicles[lane];
            vehicleCount = vehicles.size();
        }
        int indexToRemove = random.nextInt(vehicleCount);
        if (vehicles.get(indexToRemove).type() == Vehicle.Type.TEST_CAR && vehicleCount > 1) {
            // only remove the test car if it is the last car remaining
            indexToRemove = (indexToRemove + 1) % vehicleCount;
        }
        vehicles.remove(indexToRemove);
    }

    /**
     * Chooses a random vehicle near the start of the road segment.
     * 
     * @return a random vehicle near the start of the road segment
     */
    public Vehicle getVehicleForPerturbation() {
        if (totalVehicleCount() < 1) {
            return null;
        }
        final long seed = 987654321L;
        final Random random = new Random(seed);
        int lane = random.nextInt(laneCount);
        ArrayList<Vehicle> vehicles = laneVehicles[lane];
        while (vehicles.size() == 0) {
            // make sure we didn't choose an empty lane
            lane = random.nextInt(laneCount);
            vehicles = laneVehicles[lane];
        }
        final int vehicleCount = vehicles.size();
        final int index = 5 * vehicleCount / 6;
        return vehicles.get(index);
    }

    /**
     * Removes any vehicles that have moved past the end of this road segment.
     */
    protected void removeVehiclesPastEnd() {
        for (final ArrayList<Vehicle> vehicles : laneVehicles) {
            int vehicleCount = vehicles.size();
            // remove any vehicles that have gone past the end of this road segment
            while (vehicleCount > 0 && vehicles.get(0).posRearBumper() > roadLength) {
                vehicles.remove(0);
                removedVehicleCount++;
                vehicleCount--;
            }
        }
    }

    /**
     * Adds an obstacle to this road segment.
     * 
     * @param obstacle
     */
    public void addObstacle(Vehicle obstacle) {
        assert obstacle.type() == Vehicle.Type.OBSTACLE;
        addVehicle(obstacle);
    }

    /**
     * Adds a vehicle to this road segment.
     * 
     * @param vehicle
     */
    public void addVehicle(Vehicle vehicle) {
        assert vehicle.posRearBumper() >= 0.0;
        assert vehicle.getSpeed() >= 0.0;
        assert vehicle.getLane() >= Lane.LANE1;
        assert vehicle.getLane() < laneCount;
        final int index = positionBinarySearch(vehicle.getLane(), vehicle.posRearBumper());
        if (index < 0) {
            laneVehicles[vehicle.getLane()].add(-index - 1, vehicle);
        } else if (index == 0) {
            laneVehicles[vehicle.getLane()].add(0, vehicle);
        } else {
            // vehicle is in the same position as an existing vehicle - this should not happen
            assert false;
        }
//        vehicle.setRoadSegment(id, roadLength);
        assert eachLaneIsSorted();
    }

    /**
     * Adds a vehicle to the start of this road segment.
     * 
     * @param vehicle
     */
    public void appendVehicle(Vehicle vehicle) {
        assert vehicle.posRearBumper() >= 0.0;
        assert vehicle.getSpeed() >= 0.0;
        assert vehicle.getLane() >= Lane.LANE1;
        assert vehicle.getLane() < laneCount;
//        vehicle.setRoadSegment(id, roadLength);
        assert eachLaneIsSorted();
        if (DEBUG) {
            if (laneVehicles[vehicle.getLane()].size() > 0) {
                final Vehicle lastVehicle = laneVehicles[vehicle.getLane()].get(laneVehicles[vehicle.getLane()].size() - 1);
                if (lastVehicle.posRearBumper() < vehicle.posRearBumper()) {
                    assert false;
                }
            }
        }
        laneVehicles[vehicle.getLane()].add(vehicle);
        assert eachLaneIsSorted();
    }

    /**
     * <p>
     * For each vehicle check if a lane change is desired and safe and, if so, make the lane change.
     * </p>
     * <p>
     * <code>makeLaneChanges</code> preserves the vehicle sort order, since only lateral movements
     * of vehicles are made.
     * </p>
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     */
    protected void makeLaneChanges(double dt, double simulationTime, long iterationCount) {

        assert eachLaneIsSorted();
    }

    /**
     * <p>
     * Update the vehicle positions and velocities by calling vehicle.updatePositionAndVelocity for
     * each vehicle.
     * </p>
     * 
     * <p>
     * If there is a test car, then record its position, velocity etc.
     * </p>
     * 
     * <p>
     * If there is a traffic inhomogeneity, then apply it to each vehicle.
     * </p>
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     * @param iterationCount 
     */
    public void updateVehiclePositionsAndVelocities(double dt, double simulationTime, long iterationCount) {
        assert eachLaneIsSorted();
        // this function may change vehicle ordering in this or another road segment
        // remember V[n+1].pos < V[n].pos < V[n-1].pos ... < V[1].pos < V[0].pos
        // Vehicle iteration loop goes backwards, that is it starts with vehicles nearest
        // the start of this road segment. This is so a vehicle's new speed and position is
        // calculated before the vehicle in front of it has been moved.
        Vehicle frontFrontVehicle = null;
        Vehicle frontVehicle;
        for (int lane = 0; lane < laneCount; ++lane) {
            final ArrayList<Vehicle> vehicles = laneVehicles[lane];
            final int count = vehicles.size();
            // TODO refactor this loop so frontVehicle is reused as vehicle and end two cases are
            // unrolled
            for (int i = count - 1; i >= 0; --i) {
                final Vehicle vehicle = vehicles.get(i);
                if (i > 0) {
                    frontVehicle = vehicles.get(i - 1);
                } else {
                    if (sinkLane(lane) == Lane.NONE) {
                        // no sink lane for this lane, so there are no vehicles ahead of vehicle(0)
                        frontVehicle = null;
                    } else {
                        // the front vehicle is the rear vehicle on the sink lane
                        frontVehicle = rearVehicleOnSinkLanePosAdjusted(lane);
                         if (frontVehicle == null) {
                             // no vehicle in the sink lane, so must recursively follow the lanes
                             // to the end of the road
                             frontVehicle = frontVehicle(lane, vehicle.getPosition());
                         }
                    }
                }
           }
        }
        // occasionally updatePositionAndVelocity could cause the vehicles array
        // to become unsorted if LongitudinalDriverModel.MAX_DECELERATION >
        // LaneChangeModel.maxSafeBraking, since a new entry into a lane might cause
        // excessive breaking
        // sortVehicles();
        assert eachLaneIsSorted();
    }


    protected void updateVehiclePositionsAndVelocities(int lane, double[] outputPos, double[] outputVel, int count) {
        final ArrayList<Vehicle> vehicles = laneVehicles[lane];
        for (int i = 0; i < count; ++i) {
            final Vehicle v = vehicles.get(i);
            v.setPosition(outputPos[i]);
            v.setSpeed(outputVel[i]);
        }
    }

  /**
     * If there is a traffic sink, use it to perform any traffic outflow.
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     */
    protected void outFlow(double dt, double simulationTime, long iterationCount) {
        assert eachLaneIsSorted();
        // in each lane, remove any vehicles that have gone past the end of this road segment
        for (int lane = 0; lane < laneCount; ++lane) {
            final RoadSegment sinkRoad = sinkRoadSegment(lane);
            if (sinkRoad != null) {
                final int laneOnNewRoadSegment = sinkLane[lane];
                final ArrayList<Vehicle> vehicles = laneVehicles[lane];
                int count = vehicles.size();
                // remove any vehicles that have gone past the end of this road segment
                while (count > 0) {
                    final Vehicle vehicle = vehicles.get(0);
                    if (vehicle.posRearBumper() < roadLength) {
                        break;
                    }
                    // if the vehicle is past the end of this road segment then move it onto the
                    // sink lane for its lane
                    // TODO - check previous lane correct (used for drawing vehicle when changing lanes)
                    // final int prevLaneOnNewRoadSegment = lane;
                    // final int prevLaneOnNewRoadSegment = sinkLane[vehicle.previousLane()];
                    final double positionOnNewRoadSegment = vehicle.posRearBumper() - roadLength;
                    double exitEndPos = Vehicle.EXIT_POSITION_NOT_SET;
                    if (sinkRoad.laneType(laneOnNewRoadSegment) == Lane.Type.TRAFFIC) {
                        final int exitRoadSegmentId = 0; //vehicle.exitRoadSegmentId();
                        if (exitRoadSegmentId == sinkRoad.id()) {
                            // vehicle is on exit exit road segment, so exit end pos is end of this
                            // road segment
                            exitEndPos = sinkRoad.roadLength();
                        } else {
                            // check if next segment is exit segment
                            final RoadSegment sinkSinkRoad = sinkRoad.sinkRoadSegment(Lane.LANE1);
                            if (sinkSinkRoad != null && sinkSinkRoad.id() == exitRoadSegmentId) {
                                // next road segment is exit road segment
                                exitEndPos = sinkRoad.roadLength() + sinkSinkRoad.roadLength();
                            }
                        }
                    }
                    vehicle.moveToNewRoadSegment(laneOnNewRoadSegment, positionOnNewRoadSegment, exitEndPos);
                    // remove vehicle from this road segment
                    vehicles.remove(0);
                    --count;
                    ++removedVehicleCount;
                    // put the vehicle onto the new road segment (note that even when a road segment
                    // is joined to itself (eg for a traffic circle) the vehicle needs to be added
                    // and removed - this ensures vehicles remain sorted)
                    sinkRoad.appendVehicle(vehicle);
                }
            }
        }
//        if (sink != null) {
//            sink.timeStep(dt, simulationTime, iterationCount);
//        }
    }

    /**
     * If there is a traffic source, use it to perform any traffic inflow.
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     */
    protected void inFlow(double dt, double simulationTime, long iterationCount) {
        assert eachLaneIsSorted();
//        if (source != null) {
//            source.timeStep(dt, simulationTime, iterationCount);
//        }
    }

     /**
     * Returns the rear vehicle on the given lane.
     * 
     * @param lane
     * @return the rear vehicle on the given lane
     */
    protected Vehicle rearVehicleOnLane(int lane) {
        final int count = laneVehicles[lane].size();
        if (count > 0) {
            return laneVehicles[lane].get(count - 1);
        }
        return null;
    }

    /**
     * Finds the vehicle in the given lane immediately at or behind the given position.
     * 
     * @param lane
     *            lane in which to search
     * @return reference to the rear vehicle
     */
    Vehicle rearVehicle(int lane, double vehiclePos) {

        final ArrayList<Vehicle> vehicles = laneVehicles[lane];
        final int index = positionBinarySearch(lane, vehiclePos);
        final int insertionPoint = -index - 1;
        if (index >= 0) {
            // exact match found, so return the matched vehicle
            if (index < vehicles.size()) {
                return vehicles.get(index);
            }
        } else {
            // get next vehicle if not past end
            if (insertionPoint < vehicles.size()) {
                return vehicles.get(insertionPoint);
            }
        }
        // index == laneVehicles[lane].size() - 1 || insertionPoint == laneVehicles[lane].size()
        // subject vehicle is rear vehicle on this road segment, so check source road segment
        if (sourceRoadSegment != null) {
            // didn't find a rear vehicle in the current road segment, so
            // check the previous (source) road segment
            final RoadSegment sourceRoad = sourceRoadSegment(lane);
            if (sourceRoad != null) {
                // find the front vehicle in the source lane on the source road segment
                final int sLane = sourceLane[lane];
                final Vehicle sourceFrontVehicle = sourceRoad.frontVehicleOnLane(sLane);
                if (sourceFrontVehicle != null) {
                    // return a copy of the front vehicle on the source road segment, with its
                    // position set relative to the current road segment
                    final Vehicle rearVehicle = new Vehicle(sourceFrontVehicle);
                    rearVehicle.setPosition(rearVehicle.getPosition() - sourceRoad.roadLength());
                    return rearVehicle;
                }
            }
        }
        return null;
    }

    public Vehicle rearVehicleOnSinkLanePosAdjusted(int lane) {

        // subject vehicle is front vehicle on this road segment, so check sink road segment
        if (sinkRoadSegment == null) {
            return null;
        }
        final int sLane = sinkLane[lane];
        if (sLane == Lane.NONE) {
            return null;
        }
        final RoadSegment sinkRoad = sinkRoadSegment[lane];
        if (sinkRoad == null) {
            return null;
        }
        // find the rear vehicle in the sink lane on the sink road segment
        final Vehicle sinkRearVehicle = sinkRoad.rearVehicleOnLane(sLane);
        if (sinkRearVehicle == null) {
            return null;
        }
        // return a copy of the rear vehicle on the sink road segment, with its position
        // set relative to the current road segment
        final Vehicle ret = new Vehicle(sinkRearVehicle);
        ret.setPosition(ret.getPosition() + roadLength);
        return ret;
    }

    Vehicle secondLastVehicleOnSinkLanePosAdjusted(int lane) {

        // subject vehicle is front vehicle on this road segment, so check sink road segment
        if (sinkRoadSegment == null) {
            return null;
        }
        final int sLane = sinkLane[lane];
        if (sLane == Lane.NONE) {
            return null;
        }
        final RoadSegment sinkRoad = sinkRoadSegment[lane];
        if (sinkRoad == null) {
            return null;
        }
        // find the rear vehicle in the sink lane on the sink road segment
        final int sinkLaneVehicleCount = sinkRoad.vehicleCount(sLane);
        if (sinkLaneVehicleCount < 2) {
            // should actually check sinkLane of sinkLane, but as long as sinkLane not
            // outrageously short, the assumption that there is no vehicle is reasonable
            return null;
        }
        final Vehicle vehicle = sinkRoad.getVehicle(sLane, sinkLaneVehicleCount - 2);
        // return a copy of the rear vehicle on the sink road segment, with its position
        // set relative to the current road segment
        final Vehicle ret = new Vehicle(vehicle);
        ret.setPosition(ret.getPosition() + roadLength);
        return ret;
    }

    /**
     * Returns the front vehicle on the given lane.
     * 
     * @param lane
     * @return the front vehicle on the given lane
     */
    public Vehicle frontVehicleOnLane(int lane) {
        if (laneVehicles[lane].size() > 0) {
            return laneVehicles[lane].get(0);
        }
        return null;
    }

    /**
     * Finds the vehicle in the given lane immediately in front of the given position.
     * That is a vehicle such that vehicle.positon() > vehicePos (strictly greater than).
     * The vehicle whose position equals vehiclePos is deemed to be in the rear.
     * 
     * @param lane
     *            lane in which to search
     * @param subjectVehicle
     * @param index
     *            index of subject vehicle
     * @return reference to the front vehicle
     */
    Vehicle frontVehicle(int lane, double vehiclePos) {

        final ArrayList<Vehicle> vehicles = laneVehicles[lane];
        // index = Collections.binarySearch(vehicles, subjectVehicle, vehiclePositionComparator);
        final int index = positionBinarySearch(lane, vehiclePos);
        final int insertionPoint = -index - 1;
        if (index > 0) {
            // exact match found
            return vehicles.get(index - 1);
        } else if (insertionPoint > 0) {
            return vehicles.get(insertionPoint - 1);
        }
        // index == 0 or insertionPoint == 0
        // subject vehicle is front vehicle on this road segment, so check for vehicles
        // on sink road segment
        if (sinkRoadSegment != null) {
            // didn't find a front vehicle in the current road segment, so
            // check the next (sink) road segment
            final RoadSegment sinkRoad = sinkRoadSegment[lane];
            if (sinkRoad != null) {
                // find the rear vehicle in the sink lane on the sink road segment
                final int sLane = sinkLane[lane];
                final Vehicle sinkRearVehicle = sinkRoad.rearVehicleOnLane(sLane);
                if (sinkRearVehicle != null) {
                    // return a copy of the rear vehicle on the sink road segment, with its position
                    // set relative to the current road segment
                    final Vehicle frontVehicle = new Vehicle(sinkRearVehicle);
                    frontVehicle.setPosition(frontVehicle.getPosition() + roadLength);
                    return frontVehicle;
                }
            }
        }
        return null;
    }

    private int positionBinarySearch(int lane, double vehiclePos) {
        int low = 0;
        int high = laneVehicles[lane].size() - 1;

        while (low <= high) {
            final int mid = (low + high) >> 1;
            final double midPos = laneVehicles[lane].get(mid).posRearBumper();
            // final int compare = Double.compare(midPos, vehiclePos);
            // note vehicles are sorted in reverse order of position
            final int compare = Double.compare(vehiclePos, midPos);
            if (compare < 0) {
                low = mid + 1;
            } else if (compare > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1); // key not found
    }

    /**
     * Returns true if each lane in the vehicle array is sorted.
     * 
     * @return true if each lane in the vehicle array is sorted
     */
    public boolean eachLaneIsSorted() {
        for (int lane = 0; lane < laneCount; ++lane) {
            final ArrayList<Vehicle> vehicles = laneVehicles[lane];
            final int count = vehicles.size();
            if (count > 1) { // if zero or one vehicles in lane then it is necessarily sorted
                Vehicle frontVehicle = vehicles.get(0);
                for (int i = 1; i < count; ++i) {
                    final Vehicle vehicle = vehicles.get(i);
                    if (frontVehicle.posRearBumper() < vehicle.posRearBumper()) {
                        return false;
                    }
                    // current vehicle is front vehicle next time around
                    frontVehicle = vehicle;
                }
            }
        }
        return true;
    }

    /**
     * Simple bubble sort of the vehicles in each lane.
     * Useful for debugging.
     */
    @SuppressWarnings("unused")
    private void sortVehicles() {
        for (int lane = 0; lane < laneCount; ++lane) {
            final ArrayList<Vehicle> vehicles = laneVehicles[lane];
            // Collections.sort(vehicles, vehiclePositionComparator);
            final int count = vehicles.size();
            boolean sorted = false;
            while (!sorted) {
                sorted = true;
                for (int i = 1; i < count; ++i) {
                    final Vehicle front = vehicles.get(i - 1);
                    final Vehicle rear = vehicles.get(i);
                    if (rear.posRearBumper() > front.posRearBumper()) {
                        sorted = false;
                        // swap the two vehicles
                        vehicles.set(i - 1, rear);
                        vehicles.set(i, front);
                    }
                }
            }
        }
    }

 

    private class VehicleIterator implements Iterator<Vehicle> {
        int lane;
        int index;
        int count;

        public VehicleIterator() {
        }

        @Override
        public boolean hasNext() {
            if (index < laneVehicles[lane].size()) {
                return true;
            }
            int nextLane = lane + 1;
            while (nextLane < laneCount) {
                if (laneVehicles[nextLane].size() > 0) {
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
            if (index < laneVehicles[lane].size()) {
                // get the next vehicle in the current lane
                ++count;
                return laneVehicles[lane].get(index++);
            }
            int nextLane = lane + 1;
            while (nextLane < laneCount) {
                if (laneVehicles[nextLane].size() > 0) {
                    lane = nextLane;
                    index = 0;
                    ++count;
                    return laneVehicles[lane].get(index++);
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

    
    // TODO roadsections: process roadInput from movsim xml here !!! 
    public void addInput(RoadInput roadInput) {
        // TODO Auto-generated method stub
    }
}
