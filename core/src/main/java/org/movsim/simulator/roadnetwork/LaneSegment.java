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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A LaneSegment represents a lane within a RoadSegment.
 * </p>
 * <p>
 * Lanes are of different types including traffic lanes, exit (deceleration) lanes and entrance (acceleration) lanes. Typically vehicle
 * behavior (and especially lane change behavior) is different in each type of lane.
 * </p>
 * <p>
 * The vehicles in a lane segment are stored in a sorted ArrayList. This ArrayList is kept sorted so that the vehicles in front of and
 * behind a given vehicle can be found efficiently.
 * </p>
 * <p>
 * Vehicles are sorted in order of decreasing position:
 * </p>
 * <p>
 * V[n+1].pos < V[n].pos < V[n-1].pos ... < V[1].pos < V[0].pos
 * </p>
 */
public class LaneSegment implements Iterable<Vehicle> {

    final static Logger logger = LoggerFactory.getLogger(LaneSegment.class);

    private static final boolean DEBUG = false;
    private static final int VEHICLES_PER_LANE_INITIAL_SIZE = 50;
    // Lanes linkage
    private final RoadSegment roadSegment;
    private LaneSegment sinkLaneSegment;
    private LaneSegment sourceLaneSegment;

    // physical lane, not the laneIndex
    private final int lane;
    private Lanes.Type type;
    final ArrayList<Vehicle> vehicles;
    private int removedVehicleCount; // used for calculating traffic flow

    /**
     * Constructor.
     * 
     * @param roadSegment
     * @param lane
     *            (not the laneIndex)
     */
    LaneSegment(RoadSegment roadSegment, int lane) {
        this.roadSegment = roadSegment;
        assert lane >= Lanes.MOST_INNER_LANE;
        this.lane = lane;
        vehicles = new ArrayList<>(VEHICLES_PER_LANE_INITIAL_SIZE);
        type = Lanes.Type.TRAFFIC;
    }

    /**
     * Returns the lane.
     * <p>
     * The lane is an identifier of the lane in the physical network starting with a value of 1 for the most inner lane.
     * </p>
     * 
     * @return lane, not the index of the lane in the roadSegment
     */
    public final int lane() {
        return lane;
    }

    /**
     * Sets the type of the lane.
     * 
     * @param type
     */
    public final void setType(Lanes.Type type) {
        this.type = type;
        if (type == Lanes.Type.ENTRANCE) {
            setSinkLaneSegment(null);
        }
    }

    /**
     * Returns the type of the lane.
     * 
     * @return type of lane
     */
    public final Lanes.Type type() {
        return type;
    }

    /**
     * Returns the road segment for the lane.
     * 
     * @return road segment
     */
    public final RoadSegment roadSegment() {
        return roadSegment;
    }

    /**
     * Returns the length of the lane.
     * 
     * @return length of lane
     */
    public final double roadLength() {
        return roadSegment.roadLength();
    }

    public final void setSourceLaneSegment(LaneSegment sourceLaneSegment) {
        this.sourceLaneSegment = sourceLaneSegment;
    }

    public final LaneSegment sourceLaneSegment() {
        return sourceLaneSegment;
    }

    public final void setSinkLaneSegment(LaneSegment sinkLaneSegment) {
        this.sinkLaneSegment = sinkLaneSegment;
    }

    public final LaneSegment sinkLaneSegment() {
        return sinkLaneSegment;
    }

    /**
     * Clears this lane segment of any vehicles.
     */
    public final void clearVehicles() {
        vehicles.clear();
    }

    /**
     * Returns the number of vehicles on this lane segment.
     * 
     * @return the number of vehicles on this lane segment
     */
    public final int vehicleCount() {
        return vehicles.size();
    }

    public int stoppedVehicleCount() {
        int stoppedVehicleCount = 0;
        for (final Vehicle vehicle : vehicles) {
            if (vehicle.type() != Vehicle.Type.OBSTACLE && vehicle.getSpeed() <= 0.01) {
                ++stoppedVehicleCount;
            }
        }
        return stoppedVehicleCount;
    }

