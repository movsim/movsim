package org.movsim.simulator.roadnetwork.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint.SignalPointType;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

public abstract class RoadObjectController implements RoadObject, Comparable<RoadObject> {

    protected static final Logger LOG = LoggerFactory.getLogger(RoadObjectController.class);

    protected final RoadObjectType type;

    protected final double position;

    protected final RoadSegment roadSegment;

    private boolean laneValidity[];

    protected double simulationTime;
    protected List<Vehicle> vehiclesPassedStart = new ArrayList<>();
    protected List<Vehicle> vehiclesPassedEnd = new ArrayList<>();

    public RoadObjectController(RoadObjectType type, double position, RoadSegment roadSegment) {
        this.type = type;
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        Preconditions.checkArgument(position >= 0 && position <= roadSegment.roadLength(),
                "inconsistent input data: roadObject " + type + " at position=" + position
                        + " does not fit onto roadId=" + roadSegment.userId());
        this.position = position;
        // default: traffic sign applies to all lanes
        laneValidity = new boolean[roadSegment.laneCount()];
        Arrays.fill(laneValidity, true);
    }

    public RoadObjectController(RoadObjectType type, double position, int lane, RoadSegment roadSegment) {
        this(type, position, roadSegment);
        // set only one lane valid
        Arrays.fill(laneValidity, false);
        setLaneValidity(lane, true);
    }

    @Override
    public RoadObjectType getType() {
        return type;
    }

    @Override
    public int compareTo(RoadObject compareSign) {
        // ascending order
        return Double.compare(position, compareSign.position());
    }

    @Override
    public double position() {
        return position;
    }

    @Override
    public RoadSegment roadSegment() {
        return roadSegment;
    }

    @Override
    public boolean isValidLane(int lane) {
        Preconditions.checkArgument(lane >= Lanes.MOST_INNER_LANE && lane <= laneValidity.length, "invalid lane="
                + lane);
        return laneValidity[lane - 1];
    }

    protected void setLaneValidity(int lane, boolean value) {
        Preconditions.checkArgument(lane >= Lanes.MOST_INNER_LANE && lane <= laneValidity.length, "invalid lane="
                + lane);
        laneValidity[lane - 1] = value;

    }

    @Override
    public String toString() {
        return "RoadObjectController [roadSegment=" + roadSegment + ", position=" + position + ", type=" + type + "]";
    }

    @Override
    public abstract void timeStep(double dt, double simulationTime, long iterationCount);

    public void registerVehicles(SignalPointType signalPointType, double simulationTime,
            Iterator<Vehicle> passedVehicles) {
        this.simulationTime = simulationTime;
        if (signalPointType == SignalPointType.START) {
            vehiclesPassedStart.clear();
            Iterators.addAll(vehiclesPassedStart, passedVehicles);
        } else {
            vehiclesPassedEnd.clear();
            Iterators.addAll(vehiclesPassedEnd, passedVehicles);
        }
    }

}
