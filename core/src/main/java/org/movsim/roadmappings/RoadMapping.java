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
import java.util.Arrays;

import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.vehicles.Vehicle;

/**
 * A RoadMapping maps a logical road position (given by a lane and a position on a road segment) onto a physical
 * position, that is an x,y coordinate (given in meters).
 */
public abstract class RoadMapping {

    /**
     * 
     * @param roadPos
     * @param lateralOffset
     *            offset from center of road, used mainly for drawing roadlines and road edges
     * @return a PosTheta object giving position and direction
     */
    public abstract PosTheta map(double roadPos, double lateralOffset);

    // Immutable Properties
    protected LaneGeometries laneGeometries;

    // Road
    protected double roadLength;
    protected int roadColor;
    protected static int defaultRoadColor = 8421505; // gray

    // Positioning
    // pre-allocate single posTheta for the road mapping. This is shared and reused, so must be used
    // carefully.
    protected final PosTheta posTheta = new PosTheta();
    protected double x0;
    protected double y0;

    // Clipping Region
    protected static final int POINT_COUNT = 4;

    protected final PolygonFloat polygonFloat = new PolygonFloat(POINT_COUNT);
    protected ArrayList<PolygonFloat> clippingPolygons;
    protected PolygonFloat outsideClippingPolygon;

    protected final PolygonFloat lineFloat = new PolygonFloat(2);

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     * @param y0
     */
    protected RoadMapping(LaneGeometries laneGeometries, double x0, double y0) {
        this.x0 = x0;
        this.y0 = y0;
        this.laneGeometries = laneGeometries;
        roadColor = defaultRoadColor;
    }

    /**
     * Called when the system is running low on memory, and would like actively running process to try to tighten their
     * belts.
     */
    protected void onLowMemory() {
        // By default does nothing. Subclasses may implement memory saving.
    }

    /**
     * Returns the default road color.
     * 
     * @return the default road color
     */
    public static int defaultRoadColor() {
        return RoadMapping.defaultRoadColor;
    }

    /**
     * Sets the default road color.
     * 
     * @param defaultRoadColor
     */
    public static void setDefaultRoadColor(int defaultRoadColor) {
        RoadMapping.defaultRoadColor = defaultRoadColor;
    }

    /**
     * Convenience function, returns the start position of the road.
     * 
     * @return start position of the road
     */
    public PosTheta startPos() {
        return startPos(0.0);
    }

    /**
     * Convenience function, returns the start position of the road for a given lateral offset.
     * 
     * @param lateralOffset
     * 
     * @return start position of the road for given lateral offset
     */
    public PosTheta startPos(double lateralOffset) {
        return map(0.0, lateralOffset);
    }

    /**
     * Convenience function, returns the end position of the road.
     * 
     * @return end position of the road
     */
    public PosTheta endPos() {
        return endPos(0.0);
    }

    /**
     * Convenience function, returns the end position of the road for a given lateral offset.
     * 
     * @param lateralOffset
     * 
     * @return end position of the road for given lateral offset
     */
    public PosTheta endPos(double lateralOffset) {
        return map(roadLength, lateralOffset);
    }

    /**
     * Returns the end position of the ramp lane.
     * 
     * @return end position of the ramp lane
     */
    public PosTheta endPosRamp() {
        double lateralOffset = laneOffset(laneCount());
        return endPos(lateralOffset);
    }

    /**
     * Map a longitudinal position on the road onto a position and direction in real space.
     * 
     * @param roadPos
     * @return posTheta giving position and direction in real space
     */
    public PosTheta map(double roadPos) {
        return map(roadPos, 0.0);
    }

    /**
     * Returns the length of the road.
     * 
     * @return road length, in meters
     */
    public final double roadLength() {
        return roadLength;
    }

    /**
     * Returns the width of the road.
     * 
     * @return road width, in meters
     */
    public final double roadWidth() {
        return laneGeometries.getTotalLaneCount() * laneWidth();
    }

    /**
     * Sets the road color.
     * 
     * @param color
     */
    public final void setRoadColor(int color) {
        this.roadColor = color;
    }

    /**
     * Returns the road color.
     * 
     * @return road color
     */
    public final int roadColor() {
        return roadColor;
    }

    /**
     * Returns the width of the lanes.
     * 
     * @return the width of the lanes, in meters
     */
    public final double laneWidth() {
        return laneGeometries.getLaneWidth();
    }