    /** Returns the number of real vehicles (without 'obstacles') n this lane segment. */
    // TODO think about iterating only over vehicles but not obstacles which are used only internally
    public final int vehicleCountWithoutObstacles() {
        return vehicles.size() - obstacleCount();
    }

    /**
     * Returns the number of obstacles on this lane segment.
     * 
     * @return the number of obstacles on this lane segment
     */
    public final int obstacleCount() {
        int obstacleCount = 0;
        for (final Vehicle vehicle : vehicles) {
            if (vehicle.type() == Vehicle.Type.OBSTACLE) {
                ++obstacleCount;
            }
        }
        return obstacleCount;
    }

    /**
     * Returns the total travel time of all vehicles on this lane segment.
     * 
     * @return the total vehicle travel time
     */
    public double totalVehicleTravelTime() {
        double totalVehicleTravelTime = 0;
        for (final Vehicle vehicle : vehicles) {
            totalVehicleTravelTime += vehicle.totalTravelTime();
        }
        return totalVehicleTravelTime;
    }

    /**
     * Returns the total travel distance of all vehicles on this lane segment.
     * 
     * @return the total vehicle travel distance
     */
    public double totalVehicleTravelDistance() {
        double totalVehicleTravelDistance = 0;
        for (final Vehicle vehicle : vehicles) {
            totalVehicleTravelDistance += vehicle.totalTravelDistance();
        }
        return totalVehicleTravelDistance;
    }

    /**
     * Returns the total fuel used by all vehicles on this lane segment.
     * 
     * @return the total vehicle fuel used
     */
    public double totalVehicleFuelUsedLiters() {
        double totalVehicleFuelUsedLiters = 0;
        for (final Vehicle vehicle : vehicles) {
            totalVehicleFuelUsedLiters += vehicle.totalFuelUsedLiters();
        }
        return totalVehicleFuelUsedLiters;
    }
    
    public double instantaneousFuelUsedLitersPerS() {
        double instFuelUsedLiters = 0;
        for (final Vehicle vehicle : vehicles) {
            instFuelUsedLiters += vehicle.getActualFuelFlowLiterPerS();
        }
        return instFuelUsedLiters;
    }

    /**
     * <p>
     * Returns the vehicle at the given index.
     * </p>
     * 
     * 
     * @param index
     * 
     * @return vehicle at given index
     */
    public Vehicle getVehicle(int index) {
        return vehicles.get(index);
    }

    /**
     * Removes the vehicle at the given index.
     * 
     * @param index
     *            index of vehicle to remove
     */
    public void removeVehicle(int index) {
        vehicles.remove(index);
    }

    /**
     * Removes the given vehicle.
     * 
     * @param vehicleToRemove
     */
    public void removeVehicle(Vehicle vehicleToRemove) {
        // TODO improve primitive implementation
        final long vehicleId = vehicleToRemove.getId();
        final int count = vehicles.size();
        for (int i = 0; i < count; ++i) {
            final Vehicle vehicle = vehicles.get(i);
            if (vehicle.getId() == vehicleId) {
                vehicles.remove(i);
                return;
            }
        }
    }

    /**
     * Removes the front vehicle on this lane segment.
     */
    public void removeFrontVehicleOnLane() {
        if (vehicles.size() > 0) {
            vehicles.remove(0);
        }
    }

    /**
     * Removes any vehicles that have moved past the end of this road segment.
     * 
     * @return the number of vehicles removed
     */
    public int removeVehiclesPastEnd(TrafficSink sink) {
        int count = 0;
        final double roadLength = roadSegment.roadLength();
        int vehicleCount = vehicles.size();
        // remove any vehicles that have gone past the end of this road segment
        while (vehicleCount > 0 && vehicles.get(0).getRearPosition() > roadLength) {
            sink.recordRemovedVehicle(vehicles.get(0));
            vehicles.remove(0);
            ++removedVehicleCount;
            --vehicleCount;
            ++count;
        }
        return count;
    }

