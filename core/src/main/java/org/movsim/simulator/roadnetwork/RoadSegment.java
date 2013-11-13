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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.simulator.roadnetwork.boundaries.AbstractTrafficSource;
import org.movsim.simulator.roadnetwork.boundaries.SimpleRamp;
import org.movsim.simulator.roadnetwork.boundaries.TrafficSink;
import org.movsim.simulator.roadnetwork.controller.GradientProfile;
import org.movsim.simulator.roadnetwork.controller.RoadObject;
import org.movsim.simulator.roadnetwork.controller.RoadObject.RoadObjectType;
import org.movsim.simulator.roadnetwork.controller.RoadObjects;
import org.movsim.simulator.roadnetwork.controller.SpeedLimit;
import org.movsim.simulator.roadnetwork.controller.TrafficLight;
import org.movsim.simulator.roadnetwork.controller.VariableMessageSignDiversion;
import org.movsim.simulator.roadnetwork.predicates.VehicleWithinRange;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * <p>
 * A RoadSegment is a unidirectional stretch of road that contains a number of lane segments. A bidirectional stretch of road may be created
 * by combining two road segments running in opposite directions.
 * </p>
 * <p>
 * RoadSegmentUtils may be combined to form a road network.
 * </p>
 * <p>
 * A RoadSegment is normally connected to two other road segments: a source road from which vehicles enter the road segment and a sink road
 * to which vehicles exit. RoadSegmentUtils at the edge of the network will normally be connected to only one other road segment: traffic inflow
 * and outflow will be controlled directly by source and sink objects.
 * </p>
 * <p>
 * RoadSegmentUtils are connected to each other on a lane-wise basis: each sink (outgoing) lane of a road segment may be connected to a source
 * (incoming) lane of another road segment. This allows the forking and merging of road segments, the creation of on-ramps and off-ramps. By
 * connecting the lanes of a number of road segments in this way, complex junctions and interchanges may be created.
 * </p>
 * <p>
 * A RoadSegment is a logical entity, not a physical one. That is a RoadSegment does not know if it is straight or winding, it just knows
 * about the vehicles it contains and what it is connected to. A vehicle's coordinates on a RoadsSegment are given by the vehicle's position
 * relative to the start of the RoadSegment and the vehicle's lane.
 * </p>
 * <p>
 * A RoadSegment has <code>laneCount</code> lanes. Lanes within a RoadSegment are represented by the LaneSegment class.
 * </p>
 * <p>
 * The mapping from a position on a RoadSegment to coordinates in physical space is determined by a RoadSegment's RoadMapping. Although the
 * RoadMapping is primarily used by software that draws the road network and the vehicles upon it, elements of the RoadMapping may influence
 * vehicle behavior, in particular a road's curvature and its gradient.
 * </p>
 */
// TODO avoid iterating also over Vehicle.Type.OBSTACLE at lane ends.
public class RoadSegment extends DefaultWeightedEdge implements Iterable<Vehicle> {

    private static final long serialVersionUID = -2991922063982378462L;

    private static final Logger LOG = LoggerFactory.getLogger(RoadSegment.class);

    static final int ID_NOT_SET = -1;
    static final int INITIAL_ID = 1;
    private static int nextId = INITIAL_ID;

    private RoadSegmentDirection directionType = RoadSegmentDirection.FORWARD;

    /** the nodeId is an internally used unique identifier for the road. */
    private final int id;
    /** the userId is the nodeId specified in the .xodr and .xml files. */
    private String userId;
    /** road name specified in the openDrive .xodr network file. */
    private String roadName;

    private final double roadLength;
    private final int laneCount;
    private final LaneSegment laneSegments[];

    // TODO extend Node idea to keep information of connecting roadSegments
    private int sizeSourceRoadSegments = -1;
    private int sizeSinkRoadSegments = -1;

    private final RoadObjects roadObjects;
    private final SignalPoints signalPoints = new SignalPoints();

    /** will be initialized lazily */
    private final LaneSegment overtakingSegment;
    private boolean overtakingSegmentInitialized = false;

    // Sources and Sinks
    private AbstractTrafficSource trafficSource;
    private TrafficSink sink;
    private RoadMapping roadMapping;

    private RoadSegment peerRoadSegment;

    private Node origin = new NodeImpl("origin");
    private Node destination = new NodeImpl("destination");

    /** simple ramp (source) with dropping mechanism */
    private SimpleRamp simpleRamp;

    /** dynamic ff speed, considering speed limits. */
    private double meanFreeFlowSpeed = -1;

