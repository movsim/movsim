package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

/**
 *
 */
// TODO documentation like in IModelParameterIDM
public interface IModelParameterPTM extends IModelParameter {
    double getTau();
    double getWeightMinus();
    double getA0();
    double getGamma();
    double getWeightCrash();
    double getTauMax();
    double getAlpha();
    double getBetaLogit();
    double getTauCorrelation();
    double getBMax();
}