    public Collection<? extends Vehicle> getVehiclesPastEnd(TrafficSink sink) {
        ArrayList<Vehicle> vehiclesPastEnd = new ArrayList<>();
        int index = 0;
        while (index < vehicles.size() && vehicles.get(index).getRearPosition() > roadSegment.roadLength()) {
            vehiclesPastEnd.add(vehicles.get(index));
            index++;
        }
        return vehiclesPastEnd;
    }

    /**
     * @return the removedVehicleCount
     */
    public int getRemovedVehicleCount() {
        return removedVehicleCount;
    }

    /**
     * Adds a vehicle to this lane segment.
     * 
     * @param vehicle
     */
    public void addVehicle(Vehicle vehicle) {
        // TODO assert vehicle.getFrontPosition() >= 0.0;
        assert vehicle.getSpeed() >= 0.0 : "vehicleSpeed=" + vehicle.getSpeed();
        assert vehicle.lane() == lane;
        assert vehicle.roadSegmentId() == roadSegment.id();
        assert assertInvariant();
        final int index = positionBinarySearch(vehicle.getRearPosition());
        if (index < 0) {
            vehicles.add(-index - 1, vehicle);
        } else if (index == 0) {
            vehicles.add(0, vehicle);
        } else {
            // vehicle is in the same position as an existing vehicle - this should not happen
            assert false;
        }
        assert laneIsSorted();
        assert assertInvariant();
    }

    public int addVehicleTemp(Vehicle vehicle) {
        // assert vehicle.getFrontPosition() >= 0.0;
        assert vehicle.getSpeed() >= 0.0;
        assert vehicle.lane() == lane;
        assert assertInvariant();
        final int index = positionBinarySearch(vehicle.getRearPosition());
        int pos = 0;
        if (index < 0) {
            pos = -index - 1;
            vehicles.add(pos, vehicle);
        } else if (index == 0) {
            vehicles.add(pos, vehicle);
        } else {
            // vehicle is in the same position as an existing vehicle - this should not happen
            assert false;
        }
        assert laneIsSorted();
        assert assertInvariant();
        return pos;
    }

    // TODO testwise add vehicle
//    public void addVehicleTestwise(Vehicle vehicle) {
//        if (vehicle != null) {
//            // assert vehicle.getPosition() >= 0.0;
//            assert vehicle.getSpeed() >= 0.0;
//            final int index = positionBinarySearch(vehicle.getRearPosition());
//            if (index < 0) {
//                vehicles.add(-index - 1, vehicle);
//            } else if (index == 0) {
//                vehicles.add(0, vehicle);
//            } else {
//                // vehicle is in the same position as an existing vehicle - this should not happen
//                assert false;
//            }
//        }
//    }

    public void appendVehicle(Vehicle vehicle) {
        assert vehicle.getFrontPosition() >= 0.0;
        assert vehicle.getSpeed() >= 0.0;
        assert vehicle.lane() == lane;
        assert vehicle.roadSegmentId() == roadSegment.id();
        assert laneIsSorted();
        assert assertInvariant();
        if (DEBUG) {
            if (vehicles.size() > 0) {
                final Vehicle lastVehicle = vehicles.get(vehicles.size() - 1);
                if (lastVehicle.getRearPosition() < vehicle.getRearPosition()) {
                    assert false;
                }
            }
        }
        vehicles.add(vehicle);
        assert laneIsSorted();
        assert assertInvariant();
    }

    /**
     * Returns the rear vehicle.
     * 
     * @return the rear vehicle
     */
    public Vehicle rearVehicle() {
        final int count = vehicles.size();
        if (count > 0) {
            return vehicles.get(count - 1);
        }
        return null;
    }

