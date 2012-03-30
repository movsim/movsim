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
package org.movsim.simulator.roadnetwork;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Iterable collection of the road segments that form a route that can be take through the road network.
 */
public class Route implements Iterable<RoadSegment> {
    private final ArrayList<RoadSegment> roadSegments;
    private String name;
    private double length;

    /**
     * Constructor.
     */
    public Route(String name) {
        roadSegments = new ArrayList<RoadSegment>();
        this.name = name;
    }

    /**
     * Returns the name of the route.
     * 
     * @return the name of the route
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the length of the route.
     * 
     * @return the length of the route
     */
    public final double getLength() {
        return length;
    }

    /**
     * Returns the number of RoadSegments in the route.
     * 
     * @return the number of RoadSegments in route
     */
    public final int size() {
        return roadSegments.size();
    }

    /**
     * Adds a road segment to the road route.
     * 
     * @param roadSegment
     * @return roadSegment for convenience
     */
    public RoadSegment add(RoadSegment roadSegment) {
        // TODO - check that the roadSegment is contiguous with the previous roadSegment
        assert roadSegment != null;
        roadSegments.add(roadSegment);
        length += roadSegment.roadLength();
        return roadSegment;
    }

    /**
     * Gets the road segment of the given index
     */
    public RoadSegment get(int index) {
        return roadSegments.get(index);
    }

    /**
     * Returns an iterator over all the road segments in the road network.
     * 
     * @return an iterator over all the road segments in the road network
     */
    @Override
    public Iterator<RoadSegment> iterator() {
        return roadSegments.iterator();
    }
}
