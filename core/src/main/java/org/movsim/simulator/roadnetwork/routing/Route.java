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
package org.movsim.simulator.roadnetwork.routing;

import java.util.Iterator;
import java.util.LinkedList;

import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.RoadSegmentUtils;

import com.google.common.base.Preconditions;

/**
 * Iterable collection of the road segments that form a route that can be taken through the road network.
 */
public class Route implements Iterable<RoadSegment> {

    private final LinkedList<RoadSegment> roadSegments;
    private String name;
    private double length;

    /**
     * Constructor.
     */
    public Route(String name) {
        Preconditions.checkArgument(!name.isEmpty(), "route without name");
        roadSegments = new LinkedList<>();
        this.name = name;
    }

    /**
     * Adds a road segment to the road route.
     * 
     * @param roadSegment
     * @return roadSegment for convenience
     */
    public RoadSegment add(RoadSegment roadSegment) {
        Preconditions.checkNotNull(roadSegment);
        Preconditions.checkArgument(!roadSegments.contains(roadSegment), "roadSegment=" + roadSegment
                + " already added to route.");

        if (!roadSegments.isEmpty()) {
            Preconditions.checkState(RoadSegmentUtils.isConnected(roadSegments.getLast(), roadSegment),
                    "Segments not connected: upstream=" + roadSegments.getLast() + ", downstream=" + roadSegment);
        }

        roadSegments.add(roadSegment);
        length += roadSegment.roadLength();
        return roadSegment;
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
     * Returns the number of RoadSegmentUtils in the route.
     * 
     * @return the number of RoadSegmentUtils in route
     */
    public final int size() {
        return roadSegments.size();
    }

    /**
     * Gets the road segment of the given index
     */
    public RoadSegment get(int index) {
        return roadSegments.get(index);
    }

    /**
     * Returns the first {@code RoadSegment} of the {@code Route}.
     * 
     * @return first {@code RoadSegment} of the {@code Route}
     */
    public RoadSegment getOrigin() {
        Preconditions.checkArgument(!roadSegments.isEmpty(), "route without any roadSegments.");
        return roadSegments.getFirst();
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

    @Override
    public String toString() {
        return "Route [name=" + name + ", length=" + length + ", roadSegments=" + roadSegments + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(length);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((roadSegments == null) ? 0 : roadSegments.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Route other = (Route) obj;
        if (Double.doubleToLongBits(length) != Double.doubleToLongBits(other.length)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (roadSegments == null) {
            if (other.roadSegments != null) {
                return false;
            }
        } else if (!roadSegments.equals(other.roadSegments)) {
            return false;
        }
        return true;
    }

}
