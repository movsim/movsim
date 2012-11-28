package org.movsim.consumption.model;

public interface EngineEfficienyModel {

    double getFuelFlow(double frequency, double power);

    double getIdleConsumptionRate();

    double getMaxPower();

}
