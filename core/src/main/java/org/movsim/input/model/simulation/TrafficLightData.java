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
package org.movsim.input.model.simulation;

import java.util.Map;

public class TrafficLightData {

    private final double x;

    private final double greenTime;

    private final double redTime;

    private final double greenRedTimePeriod;

    private final double redGreenTimePeriod;

    private final double phaseShift;

    /**
     * Instantiates a new TrafficLightData.
     * 
     * @param map
     *            the map
     */
    public TrafficLightData(Map<String, String> map) {
        this.x = Double.parseDouble(map.get("x"));
        this.greenTime = Double.parseDouble(map.get("green_time"));
        this.redTime = Double.parseDouble(map.get("red_time"));
        this.greenRedTimePeriod = Double.parseDouble(map.get("green_red_time"));
        this.redGreenTimePeriod = Double.parseDouble(map.get("red_green_time"));
        this.phaseShift = Double.parseDouble(map.get("phase_shift"));
    }

    public double getX() {
        return x;
    }

    public double getGreenTime() {
        return greenTime;
    }

    public double getRedTime() {
        return redTime;
    }

    public double getGreenRedTimePeriod() {
        return greenRedTimePeriod;
    }

    public double getRedGreenTimePeriod() {
        return redGreenTimePeriod;
    }

    public double getPhaseShift() {
        return phaseShift;
    }
}
