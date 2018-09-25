package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

public interface EquilibriumProperties {

    /**
     * Gets the q max.
     *
     * @return the q max
     */
    double getQMax();

    /**
     * Gets the rho max.
     *
     * @return the rho max
     */
    double getRhoMax();

    /**
     * Gets the rho q max.
     *
     * @return the rho q max
     */
    double getRhoQMax();

    /**
     * Gets the net distance.
     *
     * @param rho the rho
     * @return the net distance
     */
    double getNetDistance(double rho);

    double getVEq(double rho);

    double getRho(int i);

    int getVEqCount();

}