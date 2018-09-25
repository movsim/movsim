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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.movsim.simulator.roadnetwork.predicates.VehiclePassedPosition;
import org.movsim.simulator.vehicles.Vehicle;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class SignalPoint {

    private final double position;

    private final Predicate<Vehicle> predicate; // TODO measure performance for this vehicle iterator

    protected double simulationTime;

    protected Set<Vehicle> vehiclesPassed = new LinkedHashSet<>(); // to assure uniqueness of entries

    // TODO roadSegment not needed as reference, just check here for correct position
    public SignalPoint(double position, RoadSegment roadSegment) {
        Preconditions.checkArgument(position >= 0 && position <= roadSegment.roadLength(),
                "cannot create signalPoint at invalid position=" + position);
        this.position = position;
        predicate = new VehiclePassedPosition(position);
    }

    // will be called twice, therefore cleaning separately
    void registerPassingVehicles(double simulationTime, Iterator<Vehicle> vehicles) {
        this.simulationTime = simulationTime;
        Iterators.addAll(vehiclesPassed, Iterators.filter(vehicles, predicate));
    }

    public double position() {
        return position;
    }

    public Collection<Vehicle> passedVehicles() {
        return vehiclesPassed;
    }

    double getSimulationTimeOfRegistering() {
        return simulationTime;
    }

    // called by RoadSegment
    void clear() {
        vehiclesPassed.clear();
    }

    @Override
    public String toString() {
        return "SignalPoint [position=" + position + ", vehiclesPassed.size=" + vehiclesPassed.size() + "]";
    }

}