    /** static freeflow speed as maximum speed that is allowed. */
    private double freeFlowSpeed = RoadTypeSpeeds.INSTANCE.getDefaultFreeFlowSpeed();

    public static class TestCar {
        public double s = 0.0; // distance
        public double vdiff = 0.0; // approaching rate
        public double vel = 0.0; // velocity
        public double acc = 0.0; // acceleration
    }

    /**
     * Resets the next nodeId.
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
        assert laneCount >= 1 : "laneCount=" + laneCount;
        laneSegments = new LaneSegment[laneCount];
        for (int index = 0; index < laneCount; ++index) {
            laneSegments[index] = new LaneSegment(this, index + 1);
        }
        id = nextId++;
        assert roadLength > 0;
        this.roadLength = roadLength;
        this.laneCount = laneCount;
        this.roadObjects = new RoadObjects(this);
        overtakingSegment = new LaneSegment(this, Lanes.OVERTAKING);
    }

    public RoadSegment(double roadLength, int laneCount, RoadMapping roadMapping,
            RoadSegmentDirection roadSegmentDirection) {
        this(roadLength, laneCount);
        this.directionType = roadSegmentDirection;
        this.roadMapping = Preconditions.checkNotNull(roadMapping);
    }

    /**
     * Sets a default sink for this road segment.
     */
    public final void addDefaultSink() {
        if (sink != null) {
            LOG.warn("sink already set on road=" + userId());
        }
        sink = new TrafficSink(this);
    }

    /**
     * Returns this road segment's nodeId
     * 
     * @return this road segment's nodeId
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
     * Returns this road segment's userId. The userId is the road's nodeId as set in the .xodr and .xml files.
     * 
     * @return this road segment's userId
     */
    public final String userId() {
        return userId == null ? Integer.toString(id) : userId;
    }

    public final RoadSegmentDirection directionType() {
        return directionType;
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
    public final AbstractTrafficSource trafficSource() {
        return trafficSource;
    }

    /**
     * Sets the traffic source (upstream boundary) for this road segment.
     * 
     * @param trafficSource
     *            the traffic source
     */
    public final void setTrafficSource(AbstractTrafficSource trafficSource) {
        Preconditions.checkArgument(this.trafficSource == null, "roadSegment=" + id()
                + " already has a traffic source.");
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

    public final boolean hasSink() {
        return sink != null;
    }

    // /**
    // * Sets the traffic sink for this road segment.
    // *
    // * @param sink
    // * the traffic sink
    // */
    // final void setSink(TrafficSink sink) {
    // this.sink = sink;
    // }

    /**
     * Returns this road segment's length.
     * 
     * @return road segment length in meters
     */
    public final double roadLength() {
        return roadLength;
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
    public void setLaneType(int lane, Lanes.Type laneType) {
        laneSegments[lane - 1].setType(laneType);
        if (roadMapping != null) {
            // roadMapping.setTrafficLaneMin(trafficLaneMin());
            // roadMapping.setTrafficLaneMax(trafficLaneMax());
        }
    }

    /**
     * Returns the type of the given lane.
     * 
     * @param lane
     * 
     * @return type of lane
     */
    public Lanes.Type laneType(int lane) {
        return laneSegments[lane - 1].type();
    }

    /**
     * Returns the minimum traffic lane (that is not an entry or exit lane).
     * 
     * @return the minimum traffic lane
     */
    public int trafficLaneMin() {
        int trafficLaneMin = Lanes.MOST_INNER_LANE;
        while (laneSegments[trafficLaneMin - 1].type() != Lanes.Type.TRAFFIC) {
            ++trafficLaneMin;
        }
        return trafficLaneMin;
    }

    /**
     * Returns the maximum traffic lane (that is not an entry or exit lane).
     * 
     * @return the maximum traffic lane
     */
    public int trafficLaneMax() {
        int trafficLaneMax = laneCount;
        while (laneSegments[trafficLaneMax - 1].type() != Lanes.Type.TRAFFIC) {
            --trafficLaneMax;
        }
        return trafficLaneMax;
    }

    public final LaneSegment laneSegment(int lane) {
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount, "lane=" + lane);
        return laneSegments[lane - 1];
    }

    final void setSourceLaneSegmentForLane(LaneSegment sourceLaneSegment, int lane) {
        Preconditions.checkNotNull(sourceLaneSegment);
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount, "lane=" + lane);
        laneSegments[lane - 1].setSourceLaneSegment(sourceLaneSegment);
    }

    public final LaneSegment sourceLaneSegment(int lane) {
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount, "lane=" + lane
                + " not defined for roadId=" + userId());
        return laneSegments[lane - 1].sourceLaneSegment();
    }

