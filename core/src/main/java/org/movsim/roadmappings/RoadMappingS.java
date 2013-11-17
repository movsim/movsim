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
 * <p>
 * Maps a road segment onto an S-shaped section of road.
 * </p>
 * <p>
 * NOT PART OF ROADXML SPECIFICATION, so do not use if you wish your simulation to be saved as a ROADXML file.
 * </p>
 */
public class RoadMappingS extends RoadMappingU {

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     *            start of S, x coordinate
     * @param y0
     *            start of S, y coordinate
     * @param radius
     *            radius of curve in S
     * @param straight
     *            length of straight part of S
     */
    RoadMappingS(LaneGeometries laneGeometries, double x0, double y0, double radius, double straight) {
        super(laneGeometries, x0, y0, radius, straight);
        roadLength = 2 * (Math.PI * radius + straight);
    }

    /**
     * Constructor.
     */
    RoadMappingS(LaneGeometries laneGeometries, double x0, double y0) {
        super(laneGeometries, x0, y0);
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        // lateralOffset is perpendicular to road
        final double curveLength = radius * Math.PI;
        if (roadPos <= curveLength) {
            // on the first curve of the S
            final double arcLength = roadPos;
            final double arcTheta = arcLength / radius + 0.5 * Math.PI;
            final double ca = Math.cos(arcTheta);
            final double sa = Math.sin(arcTheta);
            final double r = radius + lateralOffset;
            posTheta.x = x0 + r * ca;
            posTheta.y = y0 + radius - r * sa;
            final double theta = arcTheta + 0.5 * Math.PI;
            posTheta.cosTheta = Math.cos(theta);
            posTheta.sinTheta = Math.sin(theta);
        } else if (roadPos <= curveLength + straightLength) {
            // on the first straight of the S
            posTheta.cosTheta = 1.0;
            posTheta.sinTheta = 0.0;
            posTheta.x = x0 + roadPos - curveLength;
            posTheta.y = y0 + lateralOffset + 2 * radius;
        } else if (roadPos <= straightLength + 2 * curveLength) {
            // on the second curve of the S
            final double arcLength = roadPos - straightLength - 2 * curveLength;
            final double arcTheta = arcLength / radius - 0.5 * Math.PI;
            final double ca = Math.cos(arcTheta);
            final double sa = Math.sin(arcTheta);
            final double r = radius - lateralOffset;
            posTheta.x = x0 + straightLength - r * ca;
            posTheta.y = y0 + 3 * radius - r * sa;
            final double theta = -arcTheta + 0.5 * Math.PI;
            posTheta.cosTheta = Math.cos(theta);
            posTheta.sinTheta = Math.sin(theta);
        } else {
            // on the second straight of the S
            posTheta.cosTheta = -1.0;
            posTheta.sinTheta = 0.0;
            posTheta.x = x0 + 2 * (curveLength + straightLength) - roadPos;
            posTheta.y = y0 - lateralOffset + 4 * radius;
        }
        return posTheta;
    }
}
