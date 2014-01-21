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

import java.util.Arrays;

/**
 * Road mapping defined by a quadratic Bezier curve.
 */
public class RoadMappingBezier extends RoadMapping {

    // Bezier curve endpoints and control point
    protected double p0x;
    protected double p0y;
    protected double p1x;
    protected double p1y;
    protected double p2x;
    protected double p2y;
    // arc length parameterization
    protected int S_COUNT = 10;
    protected double sValues[];

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     *            x-position of start of curve
     * @param y0
     *            y-position of start of curve
     * @param x1
     *            x-position of end of curve
     * @param y1
     *            y-position of end of curve
     * @param cX
     *            x-position of control point
     * @param cY
     *            y-position of control point
     */
    RoadMappingBezier(LaneGeometries laneGeometries, double x0, double y0, double x1, double y1, double cX, double cY) {
        super(laneGeometries, x0, y0);
        p0x = x0;
        p0y = y0;
        p1x = cX;
        p1y = cY;
        p2x = x1;
        p2y = y1;
        roadLength = bezierLength();
        assert !Double.isNaN(roadLength);
    }

    protected void init() {
        roadLength = bezierLength();
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     *            number of lanes in road mapping
     * @param s
     * @param x0
     *            x-position of start of curve
     * @param y0
     *            y-position of start of curve
     * @param theta
     *            direction of curve
     * @param length
     *            length of curve
     * @param a
     * @param b
     * @param c
     * @param d
     */
    RoadMappingBezier(LaneGeometries laneGeometries, double s, double x0, double y0, double theta, double length,
            double a, double b, double c, double d) {
        super(laneGeometries, x0, y0);
        p0x = x0;
        p0y = y0;
        p2x = a;
        p2y = b;
        posTheta.sinTheta = Math.sin(theta);
        posTheta.cosTheta = Math.cos(theta);
        // final double t = length / 2.0;
        p1x = c;
        p1y = d;
        roadLength = bezierLength();
    }

    /**
     * Constructor to append a bezier road mapping onto a previously existing road mapping, matching the endpoints and
     * tangents at the endpoints.
     * 
     * @param roadMapping
     *            the road mapping to append to
     * @param x1
     *            new point, x coordinate
     * @param y1
     *            new point, y coordinate
     * @param t
     *            single degree of freedom in setting the control point
     */
    RoadMappingBezier(RoadMapping roadMapping, LaneGeometries laneGeometries, double x1, double y1, double t) {
        super(laneGeometries, 0, 0);
        final PosTheta posTheta = roadMapping.endPos();
        p0x = posTheta.x;
        p0y = posTheta.y;
        p1x = posTheta.x + t * posTheta.cosTheta;
        p1y = posTheta.y - t * posTheta.sinTheta;
        p2x = x1;
        p2y = y1;
        // System.out.println("Bezier t=" + (int)t); //$NON-NLS-1$
        // System.out.println("Bezier C=" + (int)p1x + ", " + (int)p1y); //$NON-NLS-1$ //$NON-NLS-2$
        roadLength = bezierLength();
        // road length must be longer than a that of a straight line connecting the two endpoints
        assert roadLength >= Math.sqrt((p2x - p0x) * (p2x - p0x) + (p2y - p0y) * (p2y - p0y));
    }

    // private double controlT() {
    // final PosTheta posTheta = startPos();
    // if (posTheta.cosTheta == 0.0) {
    // return (-p1y + posTheta.y) / posTheta.sinTheta;
    // }
    // return (p1x - posTheta.x) / posTheta.cosTheta;
    // }

    /**
     * Returns the x-coordinate of the control point.
     * 
     * @param lateralOffset
     * @return x-coordinate of the control point
     */
    public double controlX(double lateralOffset) {
        final double opp = p2y - p0y;
        final double adj = p2x - p0x;
        final double h = Math.sqrt(opp * opp + adj * adj);
        return p1x - lateralOffset * opp / h;
    }

    /**
     * Returns the y-coordinate of the control point.
     * 
     * @param lateralOffset
     * @return y-coordinate of the control point
     */
    public double controlY(double lateralOffset) {
        final double opp = p2y - p0y;
        final double adj = p2x - p0x;
        final double h = Math.sqrt(opp * opp + adj * adj);
        return p1y + lateralOffset * adj / h;
    }

    protected RoadMappingBezier(LaneGeometries laneGeometries, double x0, double y0) {
        super(laneGeometries, x0, y0);
        p0x = x0;
        p0y = y0;
    }

    @Override
    public PosTheta startPos() {
        posTheta.x = p0x;
        posTheta.y = p0y;
        final double opp = p1y - p0y;
        final double adj = p1x - p0x;
        final double h = Math.sqrt(opp * opp + adj * adj);
        posTheta.sinTheta = -opp / h;
        posTheta.cosTheta = adj / h;
        return posTheta;
    }

    @Override
    public PosTheta endPos() {
        posTheta.x = p2x;
        posTheta.y = p2y;
        final double opp = p2y - p1y;
        final double adj = p2x - p1x;
        final double h = Math.sqrt(opp * opp + adj * adj);
        posTheta.sinTheta = -opp / h;
        posTheta.cosTheta = adj / h;
        return posTheta;
    }

    @Override
    public PosTheta endPos(double lateralOffset) {
        final PosTheta posTheta = endPos();
        // adjust for the lateral offset
        posTheta.x += lateralOffset * posTheta.sinTheta;
        posTheta.y += lateralOffset * posTheta.cosTheta;

        return posTheta;
    }

    /**
     * Arc length parameterization. Convert from road position (arc length) to natural Bezier parameter(t) using linear
     * interpolation of pre-computed arc lengths.
     * 
     * @param roadPos
     * @return natural Bezier parameter
     */
    protected double roadPosToT(double roadPos) {
        // see http://www.planetclegg.com/projects/WarpingTextToSplines.html
        // and http://www.algorithmist.net/arclengthparam.html
        int index = Arrays.binarySearch(sValues, roadPos);
        if (index >= 0) {
            // exact match found
            return ((double) index) / (S_COUNT - 1);
        }
        // index == -(insertion point) - 1
        index = -index - 1;
        if (index >= S_COUNT) {
            return 1.0;
        }
        final double p0 = sValues[index - 1];
        final double p1 = sValues[index];
        final double prop = (roadPos - p0) / (p1 - p0);
        final double ret = (index + prop - 1) / (S_COUNT - 1);
        return ret;
        // return roadPos / roadLength;

        // simple linear search of arc length array (binary search would be better).
        // int i = 0;
        // while (sValues[i] <= roadPos) {
        // i++;
        // if (i == S_COUNT) {
        // return 1.0;
        // }
        // }
        // if (i == 0) {
        // return 0.0;
        // }
        // double p0 = sValues[i - 1];
        // double p1 = sValues[i];
        // double prop = (roadPos - p0) / (p1 - p0);
        // return (i + prop - 1) / (S_COUNT - 1);
    }

    @Override
    public PosTheta map(double roadPos, double lateralOffset) {
        final double t = roadPosToT(roadPos);
        bezier(t);
        // and finally adjust for the lateral offset
        posTheta.x += lateralOffset * posTheta.sinTheta;
        posTheta.y += lateralOffset * posTheta.cosTheta;

        return posTheta;
    }

    private PosTheta bezier(double t) {
        // see http://www.cubic.org/docs/bezier.htm for a good visual explanation of the
        // the DeCasteljau algorithm for evaluating points on a Bezier curve
        // calculate the interpolated point between p0 and p1
        final double m0x = (1 - t) * p0x + t * p1x;
        final double m0y = (1 - t) * p0y + t * p1y;
        // calculate the interpolated point between p1 and p2
        final double m1x = (1 - t) * p1x + t * p2x;
        final double m1y = (1 - t) * p1y + t * p2y;
        // and then interpolate between these points
        posTheta.x = (1 - t) * m0x + t * m1x;
        posTheta.y = (1 - t) * m0y + t * m1y;

        // the tangent falls out naturally
        final double opp = m1y - m0y;
        final double adj = m1x - m0x;
        final double h = Math.sqrt(opp * opp + adj * adj);
        posTheta.sinTheta = -opp / h;
        posTheta.cosTheta = adj / h;
        return posTheta;
    }

    private PosTheta bezierPos(double t) {
        // see http://www.cubic.org/docs/bezier.htm for a good visual explanation of the
        // the DeCasteljau algorithm for evaluating points on a Bezier curve
        // calculate the interpolated point between p0 and p1
        final double m0x = (1 - t) * p0x + t * p1x;
        final double m0y = (1 - t) * p0y + t * p1y;
        // calculate the interpolated point between p1 and p2
        final double m1x = (1 - t) * p1x + t * p2x;
        final double m1y = (1 - t) * p1y + t * p2y;
        // and then interpolate between these points
        posTheta.x = (1 - t) * m0x + t * m1x;
        posTheta.y = (1 - t) * m0y + t * m1y;
        return posTheta;
    }

    private double bezierLength() {
        if (sValues == null) {
            // pre-compute arc lengths along curve, for use in arc-length parameterization
            sValues = new double[S_COUNT];
            final double dt = 1.0 / (S_COUNT - 1);
            double t = 0.0;
            sValues[0] = 0.0;
            PosTheta p = bezierPos(t);
            double x0 = p.x;
            double y0 = p.y;
            for (int i = 1; i < S_COUNT; ++i) {
                t += dt;
                p = bezierPos(t);
                final double dx = x0 - p.x;
                final double dy = y0 - p.y;
                sValues[i] = sValues[i - 1] + Math.sqrt(dx * dx + dy * dy);
                x0 = p.x;
                y0 = p.y;
            }
            // correct pre-computed arc lengths
            final double estimatedLength = sValues[S_COUNT - 1];
            final double correction = bezierLengthComputed() / estimatedLength;
            for (int i = 1; i < S_COUNT; ++i) {
                sValues[i] *= correction;
            }
        }
        return sValues[S_COUNT - 1];
    }

    private double bezierLengthComputed() {
        // see http://segfaultlabs.com/docs/quadratic-bezier-curve-length
        // useful info also at
        // http://algorithmist.wordpress.com/2009/01/05/quadratic-bezier-arc-length/
        final double ax = p0x - 2 * p1x + p2x;
        final double ay = p0y - 2 * p1y + p2y;
        final double bx = 2 * p1x - 2 * p0x;
        final double by = 2 * p1y - 2 * p0y;
        final double A = 4 * (ax * ax + ay * ay);
        if (A == 0.0) {
            // if A is 0, then control point is in line with endpoints, so we have a straight line
            final double dx = p2x - p0x;
            final double dy = p2y - p0y;
            return Math.sqrt(dx * dx + dy * dy);
        }
        final double B = 4 * (ax * bx + ay * by);
        final double C = bx * bx + by * by;

        final double Sabc = 2 * Math.sqrt(A + B + C);
        final double A_2 = Math.sqrt(A);
        final double A_32 = 2 * A * A_2;
        final double C_2 = 2 * Math.sqrt(C);
        final double BA = B / A_2;

        final double P = A_32 * Sabc + A_2 * B * (Sabc - C_2);
        final double X = 4 * C * A - B * B;
        double Q = 0.0;
        // checks to avoid dividing by zero, or taking log of zero
        if (X != 0.0) {
            if (2 * A_2 + Sabc - C_2 != 0.0) { // equivalent to if (Y != Z)
                final double Y = 2 * A_2 + BA + Sabc;
                final double Z = BA + C_2;
                assert Z != 0.0;
                Q = X * Math.log(Y / Z);
            }
        }
        assert A_32 != 0.0;
        return (P + Q) / (4 * A_32);
    }

}
