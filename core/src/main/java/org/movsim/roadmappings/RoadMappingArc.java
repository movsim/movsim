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

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry;

/**
 * Maps a road segment onto an arc of a circle. Angles are interpreted as in the Argand diagram, that is 0 is at the 3
 * o'clock position. A positive angle indicates a counter-clockwise rotation while a negative angle indicates a
 * clockwise rotation.
 */
public class RoadMappingArc extends RoadMappingCircle {

    protected double startAngle;
    protected double arcAngle;

    public static RoadMappingArc create(RoadGeometry roadGeometry) {
        return create(roadGeometry.getLaneGeometries(), roadGeometry.geometry());
    }

    private static RoadMappingArc create(LaneGeometries laneGeometries, Geometry geometry) {
        return new RoadMappingArc(laneGeometries, geometry.getS(), geometry.getX(), geometry.getY(), geometry.getHdg(),
                geometry.getLength(), geometry.getArc().getCurvature());
    }

    /**
     * The arc begins at startAngle and extends for arcAngle radians.
     * 
     * @param laneCount
     *            number of lanes
     * @param x0
     *            start of arc, x coordinate
     * @param y0
     *            start of arc, y coordinate
     * @param radius
     *            radius of arc
     * @param startAngle
     *            start direction of arc, ie angle subtended at center + PI/2
     * @param arcAngle
     */
    RoadMappingArc(LaneGeometries laneGeometries, double x0, double y0, double radius, double startAngle,
            double arcAngle) {
        super(laneGeometries, x0, y0, radius, arcAngle < 0.0);
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
        // direction of travel on ramps when vehicles drive on the right
        roadLength = Math.abs(arcAngle) * radius;
        centerX = x0 - radius * Math.cos(startAngle - 0.5 * Math.PI) * (clockwise ? -1 : 1);
        centerY = y0 + radius * Math.sin(startAngle - 0.5 * Math.PI) * (clockwise ? -1 : 1);
    }

    /**
     * The arc begins at startAngle and extends for arcAngle radians.
     * 
     * @param laneCount
     *            number of lanes
     * @param s
     * @param x0
     *            start of arc, x coordinate
     * @param y0
     *            start of arc, y coordinate
     * @param startAngle
     *            start direction of arc, ie angle subtended at center + PI/2
     * @param length
     *            length of arc
     * @param curvature
     *            curvature of arc
     */
    RoadMappingArc(LaneGeometries laneGeometries, double s, double x0, double y0, double startAngle, double length,
            double curvature) {
        super(laneGeometries, x0, y0, 1.0 / Math.abs(curvature), curvature < 0.0);
        roadLength = length;
        this.startAngle = startAngle;
        arcAngle = roadLength * curvature;
        centerX = x0 - radius * Math.cos(startAngle - 0.5 * Math.PI) * (clockwise ? -1 : 1);
        centerY = y0 + radius * Math.sin(startAngle - 0.5 * Math.PI) * (clockwise ? -1 : 1);
    }

    RoadMappingArc(LaneGeometries laneGeometries, double x0, double y0, double radius, boolean clockwise) {
        super(laneGeometries, x0, y0, radius, clockwise);
    }

    RoadMappingArc(LaneGeometries laneGeometries, double s, double x, double y, double hdg, double length,
            double curvature,
            double laneWidth) {
        this(laneGeometries, s, x, y, hdg, length, curvature);
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        // tangent to arc (road direction)
        final double theta = clockwise ? startAngle - roadPos / radius : startAngle + roadPos / radius;
        // final double theta = clockwise ? startAngle + roadPos * curvature : startAngle - roadPos * curvature;
        // angle arc subtends at center
        final double arcTheta = theta - 0.5 * Math.PI;
        posTheta.cosTheta = Math.cos(theta);
        posTheta.sinTheta = Math.sin(theta);
        // lateralOffset is perpendicular to road
        final double r = radius + lateralOffset * (clockwise ? -1 : 1);
        posTheta.x = centerX + r * Math.cos(arcTheta) * (clockwise ? -1 : 1);
        posTheta.y = centerY - r * Math.sin(arcTheta) * (clockwise ? -1 : 1);
        return posTheta;
    }

    /**
     * Returns the start angle of the arc.
     * 
     * @return the start angle of the arc, radians
     */
    public double startAngle() {
        return startAngle;
    }

    /**
     * Returns the sweep angle of the arc.
     * 
     * @return sweep angle of the arc, radians
     */
    public double arcAngle() {
        return arcAngle;
    }

}
