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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

public final class RoadSegmentUtils {

    public enum VehiclesIncreasing implements Comparator<Vehicle> {
        INSTANCE;

        @Override
        public int compare(Vehicle o1, Vehicle o2) {
            return Double.compare(o1.getFrontPosition(), o2.getFrontPosition());
        }
    }

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

    private static Collection<Vehicle> sortVehicles(Iterator<Vehicle> vehicleIterator, Comparator<Vehicle> vehComparator) {
        List<Vehicle> vehicles = new ArrayList<>(500);
        Iterators.addAll(vehicles, vehicleIterator);
        Collections.sort(vehicles, vehComparator);
        return vehicles;
    }

    public static Collection<Vehicle> sortVehiclesIncreasingPosition(Iterator<Vehicle> vehicleIterator) {
        return sortVehicles(vehicleIterator, VehiclesIncreasing.INSTANCE);
    }

    public static Collection<Vehicle> sortVehiclesDecreasingPosition(Iterator<Vehicle> vehicleIterator) {
        return sortVehicles(vehicleIterator, Collections.reverseOrder(VehiclesIncreasing.INSTANCE));
    }

}
