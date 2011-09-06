package org.movsim.input.model.vehicle.laneChanging;

public interface LaneChangingMobilData {

    double getSafeDeceleration();

    double getMinimumGap();

    double getThresholdAcceleration();

    double getRightBiasAcceleration();

    double getPoliteness();

}