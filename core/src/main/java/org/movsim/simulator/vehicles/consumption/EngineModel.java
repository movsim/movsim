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
import java.util.List;
import java.util.Locale;

import org.movsim.input.model.vehicle.consumption.ConsumptionEngineModelInput;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(EngineModel.class);

    private final CarModel carModel;

    private double idleConsumptionRate; // idling consumption rate (Liter/s)

    private double maxPower; // max. effective mechanical engine power (W)

    private double cylinderVolume; // effective volume of the cylinders of the engine

    private double minEffPressure; // in Pascal, effective part of pe lost by gear and engine friction (N/m^2)

    private double maxEffPressure; // in Pascal

    private double minFrequency; // idle rotation rate, minimum frequency(1/s)

    private double maxFrequency; // maximum rotation rate of engine (1/s)

    private double idleMoment;

    private List<Double> gears;

    private double maxMoment;

    private double cSpec0Idle;

    private double minSpecificConsumption; // (kg/Ws)

    public EngineModel(ConsumptionEngineModelInput engineInput, CarModel carModel) {

        this.carModel = carModel;
        initialize(engineInput);

        // TODO
        final String label = "carConsumption";
        final String filename = "sim/startStop_ACC." + label + ".specCons";
        
        // TODO fix output nullpointer ex
        writeOutput(filename);
    }

    private void initialize(ConsumptionEngineModelInput engineInput) {
        maxPower = engineInput.getMaxPower();

        idleConsumptionRate = engineInput.getIdleConsumptionRateLiterPerSecond();
        minSpecificConsumption = engineInput.getMinSpecificConsumption();

        cylinderVolume = engineInput.getCylinderVolume();

        minEffPressure = engineInput.getEffectivePressureMinimum();
        maxEffPressure = engineInput.getEffectivePressureMaximum();

        minFrequency = engineInput.getIdleRotationRate();
        maxFrequency = engineInput.getMaxRotationRate();

        gears = engineInput.getGearRatios();

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("maxPower=%.2f Watt, " + "idleConsumptionRate=%.3fl/h, cylinderVolume=%.2fl, "
                    + "minPressure=%.2fPa, maxPressure=%.2fPa, " + "minFrequency=%.2f/s, maxFrequency=%.2f/s",
                    maxPower, idleConsumptionRate * 3600, cylinderVolume, minEffPressure, maxEffPressure, minFrequency,
                    maxFrequency));
        }

        maxMoment = getMaxMoment();
        idleMoment = getModelLossMoment(getIdleFrequency());
        final double powerIdle = getLossPower(getIdleFrequency());
        cSpec0Idle = idleConsumptionRate * FuelConstants.RHO_FUEL_PER_LITER / powerIdle; // in kg/(Ws)

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("powerIdle=%f W", getLossPower(getIdleFrequency())));
            logger.debug(String.format("maxMoment=%f Nm", maxMoment));
            logger.debug(String.format("idleMoment=%f Nm", idleMoment));

            logger.debug(String.format(
                    "cSpez0Idle=%f kg/kWh=%f l/kWh\n minimumSpecificConsumption=%e kg/Ws=%f kg/kWh \n"
                            + "cSpez0(fIdle,M=160Nm)=%f g/kWh", 3.6e6 * cSpec0Idle, 3.6e6 * cSpec0Idle
                            / FuelConstants.RHO_FUEL_PER_LITER, minSpecificConsumption, 3.6e6 * minSpecificConsumption,
                    cSpecific0(getIdleFrequency(), 160, minSpecificConsumption) * 3.6e9));

            logger.debug(String.format("Test: dotC(f_idle,0)=%f l/h",
                    3.6e6 * consRateAnalyticModel(getIdleFrequency(), 0)));

            logger.debug(String.format("dotC(0.5*fmax,0.5*Pmax)=%f l/h",
                    3.6e6 * consRateAnalyticModel(0.5 * getMaxFrequency(), 0.5 * getMaxPower())));

            logger.debug(String.format("cSpecific(f=3000/min, M=160Nm)=%f g/kWh",
                    3.6e9 * cSpecific0ForMechMoment(3000 / 60., 160)));
            logger.debug(String.format("cSpecific(f=5000/min, M=200Nm)=%f g/kWh",
                    3.6e9 * cSpecific0ForMechMoment(5000 / 60., 200)));
        }

    }

    // --------------------------------------------------------

    private double getGearRatio(int gearIndex) {
        return gears.get(gearIndex);
    }

    public double getEngineFrequency(double v, int gearIndex) {
        if (gearIndex < 0 || gearIndex > getMaxGearIndex()) {
            logger.error("gear out of range! g={}", gearIndex);
        }
        final double freq = getGearRatio(gearIndex) * v / carModel.getDynamicWheelCircumfence();
        return Math.max(minFrequency, Math.min(freq, maxFrequency));
    }

    public boolean isFrequencyPossible(double v, int gearIndex) {
        if (gearIndex < 0 || gearIndex > getMaxGearIndex()) {
            logger.error("gear out of range !  g={}", gearIndex);
        }
        final double frequencyTest = getGearRatio(gearIndex) * v / carModel.getDynamicWheelCircumfence();
        if (frequencyTest > maxFrequency || frequencyTest < minFrequency) {
            return false;
        }
        return true;
    }

    // --------------------------------------------------------
    // power = 2*pi*f*M
    public static double getMoment(double power, double frequency) {
        return power / (2 * Math.PI * frequency);
    }

    public static double getPower(double moment, double frequency) {
        return 2 * Math.PI * frequency * moment;
    }

    // --------------------------------------------------------
    // model for loss moment
    public static double getLossPower(double frequency) {
        return getPower(getModelLossMoment(frequency), frequency);
    }

    public static double getModelLossMoment(double frequency) {
        // heuristic parameters, assume constant coefficient for *all* gears
        final double a = 0.003;
        final double b = 0.03;
        final double c = 12;
        return a * frequency * frequency + b * frequency + c;
    }

    // --------------------------------------------------------

    public double getFuelFlow(double frequency, double power) {
        return consRateAnalyticModel(frequency, power); // returns m^3/s !!
    }

    // consumption rate (m^3/s) as function of frequency and output power
    private double consRateAnalyticModel(double frequency, double mechPower) {
        final double indMoment = getMoment(mechPower, frequency) + getModelLossMoment(frequency);
        final double totalPower = mechPower + getLossPower(frequency);
        final double dotCInLiterPerSecond = 1. / FuelConstants.RHO_FUEL_PER_LITER * totalPower
                * cSpecific0(frequency, indMoment, minSpecificConsumption);
        return Math.max(0, dotCInLiterPerSecond / 1000.);
    }

    // model output 1: specific consumption per power as function of moment
    private double cSpecific0ForMechMoment(double frequency, double mechMoment) {
        final double indMoment = mechMoment + getModelLossMoment(frequency);
        return (mechMoment <= 0 || mechMoment > maxMoment) ? 0 : cSpecific0(frequency, indMoment,
                minSpecificConsumption) * (indMoment / mechMoment);
    }

    // model output 2: consumption rate (liter/s) as function of power
    private double cSpecific0(double frequency, double indMoment, double minCSpec0) {
        return minCSpec0
                + (cSpec0Idle - minCSpec0)
                * (Math.exp(1 - indMoment / idleMoment) + Math.exp(1
                        - (maxMoment + getModelLossMoment(frequency) - indMoment) / idleMoment));
    }

    // --------------------------------------------------------

    public double idleConsumptionRateLiter() {
        return idleConsumptionRate;
    }

    private double getIdleFrequency() {
        return minFrequency;
    }

    public double getIdleConsumptionRate() {
        return idleConsumptionRate;
    }

    public double getMaxPower() {
        return maxPower;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    public int getNumberOfGears() {
        return gears.size();
    }

    public int getMaxGearIndex() {
        return gears.size() - 1;
    }

    public double getMaxMoment() {
        return cylinderVolume * maxEffPressure / (4 * Math.PI);
    }

    // -------------------------------------------------------------

    // TODO
    private void writeOutput(String filename) {
        final PrintWriter fstr = FileUtils.getWriter(filename);
        if(fstr==null){
            logger.error("cannot write to file={}", filename);
            return;
        }
        logger.info("write to file = {}", filename);
        // fstr.printf("# power in idle mode = %f kW%n", 0.001*powIdle);
        fstr.printf("# c_spec0 in idle mode = %f kg/kWh = %f Liter/kWh %n", 3.6e6 * cSpec0Idle, 3.6e6
                / FuelConstants.RHO_FUEL_PER_LITER * cSpec0Idle);
        fstr.println("#f(1/min); powerMech(kW); consRate(l/h); moment(Nm); specCons(g/kWh)");

        final int N_FREQ = 40;
        final double df = (maxFrequency - minFrequency) / (N_FREQ - 1);
        final int N_POW = 80;
        final double powMin = -20000; // range
        final double dPow = (maxPower - powMin) / (N_POW - 1);

        for (int i = 0; i < N_FREQ; i++) {
            final double f = minFrequency + i * df;
            for (int j = 0; j <= N_POW; j++) {
                final double pow = powMin + j * dPow;
                final double dotC = getFuelFlow(f, pow);
                final double indMoment = getMoment(pow, f); // + getModelLossMoment(f);
                final double cSpec = cSpecific0ForMechMoment(f, indMoment);
                // factor 3.6e6 for converting from m^3/s to liter/h
                fstr.printf(Locale.US, "%.1f, %.3f, %.9f, %.9f, %.9f%n", f * 60, pow / 1000., 3.6e6 * dotC, indMoment,
                        cSpec * 3.6e9);
            }
            fstr.println(); // gnuplot block
        }
        fstr.close();
    }

}
