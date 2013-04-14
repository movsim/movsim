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

import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.vehicles.TestVehicle;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple onramp model which drops vehicles in the largest gap on the {@link RoadSegment}.
 * 
 * Ignores the initial speed settings from input.
 * 
 */
public class SimpleRamp extends AbstractTrafficSource {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleRamp.class);

    private static final double MINIMUM_GAP_BOUNDARY = 3;

    private final double relativeGapToLeader;

    private final double relativeSpeedToLeader;

    private final InflowTimeSeries inflowTimeSeries;

    public SimpleRamp(TrafficCompositionGenerator vehGenerator, RoadSegment roadSegment,
            org.movsim.autogen.SimpleRamp simpleRampData,
            InflowTimeSeries inflowTimeSeries) {
        super(vehGenerator, roadSegment);
        this.inflowTimeSeries = inflowTimeSeries;
        this.relativeSpeedToLeader = simpleRampData.getRelativeSpeed();
        this.relativeGapToLeader = simpleRampData.getRelativeGap();

    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("simple ramp timestep=%.2f, current inflow=%d, waiting vehicles=%d",
                    simulationTime, +(int) (getTotalInflow(simulationTime) * Units.INVS_TO_INVH), getQueueLength()));
        }

        final double totalInflow = getTotalInflow(simulationTime);
        nWait += totalInflow * dt;

        calcApproximateInflow(dt);

        if (nWait >= 1.0) {
            // try to insert vehicle
            final TestVehicle testVehicle = vehGenerator.getTestVehicle();
            SortedSet<GapCandidate> gapCandidates = findLargestPossibleGap(testVehicle); // only one insert per timestep

            if (!gapCandidates.isEmpty()) {
                GapCandidate gap = gapCandidates.first();
                addVehicle(roadSegment.laneSegment(gap.laneIndex), testVehicle, gap.enterPosition, gap.enterSpeed);
                // TODO testwise adding, check for accidents
                nWait--;
                incrementInflowCount(1);
                recordData(simulationTime, totalInflow);
            }
        }
    }

    private SortedSet<GapCandidate> findLargestPossibleGap(TestVehicle testVehicle) {
        SortedSet<GapCandidate> gapCandidates = new TreeSet<>();

        for (LaneSegment laneSegment : roadSegment.laneSegments()) {
            for (Vehicle vehicle : laneSegment) {
                evaluateVehicle(vehicle, laneSegment, testVehicle, gapCandidates);
            }

            // check also rear vehicles of next downstream segment
            Vehicle rearVehicleNextLaneSegment = laneSegment.sinkLaneSegment().rearVehicle();
            if (rearVehicleNextLaneSegment != null) {
                evaluateVehicle(rearVehicleNextLaneSegment, laneSegment, testVehicle, gapCandidates);
            }
            if (rearVehicleNextLaneSegment == null && laneSegment.vehicleCountWithoutObstacles() == 0) {
                gapCandidates.add(new GapCandidate(MovsimConstants.GAP_INFINITY, laneSegment.lane(), 0.5
                        * roadSegment.roadLength() - testVehicle.length(), testVehicle.getRelativeRandomizationV0()));
            }
        }

        // if (false) {
        // int counter = 0;
        // System.out.println("gap candidated size=" + gapCandidates.size());
        // for (GapCandidate gap : gapCandidates) {
        // ++counter;
        // System.out.println("candidate " + counter + ":" + gap.toString());
        // }
        // }

        return gapCandidates;

    }

    private void evaluateVehicle(Vehicle vehicle, LaneSegment laneSegment, TestVehicle testVehicle,
            SortedSet<GapCandidate> gapCandidates) {
        if (vehicle.getRearPosition() < testVehicle.length() + MINIMUM_GAP_BOUNDARY) {
            // available upstream road segment too small
            LOG.debug("no sufficient upstream gap: rearPosition={}", vehicle.getRearPosition());
            return;
        }
        Vehicle rearVehicle = laneSegment.rearVehicle(vehicle.getRearPosition() - 1); // TODO finds not rear vehicle but
                                                                                      // itself !!!
                                                                                      // if (rearVehicle == vehicle) {
        // System.out.println("!!! rear vehicle is identical to vehicle!");
        // }
        final double gap = vehicle.getNetDistanceToRearVehicle(rearVehicle);
        if (gap < testVehicle.length() + 2 * MINIMUM_GAP_BOUNDARY) {
            // gap too small
            // System.out.println("gap too small, gap=" + gap + ", vehicle=" + vehicle.toString() + ", rearVehicle="
            // + rearVehicle.toString());
            return;
        }

        double enterFrontPosition = Math.max(testVehicle.length(), vehicle.getRearPosition() - relativeGapToLeader * gap
                + 0.5 * testVehicle.length());
        final double gapToLeader = vehicle.getRearPosition() - enterFrontPosition;
        double speed = relativeSpeedToLeader * vehicle.getSpeed();
        gapCandidates.add(new GapCandidate(gapToLeader, laneSegment.lane(), enterFrontPosition, speed));
    }

    @Override
    public double getTotalInflow(double time) {
        return inflowTimeSeries.getFlowPerLane(time) * roadSegment.laneCount();
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

            // gaps identical, compare lanes, prefer small laneIndex index (i.e. laneIndex to the right)
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