package org.movsim.roadmappings;

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Decorates the openDRIVE {@link Geometry}.
 * 
 * <br>
 * created: Apr 7, 2013<br>
 * 
 */
public class RoadGeometry {

    private static final Logger LOG = LoggerFactory.getLogger(RoadGeometry.class);

    public enum GeometryType {
        LINE, ARC, SPIRAL, POLY3;
    }

    protected final Geometry geometry;

    protected final LaneGeometries laneGeometries;

    public RoadGeometry(Geometry geometry, LaneGeometries laneGeometries) {
        this.geometry = Preconditions.checkNotNull(geometry);
        this.laneGeometries = Preconditions.checkNotNull(laneGeometries);
        Preconditions.checkArgument(laneGeometries.getRight().getLaneCount() > 0,
                "forward link necessary (under development)");
    }

    public Geometry geometry() {
        return geometry;
    }

    public int totalLaneCount() {
        return laneGeometries.getTotalLaneCount();
    }

    public double laneWidth() {
        return laneGeometries.getRight().getLaneWidth(); // ignore laneWidth from peer so far
    }

    public GeometryType geometryType() {
        if (geometry.isSetLine()) {
            return GeometryType.LINE;
        } else if (geometry.isSetArc()) {
            return GeometryType.ARC;
        } else if (geometry.isSetPoly3()) {
            return GeometryType.POLY3;
        } else if (geometry.isSetSpiral()) {
            return GeometryType.SPIRAL;
        } else {
            throw new IllegalArgumentException("Unknown geometry type: " + geometry);
        }
    }

    public LaneGeometries getLaneGeometries() {
        return laneGeometries;
    }


}
