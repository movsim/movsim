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
package org.movsim.simulator.vehicles.lanechange;

import com.google.common.base.Preconditions;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LaneChangeModel.
 */
public class LaneChangeModel {

    private static final Logger LOG = LoggerFactory.getLogger(LaneChangeModel.class);

    // to avoid flips
//    public static double LANECHANGE_TDELAY_S = 3.0; // delay after lane change
//    public static double LANECHANGE_TDELAY_FRONT_S = 3.0; // delay after a cut-in ahead

    public enum LaneChangeDecision {

        NONE(Lanes.NO_CHANGE),
        STAY_IN_LANE(Lanes.NO_CHANGE),
        DISCRETIONARY_TO_LEFT(Lanes.TO_LEFT),
        DISCRETIONARY_TO_RIGHT(Lanes.TO_RIGHT),
        MANDATORY_TO_LEFT(Lanes.TO_LEFT),
        MANDATORY_TO_RIGHT(Lanes.TO_RIGHT),
        MANDATORY_STAY_IN_LANE(Lanes.NO_CHANGE),
        OVERTAKE_VIA_PEER(Lanes.TO_LEFT);

        private final int laneChangeDirection;

        private LaneChangeDecision(int laneChangeDirection) {
            this.laneChangeDirection = laneChangeDirection;
        }

        public boolean isMandatory() {
            return (this == MANDATORY_TO_LEFT || this == MANDATORY_TO_RIGHT || this == MANDATORY_STAY_IN_LANE);
        }

        public boolean isDiscretionary() {
            return !isMandatory();
        }

        public boolean noDecisionMade() {
            return this == NONE;
        }

