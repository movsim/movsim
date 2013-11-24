/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */

package org.movsim.roadmappings;

import java.util.ArrayList;
import java.util.Iterator;

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry;
import org.movsim.roadmappings.RoadGeometry.GeometryType;

/**
 * RoadMapping consisting of a number of consecutive heterogeneous RoadMappingUtils.
 */
public class RoadMappingPoly extends RoadMapping implements Iterable<RoadMapping> {

    protected final ArrayList<RoadMapping> roadMappings = new ArrayList<>();

    @Override
    public Iterator<RoadMapping> iterator() {
        return roadMappings.iterator();
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     */
    public RoadMappingPoly(LaneGeometries laneGeometries) {
        super(laneGeometries, 0, 0);
    }

    /**
     * Constructor, adds an initial line.
     * 
     * @param laneCount
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     */
    public RoadMappingPoly(LaneGeometries laneGeometries, double x0, double y0, double x1, double y1) {
        super(laneGeometries, x0, y0);
        final RoadMapping roadMapping = new RoadMappingLine(laneGeometries, x0, y0, x1, y1);
        roadLength = roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    /**
     * Called when the system is running low on memory, and would like actively running process to try to tighten their
     * belts.
     */
    @Override
    protected void onLowMemory() {
        roadMappings.trimToSize();
    }

    @Override
    public PosTheta startPos() {
        return roadMappings.get(0).startPos();
    }

    @Override
    public PosTheta startPos(double lateralOffset) {
        return roadMappings.get(0).startPos(lateralOffset);
    }

    @Override
    public PosTheta endPos() {
        return roadMappings.get(roadMappings.size() - 1).endPos();
    }

    @Override
    public PosTheta endPos(double lateralOffset) {
        return roadMappings.get(roadMappings.size() - 1).endPos(lateralOffset);
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {

        double pos = roadPos;
        for (final RoadMapping roadMapping : roadMappings) {
            if (pos <= roadMapping.roadLength()) {
                return roadMapping.map(pos, lateralOffset);
            }
            pos -= roadMapping.roadLength();
        }
        // have gone past end of last road mapping in road segment
        // this can happen by up to half a vehicle length - vehicle's rear position is
        // on road mapping, but vehicle's mid position (which is used for drawing) has
        // gone past the end, so fix this as a special case.
        final RoadMapping roadMapping = roadMappings.get(roadMappings.size() - 1);
        return roadMapping.map(pos + roadMapping.roadLength(), lateralOffset);
    }

    public void addLinePoint(double x, double y) {
        final RoadMapping lastRoadMapping = roadMappings.get(roadMappings.size() - 1);
        final RoadMappingLine roadMapping = new RoadMappingLine(lastRoadMapping, this.laneGeometries, x, y);
        roadLength += roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    public void addLinePointRelative(double dx, double dy) {
        final RoadMapping lastRoadMapping = roadMappings.get(roadMappings.size() - 1);
        final PosTheta posTheta = lastRoadMapping.endPos();
        final RoadMappingLine roadMapping = new RoadMappingLine(lastRoadMapping, this.laneGeometries, posTheta.x + dx,
                posTheta.y + dy);
        roadLength += roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    public void addLine(double s, double x0, double y0, double theta, double length) {
        final RoadMappingLine roadMapping = new RoadMappingLine(this.laneGeometries, s, x0, y0, theta, length);
        roadLength += length;
        roadMappings.add(roadMapping);
    }

    public void addLine(Geometry geometry) {
        addLine(geometry.getS(), geometry.getX(), geometry.getY(), geometry.getHdg(), geometry.getLength());
    }

    public void addArc(double s, double x0, double y0, double theta, double length, double curvature) {
        // final RoadMapping lastRoadMapping = roadMappings.get(roadMappings.size() - 1);
        // final PosTheta posTheta = lastRoadMapping.endPos();
        // <geometry s="3.66" x="-4.64" y="4.34" hdg="5.29" length="9.19">
        // <arc curvature="-1.2698412698412698e-01"/>
        // </geometry>
        // RoadMappingArc(laneCount, s, x0, y0, theta, length, curvature) {
        final RoadMappingArc roadMapping = new RoadMappingArc(this.laneGeometries, s, x0, y0, theta, length, curvature);
        roadLength += length;
        roadMappings.add(roadMapping);
    }

    public void addArc(Geometry geometry) {
        addArc(geometry.getS(), geometry.getX(), geometry.getY(), geometry.getHdg(), geometry.getLength(), geometry
                .getArc().getCurvature());
    }

    public void addSpiral(double s, double x0, double y0, double theta, double length, double startCurvature,
            double endCurvature) {
        final RoadMappingSpiral roadMapping = new RoadMappingSpiral(this.laneGeometries, s, x0, y0, theta, length,
                startCurvature, endCurvature);
        roadLength += length;
        roadMappings.add(roadMapping);
    }

    public void addPoly3(double s, double x0, double y0, double theta, double length, double a, double b, double c,
            double d) {
        final RoadMappingBezier roadMapping = new RoadMappingBezier(this.laneGeometries, s, x0, y0, theta, length, a,
                b, c, d);
        roadLength += length;
        roadMappings.add(roadMapping);
    }

    public void add(RoadGeometry roadGeometry) {
        if (roadGeometry.geometryType() == GeometryType.LINE) {
            addLine(roadGeometry.geometry());
        } else if (roadGeometry.geometryType() == GeometryType.ARC) {
            addArc(roadGeometry.geometry());
        } else if (roadGeometry.geometryType() == GeometryType.POLY3) {
            throw new IllegalArgumentException("POLY3 geometry not yet supported");
        } else if (roadGeometry.geometryType() == GeometryType.SPIRAL) {
            throw new IllegalArgumentException("SPIRAL geometry not yet supported");
        } else {
            throw new IllegalArgumentException("Unknown geometry");
        }
    }
}
