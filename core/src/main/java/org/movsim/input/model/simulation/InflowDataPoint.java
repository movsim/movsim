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
package org.movsim.input.model.simulation;

import java.util.Map;

public class InflowDataPoint {

    /** The time. */
    private final double time; // in s (seconds)

    /** The flow. */
    private final double flow; // in 1/s

    /** The speed. */
    private final double speed; // in m/s

    /**
     * Constructor.
     * 
     * @param map
     *            the map
     */
    public InflowDataPoint(Map<String, String> map) {
        this.time = Double.parseDouble(map.get("t"));
        // convert to SI
        this.flow = Double.parseDouble(map.get("q_per_hour")) / 3600.0;
        this.speed = Double.parseDouble(map.get("v"));
    }

    /**
     * Constructor.
     * 
     * @param time
     * @param flowPerHour
     * @param speed
     */
    public InflowDataPoint(double time, double flowPerHour, double speed) {
        this.time = time;
        // convert to SI
        this.flow = flowPerHour / 3600.0;
        this.speed = speed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.InflowDataPoint#getTime()
     */
    public double getTime() {
        return time;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.InflowDataPoint#getFlow()
     */
    public double getFlow() {
        return flow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.InflowDataPoint#getSpeed()
     */
    public double getSpeed() {
        return speed;
    }

}
