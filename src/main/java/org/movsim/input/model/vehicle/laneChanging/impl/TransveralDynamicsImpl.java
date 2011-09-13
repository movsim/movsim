package org.movsim.input.model.vehicle.laneChanging.impl;

public class TransveralDynamicsImpl {

    
    private static final double FINITE_LANE_CHANGE_TIME_S = 5;
    
    private static final boolean WITH_INSTANT_LANECHANGE = true;

    private static final double ACTUAL_LANE_WIDTH = 3;

    private static final double A_LANECHANGE = 2.0; // transversal acceleration
                                                    // (m/s^2)

    private double vel_trans = 0;
    private double acc_trans = 0;
    
    private double tDelay;
    
    private int oldLane;
    private double currentContinousLane;
    private int targetLane;
   
    

    public TransveralDynamicsImpl() {

        tDelay = 0;
    }

    public void performLaneChange(int oldLane, int newLane) {
        this.oldLane = oldLane; // startLane
        currentContinousLane = oldLane;
        targetLane = newLane;
        resetDelay();
    }

    public void update(double dt) {
        tDelay += dt;

        // do trans update
        
        //updateTransverseTranslation(dt);

    }

    private void resetDelay() {
        tDelay = 0;
    }

    // public boolean laneChanging() {
    // return (laneChangeStatus() != NO_CHANGE);
    // }

    // public final int laneChangeStatus() {
    // double dir = targetLane - startLane;
    // if (dir > 0)
    // return (SimConstants.TO_RIGHT);
    // else
    // if (dir < 0) return (SimConstants.TO_LEFT);
    // // if(Math.abs(dir)>=2){
    // return (SimConstants.NO_CHANGE);
    // }

    // perform actual lane change
    // updates acc_trans, vel_trans, and (double-valued) lane=transv. position
    // could also be called accelerate_trans(dt) + translate_trans(dt)

    private void updateTransverseTranslation(double dt) {
        if (WITH_INSTANT_LANECHANGE) {
            currentContinousLane = targetLane;
            // laneChangeStatus=NO_CHANGE;
            oldLane = targetLane;
            testLaneChangeFinish();
        } else {
            if (Math.abs(targetLane - currentContinousLane) < 0.00001) {
            } else {
                boolean firstPhase = (Math.abs(targetLane - currentContinousLane) > 0.5 * Math
                        .abs(targetLane - oldLane));
                // acc_trans crucial for coffeemeter dynamics:
                acc_trans = (firstPhase) ? A_LANECHANGE * (targetLane - oldLane) : -0.5 * vel_trans * vel_trans
                        / ACTUAL_LANE_WIDTH / (targetLane - oldLane); // to
                                                                      // check
                                                                      // !!
                vel_trans += acc_trans * dt;
            }

            currentContinousLane += (vel_trans * dt - 0.5 * acc_trans * dt * dt) / ACTUAL_LANE_WIDTH;

            testLaneChangeFinish();

        }
    }

    // test if actual lane change is over
    // during changing, transversal velocity same sign as
    // (targetLane-startLane)

    private void testLaneChangeFinish() {
        boolean targetLaneReached = (Math.abs(targetLane - currentContinousLane) < 0.00001);
        boolean movingToTargetLane = (vel_trans * (targetLane - oldLane) >= 0);
        if (targetLaneReached || (!movingToTargetLane)) {
            resetDelay();
            oldLane = targetLane;
            currentContinousLane = targetLane; // eliminates erors from
                                               // integrating trans. mov.
            vel_trans = 0;
            acc_trans = 0;
        }
    }

    public boolean isLaneChanging() {
        return (tDelay<FINITE_LANE_CHANGE_TIME_S);
    }

}
