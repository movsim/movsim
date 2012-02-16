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

package org.movsim.simulator.roadnetwork;

import java.util.ArrayList;

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
    public abstract RoadMapping.PosTheta map(double roadPos, double lateralOffset);

    /**
     * Polygon with integer coordinates.
     */
    public static class Polygon {
        /**
         * Number of points in the polygon.
         */
        public int pointCount;
        /**
         * Array of x-coordinates of the polygon.
         */
        public int xPoints[];
        /**
         * Array of y-coordinates of the polygon.
         */
        public int yPoints[];

        /**
         * Constructor, allocate arrays for polygon points.
         * 
         * @param pointCount
         *            number of points in the polygon.
         */
        public Polygon(int pointCount) {
            this.pointCount = pointCount;
            xPoints = new int[pointCount];
            yPoints = new int[pointCount];
        }
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
        public float xPoints[];
        /**
         * Array of y-coordinates of the polygon.
         */
        public float yPoints[];

        /**
         * Constructor, allocate arrays for polygon points.
         * 
         * @param pointCount
         *            number of points in the polygon.
         */
        public PolygonFloat(int pointCount) {
            this.pointCount = pointCount;
            xPoints = new float[pointCount];
            yPoints = new float[pointCount];
        }
    }

    /**
     * <p>
     * Class representing a position in space plus a direction.
     * </p>
     * <p>
     * Angles are interpreted as in the Argand diagram, that is they are measured in a counter-clockwise direction from
     * the x-axis (3 o'clock position).
     * </p>
     */
    public static class PosTheta {
        /**
         * x-coordinate of point.
         */
        public double x;
        /**
         * y-coordinate of point.
         */
        public double y;
        /**
         * cosine of angle.
         */
        public double cosTheta;
        /**
         * sine of angle.
         */
        public double sinTheta;

        /**
         * Returns angle, in radians, measured counter-clockwise from x-axis.
         * 
         * @return angle, in radians, measured counter-clockwise from x-axis
         */
        public double theta() {
            return Math.atan2(sinTheta, cosTheta);
        }
    }

    // Immutable Properties
    protected final int laneCount;
    protected double laneWidth;
    protected final double roadWidth;
    // trafficLaneMin and trafficLaneMax set the range of lanes for normal traffic in the road
    // segment lanes less than trafficLaneMin or greater than trafficLaneMax are exit or entrance
    // ramps.
    private int trafficLaneMin;
    private int trafficLaneMax;
    // Road
    protected double roadLength;
    protected int roadColor;
    protected static final int ROAD_COLOR_NOT_SET = -2;
    protected static int defaultRoadColor = ROAD_COLOR_NOT_SET;
    // Positioning
    // pre-allocate single posTheta for the road mapping. This is shared and reused, so must be used
    // carefully.
    protected final PosTheta posTheta = new PosTheta();
    protected double x0;
    protected double y0;
    // Clipping Region
    protected static final int POINT_COUNT = 4;
    public static final int DEFAULT_LANE_WIDTH = 10;
    
    protected final PolygonFloat polygonFloat = new PolygonFloat(POINT_COUNT);
    protected ArrayList<PolygonFloat> clippingPolygons;
    protected PolygonFloat outsideClippingPolygon;

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     * @param y0
     */
    protected RoadMapping(int laneCount, double x0, double y0) {
        this.laneCount = laneCount;
        this.x0 = x0;
        this.y0 = y0;
        trafficLaneMin = Lane.LANE1;
        trafficLaneMax = laneCount;
        roadWidth = laneWidth * laneCount;
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
     * Sets the minimum traffic lane. Lanes with <code>lane &lt; trafficLaneMin</code> are not traffic lanes and may be
     * treated differently, especially for lane changes.
     * 
     * @param trafficLaneMin
     */
    public final void setTrafficLaneMin(int trafficLaneMin) {
        this.trafficLaneMin = trafficLaneMin;
    }

    /**
     * Returns the minimum traffic lane.
     * 
     * @return the minimum traffic lane
     */
    public final int trafficLaneMin() {
        return trafficLaneMin;
    }

    /**
     * Sets the maximum traffic lane. Lanes with <code>lane &gt; trafficLaneMax</code> are not traffic lanes and may be
     * treated differently, especially for lane changes.
     * 
     * @param trafficLaneMax
     */
    public final void setTrafficLaneMax(int trafficLaneMax) {
        this.trafficLaneMax = trafficLaneMax;
    }

    /**
     * Returns the maximum traffic lane.
     * 
     * @return the maximum traffic lane
     */
    public final int trafficLaneMax() {
        return trafficLaneMax;
    }

    /**
     * Convenience function, returns the start position of the road.
     * 
     * @return start position of the road
     */
    public RoadMapping.PosTheta startPos() {
        return map(0.0, 0.0);
    }

    /**
     * Convenience function, returns the start position of the road for a given lateral offset.
     * 
     * @param lateralOffset
     * 
     * @return start position of the road for given lateral offset
     */
    public RoadMapping.PosTheta startPos(double lateralOffset) {
        return map(0.0, lateralOffset);
    }

    /**
     * Convenience function, returns the end position of the road.
     * 
     * @return end position of the road
     */
    public RoadMapping.PosTheta endPos() {
        return map(roadLength, 0.0);
    }

    /**
     * Convenience function, returns the end position of the road for a given lateral offset.
     * 
     * @param lateralOffset
     * 
     * @return end position of the road for given lateral offset
     */
    public RoadMapping.PosTheta endPos(double lateralOffset) {
        return map(roadLength, lateralOffset);
    }

    /**
     * Returns the end position of the ramp lane.
     * 
     * @return end position of the ramp lane
     */
    public RoadMapping.PosTheta endPosRamp() {
        final double lateralOffset = laneOffset(0);
        return map(roadLength, lateralOffset);
    }

    /**
     * Map a longitudinal position on the road onto a position and direction in real space.
     * 
     * @param roadPos
     * @return posTheta giving position and direction in real space
     */
    public RoadMapping.PosTheta map(double roadPos) {
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
        return roadWidth;
    }

    /**
     * Sets the road color.
     * 
     * @param roadColor
     */
    public final void setRoadColor(int roadColor) {
        this.roadColor = roadColor;
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
        return laneWidth;
    }

    /**
     * Returns the number of lanes.
     * 
     * @return number of lanes
     */
    public final int laneCount() {
        return laneCount;
    }

    /**
     * Returns the offset of the center of the lane. Fractional lanes are supported to facilitate the drawing of
     * vehicles in the process of changing lanes.
     * 
     * @param lane
     * @return the offset of the center of the lane
     */
    private final double laneOffset(double lane) {
        return (0.5 * (trafficLaneMin + laneCount - 1) - lane) * laneWidth;
    }

    /**
     * Returns the offset of the center of the lane.
     * 
     * @param lane
     * @return the offset of the center of the lane
     */
    public final double laneOffset(int lane) {
        return lane == Lane.NONE ? 0.0 : (0.5 * (trafficLaneMin + laneCount - 1) - lane) * laneWidth;
    }

    /**
     * Returns the offset of the inside edge of the lane.
     * 
     * @param lane
     * @return the offset of the inside edge of the lane
     */
    public final double laneInsideEdgeOffset(int lane) {
        return (0.5 * (trafficLaneMin + laneCount) - lane) * laneWidth;
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
            clippingPolygons = new ArrayList<RoadMapping.PolygonFloat>();
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
        final double offset = 1.5 * laneCount * laneWidth;
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
    public ArrayList<RoadMapping.PolygonFloat> clippingPolygons() {
        return clippingPolygons;
    }

    /**
     * Returns the outside clipping polygon.
     * 
     * @return the outside clipping polygon
     */
    public RoadMapping.PolygonFloat outsideClippingPolygon() {
        return outsideClippingPolygon;
    }

    public RoadMapping.PolygonFloat mapFloat(RoadMapping.PosTheta posTheta, double length, double width) {

        final double lca = length * posTheta.cosTheta;
        final double wsa = width * posTheta.sinTheta;
        final double xbr = posTheta.x - 0.5 * (lca - wsa);
        polygonFloat.xPoints[0] = (float) (xbr + lca); // front right
        polygonFloat.xPoints[1] = (float) (xbr + lca - wsa); // front left
        polygonFloat.xPoints[2] = (float) (xbr - wsa); // back left
        polygonFloat.xPoints[3] = (float) xbr; // back right

        final double lsa = length * posTheta.sinTheta;
        final double wca = width * posTheta.cosTheta;
        final double ybr = posTheta.y + 0.5 * (lsa + wca);
        polygonFloat.yPoints[0] = (float) (ybr - lsa); // front right
        polygonFloat.yPoints[1] = (float) (ybr - wca - lsa); // front left
        polygonFloat.yPoints[2] = (float) (ybr - wca); // back left
        polygonFloat.yPoints[3] = (float) ybr; // back right
        return polygonFloat;
    }

    /**
     * Returns a polygon with its vertices at the corners of the subject vehicle.
     * 
     * @param vehicle
     * @param time
     *            current simulation time
     * @return polygon representing vehicle
     */
    public RoadMapping.PolygonFloat mapFloat(Vehicle vehicle, double time) {
        final RoadMapping.PosTheta posTheta = map(vehicle.getMidPosition(), laneOffset(vehicle.getContinousLane()));
        return mapFloat(posTheta, vehicle.getLength(), vehicle.getWidth());
    }
}
