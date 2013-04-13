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
    protected final int laneCount;
    protected final double laneWidth;

    public RoadGeometry(Geometry geometry, int laneCount, double laneWidth) {
        this.geometry = Preconditions.checkNotNull(geometry);
        Preconditions.checkArgument(laneCount > 0);
        Preconditions.checkArgument(laneWidth > 0);
        this.laneCount = laneCount;
        this.laneWidth = laneWidth;
    }

    public Geometry geometry() {
        return geometry;
    }

    public int laneCount() {
        return laneCount;
    }

    public double laneWidth() {
        return laneWidth; // ignore laneWidth from peer
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

}
