package org.movsim.roadmappings;

import com.google.common.base.Preconditions;

public class TotalRoadMapping {

    private final RoadMapping roadMapping;
    private final RoadMapping roadMappingPeer;

    public TotalRoadMapping(RoadMapping roadMapping, RoadMapping roadMappingPeer) {
        this.roadMapping = Preconditions.checkNotNull(roadMapping);
        this.roadMappingPeer = roadMappingPeer;
    }


    public boolean hasPeer() {
        return roadMappingPeer != null;
    }

    public int roadLaneCount() {
        return roadMapping.laneCount() + (hasPeer() ? roadMappingPeer.laneCount() : 0);
    }

    public RoadMapping getPeer() {
        Preconditions.checkArgument(hasPeer(), "has not peer");
        return roadMappingPeer;
    }

    public RoadMapping getRoadMapping() {
        return roadMapping;
    }

    public void addClippingRegion(double pos, double length) {
        roadMapping.addClippingRegion(pos, length);
        if (hasPeer()) {
            roadMappingPeer.addClippingRegion(pos, length);
        }
    }

}
