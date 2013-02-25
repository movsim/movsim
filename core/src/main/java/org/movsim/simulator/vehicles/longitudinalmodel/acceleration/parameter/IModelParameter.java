package org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter;

public interface IModelParameter {

    /**
     * Returns the desired speed in free traffic.
     * 
     * @return the desired speed (m/s)
     */
    public abstract double getV0();

    /**
     * Gets the minimum bumper-to-bumper distance.
     * 
     * @return the minimum bumper-to-bumper distance
     */
    public abstract double getS0();

}
