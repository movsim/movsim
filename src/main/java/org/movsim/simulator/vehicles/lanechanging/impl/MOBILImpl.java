package org.movsim.simulator.vehicles.lanechanging.impl;

import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;
import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;


public class MOBILImpl {

    private double politeness; // politeness factor

    private double threshold; // changing threshold

    private double bSafe; // maximum safe braking decel

    private double gapMin; // minimum safe (net) distance

    private double biasRight; // bias (m/s^2) to drive

    // internal: save reference values
    private double thresholdRef;

    private double biasRightRef;

    private double bSafeRef;

    private double pRef;

    private final Vehicle me;

    public MOBILImpl(final Vehicle vehicle) {
        this.me = vehicle;

        // TODO handle this case with *no* <MOBIL> xml element

    }

    public MOBILImpl(final Vehicle vehicle, LaneChangingMobilData lcMobilData) {
        this.me = vehicle;
        // TODO Auto-generated constructor stub

        bSafeRef = bSafe = lcMobilData.getSafeDeceleration();
        biasRightRef = biasRight = lcMobilData.getRightBiasAcceleration();
        gapMin = lcMobilData.getMinimumGap();
        thresholdRef = threshold = lcMobilData.getThresholdAcceleration();
        pRef = politeness = lcMobilData.getPoliteness();

    }
    
    private boolean neigborsInProcessOfLaneChanging(final Vehicle v1, final Vehicle v2, final Vehicle v3 ){
        // finite delay criterion also for neigboring vehicles 
        final boolean oldFrontVehIsLaneChanging = (v1 == null) ? false : v1.inProcessOfLaneChanging();
        final boolean newFrontVehIsLaneChanging = (v2 == null) ? false : v2.inProcessOfLaneChanging();
        final boolean newBackVehIsLaneChanging  = (v3 == null) ? false : v3.inProcessOfLaneChanging();
        return  (oldFrontVehIsLaneChanging || newFrontVehIsLaneChanging || newBackVehIsLaneChanging);
    }


    private boolean safetyCheckGaps(double gapFront, double gapBack){
        return  ((gapFront < gapMin) || (gapBack < gapMin)) ;
    }
    
    private boolean safetyCheckAcceleration(double acc){
        return acc <= -bSafe;
    }

    
    public double calcAccelerationBalanceInNewLaneSymmetric(final VehicleContainer ownLane,
            final VehicleContainer newLane) {

        double prospectiveBalance = -Double.MAX_VALUE;

        final Vehicle newFront = newLane.getLeader(me);
        final Vehicle oldFront = ownLane.getLeader(me);
        final Vehicle newBack = newLane.getFollower(me);
        
        
        // check first if other vehicles are lane-changing
        if( neigborsInProcessOfLaneChanging(oldFront, newFront, newBack) ){
            return prospectiveBalance;
        }
        
        // safety: first check distances
        final double gapFront = me.getNetDistance(newFront);
        final double gapBack = (newBack == null) ? Constants.GAP_INFINITY : newBack.getNetDistance(me);
        
        if( safetyCheckGaps(gapFront, gapBack) ){
            return prospectiveBalance;
        }

        // safety: check (MOBIL) safety constraint for new follower
        final double newBackNewAcc = (newBack == null) ? 0 : newBack.getAccelerationModel().calcAcc(newBack, me);
       
        if( safetyCheckAcceleration(newBackNewAcc)){
            return prospectiveBalance;
        }
            
        // check now incentive criterion

        // old situation
        //final double meOldAcc = me.getAccelerationModel().calcAcc(me, oldFront);
        final double meOldAcc = me.getAccelerationModel().calcAcc(me, oldFront);
        
        
        // new situation
        final double meNewAcc = me.getAccelerationModel().calcAcc(me, newFront);
        
        final Vehicle oldBack = ownLane.getFollower(me);

        final double oldBackOldAcc = (oldBack != null) ? oldBack.getAccelerationModel().calcAcc(oldBack, me) : 0;
        final double oldBackNewAcc = (oldBack != null) ? oldBack.getAccelerationModel().calcAcc(oldBack, newFront) : 0;
        final double newBackOldAcc = (newBack != null) ? newBack.getAccelerationModel().calcAcc(newBack, newFront) : 0;

        // MOBIL trade-off for driver and neighborhood
        final double oldBackDiffAcc = oldBackNewAcc - oldBackOldAcc;
        final double newBackDiffAcc = newBackNewAcc - newBackOldAcc;
        final double meDiffAcc = meNewAcc - meOldAcc;

        
        // MOBIL's incentive formula
        final int changeTo = newLane.getLaneIndex() - ownLane.getLaneIndex();
        final double biasSign = (changeTo == Constants.TO_LEFT) ? 1 : -1;

        prospectiveBalance = meDiffAcc + politeness * (oldBackDiffAcc + newBackDiffAcc) - threshold - biasSign
                * biasRight;
        
        return prospectiveBalance;
    }

