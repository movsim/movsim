package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

public interface IModelParameterACC extends IModelParameter {

    double getT();

    double getA();

    double getDelta();

    double getS1();

    double getB();

    double getCoolness();

}
