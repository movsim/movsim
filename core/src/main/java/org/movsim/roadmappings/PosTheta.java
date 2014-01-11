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
    protected double x;
    /**
     * y-coordinate of point.
     */
    protected double y;
    /**
     * cosine of angle.
     */
    protected double cosTheta;
    /**
     * sine of angle.
     */
    protected double sinTheta;

    /**
     * Returns angle, in radians, measured counter-clockwise from x-axis.
     * 
     * @return angle, in radians, measured counter-clockwise from x-axis
     */
    protected double theta() {
        return Math.atan2(sinTheta, cosTheta);
    }

    @Override
    public String toString() {
        return "PosTheta [x=" + String.format("%.1f", x) + ", y=" + String.format("%.1f", y) + ", cosTheta="
                + String.format("%.1f", cosTheta) + ", sinTheta=" + String.format("%.1f", sinTheta) + "]";
    }

    public double getScreenX() {
        return x;
    }

    public double getScreenY() {
        return -y;
    }

    public double getTheta() {
        return Math.atan2(sinTheta, cosTheta);
    }

}