        public int getDirection() {
            return laneChangeDirection;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    private boolean considerLaneChanges = true;

    private boolean considerDiscretionaryLaneChanges = true;

    private int mandatoryChangeToRestrictedLane = Integer.MIN_VALUE;

    private int mandatoryChangeToLane = Integer.MIN_VALUE;

    private Vehicle me;

    private MOBIL lcModelMOBIL;

    private OvertakingViaPeer overtakingViaPeerModel;

    private final org.movsim.autogen.LaneChangeModelType parameter;

    // Exit Handling
    // distance at which driver should think about changing lanes for exit
    private static double distanceBeforeExitWantsToChangeLanes = 500.0;
    // distance at which driver must get into exit lane
    private static double distanceBeforeExitMustChangeLanes = 300.0;

    /**
     * Instantiates a new lane changing model.
     *
     * @param laneChangeModelParameter the lc input data
     */
    public LaneChangeModel(org.movsim.autogen.LaneChangeModelType laneChangeModelParameter) {
        this.parameter = laneChangeModelParameter;
        // this.withEuropeanRules = laneChangeModelParameter.isWithEuropeanRules();
        // this.vCritEur = laneChangeModelParameter.getCritSpeedEuroRules();
        // TODO valid lane-change model only if configured by xml
        // isInitialized = laneChangeModelParameter.isInitializedMobilData();
    }

    // used in tests
    public LaneChangeModel(Vehicle vehicle, org.movsim.autogen.LaneChangeModelType laneChangeModelParameter) {
        this.parameter = Preconditions.checkNotNull(laneChangeModelParameter);
        initialize(vehicle);
    }

    /**
     * Initialize.
     *
     * @param vehicle the vehicle
     */
    public void initialize(Vehicle vehicle) {
        this.me = Preconditions.checkNotNull(vehicle);
        lcModelMOBIL = new MOBIL(me, parameter.getModelParameterMOBIL());
        if (parameter.isSetOvertakingViaPeer()) {
            overtakingViaPeerModel = new OvertakingViaPeer(this, parameter.getOvertakingViaPeer());
        }
    }

    /**
     * Checks if is initialized.
     *
     * @return true, if is initialized
     */
    public boolean isInitialized() {
        return parameter != null && lcModelMOBIL != null;
    }

    public boolean withEuropeanRules() {
        return parameter.isEuropeanRules();
    }

    public double vCritEurRules() {
        return parameter.getCritSpeedEur();
    }

    public LaneChangeDecision makeDecision(RoadSegment roadSegment) {
        LaneChangeDecision decision = LaneChangeDecision.NONE;

        if (!considerLaneChanges) {
            return decision;
        }

        decision = checkForMandatoryLaneChangeToRestrictedLane(roadSegment);
        if (decision.isMandatory()) {
            return decision;
        }

        decision = checkForMandatoryLaneChangeToLane(roadSegment);
        if (decision.isMandatory()) {
            return decision;
        }

        // check for mandatory lane changes to reach exit lane
        decision = checkForMandatoryLaneChangeToExit(roadSegment);
        if (decision.isMandatory()) {
            return decision;
        }

        decision = checkForMandatoryLaneChangeAtEntrance(roadSegment);
        if (decision.isMandatory()) {
            return decision;
        }

        decision = checkForLaneChangeForEnteringVehicle(roadSegment);
        if (!decision.noDecisionMade()) {
            return decision;
        }

        // check discretionary lane changes
        if (considerDiscretionaryLaneChanges) {
            decision = determineDiscretionaryLaneChangeDirection(roadSegment);
        }

        return decision;
    }

    boolean isSafeLaneChange(Vehicle subjectVehicle, LaneSegment laneSegment) {
        final Vehicle front = laneSegment.frontVehicle(subjectVehicle);
        final Vehicle back = laneSegment.rearVehicle(subjectVehicle);
        final boolean changeSafe = checkSafetyCriterion(subjectVehicle, front, back);
        return changeSafe;
    }

    boolean checkSafetyCriterion(Vehicle subjectVehicle, Vehicle frontVeh, Vehicle backVeh) {
        final double safeDeceleration = lcModelMOBIL.getParameter().getSafeDeceleration();
        // check distance to front vehicle
        final double gapFront = subjectVehicle.getNetDistance(frontVeh);
        if (gapFront < lcModelMOBIL.getParameter().getMinimumGap()) {
            LOG.debug("gapFront={}", gapFront);
            return false;
        }

        // check distance to vehicle at behind
        if (backVeh != null) {
            final double gapBack = backVeh.getNetDistance(subjectVehicle);
            if (gapBack < lcModelMOBIL.getParameter().getMinimumGap()) {
                LOG.debug("gapBack={}", gapBack);
                return false;
            }
            // check acceleration of back vehicle
            final double backNewAcc = backVeh.getLongitudinalModel().calcAcc(backVeh, subjectVehicle);
            if (backNewAcc <= -safeDeceleration) {
                LOG.debug("gapFront = {}, gapBack = {}", gapFront, gapBack);
                LOG.debug("backNewAcc={}, bSafe={}", backNewAcc, safeDeceleration);
                return false;
            }
        }

        // check acceleration of vehicle ahead
        final double meNewAcc = subjectVehicle.getLongitudinalModel().calcAcc(subjectVehicle, frontVeh);
        if (meNewAcc >= -safeDeceleration) {
            LOG.debug("meNewAcc={}, bSafe={}", meNewAcc, safeDeceleration);
            return true;
        }

        return false;
    }

    private LaneChangeDecision determineDiscretionaryLaneChangeDirection(RoadSegment roadSegment) {

        final int currentLane = me.lane();
        // initialize with largest possible deceleration
        double accToLeft = -Double.MAX_VALUE;
        double accToRight = -Double.MAX_VALUE;
        // consider lane-changing to right-hand side lane
        if (currentLane + Lanes.TO_RIGHT <= roadSegment.trafficLaneMax()) {
            final LaneSegment newLaneSegment = roadSegment.laneSegment(currentLane + Lanes.TO_RIGHT);
            if (newLaneSegment.type() == Lanes.Type.TRAFFIC) {
                // only consider lane changes into traffic lanes, other lane changes are handled by mandatory lane
                // changing
                accToRight = lcModelMOBIL.calcAccelerationBalance(me, Lanes.TO_RIGHT, roadSegment);
            }
        }

        // consider lane-changing to left-hand side lane
        if (currentLane + Lanes.TO_LEFT >= Lanes.MOST_INNER_LANE) {
            final LaneSegment newLaneSegment = roadSegment.laneSegment(currentLane + Lanes.TO_LEFT);
            if (newLaneSegment.type() == Lanes.Type.TRAFFIC) {
                // only consider lane changes into traffic lanes, other lane changes are handled by mandatory lane
                // changing
                accToLeft = lcModelMOBIL.calcAccelerationBalance(me, Lanes.TO_LEFT, roadSegment);
            }
        }

        // decision
        if ((accToRight > 0) || (accToLeft > 0)) {
            LOG.debug("accToRight={}, accToLeft={}", accToRight, accToLeft);
            LOG.debug("currentLane={}", currentLane);
            if (accToRight > accToLeft) {
                return LaneChangeDecision.DISCRETIONARY_TO_RIGHT;
            }
            return LaneChangeDecision.DISCRETIONARY_TO_LEFT;
        }

        return LaneChangeDecision.STAY_IN_LANE;
    }

    private LaneChangeDecision checkForMandatoryLaneChangeAtEntrance(RoadSegment roadSegment) {
        final int currentLane = me.lane();
        final LaneSegment currentLaneSegment = roadSegment.laneSegment(currentLane);

        if (currentLaneSegment.type() == Lanes.Type.ENTRANCE) {
            final int direction = (currentLane == roadSegment.laneCount()) ? Lanes.TO_LEFT : Lanes.TO_RIGHT;
            if (currentLane + direction >= Lanes.MOST_INNER_LANE) {
                final LaneSegment newLaneSegment = roadSegment.laneSegment(currentLane + direction);
                if (isSafeLaneChange(me, newLaneSegment)) {
                    double distanceToRoadSegmentEnd = me.getDistanceToRoadSegmentEnd();
                    if (distanceToRoadSegmentEnd < 0) {
                        // just a hack. should not happen.
                        LOG.info("check this: roadSegmentLength not set. Do mandatory lane change anyway.");
                        return (direction == Lanes.TO_LEFT) ?
                                LaneChangeDecision.MANDATORY_TO_LEFT :
                                LaneChangeDecision.MANDATORY_TO_RIGHT;
                    }
                    // evaluate additional motivation to leave entrance lane
                    double accInCurrentLane = me.getLongitudinalModel()
                            .calcAcc(me, currentLaneSegment.frontVehicle(me));
                    double accInNewLane = me.getLongitudinalModel().calcAcc(me, newLaneSegment.frontVehicle(me));
                    double bias = biasForMandatoryChange(distanceToRoadSegmentEnd);
                    if (accInNewLane + bias > accInCurrentLane) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format(
                                    "change lane: veh.id=%d, distanceToRoadSegmentEnd=%.2f, accInCurrentLane=%.2f, accInNewLane=%.2f, bias=%.2f",
                                    me.getId(), distanceToRoadSegmentEnd, accInCurrentLane, accInNewLane, bias));
                        }
                        return (direction == Lanes.TO_LEFT) ?
                                LaneChangeDecision.MANDATORY_TO_LEFT :
                                LaneChangeDecision.MANDATORY_TO_RIGHT;
                    }
                }
            }
            return LaneChangeDecision.MANDATORY_STAY_IN_LANE;
        }

