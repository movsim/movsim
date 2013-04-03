package org.movsim.simulator.roadnetwork;

import org.movsim.simulator.vehicles.Vehicle;

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

public class VariableMessageSignDiversion extends VariableMessageSignBase {

    private final static double VISIBILITY_OF_SIGN = 400;// sign visible from 400m

    @Override
    public void apply(Vehicle vehicle, RoadSegment roadSegment) {
        if (vehicle.lane() == roadSegment.laneCount()
                && roadSegment.roadLength() - vehicle.getFrontPosition() <= VISIBILITY_OF_SIGN) {
            final LaneSegment laneSegment = roadSegment.laneSegment(Lanes.LANE1);
            vehicle.setExitRoadSegmentId(laneSegment.sinkLaneSegment().roadSegment().id());
        }
    }

    @Override
    public void cancel(Vehicle vehicle, RoadSegment roadSegment) {
        if (vehicle.lane() == roadSegment.laneCount()) {
            vehicle.setExitRoadSegmentId(Vehicle.ROAD_SEGMENT_ID_NOT_SET);
        }
    }
}
