package org.movsim.simulator.vehicles;

import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel.LaneChangeDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class LateralModel {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LateralModel.class);

    /** needs to be > 0 */
    private final static double FINITE_LANE_CHANGE_TIME_S = 7;

    static {
        // needs to be > 0 to avoid lane-changing over 2 lanes in one update step
        Preconditions.checkArgument(FINITE_LANE_CHANGE_TIME_S > 0);
    }

    public static final int LANE_NOT_SET = -1;

    /** can be null */
    private LaneChangeModel laneChangeModel;

    private int lane = LANE_NOT_SET;
    private int laneOld;

    private final Vehicle vehicle;

    public LateralModel(Vehicle vehicle) {
        this.vehicle = Preconditions.checkNotNull(vehicle);
    }

    /**
     * variable for remembering new target lane when assigning to new
     * laneSegment
     */
    private int targetLane;

    /** finite lane-changing duration */
    private double tLaneChangeDelay;

    public final int lane() {
        return lane;
    }

    public final void setLane(int lane) {
        assert lane >= Lanes.MOST_INNER_LANE || lane == Lanes.OVERTAKING;
        assert this.lane != lane;
        laneOld = this.lane;
        this.lane = lane;
        targetLane = Lanes.NONE;
    }

    public int targetLane() {
        return targetLane;
    }

    private void setTargetLane(int targetLane) {
        assert targetLane >= Lanes.MOST_INNER_LANE || targetLane == Lanes.OVERTAKING;
        assert targetLane != lane;
        this.targetLane = targetLane;
    }

    public int oldLane() {
        return laneOld;
    }

    public void setOldLane(int oldLane) {
        this.laneOld = oldLane;
    }

    public LaneChangeModel getLaneChangeModel() {
        return laneChangeModel;
    }

    public void setLaneChangeModel(LaneChangeModel lcModel) {
        this.laneChangeModel = lcModel;
        if (laneChangeModel != null) {
            laneChangeModel.initialize(vehicle);
        }
    }

    public boolean hasLaneChangeModel() {
        return laneChangeModel != null;
    }

    public boolean inProcessOfLaneChange() {
        return (tLaneChangeDelay > 0 && tLaneChangeDelay < FINITE_LANE_CHANGE_TIME_S);
    }

    private void resetDelay(double dt) {
        tLaneChangeDelay = 0;
        updateLaneChangeDelay(dt); // TODO hack that updateLaneChangeDelay must
                                   // be called for inProcessOfLaneChange being
                                   // true
    }

    /**
     * Update lane changing delay.
     * 
     * @param dt
     *            the dt
     */
    public void updateLaneChangeDelay(double dt) {
        tLaneChangeDelay += dt;
    }

    public double continousLane() {
        if (inProcessOfLaneChange()) {
            double fractionTimeLaneChange = Math.min(1, tLaneChangeDelay / FINITE_LANE_CHANGE_TIME_S);
            return fractionTimeLaneChange * lane + (1 - fractionTimeLaneChange) * laneOld;
        }
        return lane();
    }

    public boolean considerOvertakingViaPeer(double dt, RoadSegment roadSegment) {
        LaneChangeDecision lcDecision = LaneChangeDecision.NONE;
        if (!roadSegment.hasPeer() || roadSegment.laneCount() > 1 || lane() != Lanes.MOST_INNER_LANE
                || !hasLaneChangeModel() || !getLaneChangeModel().isInitialized() || inProcessOfLaneChange()) {
            return false;
        }
        lcDecision = getLaneChangeModel().makeDecisionForOvertaking(roadSegment);
        if (lcDecision == LaneChangeDecision.OVERTAKE_VIA_PEER) {
            setTargetLane(Lanes.OVERTAKING);
            resetDelay(dt);
            LOG.debug("do overtaking lane change to={} into target lane={}", lcDecision, targetLane());
        }
        return lcDecision == LaneChangeDecision.OVERTAKE_VIA_PEER;
    }

    public boolean considerFinishOvertaking(double dt, LaneSegment laneSegment) {
        assert lane() == Lanes.OVERTAKING;
        assert !inProcessOfLaneChange();
        if (!hasLaneChangeModel() || !getLaneChangeModel().isInitialized()) {
            return false;
        }
        LaneChangeDecision lcDecision = laneChangeModel.finishOvertakingViaPeer(laneSegment);
        if (lcDecision == LaneChangeDecision.MANDATORY_TO_RIGHT) {
            setTargetLane(Lanes.MOST_INNER_LANE);
            resetDelay(dt);
            LOG.debug("finish overtaking, turn from lane={} into target lane={}", laneOld, targetLane);
            return true;
        }
        return false;
    }

    public boolean considerLaneChange(double dt, RoadSegment roadSegment) {

        if (roadSegment.laneCount() <= 1) {
            // no lane-changing decision necessary for one-lane road. already
            // checked before
            return false;
        }

        // no lane changing when not configured in xml.
        if (laneChangeModel == null || !laneChangeModel.isInitialized()) {
            return false;
        }
        assert !inProcessOfLaneChange();

        // if not in lane-changing process do determine if new lane is more
        // attractive and lane change is possible
        LaneChangeDecision lcDecision = laneChangeModel.makeDecision(roadSegment);
        final int laneChangeDirection = lcDecision.getDirection();

        // initiates a lane change: set targetLane to new value the lane will be
        // assigned by the vehicle container !!
        if (laneChangeDirection != Lanes.NO_CHANGE) {
            setTargetLane(lane + laneChangeDirection);
            resetDelay(dt);
            LOG.debug("do lane change to={} into target lane={}", laneChangeDirection, targetLane);
            return true;
        }
        return false;
    }

}
