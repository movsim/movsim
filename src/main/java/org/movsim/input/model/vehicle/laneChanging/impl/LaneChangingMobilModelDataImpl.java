package org.movsim.input.model.vehicle.laneChanging.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;

public class LaneChangingMobilModelDataImpl implements LaneChangingMobilData {
    
    private final double safeDeceleration;
    private final double minimumGap;
    private final double thresholdAcceleration;
    private final double rightBiasAcceleration; 
    private final double politeness; 
     

    public LaneChangingMobilModelDataImpl(Map<String, String> map){
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
