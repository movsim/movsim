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
package org.movsim.viewer.ui.charts.model;

public class FloatingCarDataPoint {

    private final double position;
    private final double speed;
    private final double acc;
    private final double time;

    public FloatingCarDataPoint(double time, double position, double speed, double acc) {
        this.time = time;
        this.position = position;
        this.speed = speed;
        this.acc = acc;

    }

    public double getPosition() {
        return position;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAcc() {
        return acc;
    }

    public double getTime() {
        return time;
    }

}
