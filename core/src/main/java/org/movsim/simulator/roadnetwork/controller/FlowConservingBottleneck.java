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

import java.util.Iterator;

import org.movsim.autogen.Inhomogeneity;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.roadnetwork.predicates.VehicleWithinRange;
import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Predicate;

/**
 * Modifies the driving behavior within a spatial range of a RoadSection. The range is limited to the extend of one roadSection only.
 * 
 * <p>
 * The spatial profile and the parameter values are configured in the movsim input file.
 * 
 * <br>
 * created: May 20, 2013<br>
 * 
 */
public class FlowConservingBottleneck extends RoadObjectController {

    private final Inhomogeneity inhomogeneity;

    private final double endPosition;

    private final Predicate<Vehicle> vehiclesWithRange;

    private final SignalPoint endSignalPoint;

    public FlowConservingBottleneck(Inhomogeneity inhomogeneity, RoadSegment roadSegment) {
        super(RoadObjectType.FLOW_CONSERVING_BOTTLENECK, inhomogeneity.getPosition(), roadSegment);
        this.inhomogeneity = inhomogeneity;
        this.endPosition = inhomogeneity.isSetValidLength() ? position + inhomogeneity.getValidLength() : roadSegment
                .roadLength();
        if (endPosition > roadSegment().roadLength()) {
            throw new IllegalArgumentException(
                    "FlowConservingBottleneckController can only be applied to a single roadSegment, but endPosition="
                            + endPosition + " is larger than road=" + roadSegment().userId());
        }
        this.vehiclesWithRange = new VehicleWithinRange(position, endPosition);
        endSignalPoint = new SignalPoint(endPosition, roadSegment);
    }

    public double endPosition() {
        return endPosition;
    }

    @Override
    public void createSignalPositions() {
        // needs only one signal at the end to reset all vehicles that have been influenced before.
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
            vehicle.inhomogeneityAdaptation().reset();
        }
    }

    private void apply(Vehicle vehicle) {
        double vehiclePosition = vehicle.getFrontPosition();
        assert vehiclePosition <= endPosition;
        if (vehiclePosition >= position + inhomogeneity.getAdaptationLength()) {
            vehicle.inhomogeneityAdaptation().setAlphaT(inhomogeneity.getTargetAlphaT());
            vehicle.inhomogeneityAdaptation().setAlphaV0(inhomogeneity.getTargetAlphaV0());
            return;
        }
        // interpolate between start and target quantity
        double relPosition = (vehiclePosition - position) / inhomogeneity.getAdaptationLength();
        assert relPosition >= 0 && relPosition <= 1 : "relPosition=" + relPosition;
        vehicle.inhomogeneityAdaptation().setAlphaT(
                interpolate(relPosition, inhomogeneity.getStartAlphaT(), inhomogeneity.getTargetAlphaT()));
        vehicle.inhomogeneityAdaptation().setAlphaV0(
                interpolate(relPosition, inhomogeneity.getStartAlphaV0(), inhomogeneity.getTargetAlphaV0()));
        LOG.debug("position={}, {}", vehiclePosition, vehicle.inhomogeneityAdaptation());
    }

    private static double interpolate(double x, double start, double target) {
        return (target - start) * x + start;
    }

}
