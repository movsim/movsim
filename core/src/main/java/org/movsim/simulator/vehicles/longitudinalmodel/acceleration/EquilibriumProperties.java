package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

public interface EquilibriumProperties {

    /**
     * Gets the q max.
     * 
     * @return the q max
     */
    public double getQMax();

    /**
     * Gets the rho max.
     * 
     * @return the rho max
     */
    public double getRhoMax();

    /**
     * Gets the rho q max.
     * 
     * @return the rho q max
     */
    public double getRhoQMax();

    /**
     * Gets the net distance.
     * 
     * @param rho
     *            the rho
     * @return the net distance
     */
    public double getNetDistance(double rho);

    public double getVEq(double rho);

    public double getRho(int i);

    public int getVEqCount();

}