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
package org.movsim.viewer.util;

import java.util.Formatter;

public class StringHelper {


    public static String getTime(double time_s, boolean withH, boolean withM, boolean withS) {
        final int time = (int) time_s;
        final int n_h = time / 3600;
        final int n_min = (time - 3600 * n_h) / 60;
        final int n_sec = time % 60;

        String timeString = "";
        if (withH) {
            timeString += String.valueOf(n_h);
        }
        if (withM) {
            timeString += ":";
            timeString += (n_min < 10) ? ("0" + String.valueOf(n_min)) : String.valueOf(n_min);

        }
        if (withS) {
            timeString += ":";
            timeString += (n_sec < 10) ? ("0" + String.valueOf(n_sec)) : String.valueOf(n_sec);
        }

        return timeString;
    }

    public static String getFormatedTime(double timeInSeconds) {
        int intTime = (int) timeInSeconds;
        final int hours = intTime / 3600;
        intTime = intTime % 3600;
        final int min = intTime / 60;
        intTime = intTime % 60;
        final StringBuilder stringBuilder = new StringBuilder();
        final Formatter formatter = new Formatter(stringBuilder);
        formatter.format("%02d:%02d:%02d", hours, min, intTime);
        formatter.close();
        return stringBuilder.toString();
    }
}
