package org.movsim.consumption.model;

public final class FuelAndGear {

    private final double fuelFlow;
    private final int gear;

    public FuelAndGear(double fuelFlow, int gear) {
        this.fuelFlow = fuelFlow;
        this.gear = gear;
    }

    /**
     * @return the fuel flow in m^3/s
     */
    public double getFuelFlow() {
        return fuelFlow;
    }

    /**
     * @return the fuel flow in liter/s
     */
    public double getFuelFlowInLiterPerSecond() {
        // conversion from m^3/s to liter/s
        return 1000 * fuelFlow;
    }

    /**
     * @return the optimal gear
     */
    public int getGear() {
        return gear;
    }
}
