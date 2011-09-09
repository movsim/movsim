package org.movsim.input.model.vehicle.laneChanging;

import java.util.Map;

public interface LaneChangingMobilData {


    void init(final Map<String, String> map);
    
    double getSafeDeceleration();

    double getMinimumGap();

    double getThresholdAcceleration();

    double getRightBiasAcceleration();

    double getPoliteness();

}