        return LaneChangeDecision.NONE;
    }

    private LaneChangeDecision checkForMandatoryLaneChangeToRestrictedLane(RoadSegment roadSegment) {
        if (!hasMandatoryChangeToRestrictedLane()) {
            return LaneChangeDecision.NONE;
        }

        final int currentLane = me.lane();
        final LaneSegment currentLaneSegment = roadSegment.laneSegment(currentLane);
        if (currentLane == mandatoryChangeToRestrictedLane && currentLaneSegment.type() == Lanes.Type.RESTRICTED) {
            LOG.debug("restricted lane={} already reached from veh={}", currentLane, me);
            return LaneChangeDecision.MANDATORY_STAY_IN_LANE;
        }

        if (roadSegment.laneCount() < mandatoryChangeToRestrictedLane) {
            LOG.debug("requested restricted lane={} not available on roadSegment={}", mandatoryChangeToRestrictedLane,
                    roadSegment);
            return LaneChangeDecision.NONE;
        }

        if (roadSegment.laneSegment(mandatoryChangeToRestrictedLane).type() != Lanes.Type.RESTRICTED) {
            LOG.debug("requested restricted lane={} available but not of restricted type on roadSegment={}",
                    mandatoryChangeToRestrictedLane, roadSegment);
        }

        final int direction = (currentLane < mandatoryChangeToRestrictedLane) ? Lanes.TO_RIGHT : Lanes.TO_LEFT;
        if (currentLane + direction >= Lanes.MOST_INNER_LANE) {
            final LaneSegment newLaneSegment = roadSegment.laneSegment(currentLane + direction);
            if (isSafeLaneChange(me, newLaneSegment)) {
                return (direction == Lanes.TO_LEFT) ?
                        LaneChangeDecision.MANDATORY_TO_LEFT :
                        LaneChangeDecision.MANDATORY_TO_RIGHT;
            }
        }

        return LaneChangeDecision.NONE;
    }

    private LaneChangeDecision checkForMandatoryLaneChangeToLane(RoadSegment roadSegment) {
        if (!hasMandatoryChangeToLane()) {
            return LaneChangeDecision.NONE;
        }

        final int currentLane = me.lane();
        if (currentLane == mandatoryChangeToLane) {
            LOG.debug("lane={} already reached from veh={}", currentLane, me);
            return LaneChangeDecision.MANDATORY_STAY_IN_LANE;
        }

        if (roadSegment.laneCount() < mandatoryChangeToLane) {
            LOG.debug("requested lane={} not available on roadSegment={}", mandatoryChangeToLane, roadSegment);
            return LaneChangeDecision.NONE;
        }

        final int direction = (currentLane < mandatoryChangeToLane) ? Lanes.TO_RIGHT : Lanes.TO_LEFT;
        if (currentLane + direction >= Lanes.MOST_INNER_LANE) {
            final LaneSegment newLaneSegment = roadSegment.laneSegment(currentLane + direction);
            if (isSafeLaneChange(me, newLaneSegment)) {
                return (direction == Lanes.TO_LEFT) ?
                        LaneChangeDecision.MANDATORY_TO_LEFT :
                        LaneChangeDecision.MANDATORY_TO_RIGHT;
            }
        }

        return LaneChangeDecision.NONE;
    }

    private double biasForMandatoryChange(double distanceToRoadSegmentEnd) {
        final double interactionDistance = 10;
        double bias = me.getMaxDeceleration() * interactionDistance / Math.max(distanceToRoadSegmentEnd, 10.0);
        return bias;
    }

    private LaneChangeDecision checkForMandatoryLaneChangeToExit(RoadSegment roadSegment) {
        final int currentLane = me.lane();

        // consider mandatory lane-change to exit
        if (me.exitRoadSegmentId() == roadSegment.id()) {
            if (currentLane == roadSegment.laneCount()
                    && roadSegment.laneSegment(roadSegment.laneCount()).type() == Lanes.Type.EXIT) {
                // already in exit lane, so do not move out of it
                return LaneChangeDecision.MANDATORY_STAY_IN_LANE;
            } else if (currentLane < roadSegment.laneCount()) {
                // evaluate situation on the right lane
                final LaneSegment newLaneSegment = roadSegment.laneSegment(currentLane + Lanes.TO_RIGHT);
                if (isSafeLaneChange(me, newLaneSegment)) {
                    return LaneChangeDecision.MANDATORY_TO_RIGHT;
                }
                LOG.debug("cannot turn into exit lane: {} on roadSegment={}", me, roadSegment);
                return LaneChangeDecision.MANDATORY_STAY_IN_LANE;
            }
        }

        // consider mandatory lane-change to exit on next road segment ahead
        final LaneSegment sinkLaneSegment = roadSegment.laneSegment(currentLane).sinkLaneSegment();
        if (sinkLaneSegment != null && me.exitRoadSegmentId() == sinkLaneSegment.roadSegment().id()) {
            // next road segment is the exit segment
            final double distanceToExit = roadSegment.roadLength() - me.getFrontPosition();
            if (distanceToExit < distanceBeforeExitMustChangeLanes) {
                if (currentLane == roadSegment.laneCount()) {
                    // already in exit lane, so do not move out of it
                    return LaneChangeDecision.MANDATORY_STAY_IN_LANE;
                } else if (currentLane < roadSegment.laneCount()) {
                    final LaneSegment newLaneSegment = roadSegment.laneSegment(currentLane + Lanes.TO_RIGHT);
                    if (isSafeLaneChange(me, newLaneSegment)) {
                        return LaneChangeDecision.MANDATORY_TO_RIGHT;
                    }
                    return LaneChangeDecision.MANDATORY_STAY_IN_LANE;
                }
            }
        }
        return LaneChangeDecision.NONE;
    }

    // TODO first version of cooperative lane-changing behavior
    private LaneChangeDecision checkForLaneChangeForEnteringVehicle(RoadSegment roadSegment) {
        LaneChangeDecision laneChangeDecision = LaneChangeDecision.NONE;
        final int currentLane = me.lane();
        if (roadSegment.laneCount() > 2
                && roadSegment.laneSegment(roadSegment.laneCount()).type() == Lanes.Type.ENTRANCE
                && currentLane == roadSegment.trafficLaneMax()) {
            Vehicle frontVehicle = roadSegment.laneSegment(roadSegment.trafficLaneMax()).frontVehicle(me);
            if (frontVehicle == null || frontVehicle.type() == Vehicle.Type.OBSTACLE) {
                return LaneChangeDecision.NONE;
            }

            double accToFront = me.getLongitudinalModel().calcAcc(me, frontVehicle);
            if (accToFront < -lcModelMOBIL.getParameter().getSafeDeceleration()) {
                // check own disadvantage to change to left to decide to make room
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(
                            "next to entrance lane: pos=%.2f, lane=%d, netGap=%.2f, ownSpeed=%.2f, dv=%.2f, calcAccToFront=%.2f",
                            me.getFrontPosition(), currentLane, me.getNetDistance(frontVehicle), me.getSpeed(),
                            me.getRelSpeed(frontVehicle), accToFront));
                }
                final int newLane = currentLane + Lanes.TO_LEFT;
                final LaneSegment newLaneSegment = roadSegment.laneSegment(newLane);

                if (newLaneSegment.type() == Lanes.Type.ENTRANCE) {
                    // never change lane into an entrance lane
                    return LaneChangeDecision.NONE;
                }
                final Vehicle newFront = newLaneSegment.frontVehicle(me);
                if (newFront != null) {
                    if (newFront.inProcessOfLaneChange()) {
                        return LaneChangeDecision.NONE;
                    }
                    final double gapFront = me.getNetDistance(newFront);
                    if (gapFront < lcModelMOBIL.getParameter().getMinimumGap()) {
                        return LaneChangeDecision.NONE;
                    }
                }
                final Vehicle newBack = newLaneSegment.rearVehicle(me);
                if (newBack != null) {
                    if (newBack.inProcessOfLaneChange()) {
                        return LaneChangeDecision.NONE;
                    }
                    final double gapRear = newBack.getNetDistance(me);
                    if (gapRear < lcModelMOBIL.getParameter().getMinimumGap()) {
                        return LaneChangeDecision.NONE;
                    }
                }
                me.setLane(newLane);
                final int index = newLaneSegment.addVehicleTemp(me);
                final double newBackNewAcc = newBack == null ? 0 : newBack.calcAccModel(newLaneSegment, null);
                final double meNewAcc = me.calcAccModel(newLaneSegment, null);
                newLaneSegment.removeVehicle(index);
                me.setLane(currentLane);

                if (lcModelMOBIL.safetyCheckAcceleration(newBackNewAcc) || lcModelMOBIL
                        .safetyCheckAcceleration(meNewAcc)) {
                    return LaneChangeDecision.NONE;
                }
                LOG.debug("finally change to left to make room for vehicle at entrance lane ...");
                return LaneChangeDecision.DISCRETIONARY_TO_LEFT;
            }
        }
        return laneChangeDecision;
    }

    // --------------------------------------------------------------------------------------------
    // delegate overtaking decision on rural road via peer road to dedicated model
    public LaneChangeDecision makeDecisionForOvertaking(RoadSegment roadSegment) {
        if (overtakingViaPeerModel != null) {
            return overtakingViaPeerModel.makeDecisionForOvertaking(me, roadSegment);
        }
        return LaneChangeDecision.NONE;
    }

    public LaneChangeDecision finishOvertakingViaPeer(LaneSegment laneSegment) {
        if (overtakingViaPeerModel != null) {
            return overtakingViaPeerModel.finishOvertaking(me, laneSegment);
        }
        return LaneChangeDecision.NONE;
    }

    public boolean isConsiderLaneChanges() {
        return considerLaneChanges;
    }

    public void setConsiderLaneChanges(boolean considerLaneChanges) {
        this.considerLaneChanges = considerLaneChanges;
    }

    public boolean isConsiderDiscretionaryLaneChanges() {
        return considerDiscretionaryLaneChanges;
    }

    public void setConsiderDiscretionaryLaneChanges(boolean considerDiscretionaryLaneChanges) {
        this.considerDiscretionaryLaneChanges = considerDiscretionaryLaneChanges;
    }

    public boolean hasMandatoryChangeToRestrictedLane() {
        return mandatoryChangeToRestrictedLane != Integer.MIN_VALUE;
    }

    public void setMandatoryChangeToRestrictedLane(int restrictedLane) {
        Preconditions.checkArgument(restrictedLane >= Lanes.MOST_INNER_LANE);
        this.mandatoryChangeToRestrictedLane = restrictedLane;
    }

    public void unsetMandatoryChangeToRestrictedLane() {
        this.mandatoryChangeToRestrictedLane = Integer.MIN_VALUE;
    }

    public boolean hasMandatoryChangeToLane() {
        return mandatoryChangeToLane != Integer.MIN_VALUE;
    }

    public void setMandatoryChangeToLane(int lane) {
        Preconditions.checkArgument(lane >= Lanes.MOST_INNER_LANE);
        this.mandatoryChangeToLane = lane;
    }

    public void unsetMandatoryChangeToLane() {
        this.mandatoryChangeToLane = Integer.MIN_VALUE;
    }

}
