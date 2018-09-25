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

import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.Lanes.Type;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.vehicles.Vehicle;

import java.util.HashSet;
import java.util.Set;

// Tricky modeling: decision making may take a while until exit will be reached.
// Assignment of exit decision at cross-sections (via SignalPoints) produces most reasonale behavior in routing game.
// Note that the "VMS" model is not general but very specific for the routing game.
public class VariableMessageSignDiversion extends RoadObjectController {

    private boolean diversionActive = false; // also set in viewer !!

    private Set<Vehicle> controlledVehicles = new HashSet<>();

    private final double validLength;
    private final SignalPoint begin;
    private SignalPoint end;
    private RoadSegment roadSegmentEnd;

    public VariableMessageSignDiversion(double position, double validLength, RoadSegment roadSegment) {
        super(RoadObjectType.VMS_DIVERSION, position, roadSegment);
        this.validLength = validLength;
        begin = new SignalPoint(position, roadSegment);
    }

    @Override
    public void createSignalPositions() {
        roadSegment.signalPoints().add(begin);
        // non-local roadSegment needs fully inialized roadNetwork
        roadSegmentEnd = getRoadSegmentToPlaceEndPoint(position, validLength);
        if (roadSegmentEnd.laneType(roadSegmentEnd.laneCount()) != Type.EXIT) {
            throw new IllegalArgumentException(
                    "end of VariableMessageSignDiversion lies on roadSegment " + roadSegmentEnd.userId()
                            + " without exit lane!");
        }
        end = new SignalPoint(roadSegmentEnd.roadLength(), roadSegmentEnd);
        roadSegmentEnd.signalPoints().add(end);
    }

    private RoadSegment getRoadSegmentToPlaceEndPoint(double position, double validLength) {
        if (position + validLength < roadSegment.roadLength()) {
            return roadSegment;  // downstream end lies on same roadSegment
        }

        // downstream end lies on next downstream roadSegment
        RoadSegment downstreamRoadSegment = roadSegment.sinkRoadSegment(Lanes.MOST_INNER_LANE);
        for (LaneSegment laneSegment : roadSegment.laneSegments()) {
            if (laneSegment.hasSinkLaneSegment()
                    && laneSegment.sinkLaneSegment().roadSegment() != downstreamRoadSegment) {
                throw new IllegalArgumentException(
                        "downstream end of VariableMessageSignDiversion from RoadSegment=" + roadSegment.userId()
                                + " not unique. VMS model not intended for this case.");
            }
        }

        if (validLength - roadSegment.roadLength() + position > downstreamRoadSegment.roadLength()) {
            throw new IllegalArgumentException(
                    "valid length exceeds the roadlength of the downstream roadSegment=" + downstreamRoadSegment
                            .roadLength());
        }
        return downstreamRoadSegment;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        LOG.debug("VMS isActive={}, controlledVehicles.size={}", diversionActive, controlledVehicles.size());
        LOG.debug("VMS vehiclesPassedBegin={}, vehiclesPassedEnd={}", begin.passedVehicles().size(),
                end.passedVehicles().size());

        if (diversionActive) {
            for (Vehicle vehicle : begin.passedVehicles()) {
                // apply only to vehicles not in most left lane!
                if (vehicle.lane() != Lanes.MOST_INNER_LANE) {
                    vehicle.setExitRoadSegmentId(roadSegmentEnd.id());
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
            vehicle.setExitRoadSegmentId(Vehicle.ROAD_SEGMENT_ID_NOT_SET); // reset
            controlledVehicles.remove(vehicle);
        }

        if (controlledVehicles.size() > 200) {
            // precautionary measure: check if removing mechanism is working proplery
            LOG.warn("Check possible memory leak: controlledVehicles.size={}", controlledVehicles.size());
        }
    }

    public void toogleActiveStatus() {
        this.diversionActive = !this.diversionActive;
    }
}
