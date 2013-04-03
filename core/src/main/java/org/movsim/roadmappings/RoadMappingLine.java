/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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
 * Maps a road segment onto straight line.
 */
public class RoadMappingLine extends RoadMapping {

    protected double x1;
    protected double y1;

    public static RoadMapping create(int laneCount, Geometry geometry, double laneWidth) {
        return new RoadMappingLine(laneCount, geometry.getS(), geometry.getX(), geometry.getY(), geometry.getHdg(),
                geometry.getLength(), laneWidth);
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     *            number of lanes in road mapping
     * @param x0
     *            x-position of start of line
     * @param y0
     *            y-position of start of line
     * @param x1
     *            x-position of end of line
     * @param y1
     *            y-position of end of line
     */
    RoadMappingLine(int laneCount, double x0, double y0, double x1, double y1) {
        super(laneCount, x0, y0);
        this.x1 = x1;
        this.y1 = y1;
        init();
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     *            number of lanes in road mapping
     * @param s
     * @param x0
     *            x-position of start of line
     * @param y0
     *            y-position of start of line
     * @param theta
     *            direction of line
     * @param length
     *            length of line
     */
    RoadMappingLine(int laneCount, double s, double x0, double y0, double theta, double length) {
        super(laneCount, x0, y0);
        roadLength = length;
        posTheta.sinTheta = Math.sin(theta);
        posTheta.cosTheta = Math.cos(theta);
        x1 = x0 + length * posTheta.cosTheta;
        y1 = y0 + length * posTheta.sinTheta;
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     * @param y0
     */
    RoadMappingLine(int laneCount, double x0, double y0) {
        super(laneCount, x0, y0);   
    }

    /**
     * Constructor to append this road mapping onto a previously existing road mapping, matching the endpoints.
     * 
     * @param roadMapping
     *            the road mapping to append to
     * @param x1
     *            new point, x coordinate
     * @param y1
     *            new point, y coordinate
     */
    RoadMappingLine(RoadMapping roadMapping, double x1, double y1) {
        super(roadMapping.laneCount(), 0, 0);
        final RoadMapping.PosTheta posTheta = roadMapping.endPos();
        x0 = posTheta.x;
        y0 = posTheta.y;
        this.x1 = x1;
        this.y1 = y1;
        init();
    }

    RoadMappingLine(int laneCount, double s, double x, double y, double hdg, double length, double a) {
        this(laneCount, s, x, y, hdg, length);
        laneWidth = a;
        roadWidth = laneWidth * laneCount;
    }

    protected void init() {
        final double opp = y1 - y0;
        final double adj = x1 - x0;
        roadLength = Math.sqrt(opp * opp + adj * adj);
        posTheta.sinTheta = -opp / roadLength;
        posTheta.cosTheta = adj / roadLength;
    }

    protected void moveStart(double dx, double dy) {
        x0 += dx;
        y0 += dy;
    }

    @Override
    public RoadMapping.PosTheta map(double roadPos, double lateralOffset) {
        // lateralOffset offset is perpendicular to road
        posTheta.x = x0 + roadPos * posTheta.cosTheta + lateralOffset * posTheta.sinTheta;
        posTheta.y = y0 - roadPos * posTheta.sinTheta + lateralOffset * posTheta.cosTheta;
        return posTheta;
    }

}
