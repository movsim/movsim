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
package org.movsim.viewer.graphics;

import java.awt.Color;

public interface GraphicsConfigurationParameters {
    public static final Color BACKGROUND_COLOR = new Color(230, 230, 230);

    public static final Color BACKGROUND_COLOR_SIM = new Color(0x74, 0xac, 0x23); // green grass
    // public static final Color BACKGROUND_COLOR_SIM = new Color(214, 217, 223); // Nimbus

    final double INITIAL_SCALE = 1.0 / Math.sqrt(2);
    final int INITIAL_OFFSET_X = 0;
    final int INITIAL_OFFSET_Y = 0;
    final double DEFAULT_LANE_WIDTH = 10;

    static final double INITIAL_SPEEDUP_DURATION = 120.0;
    static final double INITIAL_SPEEDUP_DURATION_OFF_ON_RAMP = 440.0;
    static final double INITIAL_SPEEDUP_FACTOR = 12.0;
    static final int INITIAL_SLEEP_TIME = 26;
    static final int DEFAULT_SLEEP_TIME_MS = 40;

    public static final boolean DRAW_ROADID = true;
    public static final boolean DRAWSOURCES = true;
    public static final boolean DRAWSINKS = true;
    public static final boolean DRAWSPEEDLIMITS = true;
    public static final boolean SLOPES = true;

    public static final double VmaxForColorSpectrum = 140;

    
}
