package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;


/**
 * 
 * 
 * <br>
 * created: Feb 26, 2013
 */
// TODO documentation like in IModelParameterIDM
public interface IModelParameterCCS extends IModelParameter {

    double getP0();

    double getVC();

    double getVCHerringbone();

    double getPHerringbone();

    double getMass();

    double getFriction();

    double getA();

    double getT();

    double getBMaximum();

    double getB();

    double getCw();

}
