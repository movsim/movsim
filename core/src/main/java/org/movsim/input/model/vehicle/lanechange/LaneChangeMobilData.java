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
package org.movsim.input.model.vehicle.lanechange;

import java.util.Map;

public class LaneChangeMobilData {

    private double safeDeceleration;
    private double minimumGap;
    private double thresholdAcceleration;
    private double rightBiasAcceleration;
    private double politeness;

    public LaneChangeMobilData() {

    }

    public void init(final Map<String, String> map) {
        safeDeceleration = Double.parseDouble(map.get("b_safe"));
        minimumGap = Double.parseDouble(map.get("s_min"));
        thresholdAcceleration = Double.parseDouble(map.get("threshold"));
        rightBiasAcceleration = Double.parseDouble(map.get("bias_right"));
        politeness = Double.parseDouble(map.get("politeness"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChange.LaneChangeMobilData# getSafeDeceleration()
     */
    public double getSafeDeceleration() {
        return safeDeceleration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChange.LaneChangeMobilData# getMinimumGap()
     */
    public double getMinimumGap() {
        return minimumGap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChange.LaneChangeMobilData# getThresholdAcceleration()
     */
    public double getThresholdAcceleration() {
        return thresholdAcceleration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChange.LaneChangeMobilData# getRightBiasAcceleration()
     */
    public double getRightBiasAcceleration() {
        return rightBiasAcceleration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChange.LaneChangeMobilData# getPoliteness()
     */
    public double getPoliteness() {
        return politeness;
    }
}
