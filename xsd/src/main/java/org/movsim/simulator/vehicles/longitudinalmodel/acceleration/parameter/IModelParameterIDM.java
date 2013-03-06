package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

/**
 * A read-only interface of the model parameters of the 'Intelligent Driver Model' (IDM).
 * 
 * <p>
 * For details on the parameters see Chapter 11.3 of the textbook <i>Traffic Flow Dynamics</i> (Treiber/Kesting,
 * Springer 2013). Default values for city and highway traffic can be found in Table 11.2.
 * </p>
 * 
 * <p>
 * The model also comprises parameters for the <i>desired speed</i> and the <i>minimum gap</i> (see
 * {@link IModelParameter}.
 * </p>
 * 
 * @author kesting
 * 
 */
public interface IModelParameterIDM extends IModelParameter {

    /**
     * Returns the <i>time gap</i> parameter value in seconds.
     * 
     * @return the time gap (s)
     */
    double getT();

    /**
     * Returns the <i>maximim acceleration</i> parameter value in m/s^2.<br>
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
