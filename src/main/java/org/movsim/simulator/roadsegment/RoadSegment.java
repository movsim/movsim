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

package org.movsim.simulator.roadsegment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.movsim.simulator.RoadMapping;
import org.movsim.simulator.roadSection.FlowConservingBottlenecks;
import org.movsim.simulator.roadSection.UpstreamBoundary;
import org.movsim.simulator.roadSection.impl.AbstractRoadSection;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    final static Logger logger = LoggerFactory.getLogger(RoadSegment.class);
    
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
    private final int laneCount;
    private final LaneSegment laneSegments[];
    private List<Vehicle> stagedVehicles;
    private FlowConservingBottlenecks flowConsBottlenecks;

    
    // Sources and Sinks
    private UpstreamBoundary upstreamBoundary;
    private TrafficSource source;
    // sink is of type TrafficFlowBase to allow the sink to be a TrafficFlowOnRamp
    private TrafficFlowBase sink;
    private int removedVehicleCount; // used for calculating traffic flow
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
        stagedVehicles = new LinkedList<Vehicle>();
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
     * Sets the upstream boundary (traffic source) for this road segment.
     * 
     * @param source
     *            the traffic source
     */
    public final void setUpstreamBoundary(UpstreamBoundary upstreamBoundary) {
        assert upstreamBoundary != null;
        this.upstreamBoundary = upstreamBoundary;
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
//        final RoadSegment sourceRoadSegment = sourceRoadSegment(trafficLaneMax() - 1);
//        cumulativeRoadLength = sourceRoadSegment == null ? 0.0 : sourceRoadSegment.cumulativeRoadLength() + sourceRoadSegment.roadLength();
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
    // TODO ake this is a property of/task for the downstream boundary condition --> remove here 
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
    // TODO ake adding an obstacle is also used in the OnrampMobilImpl --> setObstacleAtEndOfLane()  
    public void addObstacle(Vehicle obstacle) {
        assert obstacle.type() == Vehicle.Type.OBSTACLE;
        addVehicle(obstacle);
    }

    /**
     * Adds a vehicle to this road segment.
     * 
     * @param vehicle
     */
    
    // TODO ake movsim has the sophisticated vehicleGenerator for this
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
     * Lane changing.
     * <p>
     * For each vehicle check if a lane change is desired and safe and, if so, make the lane change.
     * </p>
     * <p>
     * <code>laneChanging</code> preserves the vehicle sort order, since only lateral movements
     * of vehicles are made.
     * </p>
     *
     * @param dt
     *            simulation time interval
     * @param simulationTime
     * @param iterationCount 
     */
    public void laneChanging(double dt, double simulationTime, long iterationCount) {
    	for (final LaneSegment laneSegment : laneSegments) {
            stagedVehicles.clear();
            for (final Vehicle vehicle : laneSegment) {
                if (vehicle.considerLaneChanging(dt, this)) {
                    stagedVehicles.add(vehicle);
                }
            }

            // assign staged vehicles to new lanes
            // necessary update of new situation *after* lane-changing decisions
            for (final Vehicle vehicle : stagedVehicles) {
            	laneSegments[vehicle.getLane()].removeVehicle(vehicle);
            	laneSegments[vehicle.getTargetLane()].addVehicle(vehicle);
            }
        }
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
    // TODO ake method in AbstractRoadSection is leaner and some functionality has been moved to the vehicle 
    public void updateVehiclePositionsAndVelocities(double dt, double simulationTime, long iterationCount) {
    	for (final LaneSegment laneSegment : laneSegments) {
    		laneSegment.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
    	}
    }
    /**
     * Accelerate.
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     * @param iterationCount 
     */
    public void accelerate(double dt, double simulationTime, long iterationCount) {
    	for (final LaneSegment laneSegment : laneSegments) {
            //final int leftLaneIndex = laneSegment.getLaneIndex()+MovsimConstants.TO_LEFT;
            final LaneSegment leftLaneSegment = null; // TODO get left lane ( leftLaneIndex < vehContainers.size() ) ? vehContainers.get(leftLaneIndex) : null;
            //for (int i = 0, N = vehiclesOnLane.size(); i < N; i++) {
            for(final Vehicle vehicle : laneSegment){
                //final Vehicle veh = vehiclesOnLane.get(i);
                final double x = vehicle.getPosition();
                // TODO treat null case 
                final double alphaT = (flowConsBottlenecks==null) ? 1 : flowConsBottlenecks.alphaT(x);
                final double alphaV0 = (flowConsBottlenecks==null) ? 1 : flowConsBottlenecks.alphaV0(x);
                // logger.debug("i={}, x_pos={}", i, x);
                // logger.debug("alphaT={}, alphaV0={}", alphaT, alphaV0);
                vehicle.calcAcceleration(dt, laneSegment, leftLaneSegment, alphaT, alphaV0);
            }
        }
    }

    /**
     * Update position and speed.
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     * @param iterationCount 
     */
    public void updatePositionAndSpeed(double dt, double simulationTime, long iterationCount) {
    	for (final LaneSegment laneSegment : laneSegments) {
            for (final Vehicle vehicle : laneSegment) {
                vehicle.updatePostionAndSpeed(dt);
            }
        }
    }
    

  /**
     * If there is a traffic sink, use it to perform any traffic outflow.
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     */
    // TODO ake properties of the downstream boundary condition should be encapsulated in own class ...
    public void outFlow(double dt, double simulationTime, long iterationCount) {
    	for (final LaneSegment laneSegment : laneSegments) {
    		laneSegment.outFlow(dt, simulationTime, iterationCount);
    	}
        if (sink != null) {
            sink.timeStep(dt, simulationTime, iterationCount);
        }
    }

    /**
     * If there is a traffic source, use it to perform any traffic inflow.
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     * @param iterationCount 
     */
    public void inFlow(double dt, double simulationTime, long iterationCount) {
        assert eachLaneIsSorted();
        if (upstreamBoundary != null) {
            upstreamBoundary.timeStep(dt, simulationTime, iterationCount);
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
    public Vehicle frontVehicle(int lane, double vehiclePos) {

    	return laneSegments[lane].frontVehicle(vehiclePos);
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

    private class VehicleIterator implements Iterator<Vehicle> {
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
     * @param iterationCount the iteration count
     * @param time the time
     * @param isWithCrashExit the is with crash exit
     */
    public void checkForInconsistencies(double time, long iterationCount, boolean isWithCrashExit) {
        for (final LaneSegment laneSegment : laneSegments) {
            // for(final Vehicle vehicle : laneSegment){
            for (int index = 0, N = laneSegment.vehicleCount(); index < N; index++) {
                final Vehicle vehicle = laneSegment.getVehicle(index);
                final Vehicle vehFront = laneSegment.frontVehicle(vehicle);
                final double netDistance = vehicle.getNetDistance(vehFront);
                if (netDistance < 0) {
                    logger.error("#########################################################");
                    logger.error("Crash of Vehicle i = {} at x = {}m", index, vehicle.getPosition());
                    if (vehFront != null) {
                        logger.error("with veh in front at x = {} on lane = {}", vehFront.getPosition(), vehicle.getLane());
                    }
                    logger.error("roadID = {}", id);
                    logger.error("net distance  = {}", netDistance);
                    logger.error("lane index    = {}", laneSegment.lane());
                    logger.error("container.size = {}", laneSegment.vehicleCount());
                    final StringBuilder sb = new StringBuilder("\n");
                    for (int j = Math.max(0, index - 8), M = laneSegment.vehicleCount(); j <= Math.min(index + 8, M - 1); j++) {
                        final Vehicle veh = laneSegment.getVehicle(j);
                        sb.append(String.format(
                                "veh=%d, pos=%6.2f, speed=%4.2f, accModel=%4.3f, length=%3.1f, lane=%d, id=%d%n", j,
                                veh.getPosition(), veh.getSpeed(), veh.accModel(), veh.getLength(), veh.getLane(), veh.getId()));
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
        
        
        
    
}
