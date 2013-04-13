package org.movsim.roadmappings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class RoadMappingPeer extends RoadMappingAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(RoadMappingPeer.class);

    private final RoadMapping roadMapping;

    public RoadMappingPeer(RoadMapping roadMapping) {
        super(roadMapping.laneCount(), roadMapping.laneWidth(), roadMapping.startPos().x, roadMapping.startPos().y);
        this.roadMapping = Preconditions.checkNotNull(roadMapping);
        this.roadLength = roadMapping.roadLength();
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        return roadMapping.map(roadLength - roadPos, -roadWidth() + lateralOffset);
    }

    // @Override
    // protected double laneOffset(double lane) {
    // return (lane == Lanes.NONE) ? 0.0 : -(0.5 * (1 - laneCount) + (lane - 1)) * laneWidth;
    // // return (0.5 * (trafficLaneMin + laneCount - 1) - lane) * laneWidth;
    // }

    @Override
    public boolean isPeer() {
        return true;
    }

}
