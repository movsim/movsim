package org.movsim.consumption;

public interface EngineModel {

    double getMaxPower();

    double getMaxFrequency();

    double getMinFrequency();

    int getMaxGearIndex();

    int getNumberOfGears();

    double getFuelFlow(double frequency, double power);

    double getEngineFrequency(double v, int gearIndex);

    boolean isFrequencyPossible(double v, int gearIndex);
}
