package org.movsim.simulator.roadnetwork;

import java.util.Arrays;

import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Preconditions;

public class TrafficSignBase implements TrafficSign {

    protected final TrafficSignType type;

    protected final double position;

    protected final RoadSegment roadSegment;

    private boolean laneValidity[];

    public TrafficSignBase(TrafficSignType type, double position, RoadSegment roadSegment) {
        this.type = type;
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        Preconditions.checkArgument(position() >= 0 && position() <= roadSegment.roadLength(),
                "inconsistent input data: trafficSign " + type.toString() + " at position=" + position()
                        + " does not fit onto road with id=" + roadSegment.userId());
        this.position = position;
        // default: traffic sign applies to all lanes
        laneValidity = new boolean[roadSegment.laneCount()];
        Arrays.fill(laneValidity, true);
    }

    public TrafficSignBase(TrafficSignType type, double position, int lane, RoadSegment roadSegment) {
        this(type, position, roadSegment);
        // set only one lane valid
        Arrays.fill(laneValidity, false);
        setLaneValidity(lane, true);
    }

    @Override
    public TrafficSignType getType() {
        return type;
    }

    @Override
    public int compareTo(TrafficSign compareSign) {
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
        return "TrafficSignBase [roadSegment=" + roadSegment + ", position=" + position + ", type=" + type + "]";
    }

    @Override
    public void apply(Vehicle vehicle) {
        // dummy, overwrite if needed
    }

}
