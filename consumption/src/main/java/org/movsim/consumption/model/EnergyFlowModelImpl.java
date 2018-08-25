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

import org.apache.commons.lang3.StringUtils;
import org.movsim.autogen.ConsumptionModel;
import org.movsim.autogen.VehicleData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

class EnergyFlowModelImpl implements EnergyFlowModel {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(EnergyFlowModelImpl.class);

    /** if cons(m^3/(Ws)) higher, point (f,pe) out of Bounds 900=3-4 times the minimum, in m^3/(Ws) */
    private final double limitSpecificConsumption;

    /** extremely high flow in motor regimes that cannot be reached. Set to 10000 KW */
    private final double POW_ERROR = 1e7;

    private final double fuelFlowError;

    private final InstantaneousPowerModel carPowerModel;

    private final EngineEfficiencyModel engineEfficiencyModel;

    private final EngineRotationModel engineRotationModel;

    private final VehicleData vehicleData;

    EnergyFlowModelImpl(String keyLabel, ConsumptionModel modelInput) {
        Preconditions.checkNotNull(modelInput);
        Preconditions.checkArgument(!StringUtils.isBlank(keyLabel));
        vehicleData = modelInput.getVehicleData();
        carPowerModel = new InstantaneousPowerModelImpl(vehicleData);
        engineRotationModel = new EngineRotationModel(modelInput.getRotationModel());

        // limit values, dependent on fuel density
        double fuelDensity = vehicleData.getFuelDensity();
        limitSpecificConsumption = 900 * ConsumptionConstants
                .conversionFactorGrammPerEnergyToVolumePerEnergy(fuelDensity);
        fuelFlowError = POW_ERROR * limitSpecificConsumption;
        if (modelInput.isSetEngineCombustionMap()) {
            engineEfficiencyModel = new EngineEfficiencyModelAnalyticImpl(modelInput.getEngineCombustionMap(),
                    engineRotationModel, vehicleData);
        } else if (modelInput.isSetEngineConstantMap()) {
            engineEfficiencyModel = new EngineConstantMapImpl(modelInput.getEngineConstantMap(), vehicleData);
        } else {
            throw new IllegalArgumentException("no engine efficiency mapping provided");
        }

        if (modelInput.isOutput()) {
            writeOutput(keyLabel);
        }
    }

    double fuelflowError() {
        return fuelFlowError;
    }

    /**
     * Gets the instantaneous fuel consumption in liters/100km with a cut-off for v=0.
     * 
     * @param v
     * @param acc
     * @param gear
     * @param withJante
     * @return the instantaneous consumption per 100km
     */
    @Override
    public double getInstConsumption100km(double v, double acc, int gear, boolean withJante) {
        final int gearIndex = gear - 1;
        return (1e8 * getFuelFlow(v, acc, 0, gearIndex, withJante) / Math.max(v, 0.001));
    }

    /**
     * Gets the fuel flow in m^3/s and the {code FUEL_ERROR} if the operation point is not possible.
     * 
     * @param v
     * @param acc
     * @param gearIndex
     * @param withJante
     * @return fuelFlow
     */
    @Override
    public double getFuelFlow(double v, double acc, double grade, int gearIndex, boolean withJante) {

        final double fMot = engineRotationModel.getEngineFrequency(v, gearIndex);

        final double powMech = carPowerModel.getMechanicalPower(v, acc, grade);

        // electric generator is not active near or at in standstill (v<1*3.6km/h)
        // resulting in idle fuel consumption from engine specification
        // modeling assumption becomes invalid if lot of standstills are considered
        // electric consumption is active and no electric energy is provided by generator
        final double elecPower = (v < 1) ? 0 : vehicleData.getElectricPower();

        final double powMechEl = powMech + elecPower;// can be <0

        double fuelFlow = fuelFlowError;

        if (engineRotationModel.isFrequencyPossible(v, gearIndex) || gearIndex == 0) {
            fuelFlow = engineEfficiencyModel.getFuelFlow(fMot, powMechEl);
        }

        // check if motor regime can be reached; otherwise increase fuelFlow prohibitively

        // indicates that too high power required
        if (powMech > engineEfficiencyModel.getMaxPower()) {
            fuelFlow = fuelFlowError;
        }

        // indicates that too high motor frequency
        if (withJante && (fMot > engineRotationModel.getMaxFrequency())) {
            LOG.info(String
                    .format("v_kmh=%f, acc=%f, gear=%d, motor frequency=%d/min too high -- > return fuelErrorConsumption: %.2f",
                            (3.6 * v), acc, gearIndex + 1, (int) (fMot * 60), fuelFlowError));
            fuelFlow = fuelFlowError;
        }

        // indicates too low motor frequency
        if (withJante && (fMot < engineRotationModel.getMinFrequency())) {
            if (gearIndex == 0) {
                fuelFlow = vehicleData.getElectricPower() * limitSpecificConsumption;
                LOG.info(String.format("v=%f, gear=%d, fuelFlow=%f %n", v, gearIndex + 1, fuelFlow));
            } else {
                fuelFlow = fuelFlowError;
            }
        }

        return fuelFlow;

    }

    /**
     * Gets the optimum fuel consumption flow in m^3/s and the used fuel-optimized gear
     * 
     * @param v
     * @param acc
     * @param grade
     *            in radians
     * @param withJante
     * @return fuelFlow and gear
     */
    @Override
    public FuelAndGear getMinFuelFlow(double v, double acc, double grade, boolean withJante) {
        int gear = 1;
        double fuelFlow = fuelFlowError;
        for (int testGearIndex = engineRotationModel.getMaxGearIndex(); testGearIndex >= 0; testGearIndex--) {
            final double fuelFlowGear = getFuelFlow(v, acc, grade, testGearIndex, withJante);
            if (fuelFlowGear < fuelFlow) {
                gear = testGearIndex + 1;
                fuelFlow = fuelFlowGear;
            }
        }
        return new FuelAndGear(fuelFlow, gear);
    }

    /**
     * Gets the optimum fuel consumption flow in liter per s
     * 
     * @param v
     * @param acc
     * @return the fuel flow in liter per s
     */
    @Override
    public double getFuelFlowInLiterPerS(double v, double acc) {
        return getMinFuelFlow(v, acc, 0, true).getFuelFlowInLiterPerSecond();
    }

    /**
     * Returns the instantaneous fuel consumption flowin liter per second considering speed, acceleration and the gradient (in radians)
     * 
     * @param v
     * @param acc
     * @param grade
     * @return
     */
    @Override
    public double getFuelFlowInLiterPerS(double v, double acc, double grade) {
        return getMinFuelFlow(v, acc, grade, true).getFuelFlowInLiterPerSecond();
    }

    // TODO Output needs to be refactored!
    private void writeOutput(String keyLabel) {
        final FileFuelConsumptionModel fileOutput = new FileFuelConsumptionModel(keyLabel, this);
        fileOutput.writeJanteOptimalGear(vehicleData, carPowerModel);
        fileOutput.writeZeroAccelerationTest(vehicleData, carPowerModel, engineRotationModel);

        // TODO design writer in a more object-oriented way
        if (engineEfficiencyModel instanceof EngineEfficiencyModelAnalyticImpl) {
            fileOutput.writeSpecificConsumption(engineRotationModel, (EngineEfficiencyModelAnalyticImpl) engineEfficiencyModel);
        } else if (engineEfficiencyModel instanceof EngineConstantMapImpl) {
            fileOutput.writeSpecificConsumption(engineRotationModel, (EngineConstantMapImpl) engineEfficiencyModel);
        }

    }

}
