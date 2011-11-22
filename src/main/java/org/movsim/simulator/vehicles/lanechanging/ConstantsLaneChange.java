/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.simulator.vehicles.lanechanging;

/**
 * Default lane-change parameters.
 */
public interface ConstantsLaneChange {

    // max safe braking decelerations
    static final double MAX_SAFE_BRAKING_CAR = 5.0;
    static final double MAX_SAFE_BRAKING_TRUCK = 4.0;
    static final double MAX_SAFE_SELF_BRAKING = 8.0;

    // minimum distances
    static final double GAP_MIN_FRONT_CAR = 4.0;
    static final double GAP_MIN_REAR_CAR = 8.0;
    static final double GAP_MIN_FRONT_TRUCK = 6.0;
    static final double GAP_MIN_REAR_TRUCK = 10.0;

    // inside-lane bias
    static final double BIAS_INSIDE_LANE_CAR = 0.1;
    static final double BIAS_INSIDE_LANE_TRUCK = 0.8;

    // politeness when changing lanes
    static final double POLITENESS_CAR = 0.2;
    static final double POLITENESS_CAR_LANE_CLOSURE = 0.0;
    static final double POLITENESS_TRUCK = 0.2;

    // lane changing thresholds (m/s^2)
    static final double THRESHOLD_CAR = 0.3;
    static final double THRESHOLD_TRUCK = 0.2;

    /**
     * Default LCM for cars.
     */
    static final double[] LCM_CAR = new double[] {
        GAP_MIN_FRONT_CAR, GAP_MIN_REAR_CAR, MAX_SAFE_BRAKING_CAR,
        POLITENESS_CAR, THRESHOLD_CAR, BIAS_INSIDE_LANE_CAR };

    /**
     * Default LCM for trucks.
     */
    static final double[] LCM_TRUCK = new double[] {
        GAP_MIN_FRONT_TRUCK, GAP_MIN_REAR_TRUCK, MAX_SAFE_BRAKING_TRUCK,
        POLITENESS_TRUCK, THRESHOLD_TRUCK, BIAS_INSIDE_LANE_TRUCK };
}
