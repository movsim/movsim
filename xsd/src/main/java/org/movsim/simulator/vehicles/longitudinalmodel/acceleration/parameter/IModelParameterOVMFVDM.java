package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

import org.movsim.autogen.OptimalVelocityFunctionEnum;

/**
 *
 */
// TODO documentation like in IModelParameterIDM
public interface IModelParameterOVMFVDM extends IModelParameter {

    double getTransitionWidth();

    OptimalVelocityFunctionEnum getOptimalSpeedFunction();

    double getBeta();

    double getTau();

    double getGamma();

}
