package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

/**
 * A read-only interface of the model 'ACC' as  extension of the IDM.
 * 
 * <p>
 * For details on the parameters see Chapter 11.3.8 of the textbook <i>Traffic Flow Dynamics</i> (Treiber/Kesting,
 * Springer 2013).
 * </p>
 * 
 * <p>
 * The model also comprises parameters for the <i>desired speed</i> and the <i>minimum gap</i>.
 * </p>
 * 
 */
public interface IModelParameterACC extends IModelParameter {

    /**
     * Returns the <i>time gap</i> parameter value in seconds.
     * 
     * @return the time gap (s)
     */
    double getT();

    /**
     * Returns the <i>maximum acceleration</i> parameter value in m/s^2.<br>
     * 
     * Typical values are in the range of 1-2 m/s^2.
     * 
     * @return the maximum acceleration (m/s^2)
     */
    double getA();

    /**
     * Returns the <i>comfortable deceleration</i> parameter value in m/s^2.<br>
     * 
     * Typical values are in the range of 1-2 m/s^2.
     * 
     * @return the comfortable deceleration (m/s^2)
     */
    double getB();

    /**
     * Returns the <i>coolness factor</i> which is ia unitless parameter.<br>
     * 
     * Useful values are in the range [0,1] while the 'coolest' reaction is modeled by 1.
     * 
     * @return the coolness factor (unitless)
     */
    double getCoolness();

    /**
     * Returns the <i>acceleration exponent</i> which is a unitless parameter.<br>
     * 
     * Typically, this parameter is used with the default value of 4.
     * 
     * @return the time gap in seconds
     */
    double getDelta();

    /**
     * Returns the <i>jam distance</i> in m.<br>
     * 
     * Typically, this parameter is set to zero.
     * 
     * @return the jam distance (m)
     */
    double getS1();
}