    /**
     * Finds the vehicle immediately at or behind the given position.
     * 
     * @param vehiclePos
     * 
     * @return reference to the rear vehicle
     */
    public Vehicle rearVehicle(double vehiclePos) {

        final int index = positionBinarySearch(vehiclePos);
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
        if (sourceLaneSegment != null) {
            // didn't find a rear vehicle in the current road segment, so
            // check the previous (source) road segment
            final Vehicle sourceFrontVehicle = sourceLaneSegment.frontVehicle();
            if (sourceFrontVehicle != null) {
                // return a copy of the front vehicle on the source road segment, with its
                // position set relative to the current road segment
                final Vehicle rearVehicle = new Vehicle(sourceFrontVehicle);
                rearVehicle.setFrontPosition(rearVehicle.getFrontPosition() - sourceLaneSegment.roadLength());
                return rearVehicle;
            }
        }
        return null;
    }

    public final Vehicle rearVehicle(Vehicle vehicle) {
        return rearVehicle(vehicle.getRearPosition());
    }

    public Vehicle rearVehicleOnSinkLanePosAdjusted() {

        // subject vehicle is front vehicle on this road segment, so check sink road segment
        if (sinkLaneSegment == null) {
            return null;
        }
        // find the rear vehicle in the sink lane on the sink lane segment
        final Vehicle sinkRearVehicle = sinkLaneSegment.rearVehicle();
        if (sinkRearVehicle == null) {
            return null;
        }
        // return a copy of the rear vehicle on the sink road segment, with its position
        // set relative to the current road segment
        final Vehicle ret = new Vehicle(sinkRearVehicle);
        ret.setFrontPosition(ret.getFrontPosition() + roadSegment.roadLength());
        return ret;
    }

    Vehicle secondLastVehicleOnSinkLanePosAdjusted() {
        // subject vehicle is front vehicle on this lane segment, so check sink lane segment
        if (sinkLaneSegment == null) {
            return null;
        }
        // find the rear vehicle in the sink lane segment
        final int sinkLaneVehicleCount = sinkLaneSegment.vehicleCount();
        if (sinkLaneVehicleCount < 2) {
            // should actually check sinkLane of sinkLane, but as long as sinkLane not
            // outrageously short, the assumption that there is no vehicle is reasonable
            return null;
        }
        final Vehicle vehicle = sinkLaneSegment.getVehicle(sinkLaneVehicleCount - 2);
        // return a copy of the rear vehicle on the sink lane segment, with its position
        // set relative to the current road segment
        final Vehicle ret = new Vehicle(vehicle);
        ret.setFrontPosition(ret.getFrontPosition() + roadSegment.roadLength());
        return ret;
    }

    /**
     * Returns the front vehicle which is the most downstream vehicle in the {@link LaneSegment}.
     * 
     * @return the front vehicle
     */
    public Vehicle frontVehicle() {
        if (vehicles.size() > 0) {
            return vehicles.get(0);
        }
        return null;
    }

    /**
     * Finds the vehicle immediately in front of the given position. That is a vehicle such that vehicle.position() >
     * vehicePos (strictly greater than). The vehicle whose position equals vehiclePos is deemed to be in the rear.
     * 
     * @param vehiclePos
     * 
     * @return reference to the front vehicle
     */
    public Vehicle frontVehicle(double vehiclePos) {
        // index = Collections.binarySearch(vehicles, subjectVehicle, vehiclePositionComparator);
        final int index = positionBinarySearch(vehiclePos);
        final int insertionPoint = -index - 1;
        if (index > 0) {
            // exact match found
            return vehicles.get(index - 1);
        } else if (insertionPoint > 0) {
            return vehicles.get(insertionPoint - 1);
        }
        // index == 0 or insertionPoint == 0
        // subject vehicle is front vehicle on this road segment, so check for vehicles
        // on sink lane segment
        if (sinkLaneSegment != null) {
            // didn't find a front vehicle in the current road segment, so
            // check the next (sink) road segment
            // find the rear vehicle in the sink lane on the sink road segment
            final Vehicle sinkRearVehicle = sinkLaneSegment.rearVehicle();
            if (sinkRearVehicle != null) {
                // return a copy of the rear vehicle on the sink road segment, with its position
                // set relative to the current road segment
                final Vehicle frontVehicle = new Vehicle(sinkRearVehicle);
                frontVehicle.setFrontPosition(frontVehicle.getFrontPosition() + roadSegment.roadLength());
                return frontVehicle;
            }
        }
        return null;
    }

