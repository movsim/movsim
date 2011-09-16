package org.movsim.simulator.vehicles.lanechanging.impl;

import java.util.List;

import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;
import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.impl.VehicleContainerImpl;


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
        // finite delay criterion also for neighboring vehicles 
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

    
    public double calcAccelerationBalanceInNewLane(final int direction, final List<VehicleContainer> lanes) {
        
        final boolean DEBUG = false;
        
        final int currentLane = me.getLane();
        
        final VehicleContainer ownLane = lanes.get(currentLane);
        final VehicleContainer newLane = lanes.get(currentLane + direction);
        
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

        
        final VehicleContainer newSituationNewBack = new VehicleContainerImpl(0);
        newSituationNewBack.addTestwise(newBack);
        newSituationNewBack.addTestwise(me);
        final VehicleContainer leftLaneNew = (currentLane + direction + Constants.TO_LEFT)>=lanes.size() ? null : lanes.get(currentLane + direction + Constants.TO_LEFT); 
        final double newBackNewAcc = (newBack == null) ? 0 : newBack.calcAccModel(newSituationNewBack, leftLaneNew);

        if (DEBUG) {
            // safety: check (MOBIL) safety constraint for new follower
            final double newBackNewAccTest = (newBack == null) ? 0 : newBack.getAccelerationModel()
                    .calcAcc(newBack, me);

            if (Math.abs(newBackNewAccTest - newBackNewAcc) > 0.0001) {
                System.err.printf("deviation in new newBackNewAcc!!!\n");
            }
        }
        
        if( safetyCheckAcceleration(newBackNewAcc)){
            return prospectiveBalance;
        }
            
        // check now incentive criterion
        // consider three vehicles: me, oldBack, newBack

        // old situation for me
        final VehicleContainer leftLaneMeOld = (currentLane + Constants.TO_LEFT)>=lanes.size() ? null : lanes.get(currentLane + Constants.TO_LEFT);
        final double meOldAcc = me.calcAccModel(ownLane, leftLaneMeOld);
        
        if (DEBUG) {
            final double meOldAccTest = me.getAccelerationModel().calcAcc(me, oldFront);
            if (Math.abs(meOldAccTest - meOldAcc) > 0.0001) {
                System.err.printf("meOldAccTest=%.4f, meOldAcc=%.4f\n", meOldAccTest, meOldAcc);
            }
        }
        
        
        // old situation for old back 
        final Vehicle oldBack = ownLane.getFollower(me);

        final double oldBackOldAcc = (oldBack != null) ? oldBack.calcAccModel(ownLane, leftLaneMeOld) : 0;
        
        if (DEBUG) {
            final double oldBackOldAccTest = (oldBack != null) ? oldBack.getAccelerationModel().calcAcc(oldBack, me)
                    : 0;
            if (Math.abs(oldBackOldAccTest - oldBackOldAcc) > 0.0001) {
                System.err.printf("oldBackAccTest=%.4f, oldBackAcc=%.4f\n", oldBackOldAccTest, oldBackOldAcc);
            }
        }
        
        // old situation for new back
        final double newBackOldAcc = (newBack != null) ? newBack.calcAccModel(newLane, leftLaneNew) : 0;
        
        if (DEBUG) {
            final double newBackOldAccTest = (newBack != null) ? newBack.getAccelerationModel().calcAcc(newBack,
                    newFront) : 0;
            if (Math.abs(newBackOldAccTest - newBackOldAcc) > 0.0001) {
                System.err.printf("newBackOldAccTest=%.4f, newBackOldAcc=%.4f\n", newBackOldAccTest, newBackOldAcc);
            }
        }
       
        
        // new traffic situation: set subject virtually into new lane under consideration
        
        
        
        final VehicleContainer newSituationMe = new VehicleContainerImpl(0); 
        newSituationMe.addTestwise(me);
        newSituationMe.addTestwise(newFront);
        final double meNewAcc = me.calcAccModel(newSituationMe, leftLaneNew);
        

        if (DEBUG) {
            final double meNewAccTest = me.getAccelerationModel().calcAcc(me, newFront);
            if (Math.abs(meNewAccTest - meNewAcc) > 0.0001) {
                System.err.printf("deviation in meNewAccTest!!!\n");
            }
        }        
        
        
        final VehicleContainer newSituationOldBack = new VehicleContainerImpl(0); 
        newSituationOldBack.addTestwise(oldFront);
        newSituationOldBack.addTestwise(oldBack);
        // TODO: if TO_LEFT: new situation also on leftLane (me)
        // if TO_RIGHT: no new situation (just take leftLane)
        
        final double oldBackNewAcc = (oldBack != null) ? oldBack.calcAccModel(newSituationOldBack, null) : 0;
        
        // compare
        if (DEBUG) {
            final double oldBackNewAccTest = (oldBack != null) ? oldBack.getAccelerationModel().calcAcc(oldBack,
                    oldFront) : 0;
            if (Math.abs(oldBackNewAccTest - oldBackNewAcc) > 0.0001) {
                System.err.printf("deviation in oldBackNewAccTest !!\n");
            }
        }        
        
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
    
}