    public double getMinimumGap() {
        return gapMin;
    }

    public double getSafeDeceleration() {
        return bSafe;
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // Asymmetric MOBIL criterion for europeanRules
    // (consider that obstacles have no vicinity (return -2)!)
    // calc balance:
    // deltaMe+p*(deltaLeftHandVehicle)-threshold-bias
    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    private double calcAccelerationBalanceInNewLaneAsymmetric(final VehicleContainer ownLane, final VehicleContainer newLane){
    
    double prospectiveBalance = -Double.MAX_VALUE;

    final Vehicle newFront = newLane.getLeader(me);
    final Vehicle oldFront = ownLane.getLeader(me);
    final Vehicle newBack = newLane.getFollower(me);
    
    
    // check first if other vehicles are lane-changing
    if( neigborsInProcessOfLaneChanging(oldFront, newFront, newBack) ){
        return prospectiveBalance;
    }
    
    
    // safety: first check distances
    final double gapFront = me.getNetDistance(newFront);
    final double gapBack = (newBack == null) ? Constants.GAP_INFINITY : newBack.getNetDistance(me);
    
    if( safetyCheckGaps(gapFront, gapBack) ){
        return prospectiveBalance;
    }
    
    // safety: check (MOBIL) safety constraint for new follower
    final double newBackNewAcc = (newBack == null) ? 0 : newBack.getAccelerationModel().calcAcc(newBack, me);
   
    if( safetyCheckAcceleration(newBackNewAcc)){
        return prospectiveBalance;
    }
    
    
    
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        // (3)check now incentive criterion
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

        // calculate accelerations only for vehicle TO_LEFT
        // since back vehicle cannot improve its situation in fact!
        // so choose either new or old back vehicle depending on changeTo dir
        // newBackNewAcc already calculated above!

//        int iLeftHandBack = (changeTo == SimConstants.TO_LEFT) ? me.iBackLeft() : me.iBack();
//        IMoveableExt leftHandBack = (iLeftHandBack == -1) ? null : (IMoveableExt) microstreet.vehContainer().get(iLeftHandBack);
//
//        // IMoveableExt leftHandBack = (changeTo == SimConstants.TO_LEFT) ? me.vehBackLeft() : me.vehBack();
//
//        // int iLeftHandBackFront = (leftHandBack == null) ? -1 : leftHandBack.iFront();
//        // IMoveableExt leftHandBackFront = (iLeftHandBackFront < 0) ? null
//        // : (IMoveableExt) microstreet.vehContainer().get(iLeftHandBackFront);
//        IMoveableExt leftHandBackFront = (leftHandBack == null) ? null : leftHandBack.vehFront();
//
//        // int iLeftHandBackFrontLeft = (leftHandBack == null) ? -1 : leftHandBack.iFrontLeft();
//        // IMoveableExt leftHandBackFrontLeft = (iLeftHandBackFrontLeft < 0) ? null
//        // : (IMoveableExt) microstreet.vehContainer().get(iLeftHandBackFrontLeft);
//
//        IMoveableExt leftHandBackFrontLeft = (leftHandBack == null) ? null : leftHandBack.vehFrontLeft();
//
//        double accWithMe =
//                (changeTo == SimConstants.TO_LEFT) ? newBackNewAcc : calcMobilAcceleration(changeTo, alpha_T, 1, leftHandBack,
//                        me, leftHandBackFrontLeft);
//        double accWithoutMe =
//                calcMobilAcceleration(changeTo, alpha_T, 1, leftHandBack, leftHandBackFront, leftHandBackFrontLeft);
//
//        double deltaAccLeftHandBack =
//                (changeTo == SimConstants.TO_LEFT) ? (accWithMe - accWithoutMe) : (accWithoutMe - accWithMe);
//
//        // calculate the actual and prospective acc for subject "me"
//        // actual acceleration:
//
//        // int iFront = me.iFront();
//        // IMoveableExt front = (iFront == -1) ? null : microstreet.vehContainer().get(iFront);
//        IMoveableExt front = me.vehFront();
//        // int iFrontLeft = me.iFrontLeft();
//        // IMoveableExt frontLeft = (iFrontLeft == -1) ? null : microstreet.vehContainer().get(iFrontLeft);
//
//        IMoveableExt frontLeft = me.vehFront();
//
//        double meAcc = calcMobilAcceleration(changeTo, alpha_T, 1, me, front, frontLeft);
//
//        // int iFront = (changeTo == SimConstants.TO_LEFT) ? me.iFrontLeft() : me.iFrontRight();
//        // front = (iFront == -1) ? null : microstreet.vehContainer().get(iFront);
//        front = (changeTo == SimConstants.TO_LEFT) ? me.vehFrontLeft() : me.vehFrontRight();
//
//        // treat special case when left neigbour for NEW situation after
//        // change TO_LEFT is needed for europeanAcc ...
//        // iFrontLeft = (changeTo == SimConstants.TO_LEFT) ? me.iNextFrontLeft() : me.iFront();
//        // Logger.log("LaneChange.Asymmetric: nextFrontLeft = "+iFrontLeft);
//        // frontLeft = (iFrontLeft == -1) ? null : microstreet.vehContainer().get(iFrontLeft);
//        frontLeft = (changeTo == SimConstants.TO_LEFT) ? me.vehNextFrontLeft() : me.vehFront();
//
//        // arne 24-11-04: test for change to right rescaling of new distance
//        double myVel = me.vel();
//        double alpha_sLoc = ((changeTo == SimConstants.TO_RIGHT) && (!me.isTruck()) && (myVel > vCritEur)) ? alpha_s : 1;
//
//        double meNewAcc = calcMobilAcceleration(changeTo, alpha_T, alpha_sLoc, me, front, frontLeft);
//        double deltaAccMe = meNewAcc - meAcc;
//
//        // check for mandatory change
//        if (mandatoryLaneChange(changeTo, meNewAcc, me, newBack, microstreet.mostRightLane()))
//            return (ModelConstants.BMAX);
//
//        // (ii) MOBIL trade-off for driver and neigbourhood
//        // (gleichmaessiges Auffuellen der Spur:)
//        // vehicles already on the correct lane want to change
//        // if other lane is empty
//
//        // bias sign applies for euro as symmetric rules!!!
//        // but in case of mandatory lc the sign is modified!
//        double biasSign = (changeTo == SimConstants.TO_LEFT) ? 1 : -1;
//
//        // finally: all in one MOBIL's incentive formula:
//        prospectiveBalance = deltaAccMe + p * deltaAccLeftHandBack - threshold - biasSign * biasRight;
//// if(false && changeTo==SimConstants.TO_RIGHT)Logger.log(String.format("LC: id=%d, prospectiveBalance: changeTo=%2d " +
//// "  biasSign=%3.1f biasRight=%4.1f, deltaAccMe=%4.1f, pros.Balance=%4.1f %n",me.id(), changeTo, biasSign, biasRight,
//        // deltaAccMe, prospectiveBalance));
        return (prospectiveBalance);
    }

}
