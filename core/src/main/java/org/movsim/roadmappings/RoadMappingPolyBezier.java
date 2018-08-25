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
 * RoadMapping consisting of a number of consecutive Bezier curves.
 */
public class RoadMappingPolyBezier extends RoadMapping implements Iterable<RoadMappingBezier> {

    public static final int RELATIVE_POINTS = 0;
    public static final int ABSOLUTE_POINTS = 1;
    public static final int RELATIVE_CALCULATE_CONTROL_POINTS = 2;

    private final ArrayList<RoadMappingBezier> roadMappings = new ArrayList<>();

    @Override
    public Iterator<RoadMappingBezier> iterator() {
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
     * @param cX
     * @param cY
     */
    RoadMappingPolyBezier(LaneGeometries laneGeometries, double x0, double y0, double x1, double y1, double cX,
            double cY) {
        super(laneGeometries, x0, y0);
        final RoadMappingBezier roadMapping = new RoadMappingBezier(laneGeometries, x0, y0, x1, y1, cX, cY);
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
     * @param a
     * @param b
     * @param c
     * @param d
     */
    RoadMappingPolyBezier(LaneGeometries laneGeometries, double s, double x0, double y0, double theta, double length,
            double a, double b, double c, double d) {
        super(laneGeometries, x0, y0);
        final RoadMappingBezier roadMapping = new RoadMappingBezier(laneGeometries, s, x0, y0, theta, length, a, b, c,
                d);
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
    RoadMappingPolyBezier(LaneGeometries laneGeometries, int valuesType, double[] values) {
        super(laneGeometries, values[0], values[1]);
        assert ((valuesType == RELATIVE_CALCULATE_CONTROL_POINTS && values.length % 2 == 0) || values.length % 3 == 0);

        final RoadMappingBezier roadMapping = new RoadMappingBezier(laneGeometries, values[0], values[1], values[2],
                values[3], values[4], values[5]);
        roadLength = roadMapping.roadLength();
        roadMappings.add(roadMapping);
        if (valuesType == ABSOLUTE_POINTS) {
            for (int i = 6; i < values.length; i += 3) {
                addPoint(values[i], values[i + 1], values[i + 2]);
            }
        } else if (valuesType == RELATIVE_POINTS) {
            for (int i = 6; i < values.length; i += 3) {
                addPointRelative(values[i], values[i + 1], values[i + 2]);
            }
        } else {
            for (int i = 6; i < values.length; i += 2) {
                addPointRelative(values[i], values[i + 1]);
            }
        }
        // System.out.println("Bezier roadLength=" + (int)roadLength); //$NON-NLS-1$
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
        for (final RoadMappingBezier roadMapping : roadMappings) {
            if (pos <= roadMapping.roadLength()) {
                return roadMapping.map(pos, lateralOffset);
            }
            pos -= roadMapping.roadLength();
        }
        // have gone past end of last road mapping in road segment
        // this can happen by up to half a vehicle length - vehicle's rear position is
        // on road mapping, but vehicle's mid position (which is used for drawing) has
        // gone past the end, so fix this as a special case.
        final PosTheta posTheta = endPos(lateralOffset);
        posTheta.x += pos * posTheta.cosTheta;
        posTheta.y -= pos * posTheta.sinTheta;
        return posTheta;
    }

    public void addPoint(double x, double y, double t) {
        assert roadMappings.size() >= 1;
        final RoadMappingBezier lastRoadMapping = roadMappings.get(roadMappings.size() - 1);
        final RoadMappingBezier roadMapping = new RoadMappingBezier(lastRoadMapping, this.laneGeometries, x, y, t);
        roadLength += roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    public void addPoint(double s, double x0, double y0, double theta, double length, double a, double b, double c,
            double d) {
        final RoadMappingBezier roadMapping = new RoadMappingBezier(this.laneGeometries, s, x0, y0, theta, length, a,
                b, c, d);
        roadLength += length;
        roadMappings.add(roadMapping);
    }

    public void addPointRelative(double dx, double dy, double t) {
        assert roadMappings.size() >= 1;
        final RoadMappingBezier lastRoadMapping = roadMappings.get(roadMappings.size() - 1);
        final PosTheta posTheta = lastRoadMapping.endPos();
        final RoadMappingBezier roadMapping = new RoadMappingBezier(lastRoadMapping, this.laneGeometries, posTheta.x
                + dx, posTheta.y + dy, t);
        roadLength += roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    public void addPointRelative(double dx, double dy) {
        assert roadMappings.size() >= 1;
        final RoadMappingBezier lastRoadMapping = roadMappings.get(roadMappings.size() - 1);
        PosTheta posTheta = lastRoadMapping.startPos();
        final double startX = posTheta.x;
        final double startY = posTheta.y;
        posTheta = lastRoadMapping.endPos();
        final double ldx = startX - posTheta.x;
        final double ldy = startY - posTheta.y;
        final double lastChordLength = Math.sqrt(ldx * ldx + ldy * ldy);
        final double chordLength = Math.sqrt(dx * dx + dy * dy);
        // System.out.println("Bezier chordLength=" + (int)chordLength); //$NON-NLS-1$
        // System.out.println("Bezier lastChordLength=" + (int)lastChordLength); //$NON-NLS-1$
        final double t = chordLength * chordLength / (chordLength + lastChordLength);
        final RoadMappingBezier roadMapping = new RoadMappingBezier(lastRoadMapping, this.laneGeometries, posTheta.x
                + dx, posTheta.y + dy, t);
        roadLength += roadMapping.roadLength();
        roadMappings.add(roadMapping);
    }

    public void movePoint(int index, double dx, double dy) {
        assert index > 1 && index <= roadMappings.size() - 2;
        final RoadMappingBezier m0 = roadMappings.get(index - 1);
        m0.p2x += dx;
        m0.p2y += dy;
        m0.init();
        final RoadMappingBezier m1 = roadMappings.get(index);
        m1.p0x += dx;
        m1.p0y += dy;
        m1.init();
        roadLength = 0.0;
        for (final RoadMapping roadMapping : roadMappings) {
            roadLength += roadMapping.roadLength();
        }
    }
}
