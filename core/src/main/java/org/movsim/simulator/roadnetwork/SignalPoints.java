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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SignalPoints implements Iterable<SignalPoint> {

    private final List<SignalPoint> signalPoints = new ArrayList<>();

    public void add(SignalPoint signalPoint) {
        signalPoints.add(signalPoint);
    }

    @Override
    public Iterator<SignalPoint> iterator() {
        return signalPoints.iterator();
    }

    public int size() {
        return signalPoints.size();
    }

    public boolean isEmpty() {
        return signalPoints.isEmpty();
    }

}
