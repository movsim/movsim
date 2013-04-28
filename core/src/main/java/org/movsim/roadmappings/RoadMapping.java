package org.movsim.roadmappings;

import java.util.ArrayList;

import org.movsim.simulator.vehicles.Vehicle;

public interface RoadMapping {

    /**
     * 
     * @param roadPos
     * @param lateralOffset
     *            offset from center of road, used mainly for drawing roadlines and road edges
     * @return a PosTheta object giving position and direction
     */
    PosTheta map(double roadPos, double lateralOffset);

    /**
     * Map a longitudinal position on the road onto a position and direction in real space.
     * 
     * @param roadPos
     * @return posTheta giving position and direction in real space
     */
    PosTheta map(double roadPos);

    /**
     * Sets the minimum traffic lane. Lanes with <code>lane &lt; trafficLaneMin</code> are not traffic lanes and may be
     * treated differently, especially for lane changes.
     * 
     * @param trafficLaneMin
     */
    // void setTrafficLaneMin(int trafficLaneMin);

    /**
     * Returns the minimum traffic lane.
     * 
     * @return the minimum traffic lane
     */
    // int trafficLaneMin();

    /**
     * Sets the maximum traffic lane. Lanes with <code>lane &gt; trafficLaneMax</code> are not traffic lanes and may be
     * treated differently, especially for lane changes.
     * 
     * @param trafficLaneMax
     */
    // void setTrafficLaneMax(int trafficLaneMax);

    /**
     * Returns the maximum traffic lane.
     * 
     * @return the maximum traffic lane
     */
    // int trafficLaneMax();

    /**
     * Convenience function, returns the start position of the road.
     * 
     * @return start position of the road
     */
    PosTheta startPos();

    /**
     * Convenience function, returns the start position of the road for a given lateral offset.
     * 
     * @param lateralOffset
     * 
     * @return start position of the road for given lateral offset
     */
    PosTheta startPos(double lateralOffset);

    /**
     * Convenience function, returns the end position of the road.
     * 
     * @return end position of the road
     */
    PosTheta endPos();

    /**
     * Convenience function, returns the end position of the road for a given lateral offset.
     * 
     * @param lateralOffset
     * 
     * @return end position of the road for given lateral offset
     */
    PosTheta endPos(double lateralOffset);

    /**
     * Returns the length of the road.
     * 
     * @return road length, in meters
     */
    double roadLength();

    /**
     * Returns the width of the road.
     * 
     * @return road width, in meters
     */
    double roadWidth();

    /**
     * Sets the road color.
     * 
     * @param color
     */
    void setRoadColor(int color);

    /**
     * Returns the road color.
     * 
     * @return road color
     */
    int roadColor();

    /**
     * Returns the width of the lanes.
     * 
     * @return the width of the lanes, in meters
     */
    double laneWidth();

    /**
     * Returns the number of lanes.
     * 
     * @return number of lanes
     */
    int laneCount();

    /**
     * Returns the offset of the center of the lane.
     * 
     * @param lane
     * @return the offset of the center of the lane
     */
    double laneOffset(int lane);

    /**
     * Returns the offset of the inside edge of the lane.
     * 
     * @param lane
     * @return the offset of the inside edge of the lane
     */
    double laneInsideEdgeOffset(int lane);

    /**
     * Set a clipping region based on the road position and length. Simple implementation at the moment: only one
     * clipping region is supported.
     * 
     * @param pos
     *            position of the clipping region on the road
     * @param length
     *            length of the clipping region
     */
    void addClippingRegion(double pos, double length);

    /**
     * Returns an arraylist of the clipping polygons, or null if no clipping set.
     * 
     * @return arraylist of the clipping polygons, or null if no clipping set.
     */
    ArrayList<PolygonFloat> clippingPolygons();

    /**
     * Returns the outside clipping polygon.
     * 
     * @return the outside clipping polygon
     */
    PolygonFloat outsideClippingPolygon();

    PolygonFloat mapFloat(PosTheta posTheta, double length, double width);

    /**
     * Returns a polygon with its vertices at the corners of the subject vehicle.
     * 
     * @param vehicle
     * @param time
     *            current simulation time
     * @return polygon representing vehicle
     */
    PolygonFloat mapFloat(Vehicle vehicle, double time);

    boolean isPeer();
    /**
     * Polygon with integer coordinates.
     */
    class Polygon {
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
        Polygon(int pointCount) {
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
        PolygonFloat(int pointCount) {
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
     * Angles are interpreted as in the Argand diagram, that is they are measured in a counter-clockwise direction from the x-axis (3
     * o'clock position).
     * </p>
     */
    public class PosTheta {
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

}
