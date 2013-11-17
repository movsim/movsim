package org.movsim.roadmappings;

import com.google.common.base.Preconditions;

public class LaneGeometries {

    private LaneGeometry left;
    private LaneGeometry right;


    public LaneGeometries(){
        left = new LaneGeometry();
        right = new LaneGeometry();
    }

    public LaneGeometry getLeft() {
        return left;
    }

    public void setLeft(LaneGeometry left) {
        this.left = left;
    }

    public LaneGeometry getRight() {
        return right;
    }

    public void setRight(LaneGeometry right) {
        this.right = right;
    }

    public int getTotalLaneCount() {
        return left.getLaneCount() + right.getLaneCount();
    }

    public double getLaneWidth() {
        return right.getLaneWidth(); // consider only right lane's width at the moment
    }

    public static class LaneGeometry {

        private static final double DEFAULT_LANE_WIDTH = 5; // TODO
        private final int laneCount;
        private final double laneWidth;

        public LaneGeometry() {
            this.laneCount = 0;
            this.laneWidth = DEFAULT_LANE_WIDTH;
        }

        // // convenience constructor
        public LaneGeometry(int laneCount) {
            Preconditions.checkArgument(laneCount > 0, "must be > 0: laneCount =" + laneCount);
            this.laneCount = laneCount;
            this.laneWidth = DEFAULT_LANE_WIDTH;
        }

        public LaneGeometry(int laneCount, double laneWidth) {
            Preconditions.checkArgument(laneCount > 0, "must be > 0: laneCount =" + laneCount);
            Preconditions.checkArgument(laneWidth > 0, "must be > 0: laneWidth =" + laneWidth);
            this.laneCount = laneCount;
            this.laneWidth = laneWidth;
        }

        public int getLaneCount() {
            return laneCount;
        }

        public double getLaneWidth() {
            return laneWidth;
        }

        @Override
        public String toString() {
            return "LaneGeometry [laneCount=" + laneCount + ", laneWidth=" + laneWidth + "]";
        }
    }

    @Override
    public String toString() {
        return "LaneGeometries [left=" + left + ", right=" + right + "]";
    }

}
