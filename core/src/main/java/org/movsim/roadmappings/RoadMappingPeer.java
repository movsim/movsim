package org.movsim.roadmappings;

import com.google.common.base.Preconditions;
import org.movsim.simulator.vehicles.Vehicle;

public class RoadMappingPeer extends RoadMapping {

    // private static final Logger LOG = LoggerFactory.getLogger(RoadMappingPeer.class);

    private final RoadMapping roadMapping;

    public RoadMappingPeer(RoadMapping roadMapping) {
        super(roadMapping.laneGeometries, roadMapping.startPos().x, roadMapping.startPos().y);
        this.roadMapping = Preconditions.checkNotNull(roadMapping);
        this.roadLength = roadMapping.roadLength();
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        // counterdirection simply be inverting roadPos
        return roadMapping.map(roadLength - roadPos, lateralOffset);
    }

    @Override
    public PolygonFloat mapFloat(Vehicle vehicle) {
        final PosTheta posTheta = map(vehicle.physicalQuantities().getMidPosition(),
                laneCenterOffset(vehicle.getContinuousLane()));
        return mapFloat(posTheta, vehicle.physicalQuantities().getLength(), vehicle.physicalQuantities().getWidth());
    }

    @Override
    public boolean isPeer() {
        return true;
    }

}
