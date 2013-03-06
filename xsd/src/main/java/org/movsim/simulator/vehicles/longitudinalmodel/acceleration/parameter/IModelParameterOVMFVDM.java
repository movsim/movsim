package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

/**
 *
 */
// TODO documentation like in IModelParameterIDM
public interface IModelParameterOVMFVDM extends IModelParameter {

    double getTransitionWidth();

    String getOptimalSpeedFunction();

    double getBeta();

    double getTau();

    double getGamma();

}
