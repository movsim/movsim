/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.output;

import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.impl.ObservableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LoopDetector.
 */
public class LoopDetector extends ObservableImpl {

    final static Logger logger = LoggerFactory.getLogger(LoopDetector.class);

    private final double dtSample;
    private final double detPosition;
    private double timeOffset;
    private int[] vehCount;
    private double[] vSum;
    private double[] occTime;
    private double[] sumInvV;
    private double[] sumInvQ;
    private double[] meanSpeed;
    private double[] densityArithmetic;
    private double[] flow;
    private double[] occupancy;
    private int[] vehCountOutput;
    private double[] meanSpeedHarmonic;
    private double[] meanTimegapHarmonic;
    private int laneCount;

    /**
     * Instantiates a new loop detector.
     * 
     * @param detPosition
     *            the det position
     * @param dtSample
     *            the dt sample
     * @param laneCount
     */
    public LoopDetector(double detPosition, double dtSample, int laneCount) {
        this.detPosition = detPosition;
        this.dtSample = dtSample;
        this.laneCount = laneCount;

        vehCount = new int[laneCount];
        vSum = new double[laneCount];
        occTime = new double[laneCount];
        sumInvQ = new double[laneCount];
        sumInvV = new double[laneCount];

        meanSpeed = new double[laneCount];
        densityArithmetic = new double[laneCount];
        flow = new double[laneCount];
        occupancy = new double[laneCount];
        vehCountOutput = new int[laneCount];
        meanSpeedHarmonic = new double[laneCount];
        meanTimegapHarmonic = new double[laneCount];

        timeOffset = 0;
        reset();

        notifyObservers(0);
    }

    /**
     * Reset.
     */
    private void reset() {
        for (int i = 0; i < laneCount; i++) {
            vehCount[i] = 0;
            vSum[i] = 0;
            occTime[i] = 0;
            sumInvQ[i] = 0;
            sumInvV[i] = 0;
        }

    }

    private void reset(int lane) {
        vehCount[lane] = 0;
        vSum[lane] = 0;
        occTime[lane] = 0;
        sumInvQ[lane] = 0;
        sumInvV[lane] = 0;
    }

    /**
     * Update.
     * 
     * @param time
     *            the time
     * @param roadSegment
     */
    public void update(double simulationTime, RoadSegment roadSegment) {

        // brute force search: iterate over all lanes
        final int laneCount = roadSegment.laneCount();
        for (int lane = 0; lane < laneCount; ++lane) {
            final LaneSegment laneSegment = roadSegment.laneSegment(lane);
            for (final Vehicle veh : laneSegment) {
                if ((veh.getPositionOld() < detPosition) && (veh.getPosition() >= detPosition)) {
                    countVehiclesAndDataForLane(laneSegment, lane, veh);
                }
            }
        }

        if ((simulationTime - timeOffset + MovsimConstants.SMALL_VALUE) >= dtSample) {
            for (int lane = 0; lane < laneCount; ++lane) {
                calculateAveragesForLane(lane);
            }
            notifyObservers(simulationTime);
            timeOffset = simulationTime;
        }
    }

    /**
     * @param laneSegment
     * @param lane
     * @param veh
     */
    private void countVehiclesAndDataForLane(final LaneSegment laneSegment, int lane, final Vehicle veh) {
        // new vehicle crossed detector
        vehCount[lane]++;
        final double speedVeh = veh.getSpeed();
        vSum[lane] += speedVeh;
        occTime[lane] += veh.getLength() / speedVeh;
        sumInvV[lane] += (speedVeh > 0) ? 1. / speedVeh : 0;
        // calculate brut timegap not from local detector data:
        final Vehicle vehFront = laneSegment.frontVehicle(veh);
        final double brutTimegap = (vehFront == null) ? 0 : (vehFront.getPosition() - veh.getPosition())
                / vehFront.getSpeed();
        // microscopic flow
        sumInvQ[lane] += (brutTimegap > 0) ? 1. / brutTimegap : 0;
    }

    // ############################################

    /**
     * Calculate averages.
     * 
     * @param lane
     */
    private void calculateAveragesForLane(int lane) {
        flow[lane] = vehCount[lane] / dtSample;
        meanSpeed[lane] = (vehCount[lane] == 0) ? 0 : vSum[lane] / vehCount[lane];
        densityArithmetic[lane] = (vehCount[lane] == 0) ? 0 : flow[lane] / meanSpeed[lane];
        occupancy[lane] = occTime[lane] / dtSample;
        vehCountOutput[lane] = vehCount[lane];
        meanSpeedHarmonic[lane] = (vehCount[lane] == 0) ? 0 : 1. / (sumInvV[lane] / vehCount[lane]);
        meanTimegapHarmonic[lane] = (vehCount[lane] == 0) ? 0 : sumInvQ[lane] / vehCount[lane];
        reset(lane);
    }

    public double getDtSample() {
        return dtSample;
    }

    public double getDetPosition() {
        return detPosition;
    }

    public double getMeanSpeed(int i) {
        return meanSpeed[i];
    }

    public double getDensityArithmetic(int i) {
        return densityArithmetic[i];
    }

    public double getFlow(int i) {
        return flow[i];
    }

    public double getOccupancy(int i) {
        return occupancy[i];
    }

    public int getVehCountOutput(int i) {
        return vehCountOutput[i];
    }

    public double getMeanSpeedHarmonic(int i) {
        return meanSpeedHarmonic[i];
    }

    public double getMeanTimegapHarmonic(int i) {
        return meanTimegapHarmonic[i];
    }
}
