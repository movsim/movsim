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
package org.movsim.input.model.simulation.impl;

import java.util.Map;

import org.movsim.input.model.simulation.InflowDataPoint;

// TODO: Auto-generated Javadoc
/**
 * The Class InflowDataPointImpl.
 */
public class InflowDataPointImpl implements InflowDataPoint {

    /** The time. */
    private final double time; // in s (seconds)
    
    /** The flow. */
    private final double flow; // in 1/s
    
    /** The speed. */
    private final double speed; // in m/s

    /**
     * Instantiates a new inflow data point impl.
     * 
     * @param map
     *            the map
     */
    public InflowDataPointImpl(Map<String, String> map) {
        this.time = Double.parseDouble(map.get("t"));
        this.flow = Double.parseDouble(map.get("q_per_hour")) / 3600.0; // convert to SI
        this.speed = Double.parseDouble(map.get("v"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.InflowDataPoint#getTime()
     */
    @Override
    public double getTime() {
        return time;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.InflowDataPoint#getFlow()
     */
    @Override
    public double getFlow() {
        return flow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.InflowDataPoint#getSpeed()
     */
    @Override
    public double getSpeed() {
        return speed;
    }

}
