package org.movsim.consumption;

public interface CarModel {

    double getMass();

    double getEmptyMass();

    double getCwValue();

    double getCrossSectionSurface();

    double getConsFrictionCoefficient();

    double getvFrictionCoefficient();

    double getElectricPower();

    double getDynamicRadius();

    double getDynamicWheelCircumfence();

    double getForceMech(double v, double acc);

    double getFreeWheelingDecel(double v);
}
