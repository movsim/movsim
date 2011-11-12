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
package org.movsim.simulator;

// TODO: Auto-generated Javadoc
/**
 * The Interface MovsimConstants.
 */
public interface MovsimConstants {

    // TODO: connection with maven version number
    final String RELEASE_VERSION = "1.1";

    /** The COMMEN t_ char. */
    final String COMMENT_CHAR = "#";

    /** The SMAL l_ value. */
    final double SMALL_VALUE = 1e-7;

    /** The MA x_ vehicl e_ speed. */
    final double MAX_VEHICLE_SPEED = 200 / 3.6;

    /** The most right lane (related to list index) */
    final int MOST_RIGHT_LANE = 0; // increment lane index for further lanes to
                                   // the left
    
    // TODO
    final int TO_LEFT = 1;
    
    final int TO_RIGHT = -1;
     
    final int NO_CHANGE = 0;
        
    final String OBSTACLE_KEY_NAME = "Obstacle";

    /** The gap infinity. */
    final double GAP_INFINITY = 10000;

    /** The invalid gap */
    final double INVALID_GAP = -1;

    final double VEHICLE_WIDTH = 4.4;

    final double CRITICAL_GAP = 2;


}
