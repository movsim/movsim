package org.movsim.consumption;

public interface FuelConsumption {

	// optimum fuel consumption flow in m^3/s
    double[] getMinFuelFlow(double v, double acc, boolean withJante);
	
}
