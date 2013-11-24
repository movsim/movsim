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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.movsim.autogen.TrafficLightStatus;
import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Controller;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Signals.Signal;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.roadnetwork.regulator.Regulator;
import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Represents a 'traffic light' to which vehicles will react. The visibility range is limited to {@code MAX_LOOK_AHEAD_DISTANCE}, e.g.
 * {@literal 1000m}, or to two {@link RoadSegment}s.
 * 
 */
public class TrafficLight extends RoadObjectController {

    public static final double MAX_LOOK_AHEAD_DISTANCE = 1000;

    /** The status. */
    private TrafficLightStatus status;

    private final String groupId; // unique mapping to infrastructure

    private TriggerCallback triggerCallback;

    private final Set<TrafficLightStatus> possibleStati = new HashSet<>(); // deprecated, just for drawing trafficlights in viewer

    private final Signal signal;
    private final Controller controller;
    private final String signalType;

    private final SignalPoint signalPointEnd;
    private final Map<RoadSegment, SignalPoint> signalPointsBegin = new HashMap<>();

    public TrafficLight(Signal signal, Controller controller, RoadSegment roadSegment) {
        super(RoadObjectType.TRAFFICLIGHT, signal.getS(), roadSegment);
        if (signal.isSetValidity()) {
            throw new IllegalArgumentException(
                    "trafficlights always apply to all lanes, cannot use xodr validity information from signal-id="
                            + signal.getId());
        }
        this.controller = Preconditions.checkNotNull(controller);
        this.signal = Preconditions.checkNotNull(signal);
        Preconditions.checkArgument(signal.isSetId(), "id not set");
        Preconditions.checkArgument(!signal.getId().isEmpty(), "empty id!");
        Preconditions.checkArgument(signal.isSetS(), "signal.s not set");
        this.signalType = Preconditions.checkNotNull(checkTypesAndExtractSignalType());
        this.groupId = controller.getId();
        signalPointEnd = new SignalPoint(position, roadSegment);
    }

    private String checkTypesAndExtractSignalType() {
        String signalType = null;
        for (OpenDRIVE.Controller.Control control : controller.getControl()) {
            if (!control.isSetType()) {
                throw new IllegalArgumentException("controller.control.type must be set in xodr for signal="
                        + signalId());
            }
            if (control.getSignalId().equals(signalId())) {
                signalType = control.getType();
            }
        }
        return signalType;
    }

    /**
     * Returns the id. This id is defined in the infrastructure configuration file.
     * 
     * @return the label
     */
    public String signalId() {
        return signal.getId();
    }

    /**
     * Returns the signal Name.
     * 
     * @return the label
     */
    public String signalName() {
        return signal.getName();
    }

    public Signal signal() {
        return signal;
    }

    /**
     * Returns the signal type assigned in the controller.control xodr input. This type links from a 'physical' signal to a 'logical'
     * representation of the trafficlight state.
     * 
     * @return the signal-type id
     */
    public String signalType() {
        return signalType;
    }

    public String controllerId() {
        return controller.getId();
    }

    public Controller getController() {
        return controller;
    }

    /**
     * Returns the controllergroup-id which allows for a unique reference to a set of signals in the infrastructure.
     * 
     * @return groupId
     */
    public String groupId() {
        return groupId;
    }

    public TrafficLightStatus status() {
        return status;
    }

    /**
     * sets the {@link TrafficLightStatus}.
     * 
     * <p>
     * The user has to assure that a single traffic light is not simultaneously controlled by the {@link TrafficLightController} and one (or
     * more) {@link Regulator}(s).
     * </p>
     * 
     * @param newStatus
     */
    public void setState(TrafficLightStatus newStatus) {
        this.status = newStatus;
    }

    // trigger from viewer via mouse-click (direct communication from signal to controller)
    // shouldn't be called from external, use controller instead
    public void triggerNextPhase() {
        if (hasTriggerCallback()) {
            LOG.debug("mouse click triggers next phase");
            triggerCallback.nextPhase();
        }
    }

    public void setTriggerCallback(TriggerCallback triggerCallback) {
        this.triggerCallback = Preconditions.checkNotNull(triggerCallback);
    }

