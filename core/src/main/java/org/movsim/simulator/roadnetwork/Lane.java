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

/**
 * <p>
 * Lane value constants.
 * </p>
 * <p>
 * Lanes are numbered from the inside lane to the outside lane. So, for example, on a three lane road LANE1 is the
 * inside lane, LANE2 is the middle lane and LANE3 is the outside lane.
 * </p>
 * Lane numbering is independent of whether traffic drives on the right or the left, indeed references to "right lanes"
 * and "left lanes" is conscientiously eschewed.
 * <p>
 * </p>
 */
public class Lane {
    public static final int LANE1 = 0;
    public static final int LANE2 = 1;
    public static final int LANE3 = 2;
    public static final int LANE4 = 3;
    public static final int LANE5 = 4;
    public static final int LANE6 = 5;
    public static final int LANE7 = 6;
    public static final int LANE8 = 7;
    public static final int HARD_SHOULDER = -1;
    public static final int NONE = -2;
    
    
    public final static int TO_LEFT = 1;
    public final static int TO_RIGHT = -1;
    public final static int NO_CHANGE = 0;
    public final static int MOST_RIGHT_LANE = LANE1; 

    /**
     * Lane type.
     */
    public static enum Type {

        /**
         * Lane for normal traffic.
         */
        TRAFFIC("driving"),
        /**
         * Entrance (acceleration) lane.
         */
        ENTRANCE("mwentry"),
        /**
         * Exit (deceleration) lane.
         */
        EXIT("ewyexit"),
        /**
         * Shoulder lane.
         */
        SHOULDER("shoulder"),
        /**
         * Restricted lane, eg bus or multiple-occupancy vehicle lane.
         */
        RESTRICTED("restricted"),
        /**
         * Bicycle lane.
         */
        BICYCLE("biking");

        private final String openDriveIdentifier;

        Type(String keyword) {
            this.openDriveIdentifier = keyword;
        }

        public String getOpenDriveIdentifier() {
            return openDriveIdentifier;
        }
    }
    
}
