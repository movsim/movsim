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

import org.movsim.output.FileDetector;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.vehicles.Vehicle;

// TODO refactoring needed, add unittests
public class LoopDetector extends RoadObjectController {

    private final double dtSample;

    private double timeOffset;

    private double meanSpeedAllLanes;

    /** vehicle count per update aggregation cycle. */
    private int vehCountOutputAllLanes;

    /** Cumulated vehicle count. */
    private long vehCumulatedCountOutputAllLanes;

    private double occupancyAllLanes;

    private double meanSpeedHarmonicAllLanes;

    private double meanTimegapHarmonicAllLanes;

    private final LaneQuantity[] laneQuantities;

    private final FileDetector fileDetector;

    private final SignalPoint crossSectionSignalPoint;

    /**
     * Constructor
     * 
     * @param roadSegment
     * @param detPosition
     * @param dtSample
     * @param logging
     * @param loggingLanes
     */
    public LoopDetector(RoadSegment roadSegment, double detPosition, double dtSample, boolean logging,
            boolean loggingLanes) {
        super(RoadObjectType.LOOPDETECTOR, detPosition, roadSegment);
        this.dtSample = dtSample;

        final int laneCount = roadSegment.laneCount();

        laneQuantities = new LaneQuantity[laneCount];
        for (int i = 0; i < laneCount; i++) {
            laneQuantities[i] = new LaneQuantity();
        }

        vehCumulatedCountOutputAllLanes = 0;

        timeOffset = 0;

        resetLaneAverages();

        fileDetector = (logging) ? new FileDetector(this, roadSegment.userId(), roadSegment.laneCount(), loggingLanes)
                : null;
        if (fileDetector != null) {
            fileDetector.writeAggregatedData(0);
        }
        crossSectionSignalPoint = new SignalPoint(position, roadSegment);
    }

    @Override
    public void createSignalPositions() {
        roadSegment.signalPoints().add(crossSectionSignalPoint);
    }

    private void resetLaneAverages() {
        meanSpeedAllLanes = 0;
        vehCountOutputAllLanes = 0;
        occupancyAllLanes = 0;
        meanSpeedHarmonicAllLanes = 0;
        meanTimegapHarmonicAllLanes = 0;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        for (Vehicle vehicle : crossSectionSignalPoint.passedVehicles()) {
            countVehiclesAndDataForLane(vehicle);
        }

        if ((simulationTime - timeOffset + MovsimConstants.SMALL_VALUE) >= dtSample) {
            for (LaneQuantity laneQuantity : laneQuantities) {
                laneQuantity.calculateAverageForLane(dtSample);
            }
            calculateAveragesOverAllLanes();
            if (fileDetector != null) {
                fileDetector.writeAggregatedData(simulationTime);
            }
            timeOffset = simulationTime;
        }
    }

    /**
     * @param laneSegment
     * @param laneIndex
     *            (lane-1)
     * @param veh
     */
    private void countVehiclesAndDataForLane(Vehicle veh) {
        int laneIndex = veh.lane() - Lanes.MOST_INNER_LANE;
        LaneQuantity laneQuantity = laneQuantities[laneIndex];
        laneQuantity.vehCount++;
        laneQuantity.vehCumulatedCountOutput++;
        double speedVeh = veh.getSpeed();
        laneQuantity.vSum += speedVeh;
        laneQuantity.occTime += (speedVeh > 0) ? veh.getLength() / speedVeh : 0;
        laneQuantity.sumInvV += (speedVeh > 0) ? 1. / speedVeh : 0;
        // brut timegap not calculate from local detector data:
        Vehicle vehFront = roadSegment.frontVehicleOnLane(veh);
        double brutTimegap = (vehFront == null) ? 0 : veh.getBrutDistance(vehFront) / vehFront.getSpeed();
        // "microscopic flow"
        laneQuantity.sumInvQ += (brutTimegap > 0) ? 1. / brutTimegap : 0;
    }

