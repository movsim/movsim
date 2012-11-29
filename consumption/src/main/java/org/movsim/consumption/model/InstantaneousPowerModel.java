package org.movsim.consumption.model;

public interface InstantaneousPowerModel {

    /** can be <0 */
    double getMechanicalPower(double speed, double acceleration, double slopeGrade);

    double getFreeWheelingDeceleration(double speed);

}
