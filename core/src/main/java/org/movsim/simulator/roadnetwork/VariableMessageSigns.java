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

package org.movsim.simulator.roadnetwork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableMessageSigns {
    final static Logger logger = LoggerFactory.getLogger(Slopes.class);
    private Collection<VariableMessageSignBase> variableMessageSigns;

    /**
     * Checks if is empty.
     * 
     * @return true, if is empty
     */
    public boolean isEmpty() {
        if (variableMessageSigns == null || variableMessageSigns.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Add a VMS
     */
    public void add(VariableMessageSignBase variableMessageSign) {
        if (variableMessageSigns == null) {
            variableMessageSigns = Collections.synchronizedCollection(new ArrayList<VariableMessageSignBase>());
        }
        variableMessageSigns.add(variableMessageSign);
    }

    /**
     * Remove a VMS
     */
    public void remove(VariableMessageSignBase variableMessageSign) {
        assert variableMessageSigns != null;
        variableMessageSigns.remove(variableMessageSign);
    }

    /**
     * Apply the VMS to a vehicle
     * @param vehicle
     */
    public void apply(Vehicle vehicle, RoadSegment roadSegment) {
        if (variableMessageSigns != null) {
            for (final VariableMessageSignBase variableMessageSign : variableMessageSigns) {
                variableMessageSign.apply(vehicle, roadSegment);
            }
        }
    }
}
