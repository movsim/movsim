/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.consumption.model;

import org.movsim.autogen.EngineCombustionMap;

class EngineEfficiencyModelAnalyticImpl implements EngineEfficienyModel {

    private static final double HOUR_TO_SECOND = 1 / 3600.;

    private static final double KW_TO_W = 1000.;

    private static final double LITER_TO_MILLILITER = 1 / 1000.;

    /** idling consumption rate (liter/s) */
    private double idleConsumptionRate; //

    /** max. effective mechanical engine power in Watts (W) */
    public double maxPower; //

    /** effective volume of the cylinders of the engine (in milliliters, SI) */
    private double cylinderVolume; //

    /** effective part of pe lost by gear and engine friction, in Pascal (N/m^2) */
    private double minEffPressure;

    /** in Pascal */
    private double maxEffPressure;

    /** */
    private double idleMoment;

    public double cSpec0Idle;

    /** in (kg/Ws) */
    private double minSpecificConsumption;

    private final EngineRotationModel engineRotationsModel;

    public EngineEfficiencyModelAnalyticImpl(EngineCombustionMap engineCombustionMap,
            EngineRotationModel engineRotationsModel) {
        this.engineRotationsModel = engineRotationsModel;
        initialize(engineCombustionMap);
    }

    private void initialize(EngineCombustionMap engineCombustionMap) {
        maxPower = engineCombustionMap.getMaxPowerKW() * KW_TO_W;
        idleConsumptionRate = engineCombustionMap.getIdleConsRateLinvh() * HOUR_TO_SECOND;
        minSpecificConsumption = engineCombustionMap.getCspecMinGPerKwh() / 3.6e9;
        cylinderVolume = engineCombustionMap.getCylinderVolL() * LITER_TO_MILLILITER;
        minEffPressure = ConsumptionConstants.CONVERSION_BAR_TO_PASCAL * engineCombustionMap.getPeMinBar();
        maxEffPressure = ConsumptionConstants.CONVERSION_BAR_TO_PASCAL * engineCombustionMap.getPeMaxBar();
        idleMoment = MomentsHelper.getModelLossMoment(engineRotationsModel.getIdleFrequency());

        double powerIdle = MomentsHelper.getLossPower(engineRotationsModel.getIdleFrequency());
        cSpec0Idle = idleConsumptionRate * ConsumptionConstants.RHO_FUEL_PER_LITER / powerIdle; // in kg/(Ws)

        // if (LOG.isDebugEnabled()) {
        // LOG.debug(String.format("powerIdle=%f W", getLossPower(getIdleFrequency())));
        // LOG.debug(String.format("maxMoment=%f Nm", maxMoment));
        // LOG.debug(String.format("idleMoment=%f Nm", idleMoment));
        //
        // LOG.debug(String.format("cSpez0Idle=%f kg/kWh=%f l/kWh\n minimumSpecificConsumption=%e kg/Ws=%f kg/kWh \n"
        // + "cSpez0(fIdle,M=160Nm)=%f g/kWh", 3.6e6 * cSpec0Idle, 3.6e6 * cSpec0Idle
        // / ConsumptionConstants.RHO_FUEL_PER_LITER, minSpecificConsumption, 3.6e6 * minSpecificConsumption,
        // cSpecific0(getIdleFrequency(), 160, minSpecificConsumption) * 3.6e9));
        //
        // LOG.debug(String
        // .format("Test: dotC(f_idle,0)=%f l/h", 3.6e6 * consRateAnalyticModel(getIdleFrequency(), 0)));
        //
        // LOG.debug(String.format("dotC(0.5*fmax,0.5*Pmax)=%f l/h",
        // 3.6e6 * consRateAnalyticModel(0.5 * getMaxFrequency(), 0.5 * getMaxPower())));
        //
        // LOG.debug(String.format("cSpecific(f=3000/min, M=160Nm)=%f g/kWh",
        // 3.6e9 * cSpecific0ForMechMoment(3000 / 60., 160)));
        // LOG.debug(String.format("cSpecific(f=5000/min, M=200Nm)=%f g/kWh",
        // 3.6e9 * cSpecific0ForMechMoment(5000 / 60., 200)));
        // }

    }

    // consumption rate (m^3/s) as function of frequency and output power
    private double calcConsumptionRate(double frequency, double mechPower) {
        final double indMoment = MomentsHelper.getMoment(mechPower, frequency) + MomentsHelper.getModelLossMoment(frequency);
        final double totalPower = mechPower + MomentsHelper.getLossPower(frequency);
        final double dotCInLiterPerSecond = 1. / ConsumptionConstants.RHO_FUEL_PER_LITER * totalPower
                * cSpecific0(frequency, indMoment, minSpecificConsumption);
        return Math.max(0, dotCInLiterPerSecond / 1000.);
    }

    // model output 1: specific consumption per power as function of moment
    public double cSpecific0ForMechMoment(double frequency, double mechMoment) {
        final double indMoment = mechMoment + MomentsHelper.getModelLossMoment(frequency);
        return (mechMoment <= 0 || mechMoment > getMaxMoment()) ? 0 : cSpecific0(frequency, indMoment,
                minSpecificConsumption) * (indMoment / mechMoment);
    }

    // model output 2: consumption rate (liter/s) as function of power
    public double cSpecific0(double frequency, double indMoment, double minCSpec0) {
        return minCSpec0
                + (cSpec0Idle - minCSpec0)
                * (Math.exp(1 - indMoment / idleMoment) + Math.exp(1
                        - (getMaxMoment() + MomentsHelper.getModelLossMoment(frequency) - indMoment) / idleMoment));
    }

    // --------------------------------------------------------

    @Override
    public double getFuelFlow(double frequency, double power) {
        return calcConsumptionRate(frequency, power);
    }

    @Override
    public double getIdleConsumptionRate() {
        return idleConsumptionRate;
    }

    @Override
    public double getMaxPower() {
        return maxPower;
    }

    public double getMaxMoment() {
        return cylinderVolume * maxEffPressure / (4 * Math.PI);
    }

}
