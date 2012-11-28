package org.movsim.consumption.model;

import java.util.List;

import org.movsim.consumption.input.xml.model.RotationModelInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineRotationModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(EngineRotationModel.class);

    /** idle rotation rate, minimum frequency(1/s) */
    public final double minFrequency;

    /** maximum rotation rate of engine (1/s) */
    public final double maxFrequency;

    /** dynamic tire radius (<static r) (m) */
    private final double dynamicRadius;

    private final List<Double> gears;

    EngineRotationModel(RotationModelInput input) {
        minFrequency = input.getIdleRotationRate();
        maxFrequency = input.getMaxRotationRate();
        dynamicRadius = input.getDynamicTyreRadius();
        gears = input.getGearRatios();
    }

    public double dynamicRadius() {
        return dynamicRadius;
    }

    public double dynamicWheelCircumfence() {
        return 2 * Math.PI * dynamicRadius;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    private double getGearRatio(int gearIndex) {
        return gears.get(gearIndex);
    }

    public int getNumberOfGears() {
        return gears.size();
    }

    public int getMaxGearIndex() {
        return gears.size() - 1;
    }

    public double getIdleFrequency() {
        return minFrequency;
    }

    public double getEngineFrequency(double v, int gearIndex) {
        if (gearIndex < 0 || gearIndex > getMaxGearIndex()) {
            logger.error("gear out of range! g={}", gearIndex);
        }
        final double freq = getGearRatio(gearIndex) * v / dynamicWheelCircumfence();
        return Math.max(minFrequency, Math.min(freq, maxFrequency));
    }

    public boolean isFrequencyPossible(double v, int gearIndex) {
        if (gearIndex < 0 || gearIndex > getMaxGearIndex()) {
            logger.error("gear out of range !  g={}", gearIndex);
        }
        final double frequencyTest = getGearRatio(gearIndex) * v / dynamicWheelCircumfence();
        if (frequencyTest > maxFrequency || frequencyTest < minFrequency) {
            return false;
        }
        return true;
    }

}
