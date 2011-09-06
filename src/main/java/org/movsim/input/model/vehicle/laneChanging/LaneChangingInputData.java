package org.movsim.input.model.vehicle.laneChanging;


public interface LaneChangingInputData{

    boolean isWithEuropeanRules();
    double getCritSpeedEuroRules();
    public LaneChangingMobilData getLcMobilData();
}