    /**
     * Returns the vehicle in front of the given vehicle.
     * 
     * @param vehicle
     * @return the next downstream vehicle
     */
    public final Vehicle frontVehicle(Vehicle vehicle) {
        return frontVehicle(vehicle.getRearPosition());
    }
    
    private int positionBinarySearch(double vehiclePos) {
        int low = 0;
        int high = vehicles.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >> 1;
            final double rearPos = vehicles.get(mid).getRearPosition();
            // final int compare = Double.compare(midPos, vehiclePos);
            // note vehicles are sorted in reverse order of position
            final int compare = Double.compare(vehiclePos, rearPos);
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

    // /**
    // * <p>
    // * Update the vehicle positions and velocities by calling vehicle.updatePositionAndVelocity for
    // * each vehicle.
    // * </p>
    // *
    // * <p>
    // * If there is a test car, then record its position, velocity etc.
    // * </p>
    // *
    // * <p>
    // * If there is a traffic inhomogeneity, then apply it to each vehicle.
    // * </p>
    // *
    // * @param dt
    // * simulation time interval
    // * @param simulationTime
    // * @param iterationCount
    // */
    // public void updateVehiclePositionsAndSpeeds(double dt, double simulationTime, long iterationCount) {
    // assert laneIsSorted();
    // // this function may change vehicle ordering in this or another road segment
    // // remember V[n+1].pos < V[n].pos < V[n-1].pos ... < V[1].pos < V[0].pos
    // // Vehicle iteration loop goes backwards, that is it starts with vehicles nearest
    // // the start of this road segment. This is so a vehicle's new speed and position is
    // // calculated before the vehicle in front of it has been moved.
    // Vehicle frontFrontVehicle = null;
    // Vehicle frontVehicle;
    // final int count = vehicles.size();
    // // TODO refactor this loop so frontVehicle is reused as vehicle and end two cases are
    // // unrolled
    // for (int i = count - 1; i >= 0; --i) {
    // final Vehicle vehicle = vehicles.get(i);
    // if (i > 0) {
    // frontVehicle = vehicles.get(i - 1);
    // } else {
    // if (sinkLaneSegment == null) {
    // // no sink lane for this lane, so there are no vehicles ahead of vehicle(0)
    // frontVehicle = null;
    // } else {
    // // the front vehicle is the rear vehicle on the sink lane
    // frontVehicle = rearVehicleOnSinkLanePosAdjusted();
    // if (frontVehicle == null) {
    // // no vehicle in the sink lane, so must recursively follow the lanes
    // // to the end of the road
    // frontVehicle = frontVehicle(vehicle.getPosition());
    // }
    // }
    // }
    // }
    // // occasionally updatePositionAndVelocity could cause the vehicles array
    // // to become unsorted if LongitudinalDriverModel.MAX_DECELERATION >
    // // LaneChangeModel.maxSafeBraking, since a new entry into a lane might cause
    // // excessive breaking
    // // sortVehicles();
    // assert laneIsSorted();
    // }

    /**
     * If there is a traffic sink, use it to perform any traffic outflow.
     * 
     * @param dt
     *            simulation time interval
     * @param simulationTime
     * @param iterationCount
     */
    public void outFlow(double dt, double simulationTime, long iterationCount) {
        assert laneIsSorted();
        assert assertInvariant();
        final double roadLength = roadSegment.roadLength();

        // remove any vehicles that have gone past the end of this lane segment
        if (sinkLaneSegment != null) {
            int count = vehicles.size();
            // remove any vehicles that have gone past the end of this road segment
            while (count > 0) {
                final Vehicle vehicle = vehicles.get(0);
                if (vehicle.getRearPosition() < roadLength) {
                    break;
                }
                // if the vehicle is past the end of this road segment then move it onto the
                // sink lane for its lane
                // TODO - check previous lane correct (used for drawing vehicle when changing lanes)
                // final int prevLaneOnNewRoadSegment = lane;
                // final int prevLaneOnNewRoadSegment = sinkLane[vehicle.previousLane()];
                final double rearPositionOnNewRoadSegment = vehicle.getRearPosition() - roadLength;
                double exitEndPos = Vehicle.EXIT_POSITION_NOT_SET;
                if (sinkLaneSegment.type() == Lanes.Type.TRAFFIC) {
                    final int exitRoadSegmentId = vehicle.exitRoadSegmentId();
                    if (exitRoadSegmentId == sinkLaneSegment.roadSegment.id()) {
                        // vehicle is on exit exit road segment, so exit end pos is end of this
                        // road segment
                        exitEndPos = sinkLaneSegment.roadLength();
                    } else {
                        // check if next segment is exit segment
                        final RoadSegment sinkSinkRoad = sinkLaneSegment.roadSegment();
                        if (sinkSinkRoad != null && sinkSinkRoad.id() == exitRoadSegmentId) {
                            // next road segment is exit road segment
                            exitEndPos = sinkLaneSegment.roadLength() + sinkSinkRoad.roadLength();
                        }
                    }
                }
                final int laneOnNewRoadSegment = sinkLaneSegment.lane();
                vehicle.moveToNewRoadSegment(sinkLaneSegment.roadSegment(), laneOnNewRoadSegment,
                        rearPositionOnNewRoadSegment, exitEndPos);
                // remove vehicle from this road segment
                vehicles.remove(0);
                --count;
                ++removedVehicleCount;
                // put the vehicle onto the new road segment (note that even when a road segment
                // is joined to itself (eg for a traffic circle) the vehicle needs to be added
                // and removed - this ensures vehicles remain sorted)
                sinkLaneSegment.appendVehicle(vehicle);
            }
        }
        assert assertInvariant();
    }

    /**
     * Returns true if the vehicle array is sorted.
     * 
     * @return true if the vehicle array is sorted
     */
    public boolean laneIsSorted() {
        final int count = vehicles.size();
        if (count > 1) { // if zero or one vehicles in lane then it is necessarily sorted
            Vehicle frontVehicle = vehicles.get(0);
            for (int i = 1; i < count; ++i) {
                final Vehicle vehicle = vehicles.get(i);
                if (frontVehicle.getRearPosition() < vehicle.getRearPosition()) {
                    return false;
                }
                // current vehicle is front vehicle next time around
                frontVehicle = vehicle;
            }
        }
        return true;
    }

    /**
     * Simple bubble sort of the vehicles. Useful for debugging.
     */
    void sortVehicles() {
        // Collections.sort(vehicles, vehiclePositionComparator);
        final int count = vehicles.size();
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = 1; i < count; ++i) {
                final Vehicle front = vehicles.get(i - 1);
                final Vehicle rear = vehicles.get(i);
                if (rear.getRearPosition() > front.getRearPosition()) {
                    sorted = false;
                    // swap the two vehicles
                    vehicles.set(i - 1, rear);
                    vehicles.set(i, front);
                }
            }
        }
    }

    /**
     * Returns an iterator over all the vehicles in this lane segment.
     * 
     * @return an iterator over all the vehicles in this lane segment
     */
    @Override
    public final Iterator<Vehicle> iterator() {
        return vehicles.iterator();
    }

    /**
     * Asserts the lane segment's class invariant. Used for debugging.
     */
    public boolean assertInvariant() {
        final int roadSegmentId = roadSegment.id();
        for (final Vehicle vehicle : vehicles) {
            assert vehicle.roadSegmentId() == roadSegmentId;
            if (vehicle.lane() != lane) {
                logger.info("vehicle lane={}, lane={}", vehicle.lane(), lane);
            }
            assert vehicle.lane() == lane;
        }
        return true;
    }

    public void clearVehicleRemovedCount() {
        removedVehicleCount = 0;
    }

    @Override
    public String toString() {
        return "LaneSegment [sinkLaneSegment=" + sinkLaneSegment + ", sourceLaneSegment=" + sourceLaneSegment
                + ", lane=" + lane + ", type=" + type + ", removedVehicleCount=" + removedVehicleCount + "]";
    }

}
