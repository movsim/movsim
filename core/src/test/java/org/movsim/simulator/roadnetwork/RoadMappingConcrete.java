package org.movsim.simulator.roadnetwork;

import org.movsim.roadmappings.LaneGeometries;
import org.movsim.roadmappings.LaneGeometries.LaneGeometry;
import org.movsim.roadmappings.PosTheta;
import org.movsim.roadmappings.RoadMapping;

class RoadMappingConcrete extends RoadMapping {

    public static RoadMappingConcrete create(int laneCount) {
        LaneGeometries laneGeometries = new LaneGeometries();
        laneGeometries.setRight(new LaneGeometry(laneCount));
        return new RoadMappingConcrete(laneGeometries);
    }

    public static RoadMappingConcrete create(int laneCount, double roadLength) {
        LaneGeometries laneGeometries = new LaneGeometries();
        laneGeometries.setRight(new LaneGeometry(laneCount));
        return new RoadMappingConcrete(laneGeometries, roadLength);
    }

    private RoadMappingConcrete(LaneGeometries laneGeometries) {
        super(laneGeometries, 0, 0);
    }

    private RoadMappingConcrete(LaneGeometries laneGeometries, double roadLength) {
        this(laneGeometries);
        this.roadLength = roadLength;
    }

    @Override
    public PosTheta map(double roadPos, double delta) {
        return posTheta;
    }
}
