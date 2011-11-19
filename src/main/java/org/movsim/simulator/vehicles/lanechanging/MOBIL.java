/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.lanechanging;

import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadsegment.LaneSegment;
import org.movsim.simulator.roadsegment.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;

/**
 * The Class MOBIL.
 */
@SuppressWarnings("unused")
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
     * Instantiates a new mOBIL impl.
     *
     * @param vehicle the vehicle
     */
    public MOBIL(final Vehicle vehicle) {
        this.me = vehicle;
        // TODO handle this case with *no* <MOBIL> xml element

    }

    /**
     * Instantiates a new mOBIL impl.
     *
     * @param vehicle the vehicle
     * @param lcMobilData the lc mobil data
     */
    public MOBIL(final Vehicle vehicle, LaneChangingMobilData lcMobilData) {
        this.me = vehicle;
        // TODO Auto-generated constructor stub

        bSafeRef = bSafe = lcMobilData.getSafeDeceleration();
        biasRightRef = biasRight = lcMobilData.getRightBiasAcceleration();
        gapMin = lcMobilData.getMinimumGap();
        thresholdRef = threshold = lcMobilData.getThresholdAcceleration();
        pRef = politeness = lcMobilData.getPoliteness();

    }

    private boolean neigborsInProcessOfLaneChanging(final Vehicle v1, final Vehicle v2, final Vehicle v3) {
        // finite delay criterion also for neighboring vehicles
        final boolean oldFrontVehIsLaneChanging = (v1 == null) ? false : v1.inProcessOfLaneChanging();
        final boolean newFrontVehIsLaneChanging = (v2 == null) ? false : v2.inProcessOfLaneChanging();
        final boolean newBackVehIsLaneChanging = (v3 == null) ? false : v3.inProcessOfLaneChanging();
        return (oldFrontVehIsLaneChanging || newFrontVehIsLaneChanging || newBackVehIsLaneChanging);
    }

    private boolean safetyCheckGaps(double gapFront, double gapBack) {
        return ((gapFront < gapMin) || (gapBack < gapMin));
    }

    private boolean safetyCheckAcceleration(double acc) {
        return acc <= -bSafe;
    }

    public double calcAccelerationBalance(final int direction, RoadSegment roadSegment) {

        final int currentLane = me.getLane();

        final LaneSegment ownLane = roadSegment.laneSegment(currentLane);
        final LaneSegment newLane = roadSegment.laneSegment(currentLane + direction);

        double prospectiveBalance = -Double.MAX_VALUE;

        final Vehicle newFront = newLane.frontVehicle(me);
        final Vehicle oldFront = ownLane.frontVehicle(me);
        final Vehicle newBack = newLane.rearVehicle(me);

        // check if other vehicles are lane-changing
        if (neigborsInProcessOfLaneChanging(oldFront, newFront, newBack)) {
            return prospectiveBalance;
        }

        // safety: check distances
        final double gapFront = me.getNetDistance(newFront);
        final double gapBack = (newBack == null) ? MovsimConstants.GAP_INFINITY : newBack.getNetDistance(me);

        if (safetyCheckGaps(gapFront, gapBack)) {
            return prospectiveBalance;
        }

        // new situation: newBack with me as leader
        // and following left lane cases
        // TO_LEFT --> just the actual situation
        // TO_RIGHT --> consideration of left-lane (with me's leader) has no
        // effect
        final LaneSegment newSituationNewBack = new LaneSegment(roadSegment, me.getLane());
        newSituationNewBack.addVehicleTestwise(newBack);
        newSituationNewBack.addVehicleTestwise(me);
        final LaneSegment leftLaneNewBack = (direction == MovsimConstants.TO_RIGHT || currentLane + direction
                + MovsimConstants.TO_LEFT >= roadSegment.laneCount()) ? null : roadSegment.laneSegment(currentLane + direction + MovsimConstants.TO_LEFT);
        final double newBackNewAcc = (newBack == null) ? 0 : newBack.calcAccModel(newSituationNewBack, leftLaneNewBack);

        if (safetyCheckAcceleration(newBackNewAcc)) {
            return prospectiveBalance;
        }

        // check now incentive criterion
        // consider three vehicles: me, oldBack, newBack

        // old situation for me
        final LaneSegment leftLaneMeOld = (currentLane + MovsimConstants.TO_LEFT) >= roadSegment.laneCount() ?
        		null : roadSegment.laneSegment(currentLane + MovsimConstants.TO_LEFT);
        final double meOldAcc = me.calcAccModel(ownLane, leftLaneMeOld);

        // old situation for old back
        final Vehicle oldBack = ownLane.rearVehicle(me);

        // in old situation same left lane as me
        final double oldBackOldAcc = (oldBack != null) ? oldBack.calcAccModel(ownLane, leftLaneMeOld) : 0;

        // old situation for new back: just provides the actual left-lane
        // situation
        final LaneSegment leftLaneNewBackOldAcc = (currentLane + direction + MovsimConstants.TO_LEFT >= roadSegment.laneCount()) ?
        		null : roadSegment.laneSegment(currentLane + direction + MovsimConstants.TO_LEFT);
        final double newBackOldAcc = (newBack != null) ? newBack.calcAccModel(newLane, leftLaneNewBackOldAcc) : 0;

        // new traffic situation: set subject virtually into new lane under
        // consideration

        final LaneSegment newSituationMe = new LaneSegment(roadSegment, me.getLane());
        newSituationMe.addVehicleTestwise(me);
        newSituationMe.addVehicleTestwise(newFront);

        // if TO_LEFT: actual situation of newBack's left lane
        // if TO_RIGHT: subject (me) still considers oldFront vehicle in left
        // lane
        final LaneSegment leftLaneNewMe;
        if (direction == MovsimConstants.TO_LEFT) {
            leftLaneNewMe = leftLaneNewBack;
        } else {
            leftLaneNewMe = new LaneSegment(roadSegment, oldFront.getLane());
            leftLaneNewMe.addVehicleTestwise(oldFront);
        }

        final double meNewAcc = me.calcAccModel(newSituationMe, leftLaneNewBack);

        final LaneSegment newSituationOldBack = new LaneSegment(roadSegment, oldFront.getLane());
        newSituationOldBack.addVehicleTestwise(oldFront);
        newSituationOldBack.addVehicleTestwise(oldBack);

        // if TO_LEFT: oldBack considers subject (me) as leader in left lane -->
        // new container
        // if TO_RIGHT: subject (me) still considers oldFront vehicle in left
        // lane
        final LaneSegment leftLaneNewSituationOldBack;
        if (direction == MovsimConstants.TO_LEFT) {
            leftLaneNewSituationOldBack = new LaneSegment(roadSegment, me.getLane());
            leftLaneNewSituationOldBack.addVehicleTestwise(me);
        } else {
            leftLaneNewSituationOldBack = leftLaneMeOld;
        }

        final double oldBackNewAcc = (oldBack != null) ? oldBack.calcAccModel(newSituationOldBack, null) : 0;

        // MOBIL trade-off for driver and neighborhood
        final double oldBackDiffAcc = oldBackNewAcc - oldBackOldAcc;
        final double newBackDiffAcc = newBackNewAcc - newBackOldAcc;
        final double meDiffAcc = meNewAcc - meOldAcc;

        // MOBIL's incentive formula
        final int changeTo = newLane.lane() - ownLane.lane();
        final double biasSign = (changeTo == MovsimConstants.TO_LEFT) ? 1 : -1;

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
