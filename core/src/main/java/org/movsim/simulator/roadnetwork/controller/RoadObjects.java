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

package org.movsim.simulator.roadnetwork.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.controller.RoadObject.RoadObjectType;

import java.util.*;

/**
 * Holds the {@link RoadObject}s of a {@link RoadSegment} in ascending order along the road. Provides iterators for single
 * {@link RoadObjectType}s and over all objects.
 */
public final class RoadObjects implements Iterable<RoadObject> {

    final RoadSegment roadSegment;

    final Map<RoadObjectType, SortedSet<RoadObject>> roadObjects = new EnumMap<>(RoadObjectType.class);

    public RoadObjects(RoadSegment roadSegment) {
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        initMap();
    }

    private void initMap() {
        for (RoadObjectType type : EnumSet.allOf(RoadObjectType.class)) {
            roadObjects.put(type, new TreeSet<RoadObject>());
        }
    }

    public void add(RoadObject roadObject) {
        Preconditions.checkNotNull(roadObject);
        SortedSet<RoadObject> sortedSet = roadObjects.get(roadObject.getType());
        if (!sortedSet.subSet(roadObject, roadObject).isEmpty()) {
            throw new IllegalStateException(
                    "cannot have identical positions of same type of roadObjects=" + roadObject.position());
        }
        if (!sortedSet.add(roadObject)) {
            throw new IllegalStateException("cannot add roadObject=" + roadObject);
        }
    }

    public boolean hasRoadObject(RoadObjectType type) {
        return !roadObjects.get(type).isEmpty();
    }

    public boolean hasRoadObjects() {
        for (SortedSet<RoadObject> set : roadObjects.values()) {
            if (!set.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends RoadObject> Iterator<T> iterator(RoadObjectType type) {
        return Iterators.unmodifiableIterator((Iterator<T>) roadObjects.get(type).iterator());
    }

    @SuppressWarnings("unchecked")
    public <T extends RoadObject> Iterable<T> values(RoadObjectType type) {
        return Iterables.unmodifiableIterable((Iterable<T>) roadObjects.get(type));
    }

    @Override
    public Iterator<RoadObject> iterator() {
        return Iterators.unmodifiableIterator(Iterables.concat(roadObjects.values()).iterator());
    }

}
