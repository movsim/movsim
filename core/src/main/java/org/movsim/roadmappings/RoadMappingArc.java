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
public class RoadMappingArc extends RoadMapping {

    private static final double HALF_PI = 0.5 * Math.PI;

    protected double centerX;
    protected double centerY;

    protected final double radius;

    protected final boolean clockwise;
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
        super(laneGeometries, x0, y0);
        this.startAngle = startAngle;
        this.roadLength = length;
        this.radius = 1.0 / Math.abs(curvature);
        this.clockwise = curvature < 0;
        arcAngle = roadLength * curvature;
        centerX = x0 - radius * Math.cos(startAngle - HALF_PI) * (clockwise ? -1 : 1);
        centerY = y0 - radius * Math.sin(startAngle - HALF_PI) * (clockwise ? -1 : 1);
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        // tangent to arc (road direction)
        final double theta = clockwise ? startAngle - roadPos / radius : startAngle + roadPos / radius;
        // angle arc subtends at center
        final double arcTheta = theta - HALF_PI;
        posTheta.cosTheta = Math.cos(theta);
        posTheta.sinTheta = Math.sin(theta);
        // lateralOffset is perpendicular to road
        final double r = radius - lateralOffset * (clockwise ? -1 : 1);
        posTheta.x = centerX + r * Math.cos(arcTheta) * (clockwise ? -1 : 1);
        posTheta.y = centerY + r * Math.sin(arcTheta) * (clockwise ? -1 : 1);
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

    /**
     * Returns true if the circle mapping is in a clockwise direction.
     * 
     * @return true if the circle mapping is in a clockwise direction
     */
    public boolean clockwise() {
        return clockwise;
    }

    /**
     * Returns the radius of the circle.
     * 
     * @return the radius of the circle
     */
    public double radius() {
        return radius;
    }

    @Override
    public String toString() {
        return "RoadMappingArc [x0=" + x0 + ", y0=" + y0 + ", centerX=" + centerX + ", centerY=" + centerY
                + ", radius=" + radius + ", clockwise=" + clockwise + ", startAngle=" + startAngle + ", arcAngle="
                + arcAngle + "]";
    }
}
