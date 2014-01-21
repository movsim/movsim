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

package org.movsim.simulator.roadnetwork.regulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.movsim.autogen.NotifyObjectType;
import org.movsim.autogen.RegulatorType;
import org.movsim.autogen.SignalType;
import org.movsim.autogen.TrafficLightStatus;
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.controller.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Regulator implements SimulationTimeStep {

    /** The Constant LOG. */
    protected static final Logger LOG = LoggerFactory.getLogger(Regulator.class);

    protected final RegulatorType parameter;

    protected final List<NotifyObject> notifyObjects = Lists.newLinkedList();

    protected final List<TrafficLight> trafficLights = Lists.newLinkedList();

    protected final Set<Vehicle> influencedVehicles = Sets.newHashSet();

    protected RegulatorFileLogging fileLogging = null;

    public static final Regulator create(RegulatorType regulatorParameter, RoadNetwork roadNetwork) {
        switch (regulatorParameter.getType()) {
        case ADAPTIVE_SPEED_LIMIT:
            return new AdaptiveSpeedLimit(regulatorParameter, roadNetwork);
        case COMMUNICATION:
            return new CommunicationControl(regulatorParameter, roadNetwork);
        case CONTROLLED_SECTION:
            return new ControlledSection(regulatorParameter, roadNetwork);
        case FERRY:
            return new Ferry(regulatorParameter, roadNetwork);
        case PARKING_DECK:
            return new ParkingDeck(regulatorParameter, roadNetwork);
        default:
            throw new IllegalArgumentException("cannot create regulator type=" + regulatorParameter.getType());
        }
    }

    protected Regulator(RegulatorType regulatorType, RoadNetwork roadNetwork) {
        this.parameter = Preconditions.checkNotNull(regulatorType);
        Preconditions.checkNotNull(roadNetwork);
        initializeNotifyObjects(roadNetwork);
        initializeTrafficLights(roadNetwork);
        if (regulatorType.isLogging()) {
            initFileLogger();
        }
    }

    private void initFileLogger() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProjectMetaData.getInstance().getProjectName());
        sb.append(".regulator_").append(parameter.getType().toString());
        sb.append(".id_").append(parameter.getId());
        sb.append(".csv");
        File file = new File(ProjectMetaData.getInstance().getPathToProjectFile(), sb.toString());
        try {
            fileLogging = new RegulatorFileLogging(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initializeTrafficLights(RoadNetwork roadNetwork) {
        for (SignalType signalType : parameter.getSignal()) {
            TrafficLight trafficLight = findTrafficSignal(signalType.getSignalId(), roadNetwork);
            if (trafficLight == null) {
                throw new IllegalArgumentException("cannot find TrafficLight with signalId=" + signalType.getSignalId()
                        + "in roadNetwork");
            }
            trafficLight.setState(TrafficLightStatus.GREEN);
            trafficLights.add(trafficLight);
        }
    }

    @CheckForNull
    private static TrafficLight findTrafficSignal(String signalId, RoadNetwork roadNetwork) {
        for (RoadSegment roadSegment : roadNetwork) {
            for (TrafficLight trafficLight : roadSegment.trafficLights()) {
                if (trafficLight.signalId().equals(signalId)) {
                    return trafficLight;
                }
            }
        }
        return null;
    }

    private void initializeNotifyObjects(RoadNetwork roadNetwork) {
        for (NotifyObjectType notifyObjectType : parameter.getNotifyObject()) {
            RoadSegment roadSegment = roadNetwork.findByUserId(notifyObjectType.getRoadId());
            if (roadSegment == null) {
                throw new IllegalArgumentException("cannot find road with userId=" + notifyObjectType.getRoadId());
            }
            NotifyObject notifyObject = new NotifyObject(notifyObjectType, roadSegment);
            notifyObjects.add(notifyObject);
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // LOG.info("update regulator at time={}, controlled trafficLights={}", simulationTime, trafficLights.size());
        // for (NotifyObject notifyObject : notifyObjects) {
        // LOG.info("notifyObject={}: passedVehicles={}", notifyObject, notifyObject.getPassedVehicles().size());
        // }

        // just dummy tests
        controllTrafficLightsDummy(iterationCount);
        // controllVehicles(iterationCount);
        // LOG.info("influencedVehicles.size={}", influencedVehicles.size());
    }

    private void controllVehicles(long iterationCount) {
        if (iterationCount % 200 == 0) {
            letVehiclesContinue();
        } else {
            letVehiclesStopAndPermitLaneChanges();
        }
    }

    private void letVehiclesContinue() {
        for (Vehicle vehicle : influencedVehicles) {
            vehicle.unsetExternalAcceleration();
            // vehicle.getLaneChangeModel().setConsiderLaneChanges(true);
            // vehicle.getLaneChangeModel().setConsiderDiscretionaryLaneChanges(true);
            vehicle.getLaneChangeModel().unsetMandatoryChangeToRestrictedLane();
        }
        influencedVehicles.clear();
    }

    private void letVehiclesStopAndPermitLaneChanges() {
        for (NotifyObject notifyObject : notifyObjects) {
            if (!notifyObject.getId().equals("suppressLaneChanges")) {
                for (Vehicle vehicle : notifyObject.getPassedVehicles()) {
                    vehicle.setExternalAcceleration(-1.0);
                    vehicle.getLaneChangeModel().setConsiderLaneChanges(false);
                    influencedVehicles.add(vehicle);
                }
            }
        }
    }

    private void controllTrafficLightsDummy(long iterationCount) {
        if (iterationCount != 0 && iterationCount % 1000 == 0) {
            for (TrafficLight trafficLight : trafficLights) {
                trafficLight.setState(trafficLight.status() == TrafficLightStatus.GREEN ? TrafficLightStatus.RED
                        : TrafficLightStatus.GREEN);
            }
        }
    }

    @SuppressWarnings("static-method")
    public void simulationCompleted(double simulationTime) {
        LOG.info("simulation completed at={}", simulationTime);
    }

    public String getName() {
        return parameter.isSetName() ? parameter.getName() : "-";
    }

    public String getId() {
        return parameter.isSetId() ? parameter.getId() : "-";
    }

    public Iterable<NotifyObject> getNotifyObjects() {
        return Iterables.unmodifiableIterable(notifyObjects);
    }

}
