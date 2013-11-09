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

package org.movsim.simulator.roadnetwork;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Preconditions;

public final class RoadSegmentUtils {

    private RoadSegmentUtils() {
        throw new IllegalStateException("do not instanciate");
    }

    /**
     * Returns true if the {@code RoadSegment} is connected in downstream direction to the provided argument and false
     * otherwise. Connection exists if at least one {@code LaneSegment} is connected.
     * 
     * @param upstreamRoadSegment
     * @return
     */
    public static boolean isConnected(RoadSegment upstream, RoadSegment downstream) {
        Preconditions.checkNotNull(upstream);
        Preconditions.checkNotNull(downstream);
        for (LaneSegment laneSegment : upstream.laneSegments()) {
            if (laneSegment.sinkLaneSegment() != null && laneSegment.sinkLaneSegment().roadSegment().equals(downstream)) {
                return true;
            }
        }
        return false;
    }

    // first element is most downstream (reverse order along roadSegment
    public Collection<Vehicle> sortVehicles(Iterator<Vehicle> vehicleIterator) {
        SortedSet<Vehicle> sortedVehicles = new TreeSet<Vehicle>();
        // FIXME implement this
        return sortedVehicles;
    }

}
