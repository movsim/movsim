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

package org.movsim.simulator.roadnetwork.controller;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.ElevationProfile;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.ElevationProfile.Elevation;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.roadnetwork.predicates.VehicleWithinRange;
import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Predicate;

/**
 * Sets the road slope within a spatial range of a RoadSection. The range is limited to the extend of one roadSection only.
 * 
 * <p>
 * The slope is calculated from the elevation profile given in the xodr network input file.
 */
public class GradientProfile extends RoadObjectController {

    private final double endPosition;

    /** mapping of positions to gradients along track */
    private final SortedMap<Double, Double> gradients = new TreeMap<>();

    private final Predicate<Vehicle> vehiclesWithRange;

    private final SignalPoint endSignalPoint;

    public GradientProfile(ElevationProfile elevationProfile, RoadSegment roadSegment) {
        super(RoadObjectType.GRADIENT_PROFILE, elevationProfile.getElevation().get(0).getS(), roadSegment);
        createGradientProfile(elevationProfile.getElevation());
        if (position != gradients.firstKey()) {
            throw new IllegalArgumentException("first given track position=" + position + " > lowest position="
                    + gradients.firstKey() + " in elevation profile");
        }
        endPosition = gradients.lastKey();
        if (endPosition > roadSegment().roadLength()) {
            throw new IllegalArgumentException("elevation profile track position s=" + endPosition
                    + " exceeds roadlength.");
        }
        this.vehiclesWithRange = new VehicleWithinRange(position, endPosition);
        endSignalPoint = new SignalPoint(endPosition, roadSegment);
    }

    @Override
    public void createSignalPositions() {
        roadSegment.signalPoints().add(endSignalPoint);
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        LOG.debug("vehiclesPassedEnd={}", endSignalPoint.passedVehicles().size());
        Iterator<Vehicle> iterator = roadSegment.filteredVehicles(vehiclesWithRange);
        while (iterator.hasNext()) {
            Vehicle vehicle = iterator.next();
            apply(vehicle);
        }
        for (Vehicle vehicle : endSignalPoint.passedVehicles()) {
            vehicle.setSlope(0); // reset
        }
    }

    private void apply(Vehicle vehicle) {
        assert vehicle.getFrontPosition() >= position;
        assert vehicle.getFrontPosition() <= endPosition;
        Double posUpstream = gradients.headMap(vehicle.getFrontPosition()).lastKey();
        double gradient = gradients.get(posUpstream);
        vehicle.setSlope(gradient);
        LOG.debug("pos={} --> slope gradient={}", vehicle.getFrontPosition(), gradient);
    }

    private void createGradientProfile(List<Elevation> elevationProfile) {
        SortedMap<Double, Double> elevation = new TreeMap<>();
        for (Elevation basePoint : elevationProfile) {
            elevation.put(basePoint.getS(), basePoint.getA());
        }

        Entry<Double, Double> previousElevationPoint = null;
        for (Entry<Double, Double> elevationPoint : elevation.entrySet()) {
            if (previousElevationPoint == null) {
                previousElevationPoint = elevationPoint;
                continue;
            }
            double deltaPosition = elevationPoint.getKey() - previousElevationPoint.getKey();
            double deltaHeight = elevationPoint.getValue() - previousElevationPoint.getValue();
            if (deltaPosition > 0) {
                gradients.put(previousElevationPoint.getKey(), deltaHeight / deltaPosition);
                previousElevationPoint = elevationPoint;
            }
        }
        gradients.put(elevation.lastKey(), 0.0);
    }

    public Set<Entry<Double, Double>> gradientEntries() {
        return Collections.unmodifiableSet(gradients.entrySet());
    }

}
