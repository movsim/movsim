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
package org.movsim.simulator;

/**
 * The Interface MovsimConstants.
 */
public interface MovsimConstants {

    // TODO: connection with maven version number
    final String RELEASE_VERSION = "1.2";

    final double SMALL_VALUE = 1e-7;

    final double MAX_VEHICLE_SPEED = 200 / 3.6;

    /** The most right lane (related to list index) */
    final int MOST_RIGHT_LANE = 0; // increment lane index for further lanes to the left

    // TODO
    final int TO_LEFT = 1;

    final int TO_RIGHT = -1;

    final int NO_CHANGE = 0;

    final double GAP_INFINITY = 10000;

    final double INVALID_GAP = -1;

    final double CRITICAL_GAP = 2;

}
