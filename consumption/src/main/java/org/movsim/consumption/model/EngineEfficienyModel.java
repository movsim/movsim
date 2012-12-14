package org.movsim.consumption.model;

public interface EngineEfficienyModel {

    /**
     * @param frequency
     * @param power
     * @return returns fuel flow in m^3/s
     */
    double getFuelFlow(double frequency, double power);

    double getIdleConsumptionRate();

    double getMaxPower();

}
