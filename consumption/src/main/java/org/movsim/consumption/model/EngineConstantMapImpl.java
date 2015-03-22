package org.movsim.consumption.model;

import org.movsim.autogen.EngineConstantMap;

import com.google.common.base.Preconditions;

public class EngineConstantMapImpl implements EngineEfficienyModel {

    /** in kg/(Ws) */
    private final double minSpecificConsumption;
    
    /** consumption rate (m^3/s) */
    private final double constConsumptionRate;
    
    /** max. effective mechanical engine power in Watt (W) */
    private final double maxPower;
    
    /** idling consumption rate (liter/s) */
    private final double idleConsumptionRate; 
    
    public EngineConstantMapImpl(EngineConstantMap engineMap) {
        Preconditions.checkNotNull(engineMap);
//        max_power_kW="75" idle_cons_rate_linvh="0.8" cspec_min_g_per_kwh="235"
        
        this.maxPower = engineMap.getMaxPowerKW() * ConsumptionConstants.KW_TO_W;
        
        // TODO in m^3/s  ???!!!!
        this.idleConsumptionRate = engineMap.getIdleConsRateLinvh() * ConsumptionConstants.HOUR_TO_SECOND;
        
        this.minSpecificConsumption = engineMap.getCspecMinGPerKwh()
                * ConsumptionConstants.GRAMM_PER_KWH_TO_KG_PER_WS;
        this.constConsumptionRate = minSpecificConsumption / ConsumptionConstants.RHO_FUEL_PER_LITER/1000.;
    }

    @Override
    public double getFuelFlow(double frequency, double power) {
        return power*constConsumptionRate;
    }

    @Override
    public double getIdleConsumptionRate() {
        return idleConsumptionRate;
    }

    @Override
    public double getMaxPower() {
        return maxPower;
    }

}
