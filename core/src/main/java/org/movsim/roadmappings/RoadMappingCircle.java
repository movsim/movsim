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

/**
 * Maps a road segment onto a circle.
 */
public class RoadMappingCircle extends RoadMapping {

    protected double centerX;
    protected double centerY;
    protected final double radius;
    protected final boolean clockwise;

    /**
     * 
     * @param laneCount
     *            number of lanes
     * @param x0
     *            start of circle, x coordinate
     * @param y0
     *            start of circle, y coordinate
     * @param radius
     *            radius of circle
     */
    RoadMappingCircle(LaneGeometries laneGeometries, double x0, double y0, double radius) {
        this(laneGeometries, x0, y0, radius, false);
    }

    protected RoadMappingCircle(LaneGeometries laneGeometries, double x0, double y0, double radius, boolean clockwise) {
        super(laneGeometries, x0, y0);
        this.radius = radius;
        this.clockwise = clockwise;
        roadLength = 2 * Math.PI * radius;
        centerX = clockwise ? x0 + radius : x0 - radius;
        centerY = y0;
    }

    /**
     * Returns the x coordinate of the center of the circle.
     * 
     * @return the x coordinate of the center of the circle
     */
    public double centerX() {
        return centerX;
    }

    /**
     * Returns the y coordinate of the center of the circle.
     * 
     * @return the y coordinate of the center of the circle
     */
    public double centerY() {
        return centerY;
    }

    /**
     * Returns the radius of the circle.
     * 
     * @return the radius of the circle
     */
    public double radius() {
        return radius;
    }

    /**
     * Returns true if the circle mapping is in a clockwise direction.
     * 
     * @return true if the circle mapping is in a clockwise direction
     */
    public boolean clockwise() {
        return clockwise;
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        final double arcTheta = clockwise ? -roadPos / radius : roadPos / radius;
        // road direction, perpendicular to angle position subtends at center
        final double theta = arcTheta + 0.5 * Math.PI;
        posTheta.cosTheta = Math.cos(theta);
        posTheta.sinTheta = Math.sin(theta);

        // lateralOffset is perpendicular to road
        final double r = radius + lateralOffset * (clockwise ? -1 : 1);
        posTheta.x = centerX + r * Math.cos(arcTheta) * (clockwise ? -1 : 1);
        posTheta.y = centerY - r * Math.sin(arcTheta) * (clockwise ? -1 : 1);
        return posTheta;
    }
}
