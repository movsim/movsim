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

import java.io.PrintWriter;
import java.util.Locale;

import org.movsim.input.model.vehicle.consumption.ConsumptionModelInput;
import org.movsim.utilities.FileUtils;
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

    public FuelConsumption(ConsumptionModelInput input) {
        carModel = new CarModel(input.getCarData());
        engineModel = new EngineModel(input.getEngineData(), carModel);

        // TODO
        final String label = "carConsumption";
        final String project = "sim/startStop_ACC." + label;
        writeOutput(true, project);
    }

    public double fuelflowError() {
        return FUELFLOW_ERROR;
    }

    // Instantaneous fuel consumption (l/(100 km))
    private double getInstConsumption100km(double v, double acc, int gear, boolean withJante) {
        final int gearIndex = gear - 1;
        return (1e8 * getFuelFlow(v, acc, gearIndex, withJante) / Math.max(v, 0.001));
    }

    private double getFuelFlow(double v, double acc, int gearIndex, boolean withJante) {

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

    private double[] getMinFuelFlow(double v, double acc, boolean withJante) {
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

    // Output methods
    // TODO
    public void writeOutput(boolean withJante, String projectName) {

        final String filenameConstAccel = projectName + ".carConstAccel";
        writeZeroAccelTest(filenameConstAccel);

        if (withJante) {
            final String filenameJanteOpt = projectName + ".JanteOpt";
            writeJanteDataFieldsOptGear(filenameJanteOpt);
            // for (int gearIndex = 0; gearIndex < engineModel.getMaxGearIndex(); gearIndex++) {
            // final int gear = gearIndex + 1;
            // final String strGear = new Integer(gear).toString();
            // final String filename = projectName + ".Jante" + strGear;
            // writeJanteDataFields(gear, filename);
            // }
        }
    }

    private void writeZeroAccelTest(String filename) {
        final PrintWriter fstr = FileUtils.getWriter(filename);
        if(fstr==null){
            logger.error("cannot write to file={}", filename);
            return;
        }
        fstr.printf("# veh mass = %.1f%n", carModel.getMass());
        fstr.printf("# number of gears = %d%n", engineModel.getNumberOfGears());
        fstr.printf("# v[m/s], accFreeWheeling[m/s^2], fuelFlow[l/h], gear, c100[l/100km]%n");
        final double vMax = 200 / 3.6;
        final double dv = 0.2;
        double v = dv;
        while (v <= vMax) {
            final double accFreeWheeling = carModel.getFreeWheelingDecel(v);
            final double acc = 0.0;
            final double[] fuelFlow = getMinFuelFlow(v, acc, true);
            final int optGear = (int) fuelFlow[1]; // !! kein gearIndex
            final double c100 = getInstConsumption100km(v, 0, optGear, true);
            fstr.printf("%.3f, %.8f,  %.8f,  %d,  %.8f%n", v, accFreeWheeling, 3.6e6 * fuelFlow[0], optGear, c100);
            fstr.flush();
            v += dv;
        }
        fstr.close();
    }

    private void writeJanteDataFieldsOptGear(String filename) {
        writeJanteDataFields(0, filename);
    }

    // TODO
    private void writeJanteDataFields(int gearTest, String filename) {
        final boolean determineOptimalGear = (gearTest == 0) ? true : false;
        final PrintWriter fstr = FileUtils.getWriter(filename);
        if(fstr==null){
            logger.error("cannot write to file={}", filename);
            return;
        }
        fstr.println("#Jante Fuel consumption:");
        fstr.println("# v(km/h), acc(m/s^2), forceMech(N), powMech(kW), fuelFlow(l/h), consump(liters/100km), Gear");

        final double accmin = -1;
        final double accmax = 3;
        final double vmin_kmh = 0;
        final double vmax_kmh = 200;
        final int NV = 101;
        final int NACC = 61;
        final double dv_kmh = (vmax_kmh - vmin_kmh) / (NV - 1);
        final double dacc = (accmax - accmin) / (NACC - 1);

        for (int iv = 0; iv < NV; iv++) {
            for (int iacc = 0; iacc < NACC; iacc++) {
                final double v_kmh = vmin_kmh + iv * dv_kmh;
                final double v = v_kmh / 3.6;
                final double acc = 0.01 * (int) (100 * (accmin + iacc * dacc));
                final double forceMech = carModel.getForceMech(v, acc);
                final double powMechEl = Math.max(v * forceMech + carModel.getElectricPower(), 0.);
                double fuelFlow = 100000;
                int gear = gearTest;
                if (determineOptimalGear) {
                    // v=const => min(consump)=min(fuelFlow)
                    final double[] res = getMinFuelFlow(v, acc, true);
                    fuelFlow = res[0];
                    gear = (int) res[1];
                } else {
                    final int gearIndex = gear - 1;
                    fuelFlow = getFuelFlow(v, acc, gearIndex, true);
                }
                final double consump_100km = 1e8 * fuelFlow / Math.max(v, 0.001);
                final double fuelFlow_lh = 3.6e6 * fuelFlow;
                fstr.printf(Locale.US, "%.2f, %.2f, %.2f, %.6f, %.5f, %.5f, %d%n", v_kmh, acc, forceMech,
                        (0.001 * powMechEl), fuelFlow_lh, consump_100km, gear);
            }
            fstr.println();
        }
        fstr.close();
    }

}
