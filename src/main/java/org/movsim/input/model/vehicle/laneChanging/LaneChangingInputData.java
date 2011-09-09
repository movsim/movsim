package org.movsim.input.model.vehicle.laneChanging;


public interface LaneChangingInputData{

    boolean isWithEuropeanRules();
    double getCritSpeedEuroRules();
    boolean isInitializedMobilData();
    LaneChangingMobilData getLcMobilData();
}
