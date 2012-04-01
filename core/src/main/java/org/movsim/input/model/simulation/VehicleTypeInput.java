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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleTypeInput {

    final static Logger logger = LoggerFactory.getLogger(VehicleTypeInput.class);
    private final String keyName;
    private final double fraction;
    private final double relativeRandomizationDesiredSpeed;
    private final String routeLabel;

    /**
     * Constructor.
     * 
     * @param map
     *            the map
     */
    public VehicleTypeInput(Map<String, String> map) {
        this.keyName = map.get("label");
        this.fraction = Double.parseDouble(map.get("fraction"));
        logger.info("rand={}     key:{}", map.get("relative_v0_randomization"), keyName);
        this.relativeRandomizationDesiredSpeed = Double.parseDouble(map.get("relative_v0_randomization"));
        final String routeLabel = map.get("route_label");
        this.routeLabel = routeLabel.equals("") ? null : routeLabel;
    }

    public String getKeyName() {
        return keyName;
    }

    public double getFraction() {
        return fraction;
    }

    public double getRelativeRandomizationDesiredSpeed() {
        return relativeRandomizationDesiredSpeed;
    }

    public String getRouteLabel() {
        return routeLabel;
    }
}
