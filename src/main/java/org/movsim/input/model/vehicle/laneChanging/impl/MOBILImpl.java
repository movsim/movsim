package org.movsim.input.model.vehicle.laneChanging.impl;

import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;
import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.impl.VehicleImpl;

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


    public double calcAccelerationBalanceInNewLaneSymmetric(final VehicleContainer ownLane, final VehicleContainer newLane) {

        // apply only in case of mandatory lane change!!!
        // double alpha_T = (mandatoryChange==NO_CHANGE)? 1 :
        // ALPHA_T_AFTER_LANECHANGE;

        double prospectiveBalance = -Double.MAX_VALUE;

        // (1) check "karenzzeit" of vehicles ahead
        final Vehicle newFront = newLane.getLeader(me);
        
        final Vehicle oldFront = ownLane.getLeader(me); 

//        if (!lastChangeSufficientlyLongAgo(newFront, oldFront))
//            return (prospectiveBalance);

        // (2) safety incentive (in two steps)

        final Vehicle newBack = newLane.getFollower(me);

        double gapFront = me.getNetDistance(newFront);
        double gapBack = (newBack == null) ? Constants.GAP_INFINITY : newBack.getNetDistance(me);

        // (i) first check distances
        // negative net distances possible because of different veh lengths!
        if ((gapFront < gapMin) || (gapBack < gapMin)) {
            return prospectiveBalance;
        }

        final double newBackNewAcc = (newBack == null) ? 0 : newBack.getAccelerationModel().calcAcc(newBack, me); 
            
        // (ii) check (MOBIL) security constraint for new follower

        if ( newBackNewAcc <= -bSafe ) {
            return prospectiveBalance;
        }

        // (3)check now incentive criterion
         
        
        final double meNewAcc = me.getAccelerationModel().calcAcc(me, newFront);
        final double meOldAcc = me.getAccelerationModel().calcAcc(me, oldFront);

        // check for mandatory change
//        if (mandatoryLaneChange(changeTo, meNewAcc, me, newBack, microstreet.mostRightLane()))
//            return (ModelConstants.BMAX);

        // calculate accelerations of new and old back vehicle
        // for actual and prospective situation
        // newBackNewAcc already calculated above
        // int iOldBack = me.iBack();
        // IMoveableExt oldBack = (iOldBack == -1) ? null : microstreet.vehContainer().get(iOldBack);
        final Vehicle oldBack = ownLane.getFollower(me);

        final double oldBackOldAcc = (oldBack != null) ? oldBack.getAccelerationModel().calcAcc(oldBack, me) : 0;
        final double oldBackNewAcc = (oldBack != null) ? oldBack.getAccelerationModel().calcAcc(oldBack, newFront) : 0;
        final double newBackOldAcc = (newBack != null) ? newBack.getAccelerationModel().calcAcc(newBack, newFront) : 0;

        // (ii) MOBIL trade-off for driver and neigbourhood
        // (gleichmaessiges Auffuellen der Spur:)
        // vehicles already on the correct lane want to change
        // if other lane is empty
        final double oldBackDiffAcc = oldBackNewAcc - oldBackOldAcc;
        final double newBackDiffAcc = newBackNewAcc - newBackOldAcc;
        final double meDiffAcc = meNewAcc - meOldAcc;

        // bias sign applies for euro as symmetric rules!!!
        // but in case of mandatory lc the sign is modified!
        final int changeTo = newLane.getLaneIndex() - ownLane.getLaneIndex();
        final double biasSign = (changeTo == Constants.TO_LEFT) ? 1 : -1;

        final double actualBiasRight = biasRight;
        // if(mandatoryChange!=NO_CHANGE){actualBiasRight = mandatoryBias;}
       
        // finally: all in one MOBIL's incentive formula:
        prospectiveBalance = meDiffAcc + politeness * (oldBackDiffAcc + newBackDiffAcc) - threshold - biasSign * actualBiasRight;
       
        return prospectiveBalance;
    }


    public double getMinimumGap() {
	return gapMin;
    }


    public double getSafeDeceleration() {
	return bSafe;
    }



}
