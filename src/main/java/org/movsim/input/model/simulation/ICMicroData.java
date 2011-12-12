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

public class ICMicroData {

    /** The init position. */
    private final double initPosition;

    /** The init speed. */
    private final double initSpeed;

    /** The init lane. */
    private final int initLane; // most right lane: MovsimConstants.MOST_RIGHT_LANE

    /** The type label. */
    private final String typeLabel; // empty string if no type

    /**
     * Instantiates a new iC micro data impl.
     * 
     * @param map
     *            the map
     */
    public ICMicroData(Map<String, String> map) {
        this.initPosition = Double.parseDouble(map.get("x"));
        this.initSpeed = Double.parseDouble(map.get("v"));
        this.initLane = Integer.parseInt(map.get("lane")) - 1; // internal MovSim
        this.typeLabel = map.get("label");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.ICMicroData#getX()
     */
    public double getX() {
        return initPosition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.ICMicroData#getSpeed()
     */
    public double getSpeed() {
        return initSpeed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.ICMicroData#getInitLane()
     */
    public int getInitLane() {
        return initLane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.impl.ICMicroData#getType()
     */
    public String getLabel() {
        return typeLabel;
    }

}
