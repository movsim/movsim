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
import org.movsim.output.fileoutput.FileFuelConsumptionModel;
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
    
    private final String type;
    
//energy consumption
    
    // values to be read from xml BEGIN
    private final double fixedGearRatio;
    private final double nomVoltage;
    private final double nomPower;
    private final double nomTorque;
    private final double maxCurrent;
    private final double maxFrequency;
    private final double shortCircuitCurrentRate;
    private final double maxOverloadTime;
    private final double overloadFactor;
    // values to be read from xml END
    private final double ROTOR_CONST;
    private final double STATOR_L;
    private double overloadTime;

    
///energy consumption
    
    public FuelConsumption(String keyLabel, ConsumptionModelInput input) {
        carModel = new CarModel(input.getCarData());
        engineModel = new EngineModel(input.getEngineData(), carModel);
        type = input.getType();
        
//energy consumption
        
        //values to be read from xml BEGIN
        fixedGearRatio = 2.636;
        nomVoltage = 650;
        nomPower = 60 * 1000;
        nomTorque = 207;
        maxCurrent = 170;
        maxFrequency = 13500 / 60;
        shortCircuitCurrentRate = 0.6;
        maxOverloadTime = 10;
        overloadFactor = 2;
        //values to be read from xml END
        
        overloadTime = 0;
        
        final double nomPhaseVoltage = nomVoltage / Math.sqrt(3);
        final double nomPhaseCurrent = nomPower / (nomVoltage * Math.sqrt(3));
        final double nomOmega = nomPower / nomTorque;
        STATOR_L = nomPhaseVoltage / (nomPhaseCurrent * shortCircuitCurrentRate * nomOmega);
        
        final double nomRotorVoltage = Math.sqrt(nomPhaseVoltage * nomPhaseVoltage + Math.pow(nomOmega * STATOR_L * nomPhaseCurrent, 2));
        ROTOR_CONST = nomRotorVoltage / nomOmega;
        
        logger.info(String.format("L=%f, k=%f", STATOR_L, ROTOR_CONST));
        
///energy consumption

        if (input.isOutput()) {
            writeOutput(keyLabel);
        }
    }

    public double fuelflowError() {
        return FUELFLOW_ERROR;
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
    public double getInstConsumption100km(double v, double acc, int gear, boolean withJante) {
        final int gearIndex = gear - 1;
        return (1e8 * getFuelFlow(v, acc, gearIndex, withJante) / Math.max(v, 0.001));
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

        // check if motor regime can be reached; otherwise increase fuelFlow prohibitively

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

    /**
     * Gets the optimum fuel consumption flow in m^3/s and the used fuel-optimized gear
     *
     * @param v
     * @param acc
     * @param withJante
     * @return fuelFlow and gear
     */
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

    /**
     * Gets the optimum fuel consumption flow in liter per s
     *
     * @param v
     * @param acc
     * @return the fuel flow in liter per s
     */
    public double getFuelFlowInLiterPerS(double v, double acc) {
        return 1000 * getMinFuelFlow(v, acc, true)[0]; // convert from m^3/s --> liter/s
    }

    private void writeOutput(String keyLabel) {
        final FileFuelConsumptionModel fileOutput = new FileFuelConsumptionModel(keyLabel, this);

        int gear = 0;
        fileOutput.writeJante(gear, carModel);
        fileOutput.writeZeroAccelerationTest(carModel, engineModel);
        fileOutput.writeSpecificConsumption(engineModel);

        // jante output per gear
        // for (int gearIndex = 0; gearIndex < engineModel.getMaxGearIndex(); gearIndex++) {
        // final int gear = gearIndex + 1;
        // final String strGear = new Integer(gear).toString();
        // final String filename = projectName + ".Jante" + strGear;
        // writeJanteDataFields(gear, filename);
        // }
    }

    public String getType() {
        return type;
    }
    
//energy consumption
    
    public double getElectricPower(double v, double a, double dt) {
        double powerEl = 0;
        final double P_ERROR = nomPower;
        
        double U1I1cosphi[] = {0, 0, 0};
        
        final double frequency = fixedGearRatio * v / carModel.getDynamicWheelCircumfence();
        final double omega = 2 * Math.PI * frequency;
        final double powerMech = v * carModel.getForceMech(v, a); // can be <0
        final double torque = EngineModel.getMoment(powerMech, frequency);
        final double rotorVoltage = ROTOR_CONST * omega;
        final double syncReactance = omega * STATOR_L;
        
        final double maxPhaseVoltage = nomVoltage / Math.sqrt(3);
        
        final boolean isEngineBeyondLimits = (overloadTime >= maxOverloadTime);
        final double maxPhaseCurrent = isEngineBeyondLimits ? maxCurrent : maxCurrent * overloadFactor;
        final double maxPowerMech = isEngineBeyondLimits ? nomPower : nomPower * overloadFactor;
        final double maxTorque = isEngineBeyondLimits ? nomTorque : nomTorque * overloadFactor;
        
        if (frequency > maxFrequency)   logger.error(String.format("motor frequency %d/min is too high", frequency * 60));
        if (powerMech > maxPowerMech)   logger.error(String.format("power demand %fkW is too high", powerMech / 1000));
        if (torque > maxTorque)         logger.error(String.format("torque demand %fNm is too high", torque));
        
        if (powerMech > 0) { // energy must be taken from battery
            
            final double minPhaseVoltage = powerMech * STATOR_L / (2.7 * ROTOR_CONST); // theta<=64°
            
            if (powerMech <= 1.5 * rotorVoltage * ROTOR_CONST / STATOR_L) { // cos(phi)=1 is possible
                
                final double D = 0.25 * Math.pow(rotorVoltage, 4) - powerMech * powerMech * syncReactance * syncReactance / 9;
                final double x1 = rotorVoltage * rotorVoltage / 2 + Math.sqrt(D);
                final double x2 = Math.max(rotorVoltage * rotorVoltage / 2 - Math.sqrt(D), 0);
                final double phaseVoltage1 = Math.sqrt(x1);
                final double phaseVoltage2 = Math.sqrt(x2);
                if (phaseVoltage1 >= minPhaseVoltage) {
                    if (phaseVoltage1 <= maxPhaseVoltage) {
                        U1I1cosphi[0] = phaseVoltage1;
                        U1I1cosphi[1] = powerMech / (3 * phaseVoltage1);
                        logger.debug("U1=U11");
                    } else if (phaseVoltage2 >= minPhaseVoltage) {
                        if (phaseVoltage2 <= maxPhaseVoltage) {
                            U1I1cosphi[0] = phaseVoltage2;
                            U1I1cosphi[1] = powerMech / (3 * phaseVoltage2);
                            logger.debug("U1=U12");
                        } else {
                            U1I1cosphi[0] = maxPhaseVoltage;
                            U1I1cosphi[1] = getI1(maxPhaseVoltage, powerMech, omega);
                            logger.debug("U1=U1max");
                        }
                    } else {
                        U1I1cosphi = getU1I1phiBest(minPhaseVoltage, powerMech, omega, maxPhaseCurrent);
                        logger.debug("U1=?");
                    }
                } else {
                    U1I1cosphi[0] = minPhaseVoltage;
                    U1I1cosphi[1] = getI1(minPhaseVoltage, powerMech, omega);
                    logger.debug("U1=U1min");
                }
                
                if (U1I1cosphi[1] > maxPhaseCurrent) {
                    U1I1cosphi = getU1I1phiBest(minPhaseVoltage, powerMech, omega, maxPhaseCurrent);
                    logger.debug("I1>I1max");
                }
                
            } else {
                logger.debug(String.format("cos(phi)=1 not possible for %fkW at %fkm/h", powerMech / 1000, v * 3.6));
                
                U1I1cosphi = getU1I1phiBest(minPhaseVoltage, powerMech, omega, maxPhaseCurrent);
                
            }
                        
            if (U1I1cosphi[0] == 0) {
                logger.error(String.format("No possible voltage found between %fV and %fV for power demand %fkW at motor frequency %f/min (t_over = %fs)", minPhaseVoltage, nomVoltage / Math.sqrt(3), powerMech / 1000, frequency * 60, overloadTime));
                return P_ERROR;
            }
            
            //is engine working beyond design limits?
            if ((U1I1cosphi[1] > maxCurrent) || (powerMech > nomPower) || (torque > nomTorque)) {
                overloadTime += dt;
                logger.debug(String.format("engine working beyond design limits - t_over = %.2f seconds", overloadTime));
            } else {
                overloadTime = 0;
            }
            
            powerEl = 3 * U1I1cosphi[0] * U1I1cosphi[1];
            
        } else { // energy can be recouped
            
            if (v > 1 / 3.6) {
                powerEl = 0.7 * powerMech;
                
                logger.debug(String.format("recouping %fkW at %.0fkm/h, decelerating with %.2fm/s²", powerEl / (-1000), v * 3.6, -a));
                
                // TODO how well does this really work...?
                
            }
            
        }
        
        final double power0 = carModel.getElectricPower();
        
        return powerEl + power0; //TODO calculate switching losses (n), Ohm losses (I1)
    }
    
    private double getI1(double phaseVoltage, double powerMech, double omega) {
        double x = Math.sqrt((ROTOR_CONST * ROTOR_CONST / (STATOR_L * STATOR_L)) - powerMech * powerMech / (9 * phaseVoltage * phaseVoltage));
        double phi = Math.atan((x - phaseVoltage / (omega * STATOR_L)) * 3 * phaseVoltage / powerMech);
        return powerMech / (3 * phaseVoltage * Math.cos(phi));
    }
    
    private double[] getU1I1phiBest(double minPhaseVoltage, double powerMech, double omega, double maxPhaseCurrent) {
        double U1I1cosphi[] = {0, 0, 0};
        for (double phaseVoltage = nomVoltage / Math.sqrt(3); phaseVoltage >= minPhaseVoltage; phaseVoltage -= 0.5 ) {
            double phaseCurrent = getI1(phaseVoltage, powerMech, omega);
            double cosphi = powerMech / (3 * phaseVoltage * phaseCurrent);
            if ((cosphi > U1I1cosphi[2]) && (phaseCurrent <= maxPhaseCurrent)) {
                U1I1cosphi[0] = phaseVoltage;
                U1I1cosphi[1] = phaseCurrent;
                U1I1cosphi[2] = cosphi;
            }
        }
        logger.debug(String.format("best U1 / I1 / cos(phi) for %.2fkW at omega=%.2fHz: %.3fV / %.3fA / %f", powerMech / 1000, omega, U1I1cosphi[0], U1I1cosphi[1], U1I1cosphi[2]));
        return U1I1cosphi;
    }
    
///energy consumption
    
}
