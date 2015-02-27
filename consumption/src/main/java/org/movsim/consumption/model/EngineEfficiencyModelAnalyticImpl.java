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

import com.google.common.base.Preconditions;

class EngineEfficiencyModelAnalyticImpl implements EngineEfficienyModel {

    private static final double HOUR_TO_SECOND = 1 / 3600.;

    private static final double KW_TO_W = 1000.;

    private static final double LITER_TO_MILLILITER = 1 / 1000.;

    /** idling consumption rate (liter/s) */
    private final double idleConsumptionRate; //

    /** max. effective mechanical engine power in Watt (W) */
    private final double maxPower;

    /** effective volume of the cylinders of the engine (in milliliters, SI) */
    private final double cylinderVolume;

    /** effective part of pe lost by gear and engine friction, in Pascal (N/m^2) */
    private final double minEffPressure;

    /** in Pascal */
    private final double maxEffPressure;

    private final double idleMoment;

    /** in kg/(Ws) */
    public final double cSpec0Idle;

    /** in kg/(Ws) */
    private final double minSpecificConsumption;

    public EngineEfficiencyModelAnalyticImpl(EngineCombustionMap engineCombustionMap,
            EngineRotationModel engineRotationModel) {
        Preconditions.checkNotNull(engineRotationModel);

        this.maxPower = engineCombustionMap.getMaxPowerKW() * KW_TO_W;
        this.idleConsumptionRate = engineCombustionMap.getIdleConsRateLinvh() * HOUR_TO_SECOND;
        this.minSpecificConsumption = engineCombustionMap.getCspecMinGPerKwh()
                * ConsumptionConstants.GRAMM_PER_KWH_TO_KG_PER_WS;
        this.cylinderVolume = engineCombustionMap.getCylinderVolL() * LITER_TO_MILLILITER;
        this.minEffPressure = ConsumptionConstants.CONVERSION_BAR_TO_PASCAL * engineCombustionMap.getPeMinBar();
        this.maxEffPressure = ConsumptionConstants.CONVERSION_BAR_TO_PASCAL * engineCombustionMap.getPeMaxBar();
        this.idleMoment = MomentsHelper.getModelLossMoment(engineRotationModel.getIdleFrequency());

        double powerIdle = MomentsHelper.getLossPower(engineRotationModel.getIdleFrequency());
        this.cSpec0Idle = idleConsumptionRate * ConsumptionConstants.RHO_FUEL_PER_LITER / powerIdle;
    }

    // consumption rate (m^3/s) as function of frequency and output power
    private double calcConsumptionRate(double frequency, double mechPower) {
        final double indMoment = MomentsHelper.getMoment(mechPower, frequency)
                + MomentsHelper.getModelLossMoment(frequency);
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
