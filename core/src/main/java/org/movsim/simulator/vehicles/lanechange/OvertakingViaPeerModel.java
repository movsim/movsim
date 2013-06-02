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

import org.movsim.autogen.OvertakingViaPeer;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel.LaneChangeDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class OvertakingViaPeerModel {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OvertakingViaPeerModel.class);

    private double minTargetGap = 100;
    private double maxGapBehindLeader = 200;
    private double safetyTimeGapParameter = 2; // could be taken from IDM family but no access

    private double critFactorTTC = 4; // 6
    private double magicFactorReduceFreeAcc = 4;

    private final LaneChangeModel lcModel;
    private final OvertakingViaPeer parameter;

    OvertakingViaPeerModel(LaneChangeModel laneChangeModel, OvertakingViaPeer parameter) {
        this.lcModel = Preconditions.checkNotNull(laneChangeModel);
        this.parameter = Preconditions.checkNotNull(parameter);
    }

    LaneChangeDecision finishOvertaking(Vehicle me, LaneSegment newLaneSegment) {
        // evaluate situation on the right lane
        if (lcModel.isSafeLaneChange(me, newLaneSegment)) {
            return LaneChangeDecision.MANDATORY_TO_RIGHT;
        }
        return LaneChangeDecision.NONE;
    }

    LaneChangeDecision makeDecisionForOvertaking(Vehicle me, RoadSegment roadSegment) {
        LaneChangeDecision lcDecision = LaneChangeDecision.NONE;
        RoadSegment peerRoadSegment = roadSegment.getPeerRoadSegment();
        double vehiclePositionOnPeer = peerRoadSegment.roadLength() - me.getFrontPosition();
        Vehicle vehicleOnPeer = peerRoadSegment.rearVehicle(Lanes.MOST_INNER_LANE, vehiclePositionOnPeer);
        LOG.debug("check for rear vehicle on peer at position={}, see vehicle={}", vehiclePositionOnPeer,
                (vehicleOnPeer != null ? vehicleOnPeer : "null"));

        if (vehicleOnPeer != null) {
            double distanceToVehicleOnPeer = calcDistance(me, peerRoadSegment, vehicleOnPeer);
            // LOG.info("========== consider vehicle in other direction of travel: distance={}", distance);
            // LOG.info("net distance from me={}, netDistancefromOther={}", getNetDistance(vehicleOnPeer),
            // vehicleOnPeer.getNetDistance(this));
            // LOG.info("roadSegmentId={}, vehiclePos={}, vehicleOnPeerPos=" + vehicleOnPeer.getFrontPosition()
            // + ", vehiclePosOnPeer=" + vehiclePositionOnPeer, roadSegment.userId(), getFrontPosition());
            if (distanceToVehicleOnPeer > 0) {
                lcDecision = makeDecision(me, vehicleOnPeer, roadSegment, distanceToVehicleOnPeer);
            }
        }
        return lcDecision;
    }

    private LaneChangeDecision makeDecision(Vehicle me, Vehicle vehicleOnPeer, RoadSegment roadSegment,
            double distanceToVehicleOnPeer) {

        assert me.lane() == Lanes.MOST_INNER_LANE;
        assert vehicleOnPeer != null;
        assert distanceToVehicleOnPeer > 0;

        LaneChangeDecision decision = LaneChangeDecision.NONE;

        // TODO handling of connecting RS
        Vehicle frontVehicleInLane = roadSegment.frontVehicleOnLane(me);
        if (frontVehicleInLane != null && !frontVehicleInLane.inProcessOfLaneChange()
                && frontVehicleInLane.type() == Vehicle.Type.VEHICLE) {
            double brutDistanceToFrontVehicleInLane = me.getBrutDistance(frontVehicleInLane);
            LOG.debug("brutDistance={}, frontVehicle={}", brutDistanceToFrontVehicleInLane, frontVehicleInLane);

            Vehicle secondFrontVehicleInLane = roadSegment.laneSegment(frontVehicleInLane.lane()).roadSegment()
                    .frontVehicleOnLane(frontVehicleInLane);
            double spaceOnTargetLane = (secondFrontVehicleInLane != null) ? frontVehicleInLane
                    .getNetDistance(secondFrontVehicleInLane) : 100000 /* infinite gap */;
            LOG.debug("space on targetlane={}", spaceOnTargetLane);

            if (me.getLongitudinalModel().getDesiredSpeed() > frontVehicleInLane.getLongitudinalModel()
                    .getDesiredSpeed()
                    && me.getBrutDistance(frontVehicleInLane) < maxGapBehindLeader
                    && spaceOnTargetLane > minTargetGap) {

                double spaceToFrontVeh = brutDistanceToFrontVehicleInLane + me.getLongitudinalModel().getMinimumGap();
                // free model acceleration: large distance, dv=0
                double accConst = me.getLongitudinalModel().calcAccSimple(10000, me.getSpeed(), 0);
                accConst /= magicFactorReduceFreeAcc;

                // time needed when accelerating constantly
                double timeManeuver = Math.sqrt(2 * spaceToFrontVeh / accConst);
                double safetyMargin = critFactorTTC * me.getSpeed() * safetyTimeGapParameter;
                double neededDist = timeManeuver * (me.getSpeed() + vehicleOnPeer.getSpeed()) + spaceToFrontVeh
                        + safetyMargin;
                if (distanceToVehicleOnPeer > neededDist) {
                    decision = LaneChangeDecision.OVERTAKE_VIA_PEER;
                }
            }
        }
        return decision;
    }

    private double calcDistance(Vehicle subjectVehicle, RoadSegment peerRoadSegment, Vehicle vehicleOnPeerRoad) {
        double vehiclePositionOnPeer = peerRoadSegment.roadLength() - subjectVehicle.getFrontPosition();
        return vehiclePositionOnPeer - vehicleOnPeerRoad.getFrontPosition();
    }
}
