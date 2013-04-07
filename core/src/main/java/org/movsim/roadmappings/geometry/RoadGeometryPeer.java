package org.movsim.roadmappings.geometry;

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the geometry of a peer which is the other driving direction of a bidirectional road. A peer geometry runs in reverse direction and
 * is laterally shifted.
 * 
 * <br>
 * created: Apr 7, 2013<br>
 * 
 */
public class RoadGeometryPeer extends RoadGeometry {
    private static final Logger LOG = LoggerFactory.getLogger(RoadGeometryPeer.class);

    private final Geometry peerGeometry; // new object because Geometry is shared between left/right lanes

    public RoadGeometryPeer(Geometry geometry, int laneCount, double laneWidth) {
        super(geometry, laneCount, laneWidth);
        peerGeometry = (Geometry) geometry.copyTo(null);
        updatePeerGeometry();
    }

    @Override
    public Geometry geometry() {
        return peerGeometry;
    }

    private void updatePeerGeometry() {
        // TODO handling of different geometries
        if (geometryType() == GeometryType.LINE) {
            updateGeometryForLine();
        } else if (geometryType() == GeometryType.ARC) {
            updateGeometryForArc();
        } else {
            LOG.warn("did not change peer geometry for " + geometryType());
        }
    }

    private void updateGeometryForArc() {
        // FIXME simple shift not sufficient for arc geometry
        peerGeometry.setY(geometry.getY() - 30);
    }

    private void updateGeometryForLine() {
        peerGeometry.setY(geometry.getY() - (laneCount + 1) * laneWidth); // FIXME offset +1 to simple
        // new (x0,y0) start point with new direction:
        peerGeometry.setX(geometry.getX() + geometry.getLength() * Math.cos(geometry.getHdg()));
        peerGeometry.setY(peerGeometry.getY() + geometry.getLength() * Math.sin(geometry.getHdg()));
        peerGeometry.setHdg(Math.PI - geometry.getHdg());
    }

}