    /**
     * Returns the number of lanes.
     * 
     * @return number of lanes
     */
    public int laneCount() {
        return laneGeometries.getTotalLaneCount();
    }

    /**
     * Returns the offset of the center of the lane. Fractional lanes are supported to facilitate the drawing of
     * vehicles in the process of changing lanes.
     * 
     * @param lane
     * @return the offset of the center of the lane
     */
    protected double laneOffset(double lane) {
        return lane == Lanes.NONE ? 0 : (lane - 1) * laneWidth();
    }

    /**
     * Returns the offset of the center of the lane.
     * 
     * @param lane
     * @return the offset of the center of the lane
     */
    public final double laneOffset(int lane) {
        return laneOffset((double) lane);
    }

    protected double laneCenterOffset(double lane) {
        return laneOffset(lane) + 0.5 * laneWidth();
    }

    /**
     * Set a clipping region based on the road position and length. Simple implementation at the moment: only one
     * clipping region is supported.
     * 
     * @param pos
     *            position of the clipping region on the road
     * @param length
     *            length of the clipping region
     */
    public void addClippingRegion(double pos, double length) {
        if (clippingPolygons == null) {
            clippingPolygons = new ArrayList<>();
        }
        if (outsideClippingPolygon == null) {
            // !!! TODO - this is temporary code, need to fix clipping region
            // set up the outside clip polygon
            final float LARGE_NUMBER = 100000.0f;
            outsideClippingPolygon = new PolygonFloat(POINT_COUNT);
            outsideClippingPolygon.xPoints[0] = -LARGE_NUMBER;
            outsideClippingPolygon.yPoints[0] = -LARGE_NUMBER;
            outsideClippingPolygon.xPoints[1] = LARGE_NUMBER;
            outsideClippingPolygon.yPoints[1] = -LARGE_NUMBER;
            outsideClippingPolygon.xPoints[2] = LARGE_NUMBER;
            outsideClippingPolygon.yPoints[2] = LARGE_NUMBER;
            outsideClippingPolygon.xPoints[3] = -LARGE_NUMBER;
            outsideClippingPolygon.yPoints[3] = LARGE_NUMBER;
        }
        final PolygonFloat clippingPolygon = new PolygonFloat(POINT_COUNT);
        final double offset = 1.5 * laneCount() * laneWidth();
        PosTheta posTheta;
        posTheta = map(pos + length, -offset);
        clippingPolygon.xPoints[0] = (float) posTheta.x;
        clippingPolygon.yPoints[0] = (float) posTheta.y;
        posTheta = map(pos + length, offset);
        clippingPolygon.xPoints[1] = (float) posTheta.x;
        clippingPolygon.yPoints[1] = (float) posTheta.y;
        posTheta = map(pos, offset);
        clippingPolygon.xPoints[2] = (float) posTheta.x;
        clippingPolygon.yPoints[2] = (float) posTheta.y;
        posTheta = map(pos, -offset);
        clippingPolygon.xPoints[3] = (float) posTheta.x;
        clippingPolygon.yPoints[3] = (float) posTheta.y;
        clippingPolygons.add(clippingPolygon);
    }

    /**
     * Returns an arraylist of the clipping polygons, or null if no clipping set.
     * 
     * @return arraylist of the clipping polygons, or null if no clipping set.
     */
    public ArrayList<PolygonFloat> clippingPolygons() {
        return clippingPolygons;
    }

    /**
     * Returns the outside clipping polygon.
     * 
     * @return the outside clipping polygon
     */
    public PolygonFloat outsideClippingPolygon() {
        return outsideClippingPolygon;
    }