    public final RoadSegment sourceRoadSegment(int lane) {
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount);
        if (laneSegments[lane - 1].sourceLaneSegment() == null) {
            return null;
        }
        return laneSegments[lane - 1].sourceLaneSegment().roadSegment();
    }

    public final int sourceLane(int lane) {
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount);
        if (laneSegments[lane - 1].sourceLaneSegment() == null) {
            return Lanes.NONE;
        }
        return laneSegments[lane - 1].sourceLaneSegment().lane();
    }

    final void setSinkLaneSegmentForLane(LaneSegment sinkLaneSegment, int lane) {
        Preconditions.checkNotNull(sinkLaneSegment);
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount);
        laneSegments[lane - 1].setSinkLaneSegment(sinkLaneSegment);
    }

    final LaneSegment sinkLaneSegment(int lane) {
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount);
        return laneSegments[lane - 1].sinkLaneSegment();
    }

    public final RoadSegment sinkRoadSegment(int lane) {
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount, "lane=" + lane + " but laneCount="
                + laneCount);
        if (laneSegments[lane - 1].sinkLaneSegment() == null) {
            return null;
        }
        return laneSegments[lane - 1].sinkLaneSegment().roadSegment();
    }

    @CheckForNull
    public RoadSegment sinkRoadSegmentPerId(int exitRoadSegmentId) {
        for (LaneSegment laneSegment : laneSegments) {
            if (laneSegment.hasSinkLaneSegment()) {
                RoadSegment sinkRoadSegment = laneSegment.sinkLaneSegment().roadSegment();
                if (sinkRoadSegment.id() == exitRoadSegmentId) {
                    return sinkRoadSegment;
                }
            }
        }
        return null;
    }

    final int sinkLane(int lane) {
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount);
        if (laneSegments[lane - 1].sinkLaneSegment() == null) {
            return Lanes.NONE;
        }
        return laneSegments[lane - 1].sinkLaneSegment().lane();
    }

    public final boolean hasUpstreamConnection() {
        return getSizeSourceRoadSegments() > 0;
    }

    public final boolean hasDownstreamConnection() {
        return getSizeSinkRoadSegments() > 0;
    }

    public final int getSizeSinkRoadSegments() {
        if (sizeSinkRoadSegments < 0) {
            // lazy init
            Set<RoadSegment> sinkRoadSegments = new HashSet<>();
            for (LaneSegment laneSegment : laneSegments) {
                if (laneSegment.hasSinkLaneSegment()) {
                    sinkRoadSegments.add(laneSegment.sinkLaneSegment().roadSegment());
                }
            }
            sizeSinkRoadSegments = sinkRoadSegments.size();
        }
        return sizeSinkRoadSegments;
    }

    public final int getSizeSourceRoadSegments() {
        if (sizeSourceRoadSegments < 0) {
            // lazy init
            Set<RoadSegment> sourceRoadSegments = new HashSet<>();
            for (LaneSegment laneSegment : laneSegments) {
                if (laneSegment.hasSourceLaneSegment()) {
                    sourceRoadSegments.add(laneSegment.sourceLaneSegment().roadSegment());
                }
            }
            sizeSourceRoadSegments = sourceRoadSegments.size();
        }
        return sizeSourceRoadSegments;
    }

    public boolean exitsOnto(int exitRoadSegmentId) {
        for (final LaneSegment laneSegment : laneSegments) {
            if (laneSegment.type() == Lanes.Type.EXIT) {
                assert laneSegment.sinkLaneSegment() != null : "roadSegment=" + userId() + " with lane="
                        + laneSegment.lane() + " has no downstream connection.";
                if (laneSegment.sinkLaneSegment().roadSegment().id() == exitRoadSegmentId) {
                    return true;
                }
            }
        }
        return false;
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
     * Returns the number of vehicles on this road segment, all lanes.
     * 
     * @return the total number of vehicles on this road segment
     */
    public int getVehicleCount() {
        int vehicleCount = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            vehicleCount += laneSegment.vehicleCount();
        }
        return vehicleCount;
    }

    public int getStoppedVehicleCount() {
        int stoppedVehicleCount = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            stoppedVehicleCount += laneSegment.stoppedVehicleCount();
        }
        return stoppedVehicleCount;
    }

    /**
     * Returns the number of obstacle vehicles on this road segment, all lanes.
     * 
     * @return the total number of vehicles on this road segment
     */
    public int getObstacleCount() {
        int obstacleCount = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            obstacleCount += laneSegment.obstacleCount();
        }
        return obstacleCount;
    }

    /**
     * Returns the number of vehicles in the given lane on this road segment.
     * 
     * @param lane
     * 
     * @return the number of vehicles in the given lane on this road segment
     */
    public int getVehicleCount(int lane) {
        Preconditions.checkArgument(lane >= Lanes.LANE1 && lane <= laneCount);
        return laneSegments[lane - 1].vehicleCount();
    }

    /**
     * Returns the total travel time of all vehicles on this road segment, all lanes.
     * 
     * @return the total vehicle travel time
     */
    protected double totalVehicleTravelTime() {
        double totalVehicleTravelTime = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            totalVehicleTravelTime += laneSegment.totalVehicleTravelTime();
        }
        return totalVehicleTravelTime;
    }

    /**
     * Returns the total travel distance of all vehicles on this road segment, all lanes.
     * 
     * @return the total vehicle travel distance
     */
    protected double totalVehicleTravelDistance() {
        double totalVehicleTravelDistance = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            totalVehicleTravelDistance += laneSegment.totalVehicleTravelDistance();
        }
        return totalVehicleTravelDistance;
    }

    /**
     * Returns the total fuel used by all vehicles on this road segment, all lanes.
     * 
     * @return the total vehicle fuel used
     */
    protected double totalVehicleFuelUsedLiters() {
        double totalVehicleFuelUsedLiters = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            totalVehicleFuelUsedLiters += laneSegment.totalVehicleFuelUsedLiters();
        }
        return totalVehicleFuelUsedLiters;
    }

    protected double instantaneousConsumptionLitersPerSecond() {
        double vehicleFuelUsedLiters = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            vehicleFuelUsedLiters += laneSegment.instantaneousFuelUsedLitersPerS();
        }
        return vehicleFuelUsedLiters;
    }

    public double meanSpeedOfVehicles() {
        double sumSpeed = 0;
        int vehCount = 0;
        for (LaneSegment laneSegment : laneSegments) {
            for (Vehicle veh : laneSegment) {
                if (veh.type() == Vehicle.Type.OBSTACLE) {
                    continue;
                }
                sumSpeed += veh.getSpeed();
                ++vehCount;
            }
        }
        return (vehCount > 0) ? sumSpeed / vehCount : getHarmonicMeanFreeflowSpeed();
    }

    private double getHarmonicMeanFreeflowSpeed() {
        if (meanFreeFlowSpeed < 0) {
            // evaluate lazy here
            double sumInvers = 0;
            double currentPosition = 0;
            double speedLimitPosition = 0;
            double currentSpeedLimit = freeFlowSpeed;

            // tricky
            for (SpeedLimit speedLimit : speedLimits()) {
                speedLimitPosition = speedLimit.position();
                sumInvers += (1. / currentSpeedLimit) * (speedLimitPosition - currentPosition);
                currentSpeedLimit = Math.min(speedLimit.getSpeedLimit(), freeFlowSpeed);
                currentPosition = speedLimitPosition;
            }

            sumInvers += (1. / currentSpeedLimit) * (roadLength - speedLimitPosition);
            meanFreeFlowSpeed = 1. / (sumInvers / roadLength);
        }
        return meanFreeFlowSpeed;
    }

    private double getSpeedLimit(double position) {
        double speedLimit = getFreeFlowSpeed();
        for (SpeedLimit sl : speedLimits()) {
            if (position < sl.position()) {
                return speedLimit;
            }
            speedLimit = sl.getSpeedLimit();
        }
        return speedLimit;
    }

    /**
     * Returns the instantaneous travel time defined by the road element length
     * and current mean speed of all vehicles. An adhoc free speed is assumed in
     * case of an empty road. An adhoc minimum speed is assumed in case of a
     * standstill to avoid a diverging traveltime.
     * 
     * @return instantantaneous travel time with assumed travel time if road is
     *         empty and with assumed maximum travel time in standstill
     */
    public double instantaneousTravelTime() {
        final double dx = 100; // TODO refactor
        double position = 0;
        double totalTravelTime = 0;
        // TODO hack here, depends on order of vehicles
        LinkedList<Vehicle> vehicles = Lists.newLinkedList();
        Iterators.addAll(vehicles, iterator());
        while (position < roadLength) {
            double end = Math.min(position + dx, roadLength);
            double maxRoadSpeed = freeFlowSpeed; // FIXME consider speedlimits
            totalTravelTime += travelTimeInRange(position, end, maxRoadSpeed, vehicles);
            position += dx;
        }
        return totalTravelTime;
    }

    private static double travelTimeInRange(double begin, double end, double maxRoadSpeed, LinkedList<Vehicle> vehicles) {
        int count = 0;
        double sumSpeed = 0;
        while (!vehicles.isEmpty() && vehicles.getLast().getFrontPosition() < end) {
            Vehicle veh = vehicles.removeLast();
            sumSpeed += veh.getSpeed();
            count++;
        }
        double avgSpeed = (count == 0) ? maxRoadSpeed : sumSpeed / count;
        return (end - begin) / avgSpeed;
    }

    /**
     * Returns the number of obstacles on this road segment.
     * 
     * @return the number of obstacles on this road segment
     */
    protected int obstacleCount() {
        int obstacleCount = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            obstacleCount += laneSegment.obstacleCount();
        }
        return obstacleCount;
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
        return laneSegments[lane - 1].getVehicle(index);
    }

    /**
     * Removes the front vehicle on the given lane.
     * 
     * @param lane
     */
    public void removeFrontVehicleOnLane(int lane) {
        laneSegments[lane - 1].removeFrontVehicleOnLane();
    }

    /**
     * Removes any vehicles that have moved past the end of this road segment.
     * 
     * @return the number of vehicles removed
     */
    public int removeVehiclesPastEnd() {
        int removedVehicleCount = 0;
        for (final LaneSegment laneSegment : laneSegments) {
            removedVehicleCount += laneSegment.removeVehiclesPastEnd(sink);
        }
        return removedVehicleCount;
    }

    /**
     * Returns all vehicles that have moved past the end of this road segment.
     * 
     * @return the number of vehicles removed
     */
    public Iterable<Vehicle> getVehiclesPastEnd() {
        ArrayList<Vehicle> vehiclesPastEnd = new ArrayList<>();
        for (final LaneSegment laneSegment : laneSegments) {
            vehiclesPastEnd.addAll(laneSegment.getVehiclesPastEnd(sink));
        }
        return vehiclesPastEnd;
    }

    /**
     * Adds an obstacle to this road segment.
     * 
     * @param obstacle
     */
    public void addObstacle(Vehicle obstacle) {
        assert obstacle.type() == Vehicle.Type.OBSTACLE;
        obstacle.setRoadSegment(this);
        addVehicle(obstacle);
    }

    /**
     * Adds a vehicle to this road segment.
     * 
     * @param vehicle
     */
    public void addVehicle(Vehicle vehicle) {
        vehicle.setRoadSegment(this);
        laneSegments[vehicle.lane() - 1].addVehicle(vehicle);
    }

    /**
     * Adds a vehicle to the start of this road segment.
     * 
     * @param vehicle
     */
    public void appendVehicle(Vehicle vehicle) {
        vehicle.setRoadSegment(this);
        laneSegments[vehicle.lane() - 1].appendVehicle(vehicle);
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
        for (RoadObject roadObject : roadObjects) {
            roadObject.timeStep(dt, simulationTime, iterationCount);
        }
    }

    // NOT ELEGANT: must be called twice because vehicles are shifted between roadSegments so that they have to be registered twice!
    // new concept needed here, perhaps temporary vehicle objects, could also be used for LaneChanges
    // Good test: check for identical numbers of vehicles passing x=xRoadLength and x=0 of successor RoadSegment.
    private boolean updateSignalPointsBeforeOutflowCalled;

    protected void updateSignalPointsBeforeOutflow(double simulationTime) {
        updateSignalPointsBeforeOutflowCalled = true;
        for (SignalPoint signalPoint : signalPoints) {
            signalPoint.clear();
            signalPoint.registerPassingVehicles(simulationTime, iterator());
        }
    }

    public void updateSignalPointsAfterOutflowAndInflow(double simulationTime) {
        assert updateSignalPointsBeforeOutflowCalled; // hack for assuring right calling process
        for (SignalPoint signalPoint : signalPoints) {
            // TODO vehicles on overtaking segment ignored here, iterate over those as well...test with iteratorAllVehicles()
            signalPoint.registerPassingVehicles(simulationTime, iterator());
        }
        updateSignalPointsBeforeOutflowCalled = false;
    }

    // TOO SLOW FOR GENERAL PURPOSE
    public Iterator<Vehicle> vehiclesWithinRange(double begin, double end) {
        return Iterators.filter(iterator(), new VehicleWithinRange(begin, end));
    }

    public Iterator<Vehicle> filteredVehicles(Predicate<Vehicle> predicate) {
        return Iterators.filter(iterator(), predicate);
    }

    /**
     * Lanes change.
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

        if (!hasPeer() && laneCount < 2) {
            // need at least 2 lanes or a peerRoad for lane changing
            return;
        }

        if (!overtakingSegmentInitialized) {
            initOvertakingLane(); // lazy init.
        }

        // TODO assure priority for lane changes from slow to fast lanes
        for (final LaneSegment laneSegment : laneSegments) {
            assert laneSegment.assertInvariant();
            for (Iterator<Vehicle> vehIterator = laneSegment.iterator(); vehIterator.hasNext();) {
                Vehicle vehicle = vehIterator.next();
                assert vehicle.roadSegmentId() == id;
                if (vehicle.inProcessOfLaneChange()) {
                    // !!! assure update in each simulation timestep
                    vehicle.updateLaneChangeDelay(dt);
                } else if (vehicle.considerLaneChange(dt, this)) {
                    final int targetLane = vehicle.getTargetLane();
                    assert targetLane != Lanes.NONE;
                    assert laneSegment(targetLane).type() != Lanes.Type.ENTRANCE;
                    vehIterator.remove();
                    vehicle.setLane(targetLane);
                    laneSegment(targetLane).addVehicle(vehicle);
                } else if (vehicle.considerOvertakingViaPeer(dt, this)) {
                    LOG.debug("### perform overtaking: vehicle={}", vehicle);
                    int targetLane = vehicle.getTargetLane();
                    assert targetLane == Lanes.OVERTAKING;
                    vehIterator.remove();
                    vehicle.setLane(targetLane);
                    overtakingSegment.addVehicle(vehicle);
                }
            }
        }
        checkFinishingOvertaking(dt);
    }

    public void makeDynamicRoutingDecisions() {
        for (LaneSegment laneSegment : laneSegments) {
            for (Vehicle vehicle : laneSegment) {
                vehicle.routingDecisions().considerRouteAlternatives(this);
            }
        }
    }

    private void initOvertakingLane() {
        // connect overtaking lane according to connections of most inner lane
        LaneSegment sinkLane1 = laneSegment(Lanes.MOST_INNER_LANE).sinkLaneSegment();
        if (sinkLane1 != null) {
            overtakingSegment.setSinkLaneSegment(sinkLane1.roadSegment().overtakingSegment);
        }
        LaneSegment sourceLane1 = laneSegment(Lanes.MOST_INNER_LANE).sourceLaneSegment();
        if (sourceLane1 != null) {
            overtakingSegment.setSourceLaneSegment(sourceLane1.roadSegment().overtakingSegment);
        }
        overtakingSegmentInitialized = true;
    }

    private void checkFinishingOvertaking(double dt) {
        for (Iterator<Vehicle> vehIterator = overtakingSegment.iterator(); vehIterator.hasNext();) {
            Vehicle vehicle = vehIterator.next();
            if (vehicle.inProcessOfLaneChange()) {
                // assure update in each simulation timestep
                vehicle.updateLaneChangeDelay(dt);
            } else if (vehicle.considerFinishOvertaking(dt, laneSegment(Lanes.MOST_INNER_LANE))) {
                LOG.debug("vehicle turns back into lane after overtaking: vehicle={}", vehicle);
                int targetLane = vehicle.getTargetLane();
                assert targetLane == Lanes.MOST_INNER_LANE;
                vehIterator.remove();
                vehicle.setLane(targetLane);
                laneSegment(Lanes.MOST_INNER_LANE).addVehicle(vehicle);
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
            final LaneSegment leftLaneSegment = getLeftLane(laneSegment);
            for (final Vehicle vehicle : laneSegment) {
                vehicle.updateAcceleration(dt, this, laneSegment, leftLaneSegment);
            }
        }
        for (final Vehicle vehicle : overtakingSegment) {
            vehicle.updateAcceleration(dt, this, overtakingSegment, null);
        }
    }

    private LaneSegment getLeftLane(LaneSegment laneSegment) {
        if (laneSegment.lane() + Lanes.TO_LEFT >= Lanes.MOST_INNER_LANE) {
            return laneSegments[laneSegment.lane() + Lanes.TO_LEFT];
        }
        return null;
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
        for (final Vehicle vehicle : overtakingSegment) {
            vehicle.updatePositionAndSpeed(dt);
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
        updateSignalPointsBeforeOutflow(simulationTime);
        for (final LaneSegment laneSegment : laneSegments) {
            laneSegment.outFlow(dt, simulationTime, iterationCount);
            assert laneSegment.assertInvariant();
        }
        overtakingSegment.outFlow(dt, simulationTime, iterationCount);
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
        if (simpleRamp != null) {
            simpleRamp.timeStep(dt, simulationTime, iterationCount);
        }
    }

    /**
     * Returns the rear vehicle on the given lane.
     * 
     * @param lane
     * @return the rear vehicle on the given lane
     */
    public Vehicle rearVehicleOnLane(int lane) {
        return laneSegments[lane - 1].rearVehicle();
    }

    /**
     * Finds the vehicle in the given lane immediately at or behind the given position.
     * 
     * @param lane
     *            lane in which to search
     * @return reference to the rear vehicle
     */
    public Vehicle rearVehicle(int lane, double vehiclePos) {
        return laneSegments[lane - 1].rearVehicle(vehiclePos);
    }

    // public Vehicle rearVehicleOnSinkLanePosAdjusted(int lane) {
    // return laneSegments[lane - 1].rearVehicleOnSinkLanePosAdjusted();
    // }

    // Vehicle secondLastVehicleOnSinkLanePosAdjusted(int lane) {
    // return laneSegments[lane - 1].secondLastVehicleOnSinkLanePosAdjusted();
    // }

    /**
     * Returns the front vehicle on the given lane.
     * 
     * @param lane
     * @return the front vehicle on the given lane
     */
    public Vehicle frontVehicleOnLane(int lane) {
        return laneSegments[lane - 1].frontVehicle();
    }

    /**
     * Returns the vehicle in front of the given vehicle in its lane.
     * 
     * @param vehicle
     * @return the next downstream vehicle in the lane
     */
    public Vehicle frontVehicleOnLane(Vehicle vehicle) {
        return laneSegments[vehicle.lane() - 1].frontVehicle(vehicle);
    }

    /**
     * Finds the vehicle in the given lane immediately in front of the given position. That is a vehicle such that
     * vehicle.position() > vehicePos (strictly greater than). The vehicle whose position equals vehiclePos is deemed to
     * be in the rear.
     * 
     * @param lane
     *            lane in which to search
     * @return reference to the front vehicle
     */
    public Vehicle frontVehicle(int lane, double vehiclePos) {
        return laneSegments[lane - 1].frontVehicle(vehiclePos);
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
        int laneIndex;
        int index;
        int count;

        public VehicleIterator() {
        }

        @Override
        public boolean hasNext() {
            if (index < laneSegments[laneIndex].vehicleCount()) {
                return true;
            }
            int nextLane = laneIndex + 1;
            while (nextLane < laneCount) {
                if (laneSegments[nextLane].vehicleCount() > 0) {
                    return true;
                }
                ++nextLane;
            }
            final int vc = getVehicleCount();
            if (vc != count) {
                assert false;
            }
            return false;
        }

        @Override
        public Vehicle next() {
            if (index < laneSegments[laneIndex].vehicleCount()) {
                // get the next vehicle in the current lane
                ++count;
                return laneSegments[laneIndex].getVehicle(index++);
            }
            int nextLane = laneIndex + 1;
            while (nextLane < laneCount) {
                if (laneSegments[nextLane].vehicleCount() > 0) {
                    laneIndex = nextLane;
                    index = 0;
                    ++count;
                    return laneSegments[laneIndex].getVehicle(index++);
                }
                ++nextLane;
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not implemented.");
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

    public final Iterator<Vehicle> overtakingVehicles() {
        return overtakingSegment.iterator();
    }

    // not yet used
    final Iterator<Vehicle> iteratorAllVehicles() {
        return Iterators.concat(iterator(), overtakingVehicles());
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
            int index = -1;
            for (Vehicle vehicle : laneSegment) {
                index++;
                if (vehicle.type() == Vehicle.Type.OBSTACLE) {
                    continue;
                }
                final Vehicle vehFront = laneSegment.frontVehicle(vehicle);
                final double netDistance = vehicle.getNetDistance(vehFront);
                if (netDistance < 0) {
                    LOG.error("Crash happened!!!");
                    final StringBuilder sb = new StringBuilder("\n");
                    sb.append(String.format("Crash of Vehicle i=%d (vehId=%d) at x=%.4f ", index, vehicle.getId(),
                            vehicle.getFrontPosition()));
                    if (vehFront != null) {
                        sb.append(String.format("with veh (vehId=%d) in front at x=%.4f on lane=%d\n",
                                vehFront.getId(), vehFront.getFrontPosition(), vehicle.lane()));
                    }
                    sb.append("internal nodeId=").append(id);
                    sb.append(", roadId=").append(userId);
                    sb.append(", net distance=").append(netDistance);
                    sb.append(", lane=").append(laneSegment.lane());
                    sb.append(", container.size=").append(laneSegment.vehicleCount());
                    sb.append(", obstacles=").append(laneSegment.obstacleCount());
                    sb.append("\n");

                    for (int j = Math.max(0, index - 8), M = laneSegment.vehicleCount(); j <= Math
                            .min(index + 8, M - 1); j++) {
                        final Vehicle veh = laneSegment.getVehicle(j);
                        sb.append(String
                                .format("veh=%d, pos=%6.2f, speed=%4.2f, accModel=%4.3f, acc=%4.3f, length=%3.1f, lane=%d, nodeId=%d%n",
                                        j, veh.getFrontPosition(), veh.getSpeed(), veh.accModel(), veh.getAcc(),
                                        veh.getLength(), veh.lane(), veh.getId()));
                    }
                    LOG.error(sb.toString());
                    if (isWithCrashExit) {
                        LOG.error(" !!! exit after crash !!! ");
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
            throw new UnsupportedOperationException("remove() not implemented.");
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

    /**
     * Returns an iterable over all the lane segments in this road segment.
     * 
     * @return an iterable over all the lane segments in this road segment
     */
    public Iterable<LaneSegment> laneSegments() {
        return ImmutableList.copyOf(laneSegmentIterator());
    }

    // convenience methods
    public Iterable<TrafficLight> trafficLights() {
        return roadObjects.values(RoadObjectType.TRAFFICLIGHT);
    }

    public Iterable<SpeedLimit> speedLimits() {
        return roadObjects.values(RoadObjectType.SPEEDLIMIT);
    }

    public Iterable<VariableMessageSignDiversion> variableMessageSignDiversions() {
        return roadObjects.values(RoadObjectType.VMS_DIVERSION);
    }

    public Iterable<GradientProfile> gradientProfiles() {
        return roadObjects.values(RoadObjectType.GRADIENT_PROFILE);
    }

    /**
     * Asserts the road segment's class invariant. Used for debugging.
     */
    public boolean assertInvariant() {
        final RoadMapping roadMapping = roadMapping();
        if (roadMapping != null) {
            // assert roadMapping.laneCount() == laneCount();
            // assert roadMapping.trafficLaneMax() == trafficLaneMax();
            // assert roadMapping.trafficLaneMin() == trafficLaneMin();
            assert Math.abs(roadMapping.roadLength() - roadLength()) < 0.1;
        }
        for (final LaneSegment laneSegment : laneSegments) {
            laneSegment.assertInvariant();
        }
        return true;
    }

    public void setSimpleRamp(SimpleRamp simpleRamp) {
        this.simpleRamp = simpleRamp;
    }

    // not yet used
    public void setUserRoadname(String name) {
        this.roadName = name;
    }

    public RoadObjects roadObjects() {
        return roadObjects;
    }

    public SignalPoints signalPoints() {
        return signalPoints;
    }

    public Node getOriginNode() {
        return origin;
    }

    public Node getDestinationNode() {
        return destination;
    }

    @Override
    public String toString() {
        return "RoadSegment [nodeId=" + id + ", userId=" + userId + ", roadName=" + roadName + ", roadLength="
                + roadLength + ", laneCount=" + laneCount + ", " + getOriginNode() + ", " + getDestinationNode() + "]";
    }

    public RoadSegment getPeerRoadSegment() {
        return peerRoadSegment;
    }

    public final boolean hasPeer() {
        return peerRoadSegment != null;
    }

    public void setPeerRoadSegment(RoadSegment peerRoadSegment) {
        Preconditions.checkNotNull(peerRoadSegment);
        Preconditions.checkArgument(!peerRoadSegment.equals(this));
        this.peerRoadSegment = peerRoadSegment;
    }

    public double getFreeFlowSpeed() {
        return freeFlowSpeed;
    }

    public void setFreeFlowSpeed(double freeFlowSpeed) {
        this.freeFlowSpeed = freeFlowSpeed;
    }

}
