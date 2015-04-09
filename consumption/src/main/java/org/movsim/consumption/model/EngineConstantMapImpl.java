package org.movsim.consumption.model;

import org.movsim.autogen.EngineConstantMap;
import org.movsim.autogen.VehicleData;

import com.google.common.base.Preconditions;

public class EngineConstantMapImpl implements EngineEfficienyModel {

    /** in kg/(Ws) */
    private final double minSpecificConsumption;

    /** consumption rate (m^3/s) */
    private final double constConsumptionRate;

    /** max. effective mechanical engine power in Watt (W) */
    private final double maxPower;

    private final double idleConsumptionRate; // TODO not yet used

    public EngineConstantMapImpl(EngineConstantMap engineMap, VehicleData vehicleData) {
        Preconditions.checkNotNull(engineMap);
        this.maxPower = engineMap.getMaxPowerKW() * ConsumptionConstants.KW_TO_W;
        this.idleConsumptionRate = engineMap.getIdleConsRateLinvh() * ConsumptionConstants.HOUR_TO_SECOND;
        this.minSpecificConsumption = engineMap.getCspecMinGPerKwh() * ConsumptionConstants.GRAMM_PER_KWH_TO_KG_PER_WS;
        double fuelDensity = 1. / ConsumptionConstants.LITER_TO_CUBICMETER
                * ConsumptionConstants.getFuelDensityPerLiter(vehicleData.getFuelDensity());
        this.constConsumptionRate = minSpecificConsumption / fuelDensity;
    }

    @Override
    public double getFuelFlow(double frequency, double power) {
        return Math.max(0, power * constConsumptionRate);
    }

    @Override
    public double getMaxPower() {
        return maxPower;
    }

}