    // FIXME number of operations can be reduced for optimization
    public PolygonFloat mapFloat(PosTheta posTheta, double length, double width) {
        final double lca = 0.5 * length * posTheta.cosTheta;
        final double wsa = 0.5 * width * posTheta.sinTheta;
        // final double xbr = posTheta.x - 0.5 * (lca - wsa); // back right position
        // polygonFloat.xPoints[0] = (float) (xbr + lca); // front right
        // polygonFloat.xPoints[1] = (float) (xbr + lca + wsa); // front left
        // polygonFloat.xPoints[2] = (float) (xbr + wsa); // back left
        // polygonFloat.xPoints[3] = (float) xbr; // back right
        polygonFloat.xPoints[0] = (float) (posTheta.x + lca + wsa); // front right
        polygonFloat.xPoints[1] = (float) (posTheta.x + lca - wsa); // front left
        polygonFloat.xPoints[2] = (float) (posTheta.x - lca - wsa); // back left
        polygonFloat.xPoints[3] = (float) (posTheta.x - lca + wsa); // back right

        final double lsa = 0.5 * length * posTheta.sinTheta;
        final double wca = 0.5 * width * posTheta.cosTheta;
        // final double ybr = posTheta.y - 0.5 * (lsa + wca); // back right position
        polygonFloat.yPoints[0] = (float) (posTheta.y + lsa - wca); // front right
        polygonFloat.yPoints[1] = (float) (posTheta.y + lsa + wca); // front left
        polygonFloat.yPoints[2] = (float) (posTheta.y - lsa + wca); // back left
        polygonFloat.yPoints[3] = (float) (posTheta.y - lsa - wca); // back right

        return polygonFloat;
    }

    public PolygonFloat mapLine(PosTheta posTheta, double length) {
        final double wsa = length * posTheta.sinTheta;
        lineFloat.xPoints[0] = (float) (posTheta.x);
        lineFloat.xPoints[1] = (float) (posTheta.x - wsa);

        final double wca = length * posTheta.cosTheta;
        lineFloat.yPoints[0] = (float) (posTheta.y);
        lineFloat.yPoints[1] = (float) (posTheta.y + wca);
        return lineFloat;
    }

    /**
     * Returns a polygon with its vertices at the corners of the subject vehicle.
     * 
     * @param vehicle
     * @return polygon representing vehicle
     */
    public PolygonFloat mapFloat(Vehicle vehicle) {
        final PosTheta posTheta = map(vehicle.physicalQuantities().getMidPosition(),
                -laneCenterOffset(vehicle.getContinousLane()));
        return mapFloat(posTheta, vehicle.physicalQuantities().getLength(), vehicle.physicalQuantities().getWidth());
    }

    @SuppressWarnings("static-method")
    public boolean isPeer() {
        return false;
    }

    /**
     * Polygon with floating point coordinates.
     * 
     */
    public static class PolygonFloat {

        /**
         * Number of points in the polygon.
         */
        public int pointCount;
        /**
         * Array of x-coordinates of the polygon.
         */
        float xPoints[];
        /**
         * Array of y-coordinates of the polygon.
         */
        float yPoints[];

        /**
         * Constructor, allocate arrays for polygon points.
         * 
         * @param pointCount
         *            number of points in the polygon.
         */
        PolygonFloat(int pointCount) {
            this.pointCount = pointCount;
            xPoints = new float[pointCount];
            yPoints = new float[pointCount];
        }

        @Override
        public String toString() {
            return "PolygonFloat [pointCount=" + pointCount + ", xPoints=" + Arrays.toString(xPoints) + ", yPoints="
                    + Arrays.toString(yPoints) + "]";
        }

        public float getXPoint(int i) {
            return xPoints[i];
        }

        public float getYPoint(int i) {
            return -yPoints[i]; // transformed coordinates!
        }

    }

    @Override
    public String toString() {
        return "RoadMapping [LaneGeometries=" + laneGeometries + ", roadLength=" + roadLength + ", posTheta="
                + posTheta + ", x0=" + x0 + ", y0=" + y0 + "]";
    }

    /**
     * Returns the offset to the centerline of the road with different number of lanes in the two driving directions (RoadSegments). The
     * centerline of a road is defined by the reference line given in the xodr network specification.
     * 
     * @return the offset to the centerline in case of different lane counts
     */
    public double calcOffsetToCenterline() {
        int laneDiff = laneGeometries.getLeft().getLaneCount() - laneGeometries.getRight().getLaneCount();
        return 0.5 * laneDiff * laneGeometries.getLaneWidth();
    }

    public LaneGeometries getLaneGeometries() {
        return laneGeometries;
    }

    public double getMaxOffsetRight() {
        return -laneGeometries.getRight().getLaneCount() * laneGeometries.getLaneWidth();
    }

    public double getOffsetLeft(int lane) {
        return Math.min(lane, laneGeometries.getLeft().getLaneCount()) * laneGeometries.getLaneWidth();
    }

    public int getLaneCountInDirection() {
        return isPeer() ? laneGeometries.getLeft().getLaneCount() : laneGeometries.getRight().getLaneCount();

    }
}
