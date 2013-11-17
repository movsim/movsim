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
 * Maps a road segment onto a U-shaped section of road.
 * </p>
 * <p>
 * NOT PART OF ROADXML SPECIFICATION, so do not use if you wish your simulation to be saved as a ROADXML file.
 * </p>
 */
public class RoadMappingU extends RoadMapping {

    protected double radius;
    protected double straightLength; // length(m) of straight sections of U-shape

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     *            start of U, x coordinate
     * @param y0
     *            start of U, y coordinate
     * @param radius
     *            radius of curved part of U
     * @param straightLength
     *            length of straight part of U
     */
    RoadMappingU(LaneGeometries laneGeometries, double x0, double y0, double radius, double straightLength) {
        super(laneGeometries, x0, y0);
        this.radius = radius;
        this.straightLength = straightLength;
        roadLength = Math.PI * radius + 2 * straightLength;
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     * @param y0
     */
    RoadMappingU(LaneGeometries laneGeometries, double x0, double y0) {
        super(laneGeometries, x0, y0);
    }

    /**
     * Returns the length of the straight part of the U
     * 
     * @return length of straight part of U
     */
    public double straightLength() {
        return straightLength;
    }

    /**
     * Returns the radius of the curved part of the U.
     * 
     * @return radius of curved part of U
     */
    public double radius() {
        return radius;
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        // lateralOffset is perpendicular to road
        final double r = radius + lateralOffset;
        if (roadPos <= straightLength) {
            // on the first straight of the U
            posTheta.cosTheta = -1.0;
            posTheta.sinTheta = 0.0;
            posTheta.x = x0 - roadPos;
            posTheta.y = y0 - lateralOffset;
        } else if (roadPos > roadLength - straightLength) {
            // on the second straight of the U
            posTheta.cosTheta = 1.0;
            posTheta.sinTheta = 0.0;
            posTheta.x = x0 + roadPos - roadLength;
            posTheta.y = y0 + 2 * radius + lateralOffset;
        } else {
            // on the arc of the U
            final double arcLength = roadPos - straightLength;
            final double arcTheta = arcLength / radius + 0.5 * Math.PI;
            final double ca = Math.cos(arcTheta);
            final double sa = Math.sin(arcTheta);
            posTheta.x = x0 - straightLength + r * ca;
            posTheta.y = y0 + radius - r * sa;
            final double theta = arcTheta + 0.5 * Math.PI;
            posTheta.cosTheta = Math.cos(theta);
            posTheta.sinTheta = Math.sin(theta);
        }
        return posTheta;
    }
}
