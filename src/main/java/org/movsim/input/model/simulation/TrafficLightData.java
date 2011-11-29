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

    /** The x. */
    private final double x;

    /** The green time. */
    private final double greenTime;

    /** The red time. */
    private final double redTime;

    /** The green red time period. */
    private final double greenRedTimePeriod;

    /** The red green time period. */
    private final double redGreenTimePeriod;

    /** The phase shift. */
    private final double phaseShift;

    /**
     * Instantiates a new traffic light data impl.
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.TrafficLightData#getX()
     */
    public double getX() {
        return x;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.TrafficLightData#getGreenTime()
     */
    public double getGreenTime() {
        return greenTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.TrafficLightData#getRedTime()
     */
    public double getRedTime() {
        return redTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.TrafficLightData#getGreenRedTimePeriod ()
     */
    public double getGreenRedTimePeriod() {
        return greenRedTimePeriod;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.TrafficLightData#getRedGreenTimePeriod ()
     */
    public double getRedGreenTimePeriod() {
        return redGreenTimePeriod;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.TrafficLightData#getPhaseShift()
     */
    public double getPhaseShift() {
        return phaseShift;
    }
}
