package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

public interface EquilibriumProperties {

    /**
     * Gets the q max.
     * 
     * @return the q max
     */
    abstract double getQMax();

    /**
     * Gets the rho max.
     * 
     * @return the rho max
     */
    abstract double getRhoMax();

    /**
     * Gets the rho q max.
     * 
     * @return the rho q max
     */
    abstract double getRhoQMax();

    /**
     * Gets the net distance.
     * 
     * @param rho
     *            the rho
     * @return the net distance
     */
    abstract double getNetDistance(double rho);

    /**
     * Gets the v eq.
     * 
     * @param rho
     *            the rho
     * @return the v eq
     */
    abstract double getVEq(double rho);

    /**
     * Gets the rho.
     * 
     * @param i
     *            the i
     * @return the rho
     */
    abstract double getRho(int i);

    abstract double getVEq(int i);

    abstract int getVEqCount();

}