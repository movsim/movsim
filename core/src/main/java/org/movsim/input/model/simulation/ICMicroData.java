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

public class ICMicroData {

    private final double initPosition;

    private final double initSpeed;

    private final int initLane;

    /** The type label. Empty string if no type */
    private final String typeLabel;

    /**
     * Instantiates a new iC micro data.
     * 
     * @param map
     *            the map
     */
    public ICMicroData(Map<String, String> map) {
        this.initPosition = Double.parseDouble(map.get("x"));
        this.initSpeed = Double.parseDouble(map.get("v"));
        // TODO check lane numbering with road segment logic and xodr !!!
        this.initLane = Integer.parseInt(map.get("lane"));
        this.typeLabel = map.get("label");
    }

    public double getX() {
        return initPosition;
    }

    public double getSpeed() {
        return initSpeed;
    }

    public int getInitLane() {
        return initLane;
    }

    public String getLabel() {
        return typeLabel;
    }

}
