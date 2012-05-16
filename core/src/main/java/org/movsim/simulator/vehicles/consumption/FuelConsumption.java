/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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
package org.movsim.simulator.vehicles.consumption;

import org.movsim.input.model.vehicle.consumption.ConsumptionModelInput;
import org.movsim.output.fileoutput.FileFuelConsumption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuelConsumption {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FuelConsumption.class);

    // if cons(m^3/(Ws)) higher, point (f,pe) out of Bounds
    // 900=3-4 times the minimum
    private final double LIMIT_SPEC_CONS = 900 * FuelConstants.CONVERSION_GRAMM_PER_KWH_TO_SI;

    // extremely high flow in motor regimes that cannot be reached
    private final double POW_ERROR = 1e7; // 10000 KW

    private final double FUELFLOW_ERROR = POW_ERROR * LIMIT_SPEC_CONS;

    // ###############################

    private final CarModel carModel;

    private final EngineModel engineModel;

    public FuelConsumption(String keyLabel, ConsumptionModelInput input) {
        carModel = new CarModel(input.getCarData());
        engineModel = new EngineModel(input.getEngineData(), carModel);

        writeOutput(keyLabel);
    }

    public double fuelflowError() {
        return FUELFLOW_ERROR;
    }

    // Instantaneous fuel consumption (l/(100 km))
    public double getInstConsumption100km(double v, double acc, int gear, boolean withJante) {
        final int gearIndex = gear - 1;
        return (1e8 * getFuelFlow(v, acc, gearIndex, withJante) / Math.max(v, 0.001));
    }

    public double getFuelFlow(double v, double acc, int gearIndex, boolean withJante) {

        final double fMot = engineModel.getEngineFrequency(v, gearIndex);

        // final double forceMech=getForceMech(v,acc); // can be <0
        final double powMech = v * carModel.getForceMech(v, acc); // can be <0

        // electric generator is not active near or at in standstill (v<1*3.6km/h)
        // resulting in idle fuel consumption from engine specification
        // modeling assumption becomes invalid if lot of standstills are considered
        // electric consumption is active and no electric energy is provided by generator
        final double elecPower = (v < 1) ? 0 : carModel.getElectricPower();

        final double powMechEl = powMech + elecPower;// can be <0

        double fuelFlow = FUELFLOW_ERROR;

        if (engineModel.isFrequencyPossible(v, gearIndex) || gearIndex == 0) {
            fuelFlow = engineModel.getFuelFlow(fMot, powMechEl);
        }

        // ### check if motor regime can be reached; otherwise increase fuelFlow prohibitively

        // indicates that too high power required
        if (powMech > engineModel.getMaxPower()) {
            fuelFlow = FUELFLOW_ERROR;
        }

        // indicates that too high motor frequency
        if (withJante && (fMot > engineModel.getMaxFrequency())) {
            if (logger.isDebugEnabled()) {
                logger.debug(String
                        .format("v_kmh=%f, acc=%f, gear=%d, motor frequency=%d/min too high -- > return fuelErrorConsumption: %.2f",
                                (3.6 * v), acc, gearIndex + 1, (int) (fMot * 60), FUELFLOW_ERROR));
            }
            fuelFlow = FUELFLOW_ERROR;
        }

        // indicates too low motor frequency
        if (withJante && (fMot < engineModel.getMinFrequency())) {
            if (gearIndex == 0) {
                fuelFlow = carModel.getElectricPower() * LIMIT_SPEC_CONS;
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("v=%f, gear=%d, fuelFlow=%f %n", v, gearIndex + 1, fuelFlow));
                }
            } else {
                fuelFlow = FUELFLOW_ERROR;
            }
        }

        return fuelFlow;

    }

    // optimum fuel consumption flow in m^3/s
    // gives also reference to fuel-optimized gear

    public double[] getMinFuelFlow(double v, double acc, boolean withJante) {
        int gear = 1;
        double fuelFlow = FUELFLOW_ERROR;
        for (int testGearIndex = engineModel.getMaxGearIndex(); testGearIndex >= 0; testGearIndex--) {
            final double fuelFlowGear = getFuelFlow(v, acc, testGearIndex, withJante);
            if (fuelFlowGear < fuelFlow) {
                gear = testGearIndex + 1;
                fuelFlow = fuelFlowGear;
            }
        }
        final double[] retValue = { fuelFlow, gear };
        return retValue;
    }

    // optimum fuel consumption flow in m^3/s
    public double getFuelFlowInLiterPerS(double v, double acc) {
        return 1000 * getMinFuelFlow(v, acc, true)[0]; // convert from m^3/s --> liter/s
    }

    // TODO
    public void writeOutput(String keyLabel) {
        final FileFuelConsumption fileOutput = new FileFuelConsumption(keyLabel, this);

        int gear = 0;
        fileOutput.writeJante(gear, carModel);
        fileOutput.writeZeroAccelerationTest(carModel, engineModel);
        fileOutput.writeSpecificConsumption(engineModel);
        
            // for (int gearIndex = 0; gearIndex < engineModel.getMaxGearIndex(); gearIndex++) {
            // final int gear = gearIndex + 1;
            // final String strGear = new Integer(gear).toString();
            // final String filename = projectName + ".Jante" + strGear;
            // writeJanteDataFields(gear, filename);
            // }
    }
}
