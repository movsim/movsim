package org.movsim.roadmappings;

/**
 * <p>
 * Class representing a position in space plus a direction.
 * </p>
 * <p>
 * Angles are interpreted as in the Argand diagram, that is they are measured in a counter-clockwise direction from the x-axis (3 o'clock
 * position).
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

    @Override
    public String toString() {
        return "PosTheta [x=" + x + ", y=" + y + ", cosTheta=" + cosTheta + ", sinTheta=" + sinTheta + "]";
    }

}
