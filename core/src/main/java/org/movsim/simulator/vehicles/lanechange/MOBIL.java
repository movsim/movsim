/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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
package org.movsim.simulator.vehicles.lanechange;

import org.movsim.input.model.vehicle.lanechange.LaneChangeMobilData;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.Lane;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;

/**
 * The Class MOBIL.
 */
public class MOBIL {

    private double politeness;

    /** changing threshold */
    private double threshold;

    /** maximum safe braking decel */
    private double bSafe;

    /** minimum safe (net) distance */
    private double gapMin;

    /** bias (m/s^2) to drive */
    private double biasRight;

    private double thresholdRef;

    private double biasRightRef;

    private double bSafeRef;

    private double pRef;

    private final Vehicle me;

    /**
     * Instantiates a new MOBIL.
     * 
     * @param vehicle
     *            the vehicle
     */
    public MOBIL(Vehicle vehicle) {
        this.me = vehicle;
        // TODO handle this case with *no* <MOBIL> xml element
    }

    /**
     * Instantiates a new MOBIL.
     * 
     * @param vehicle
     *            the vehicle
     * @param lcMobilData
     *            the lane change MOBIL data
     */
    public MOBIL(Vehicle vehicle, LaneChangeMobilData lcMobilData) {
        this.me = vehicle;
        bSafeRef = bSafe = lcMobilData.getSafeDeceleration();
        biasRightRef = biasRight = lcMobilData.getRightBiasAcceleration();
        gapMin = lcMobilData.getMinimumGap();
        thresholdRef = threshold = lcMobilData.getThresholdAcceleration();
        pRef = politeness = lcMobilData.getPoliteness();
    }

    public MOBIL(Vehicle vehicle, double minimumGap, double safeDeceleration, double politeness,
            double thresholdAcceleration, double rightBiasAcceleration) {
        this.me = vehicle;
        bSafeRef = bSafe = safeDeceleration;
        biasRightRef = biasRight = rightBiasAcceleration;
        gapMin = minimumGap;
        thresholdRef = threshold = thresholdAcceleration;
        pRef = this.politeness = politeness;
    }

    private boolean safetyCheckAcceleration(double acc) {
        return acc <= -bSafe;
    }

    public double calcAccelerationBalance(int direction, RoadSegment roadSegment) {

        // set prospectiveBalance to large negative to indicate no lane change when not safe
        double prospectiveBalance = -Double.MAX_VALUE;
        final int currentLane = me.getLane();
        final int newLane = currentLane + direction;
        final LaneSegment newLaneSegment = roadSegment.laneSegment(newLane);
        if ((newLaneSegment.type() == Lane.Type.ENTRANCE)) {
            // never change lane into an entrance lane except
            return prospectiveBalance;
        }

        final Vehicle newFront = newLaneSegment.frontVehicle(me);
        if (newFront != null) {
            if (newFront.inProcessOfLaneChange()) {
                return prospectiveBalance;
            }
            final double gapFront = me.getNetDistance(newFront);
            if (gapFront < gapMin) {
                return prospectiveBalance;
            }
        }
        final Vehicle newBack = newLaneSegment.rearVehicle(me);
        if (newBack != null) {
            if (newBack.inProcessOfLaneChange()) {
                return prospectiveBalance;
            }
            final double gapRear = newBack.getNetDistance(me);
            if (gapRear < gapMin) {
                return prospectiveBalance;
            }
        }
        final LaneSegment currentLaneSegment = roadSegment.laneSegment(currentLane);
        final Vehicle oldFront = currentLaneSegment.frontVehicle(me);
        if (oldFront != null) {
            if (oldFront.inProcessOfLaneChange()) {
                return prospectiveBalance;
            }
        }

        // new situation: newBack with me as leader and following left lane cases
        // TO_LEFT --> just the actual situation
        // TO_RIGHT --> consideration of left-lane (with me's leader) has no effect
        // temporarily add the current vehicle to the new lane to calculate the new accelerations
        me.setLane(newLane);
        final int index = newLaneSegment.addVehicleTemp(me);
        final double newBackNewAcc = newBack == null ? 0 : newBack.calcAccModel(newLaneSegment, null);
        final double meNewAcc = me.calcAccModel(newLaneSegment, null);
        newLaneSegment.removeVehicle(index);
        me.setLane(currentLane);

        if (safetyCheckAcceleration(newBackNewAcc)) {
            return prospectiveBalance;
        }

        // check now incentive criterion
        // consider three vehicles: me, oldBack, newBack

        // old situation for me
        final double meOldAcc = me.calcAccModel(currentLaneSegment, null);

        // old situation for old back
        // in old situation same left lane as me
        final Vehicle oldBack = currentLaneSegment.rearVehicle(me);
        final double oldBackOldAcc = (oldBack != null) ? oldBack.calcAccModel(currentLaneSegment, null) : 0.0;

        // old situation for new back: just provides the actual left-lane situation
        final double newBackOldAcc = (newBack != null) ? newBack.calcAccModel(newLaneSegment, null) : 0.0;

        // new situation for new back:
        final double oldBackNewAcc;
        if (oldBack == null) {
            oldBackNewAcc = 0.0;
        } else {
            // cannot temporarily remove the current vehicle from the current lane, since we are in a loop
            // that iterates over the vehicles in the current lane. So calculate oldBackNewAcc based on just
            // the front vehicle.
            if (currentLaneSegment.frontVehicle(me) != null) { // TODO remove quickhack for avoiding nullpointer
                oldBackNewAcc = oldBack.getLongitudinalModel().calcAcc(oldBack, currentLaneSegment.frontVehicle(me));
            } else {
                oldBackNewAcc = 0.0;
            }

            // currentLaneSegment.removeVehicle(me);
            // oldBackNewAcc = oldBack.calcAccModel(currentLaneSegment, null);
            // currentLaneSegment.addVehicle(me);
        }

        // MOBIL trade-off for driver and neighborhood
        final double oldBackDiffAcc = oldBackNewAcc - oldBackOldAcc;
        final double newBackDiffAcc = newBackNewAcc - newBackOldAcc;
        final double meDiffAcc = meNewAcc - meOldAcc;

        final int changeTo = newLaneSegment.lane() - currentLaneSegment.lane();

        // hack for CCS
        if (me.getLongitudinalModel().modelName() == ModelName.CCS) {
            double biasForced = 10000;
            double biasNormal = 0.3;
            double bias;
            final int laneCount = roadSegment.laneCount();

            if (roadSegment.laneSegment(currentLane).type() == Lane.Type.ENTRANCE) {
                double factor = (currentLane > 0.5 * (laneCount - 1)) ? (laneCount - currentLane) : (currentLane + 1);
                // System.out.println("currentLane: " + currentLane + " factor*biasForced=" + factor * biasForced);
                return biasForced * factor;
            }

            // assume increasing lane index from right to left
            bias = +2 * biasNormal / (laneCount - 1) * (currentLane - (0.5 * (laneCount - 1)));
            
            prospectiveBalance = meDiffAcc + politeness * (oldBackDiffAcc + newBackDiffAcc) - threshold - bias
                    * direction;

            return prospectiveBalance; // quick hack ends here
        }

        // MOBIL's incentive formula

        final int biasSign = (changeTo == MovsimConstants.TO_LEFT) ? 1 : -1;

        prospectiveBalance = meDiffAcc + politeness * (oldBackDiffAcc + newBackDiffAcc) - threshold - biasSign
                * biasRight;

        return prospectiveBalance;
    }

    public double getMinimumGap() {
        return gapMin;
    }

    public double getSafeDeceleration() {
        return bSafe;
    }
}
