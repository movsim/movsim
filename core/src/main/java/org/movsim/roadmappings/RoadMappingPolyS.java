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
 * RoadMapping consisting of a number of consecutive S-shaped sections of road.
 * </p>
 * <p>
 * NOT PART OF ROADXML SPECIFICATION, so do not use if you wish your simulation to be saved as a ROADXML file.
 * </p>
 */
public class RoadMappingPolyS extends RoadMappingS {

    protected int sCount;

    /**
     * Constructor.
     * 
     * @param laneCount
     *            number of lanes
     * @param sCount
     *            number of S shaped elements
     * @param x0
     *            start of S, x coordinate
     * @param y0
     *            start of S, y coordinate
     * @param radius
     *            radius of curve in S
     * @param straightLength
     *            length of straight part of S
     */
    public RoadMappingPolyS(LaneGeometries laneGeometries, int sCount, double x0, double y0, double radius,
            double straightLength) {
        super(laneGeometries, x0, y0);
        this.sCount = sCount;
        this.radius = radius;
        this.straightLength = straightLength;
        roadLength = 2 * sCount * (Math.PI * radius + straightLength);
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        final double sLength = 2 * (Math.PI * radius + straightLength);
        final int count = (int) (roadPos / sLength);
        final double remainder = roadPos - count * sLength;
        final PosTheta posTheta = super.map(remainder, lateralOffset);
        posTheta.y += count * 4 * radius;
        return posTheta;
    }
}
