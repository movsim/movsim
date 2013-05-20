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

import java.util.HashSet;
import java.util.Set;

import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.vehicles.Vehicle;

public class VariableMessageSignDiversion extends RoadObjectController {

    private boolean diversionActive = false; // also set in viewer !!

    private Set<Vehicle> controlledVehicles = new HashSet<>();
    
    // TODO set SignalPoints in RoadNetwork ... needs iterating over them all
    private final SignalPoint begin;
    private final SignalPoint end;
    

    public VariableMessageSignDiversion(double position, double validLength, RoadSegment roadSegment) {
        super(RoadObjectType.VMS_DIVERSION, position, roadSegment);
        begin = new SignalPoint(position, roadSegment);

        // FIXME hack here, use valid length instead and put SP on *all* roadSegments
        RoadSegment sinkRoadSegment = roadSegment().sinkRoadSegment(1);
        end = new SignalPoint(sinkRoadSegment.roadLength(), sinkRoadSegment);
    }

    @Override
    public void createSignalPositions() {
        roadSegment.signalPoints().add(begin);
        RoadSegment sinkRoadSegment = roadSegment().sinkRoadSegment(1);
        sinkRoadSegment.signalPoints().add(end);
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        LOG.debug("VMS isActive={}, controlledVehicles.size={}", diversionActive, controlledVehicles.size());
        LOG.debug("VMS vehiclesPassedBegin={}, vehiclesPassedEnd={}", begin.passedVehicles().size(), end
                .passedVehicles().size());
        if (diversionActive) {
            for (Vehicle vehicle : begin.passedVehicles()) {
                // apply only to vehicles in most right lane!
                if (vehicle.lane() == roadSegment.laneCount()) {
                    final LaneSegment laneSegment = roadSegment.laneSegment(Lanes.LANE1);
                    vehicle.setExitRoadSegmentId(laneSegment.sinkLaneSegment().roadSegment().id());
                    controlledVehicles.add(vehicle);
                    LOG.debug("set exitRoadSegmentId to vehicle={}", vehicle);
                }
            }
        } else {
            for (Vehicle vehicle : controlledVehicles) {
                vehicle.setExitRoadSegmentId(Vehicle.ROAD_SEGMENT_ID_NOT_SET);
            }
        }

        for (Vehicle vehicle : end.passedVehicles()) {
            controlledVehicles.remove(vehicle);
        }

        if (controlledVehicles.size() > 100) {
            // precautionary measure: check if removing mechanism is working proplery
            LOG.warn("Danger of memory leak: controlledVehicles.size={}", controlledVehicles.size());
        }
    }

    public void activateDiversion(boolean diversionActive) {
        this.diversionActive = diversionActive;
    }
}
