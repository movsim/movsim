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

import java.util.SortedSet;
import java.util.TreeSet;

import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple onramp model which drops vehicles in the largest gap on the {@link RoadSegment}.
 * 
 * Ignores the initial speed settings from input.
 * 
 * @author kesting
 * 
 */
public class SimpleRamp extends AbstractTrafficSource {

    final static Logger logger = LoggerFactory.getLogger(SimpleRamp.class);

    private static final double MINIMUM_GAP_BOUNDARY = 3;

    private final double relativeGapToLeader;

    private final double relativeSpeedToLeader;

    public SimpleRamp(VehicleGenerator vehGenerator, RoadSegment roadSegment, SimpleRampData simpleRampData,
            InflowTimeSeries inflowTimeSeries) {
        super(vehGenerator, roadSegment, inflowTimeSeries);
        this.relativeSpeedToLeader = simpleRampData.getRelativeSpeedToLeader();
        this.relativeGapToLeader = simpleRampData.getRelativeGapToLeader();

    }

    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("simple ramp timestep=%.2f, current inflow=%d, waiting vehicles=%d",
                    simulationTime, +(int) (getTotalInflow(simulationTime) * Units.INVS_TO_INVH), getQueueLength()));
        }

        final double totalInflow = getTotalInflow(simulationTime);
        nWait += totalInflow * dt;

        if (nWait >= 1.0) {
            // try to insert vehicle
            final VehiclePrototype prototype = vehGenerator.getVehiclePrototype();
            SortedSet<GapCandidate> gapCandidates = findLargestPossibleGap(prototype); // only one insert per timestep

            if (!gapCandidates.isEmpty()) {
                GapCandidate gap = gapCandidates.first();
                addVehicle(roadSegment.laneSegment(gap.laneIndex), prototype, gap.enterPosition, gap.enterSpeed);
                // TODO testwise adding, check for accidents
                nWait--;
                recordData(simulationTime, totalInflow);
            }
        }
    }

    private SortedSet<GapCandidate> findLargestPossibleGap(VehiclePrototype prototype) {
        SortedSet<GapCandidate> gapCandidates = new TreeSet<GapCandidate>();

        for (int i = 0, nLane = roadSegment.laneCount(); i < nLane; i++) {
            LaneSegment laneSegment = roadSegment.laneSegment(i);
            for (Vehicle vehicle : laneSegment) {
                evaluateVehicle(vehicle, laneSegment, prototype, gapCandidates);
            }

            // check also rear vehicles of next downstream segment
            Vehicle rearVehicleNextLaneSegment = laneSegment.sinkLaneSegment().rearVehicle();
            if (rearVehicleNextLaneSegment != null) {
                evaluateVehicle(rearVehicleNextLaneSegment, laneSegment, prototype, gapCandidates);
            }
            if (rearVehicleNextLaneSegment == null && laneSegment.vehicleCount() == 0) {
                gapCandidates.add(new GapCandidate(MovsimConstants.GAP_INFINITY, laneSegment.lane(), 0.5
                        * roadSegment.roadLength() - prototype.length(), prototype.getRelativeRandomizationV0()));
            }
        }

        if (false) {
        int counter = 0;
        System.out.println("gap candidated size=" + gapCandidates.size());
        for (GapCandidate gap : gapCandidates) {
            ++counter;
            System.out.println("candidate " + counter + ":" + gap.toString());
        }
        }

        return gapCandidates;

    }

    private void evaluateVehicle(Vehicle vehicle, LaneSegment laneSegment, VehiclePrototype prototype,
            SortedSet<GapCandidate> gapCandidates) {
        if (vehicle.getRearPosition() < prototype.length() + MINIMUM_GAP_BOUNDARY) {
            // available upstream road segment too small
            System.out.println("no sufficient upstream gap: rearPosition=" + vehicle.getRearPosition());
            return;
        }
        Vehicle rearVehicle = laneSegment.rearVehicle(vehicle.getRearPosition() - 1); // TODO finds not rear vehicle but
                                                                                      // itself !!!
                                                                                      // if (rearVehicle == vehicle) {
        // System.out.println("!!! rear vehicle is identical to vehicle!");
        // }
        final double gap = vehicle.getNetDistanceToRearVehicle(rearVehicle);
        if (gap < prototype.length() + 2 * MINIMUM_GAP_BOUNDARY) {
            // gap too small
            // System.out.println("gap too small, gap=" + gap + ", vehicle=" + vehicle.toString() + ", rearVehicle="
            // + rearVehicle.toString());
            return;
        }

        double enterFrontPosition = Math.max(prototype.length(), vehicle.getRearPosition() - relativeGapToLeader * gap
                + 0.5 * prototype.length());
        final double gapToLeader = vehicle.getRearPosition() - enterFrontPosition;
        double speed = relativeSpeedToLeader * vehicle.getSpeed();
        gapCandidates.add(new GapCandidate(gapToLeader, laneSegment.lane(), enterFrontPosition, speed));
    }

    final class GapCandidate implements Comparable<GapCandidate> {

        final int laneIndex;
        final double gapToLeader;
        final double enterPosition;
        final double enterSpeed;

        public GapCandidate(double gapToLeader, int laneIndex, double enterPosition, double enterSpeed) {
            this.gapToLeader = gapToLeader;
            this.laneIndex = laneIndex;
            this.enterPosition = enterPosition;
            this.enterSpeed = enterSpeed;
        }

        @Override
        public int compareTo(GapCandidate o) {
            // BEFORE = -1, EQUAL = 0, AFTER = 1

            // same instance
            if (this == o) {
                return 0;
            }

            // compare gaps
            if (this.gapToLeader > o.gapToLeader) {
                return -1;
            } else if (this.gapToLeader < o.gapToLeader) {
                return 1;
            }

            // gaps identical, compare lanes, prefer small lane index (i.e. lane to the right)
            if (this.laneIndex < o.laneIndex) {
                return -1;
            } else if (this.laneIndex > o.laneIndex) {
                return 1;
            }

            return 0;
        }

        @Override
        public String toString() {
            return "GapCandidate [laneIndex=" + laneIndex + ", gapToLeader=" + gapToLeader + ", enterPosition="
                    + enterPosition + ", enterSpeed=" + enterSpeed + "]";
        }

    }

}