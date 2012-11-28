package org.movsim.consumption.model;

public interface EnergyFlowModel {

    double getInstConsumption100km(double v, double acc, int gear, boolean withJante);

    double getFuelFlow(double v, double acc, double grade, int gearIndex, boolean withJante);
    
    double[] getMinFuelFlow(double v, double acc, double grade, boolean withJante);

    double getFuelFlowInLiterPerS(double v, double acc);
    
    public double getFuelFlowInLiterPerS(double v, double acc, double grade);
}
