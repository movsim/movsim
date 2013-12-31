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

package org.movsim.roadmappings;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * RoadMapping consisting of a number of consecutive straight sections of road.
 */
public class RoadMappingPolyLine extends RoadMapping implements Iterable<RoadMappingLine> {

    public static final int RELATIVE_POINTS = 0;
    public static final int ABSOLUTE_POINTS = 1;

    protected final ArrayList<RoadMappingLine> roadMappings = new ArrayList<>();

    @Override
    public Iterator<RoadMappingLine> iterator() {
        return roadMappings.iterator();
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     */
    RoadMappingPolyLine(LaneGeometries laneGeometries, double x0, double y0, double x1, double y1) {
        super(laneGeometries, x0, y0);
        final RoadMappingLine roadMapping = new RoadMappingLine(laneGeometries, x0, y0, x1, y1);
        roadLength = roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param s
     * @param x0
     * @param y0
     * @param theta
     * @param length
     */
    RoadMappingPolyLine(LaneGeometries laneGeometries, double s, double x0, double y0, double theta, double length) {
        super(laneGeometries, x0, y0);
        final RoadMappingLine roadMapping = new RoadMappingLine(laneGeometries, s, x0, y0, theta, length);
        roadLength = length;
        roadMappings.add(roadMapping);
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param valuesType
     * @param values
     */
    RoadMappingPolyLine(LaneGeometries laneGeometries, int valuesType, double[] values) {
        super(laneGeometries, values[0], values[1]);
        assert values.length % 2 == 0;

        final RoadMappingLine roadMapping = new RoadMappingLine(laneGeometries, values[0], values[1], values[2],
                values[3]);
        roadLength = roadMapping.roadLength();
        roadMappings.add(roadMapping);
        if (valuesType == RELATIVE_POINTS) {
            for (int i = 4; i < values.length; i += 2) {
                addPointRelative(values[i], values[i + 1]);
            }
        } else {
            for (int i = 4; i < values.length; i += 2) {
                addPoint(values[i], values[i + 1]);
            }
        }
    }

    /**
     * Called when the system is running low on memory, and would like actively running process to try to tighten their
     * belts.
     */
    @Override
    protected void onLowMemory() {
        roadMappings.trimToSize();
    }

    @Override
    public PosTheta startPos() {
        return roadMappings.get(0).startPos();
    }

    @Override
    public PosTheta startPos(double lateralOffset) {
        return roadMappings.get(0).startPos(lateralOffset);
    }

    @Override
    public PosTheta endPos() {
        return roadMappings.get(roadMappings.size() - 1).endPos();
    }

    @Override
    public PosTheta endPos(double lateralOffset) {
        return roadMappings.get(roadMappings.size() - 1).endPos(lateralOffset);
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {

        double pos = roadPos;
        for (final RoadMapping roadMapping : roadMappings) {
            if (pos <= roadMapping.roadLength()) {
                return roadMapping.map(pos, lateralOffset);
            }
            pos -= roadMapping.roadLength();
        }
        // have gone past end of last road mapping in road segment
        // this can happen by up to half a vehicle length - vehicle's rear position is
        // on road mapping, but vehicle's mid position (which is used for drawing) has
        // gone past the end, so fix this as a special case.
        final RoadMapping roadMapping = roadMappings.get(roadMappings.size() - 1);
        return roadMapping.map(pos + roadMapping.roadLength(), lateralOffset);
    }

    public void addPoint(double x, double y) {
        final RoadMapping lastRoadMapping = roadMappings.get(roadMappings.size() - 1);
        final RoadMappingLine roadMapping = new RoadMappingLine(lastRoadMapping, this.laneGeometries, x, y);
        roadLength += roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    public void addPoint(double s, double x0, double y0, double theta, double length) {
        final RoadMappingLine roadMapping = new RoadMappingLine(this.laneGeometries, s, x0, y0, theta, length);
        roadLength += length;
        roadMappings.add(roadMapping);
    }

    public void addPointRelative(double dx, double dy) {
        final RoadMapping lastRoadMapping = roadMappings.get(roadMappings.size() - 1);
        final PosTheta posTheta = lastRoadMapping.endPos();
        final RoadMappingLine roadMapping = new RoadMappingLine(lastRoadMapping, this.laneGeometries, posTheta.x + dx,
                posTheta.y + dy);
        roadLength += roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    public void movePoint(int index, double dx, double dy) {
        assert index > 1 && index <= roadMappings.size() - 2;
        final RoadMappingLine m0 = roadMappings.get(index - 1);
        m0.x1 += dx;
        m0.y1 += dy;
        m0.init();
        final RoadMappingLine m1 = roadMappings.get(index);
        m1.moveStart(dx, dy);
        m1.init();
        roadLength = 0.0;
        for (final RoadMapping roadMapping : roadMappings) {
            roadLength += roadMapping.roadLength();
        }
    }

    @Override
    public String toString() {
        return "RoadMappingPolyLine [roadMappings.size()=" + roadMappings.size() + "]";
    }
}
