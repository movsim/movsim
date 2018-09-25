/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

package org.movsim.simulator.roadnetwork.predicates;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Predicate;

/**
 * TODO add javadoc
 *
 * Uses rearPosition of vehicle because this is in line with the outflow-update of the roadSegments.
 */
public final class VehiclePassedPosition implements Predicate<Vehicle> {

    private final double position;

    public VehiclePassedPosition(double position) {
        this.position = position;
    }

    @Override
    public boolean apply(@Nullable Vehicle vehicle) {
        if (vehicle == null) {
            return false;
        }
        assert vehicle.getRearPositionOld() <= vehicle.getRearPosition() : "oldPos=" + vehicle.getRearPositionOld()
                + " > pos=" + vehicle.getRearPosition();
        return vehicle.getRearPositionOld() <= position && vehicle.getRearPosition() > position;
    }

}