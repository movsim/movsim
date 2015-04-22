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

package org.movsim.simulator.roadnetwork;

/**
 * <p>
 * Lanes value constants.
 * </p>
 * <p>
 * Lanes are numbered from the inside laneIndex to the outside laneIndex. So, for example, on a three laneIndex road LANE1 is the inside
 * laneIndex, LANE2 is the middle laneIndex and LANE3 is the outside laneIndex.
 * </p>
 * Lanes numbering is independent of whether traffic drives on the right or the left, indeed references to "right lanes"
 * and "left lanes" is conscientiously eschewed.
 * <p>
 * </p>
 */
public final class Lanes {

    private Lanes() {
        // private constructor
    }


    // from inner to outer lanes
    public static final int LANE1 = 1;
    public static final int LANE2 = 2;
    public static final int LANE3 = 3;
    public static final int LANE4 = 4;
    public static final int LANE5 = 5;
    public static final int HARD_SHOULDER = -1; // NOT DRIVABLE
    public static final int NONE = -2; // NOT DRIVABLE

    // TODO renaming: TO_OUTER, TO_INNER
    public final static int TO_LEFT = -1; // TODO decrease index
    public final static int TO_RIGHT = 1; // TODO increase index
    public final static int NO_CHANGE = 0;

    public static final int MOST_INNER_LANE = LANE1;

    /** internal overtaking lane, works with mapping */
    public static final int OVERTAKING = 0;

    /**
     * Lanes type.
     * 
     * <p>
     * Mapping of OpenDRIVE laneIndex types.
     */
    public enum Type {
        /**
         * Lanes for normal traffic.
         */
        TRAFFIC("driving"),
        /**
         * Entrance (acceleration) laneIndex.
         */
        ENTRANCE("mwyEntry"),
        /**
         * Exit (deceleration) laneIndex.
         */
        EXIT("mwyExit"),
        /**
         * Shoulder laneIndex.
         */
        SHOULDER("shoulder"),
        /**
         * Restricted laneIndex, eg bus or multiple-occupancy vehicle laneIndex.
         */
        RESTRICTED("restricted"),
        /**
         * Bicycle laneIndex.
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

    /**
     * center laneSection not supported
     * 
     * <br>
     * created: Mar 30, 2013<br>
     * 
     */
    public enum LaneSectionType {
        LEFT("-", true), RIGHT("+", false);

        private final String idAppender;

        private final boolean reverseDirection;

        private LaneSectionType(String idAppender, boolean reverseDirection) {
            this.idAppender = idAppender;
            this.reverseDirection = reverseDirection;
        }

        public String idAppender() {
            return idAppender;
        }

        public boolean isReverseDirection() {
            return reverseDirection;
        }
    }

    /**
     * 
     * 
     * <br>
     * created: Mar 30, 2013<br>
     * 
     */
    public enum RoadLinkElementType {
        ROAD("road"), JUNCTION("junction");

        private final String xodrIdentifier;

        RoadLinkElementType(String keyword) {
            this.xodrIdentifier = keyword;
        }

        public String xodrIdentifier() {
            return xodrIdentifier;
        }
    }

}
