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

import org.movsim.simulator.roadnetwork.RoadSegment;

public interface RoadObject extends Comparable<RoadObject> {

    enum RoadObjectType {
        TRAFFICLIGHT,
        SPEEDLIMIT,
        LOOPDETECTOR,
        VMS_DIVERSION,
        FLOW_CONSERVING_BOTTLENECK,
        GRADIENT_PROFILE
    }

    RoadObjectType getType();

    double position();

    RoadSegment roadSegment();

    /**
     * Separately called, signalPoints to be added after whole roadNetwork is constructed!
     */
    void createSignalPositions();

    void timeStep(double dt, double simulationTime, long iterationCount);

    /**
     * Self-defined OpenDRIVE.Road.Objects.Object.type attributes.
     */
    enum XodrRoadObjectType {
        SPEEDLIMIT("speedlimit");

        private final String openDriveIdentifier;

        XodrRoadObjectType(String keyword) {
            this.openDriveIdentifier = keyword;
        }

        public String xodrIdentifier() {
            return openDriveIdentifier;
        }
    }
}
