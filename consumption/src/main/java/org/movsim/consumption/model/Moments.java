package org.movsim.consumption.model;

public class Moments {

    public static double getMoment(double power, double frequency) {
        return power / (2 * Math.PI * frequency);
    }

    /**
     * power = 2*pi*f*M
     * 
     * @param moment
     * @param frequency
     * @return physical power
     */
    public static double getPower(double moment, double frequency) {
        return 2 * Math.PI * frequency * moment;
    }

    /** model for loss moment */
    public static double getLossPower(double frequency) {
        return getPower(getModelLossMoment(frequency), frequency);
    }

    /** heuristic parameters, assume constant coefficient for *all* gears */
    public static double getModelLossMoment(double frequency) {
        final double a = 0.003;
        final double b = 0.03;
        final double c = 12;
        return a * frequency * frequency + b * frequency + c;
    }
}
