package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

public interface IModelParameter {

    /**
     * Returns the desired speed in free traffic.
     * 
     * @return the desired speed (m/s)
     */
    public abstract double getV0();

    /**
     * Returns the minimum bumper-to-bumper distance in a standstill.
     * 
     * @return the minimum bumper-to-bumper distance (m)
     */
    public abstract double getS0();

}