    public boolean hasTriggerCallback() {
        return triggerCallback != null;
    }

    // not needed in future
    @Deprecated
    void addPossibleState(TrafficLightStatus status) {
        possibleStati.add(status);
    }

    /**
     * Return the number of lights this traffic light has, can be 1, 2 or 3.
     * 
     * @return
     */
    @Deprecated
    public static int lightCount() {
        // return Math.min(3, possibleStati.size());
        return 3;
    }

    @Override
    public String toString() {
        return "TrafficLight [controllerId = " + controllerId() + ", signalId = " + signalId() + ", status=" + status
                + ", position=" + position + ", signalType=" + signalType + ", groupId = " + groupId
                + ", roadSegment.id=" + ((roadSegment == null) ? "null" : roadSegment.userId()) + "]";
    }

    @Override
    public void createSignalPositions() {
        // downstream signal point is on local roadSegment
        roadSegment.signalPoints().add(signalPointEnd);
        LOG.info("trafficlight={}", this);
        LOG.info("trafficlight end signal point placed at position={} on roadSegment={}.", position, roadSegment);

        // create signal points for upstream signal points on potentially other roadsegments
        double upstreamPosition = position - MAX_LOOK_AHEAD_DISTANCE;
        if (upstreamPosition >= 0 || !roadSegment.hasUpstreamConnection()) {
            upstreamPosition = Math.max(0, upstreamPosition);
            addSignalPointBegin(upstreamPosition, roadSegment);
        } else {
            // put signal points to all upstream road segments while checking recursively
            Set<RoadSegment> visitedRoadSegments = Sets.newHashSet();
            double dxToGo = MAX_LOOK_AHEAD_DISTANCE - position;
            addSignalPointsToUpstreamRoadSegments(roadSegment, dxToGo, visitedRoadSegments);
        }

        if (signalPointsBegin.isEmpty()) {
            throw new IllegalStateException("did not set any upstream signal points for traffic light=" + toString());
        }

        // add created signals to roadSegments
        for (Entry<RoadSegment, SignalPoint> entry : signalPointsBegin.entrySet()) {
            entry.getKey().signalPoints().add(entry.getValue());
        }
    }

    private void addSignalPointBegin(double upstreamPosition, RoadSegment upstreamRoadSegment) {
        SignalPoint signalPoint = new SignalPoint(upstreamPosition, upstreamRoadSegment);
        signalPointsBegin.put(upstreamRoadSegment, signalPoint);
        LOG.info("trafficlight signal start point placed at position={} on roadSegment={}", upstreamPosition,
                upstreamRoadSegment);
    }

    private void addSignalPointsToUpstreamRoadSegments(RoadSegment startRoadSegment, double dxToGo,
            Set<RoadSegment> visitedRoadSegments) {
        for (LaneSegment laneSegment : startRoadSegment.laneSegments()) {
            if (laneSegment.hasSourceLaneSegment()) {
                RoadSegment upstreamRoadSegment = laneSegment.sourceLaneSegment().roadSegment();
                if (!visitedRoadSegments.contains(upstreamRoadSegment)) {
                    visitedRoadSegments.add(upstreamRoadSegment);
                    double posSignalPoint = upstreamRoadSegment.roadLength() - dxToGo;
                    if (posSignalPoint >= 0 || !upstreamRoadSegment.hasUpstreamConnection()) {
                        addSignalPointBegin(Math.max(0, posSignalPoint), upstreamRoadSegment);
                    } else {
                        // call recursively
                        addSignalPointsToUpstreamRoadSegments(upstreamRoadSegment, -posSignalPoint, visitedRoadSegments);
                    }
                }
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        for (SignalPoint signalPoint : signalPointsBegin.values()) {
            for (Vehicle vehicle : signalPoint.passedVehicles()) {
                vehicle.getTrafficLightApproaching().addTrafficLight(this);
                LOG.debug("vehicle pos={} --> set trafficlight={}", vehicle.getFrontPosition(), this);
            }
        }
        // Vehicle handles cleaning process for already passed trafficlights autonomously
    }

}
