package org.movsim.roadmappings;

import org.movsim.simulator.vehicles.Vehicle;
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
        // counterdirection simply be inverting roadPos
        return roadMapping.map(roadLength - roadPos, lateralOffset);
    }

    @Override
    public PolygonFloat mapFloat(Vehicle vehicle, double time) {
        final PosTheta posTheta = map(vehicle.physicalQuantities().getMidPosition(), -laneOffset(vehicle.lateralModel()
                .continousLane()));
        return mapFloat(posTheta, vehicle.physicalQuantities().getLength(), vehicle.physicalQuantities().getWidth());
    }

    @Override
    public boolean isPeer() {
        return true;
    }

}
