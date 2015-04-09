package org.movsim.consumption.model;

import org.movsim.autogen.EngineConstantMap;
import org.movsim.autogen.VehicleData;

import com.google.common.base.Preconditions;

public class EngineConstantMapImpl implements EngineEfficiencyModel {

    /** constant value in kg/(Ws) */
    private final double minSpecificConsumption;

    /** consumption rate (m^3/s) */
    private final double constConsumptionRate;

    /** max. effective mechanical engine power in Watt (W) */
    private final double maxPower;

    private final double idleConsumptionRate; // TODO not yet used

    /** density in kg/l */
    private final double fuelDensityPerLiter;

    public EngineConstantMapImpl(EngineConstantMap engineMap, VehicleData vehicleData) {
        Preconditions.checkNotNull(engineMap);
        this.maxPower = engineMap.getMaxPowerKW() * ConsumptionConstants.KW_TO_W;
        this.idleConsumptionRate = engineMap.getIdleConsRateLinvh() * ConsumptionConstants.HOUR_TO_SECOND;
        this.minSpecificConsumption = engineMap.getCspecMinGPerKwh() * ConsumptionConstants.GRAMM_PER_KWH_TO_KG_PER_WS;
        double fuelDensity = 1. / ConsumptionConstants.LITER_TO_CUBICMETER
                * ConsumptionConstants.getFuelDensityPerLiter(vehicleData.getFuelDensity());
        this.constConsumptionRate = minSpecificConsumption / fuelDensity;
        this.fuelDensityPerLiter = ConsumptionConstants.getFuelDensityPerLiter(vehicleData.getFuelDensity());
    }

    @Override
    public double getFuelFlow(double frequency, double power) {
        return Math.max(0, power * constConsumptionRate);
    }

    @Override
    public double getMaxPower() {
        return maxPower;
    }

    /**
     * @return constant specific consumption in kg/(Ws)
     */
    double getMinSpecificConsumption() {
        return minSpecificConsumption;
    }

    public double getFuelDensityPerLiter() {
        return fuelDensityPerLiter;
    }

}
