package org.movsim.input.model.vehicle.laneChanging.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;

public class LaneChangingMobilModelDataImpl implements LaneChangingMobilData {
    
    private double safeDeceleration;
    private double minimumGap;
    private double thresholdAcceleration;
    private double rightBiasAcceleration; 
    private double politeness; 
     


    public LaneChangingMobilModelDataImpl(){
	
    }
    
//    public LaneChangingMobilModelDataImpl(Map<String, String> map){
//        init(map);
//    }
    
    
    public void init(final Map<String, String> map){
	safeDeceleration = Double.parseDouble(map.get("b_safe"));
        minimumGap = Double.parseDouble(map.get("s_min"));
        thresholdAcceleration = Double.parseDouble(map.get("threshold"));
        rightBiasAcceleration = Double.parseDouble(map.get("bias_right"));
        politeness = Double.parseDouble(map.get("politeness"));
    }

    @Override
    public double getSafeDeceleration() {
        return safeDeceleration;
    }

    @Override
    public double getMinimumGap() {
        return minimumGap;
    }

    @Override
    public double getThresholdAcceleration() {
        return thresholdAcceleration;
    }

    @Override
    public double getRightBiasAcceleration() {
        return rightBiasAcceleration;
    }

    @Override
    public double getPoliteness() {
        return politeness;
    }
}
