package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

/**
 * 
 */
// TODO documentation like in IModelParameterIDM
public interface IModelParameterKKW extends IModelParameter {

    double getK();

    double getPa2();

    double getPb0();

    double getPb1();

    double getPa1();

    double getVp();

}