    private void calculateAveragesOverAllLanes() {
        resetLaneAverages();
        for (int i = 0; i < roadSegment().laneCount(); i++) {
            // vehicle count is extensive quantity
            vehCountOutputAllLanes += getVehCountOutput(i);
            // intensive quantities as averages weighted by vehicle counts
            meanSpeedAllLanes += getVehCountOutput(i) * getMeanSpeed(i);
            occupancyAllLanes += getOccupancy(i);
            meanSpeedHarmonicAllLanes += getVehCountOutput(i) * getMeanSpeedHarmonic(i);
            meanTimegapHarmonicAllLanes += getVehCountOutput(i) * getMeanTimegapHarmonic(i);
        }

        vehCumulatedCountOutputAllLanes += vehCountOutputAllLanes;
        meanSpeedAllLanes = vehCountOutputAllLanes == 0 ? 0 : meanSpeedAllLanes / vehCountOutputAllLanes;
        meanSpeedHarmonicAllLanes = vehCountOutputAllLanes == 0 ? 0 : meanSpeedHarmonicAllLanes
                / vehCountOutputAllLanes;
        meanTimegapHarmonicAllLanes = vehCountOutputAllLanes == 0 ? 0 : meanTimegapHarmonicAllLanes
                / vehCountOutputAllLanes;
        occupancyAllLanes /= roadSegment().laneCount();
    }

    public double getDensityArithmetic(int i) {
        return laneQuantities[i].meanSpeed == 0 ? 0 : getFlow(i) / laneQuantities[i].meanSpeed;
    }

    public double getDensityArithmeticAllLanes() {
        return (Double.compare(meanSpeedAllLanes, 0) == 0) ? 0 : getFlowAllLanes() / meanSpeedAllLanes;
    }

    public double getDtSample() {
        return dtSample;
    }

    public double getFlow(int i) {
        return laneQuantities[i].vehCountOutput / dtSample;
    }

    public double getMeanSpeed(int i) {
        return laneQuantities[i].meanSpeed;
    }

    public double getOccupancy(int i) {
        return laneQuantities[i].occupancy;
    }

    public int getVehCountOutput(int i) {
        return laneQuantities[i].vehCountOutput;
    }

    public double getMeanSpeedHarmonic(int i) {
        return laneQuantities[i].meanSpeedHarmonic;
    }

    public double getMeanTimegapHarmonic(int i) {
        return laneQuantities[i].meanTimegapHarmonic;
    }

    public long getVehCumulatedCountOutput(int i) {
        return laneQuantities[i].vehCumulatedCountOutput;
    }

    public double getFlowAllLanes() {
        return vehCountOutputAllLanes / (dtSample * roadSegment().laneCount());
    }

    public double getMeanSpeedAllLanes() {
        return meanSpeedAllLanes;
    }

    public int getVehCountOutputAllLanes() {
        return vehCountOutputAllLanes;
    }

    public double getOccupancyAllLanes() {
        return occupancyAllLanes;
    }

    public double getMeanSpeedHarmonicAllLanes() {
        return meanSpeedHarmonicAllLanes;
    }

    public double getMeanTimegapHarmonicAllLanes() {
        return meanTimegapHarmonicAllLanes;
    }

    public long getVehCumulatedCountOutputAllLanes() {
        return vehCumulatedCountOutputAllLanes;
    }

    private static final class LaneQuantity {
        int vehCount;
        double vSum;
        double occTime;
        double sumInvV;
        double sumInvQ;
        double meanSpeed;
        double occupancy;
        int vehCountOutput;
        long vehCumulatedCountOutput;
        double meanSpeedHarmonic;
        double meanTimegapHarmonic;

        void reset() {
            vehCount = 0;
            vSum = 0;
            occTime = 0;
            sumInvQ = 0;
            sumInvV = 0;
        }

        void calculateAverageForLane(double dtSample) {
            meanSpeed = (vehCount == 0) ? 0 : vSum / vehCount;
            occupancy = occTime / dtSample;
            vehCountOutput = vehCount;
            vehCumulatedCountOutput += vehCount;
            meanSpeedHarmonic = (vehCount == 0) ? 0 : 1. / (sumInvV / vehCount);
            meanTimegapHarmonic = (vehCount == 0) ? 0 : sumInvQ / vehCount;
            reset();
        }

    }
}
