package org.movsim.simulator.vehicles;

import com.google.common.base.Preconditions;
import org.movsim.consumption.model.EnergyFlowModel;

public class EnergyModel {

    private EnergyFlowModel fuelModel;

    private Vehicle vehicle;

    private double totalFuelUsedLiters = 0;

    EnergyModel(Vehicle vehicle) {
        this.vehicle = Preconditions.checkNotNull(vehicle);
    }

    public void incrementConsumption(double speed, double acc, double dt) {
        if (fuelModel != null) {
            totalFuelUsedLiters += fuelModel.getFuelFlowInLiterPerS(speed, acc) * dt;
        }
    }

    public void setModel(EnergyFlowModel fuelModel) {
        this.fuelModel = fuelModel;
    }

    public double getActualFuelFlowLiterPerS() {
        if (fuelModel == null) {
            return 0;
        }
        return fuelModel.getFuelFlowInLiterPerS(vehicle.getSpeed(), vehicle.getAcc());
    }

    /**
     * Returns the total fuel used by this vehicle.
     *
     * @return total fuel used
     */
    public double totalFuelUsedLiters() {
        return totalFuelUsedLiters;
    }

}
