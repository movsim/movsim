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
package org.movsim.simulator.vehicles;

import org.movsim.input.ProjectMetaData;
import org.movsim.io.FileOutputBase;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;

public class FileAccelerationFunctions extends FileOutputBase {

    private static final double CONST_DISTANCE = 20; // in m
    private static final double CONST_SPEED = 20; // in m/s
    private static final double CONST_SPEED_LEADER = 20; // in m/s
    private static final double CONST_APPROACHING_RATE = 0; // in m/s

    private enum QuantityRange {
        S(0, 70),
        V(10, 40),
        DV(-8, 15);

        private final double minValue;
        private final double maxValue;

        private QuantityRange(double minValue, double maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
    }

    private static final String EXTENSION_FORMAT_CONST_DISTANCE = "_%s_acc_v_dv.csv";
    private static final String EXTENSION_FORMAT_CONST_REL_SPEED = "_%s_acc_v_s.csv";
    private static final String EXTENSION_FORMAT_CONST_SPEED = "_%s_acc_dv_s.csv";
    private static final String EXTENSION_FORMAT_CONST_SPEED_LEADER = "_%s_acc_dv_s_lead.csv";

    private static final String OUTPUT_HEADING = String
            .format("%s %8s, %8s, %8s, %8s%n", FileOutputBase.COMMENT_CHAR, "s[m]", "v[m/s]", "dv[m/s]", "acc[m/s^2]");
    private static final String OUTPUT_FORMAT = "%8.5f, %8.5f, %8.5f, %8.5f%n";

    private static final double STEPWIDTH = 0.4; // too small values causes plot problems for some (stochastic) models

    public static void writeToFile(double simulationTimestep, VehiclePrototype vehiclePrototype) {
        new FileAccelerationFunctions(simulationTimestep, vehiclePrototype);
    }

    /**
     * Simulation timestep is model parameter for iterated map models (and cellular automata)
     */
    private FileAccelerationFunctions(double simulationTimestep, VehiclePrototype vehiclePrototype) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        final String label = vehiclePrototype.getLabel();
        LongitudinalModelBase accModel = vehiclePrototype.createAccelerationModel();

        writeConstDistance(EXTENSION_FORMAT_CONST_DISTANCE, simulationTimestep, label, accModel);
        writeConstRelSpeed(EXTENSION_FORMAT_CONST_REL_SPEED, simulationTimestep, label, accModel);
        writeConstSpeed(EXTENSION_FORMAT_CONST_SPEED, simulationTimestep, label, accModel);
        writeConstSpeedLeader(EXTENSION_FORMAT_CONST_SPEED_LEADER, simulationTimestep, label, accModel);
    }

    private void writeHeader(double timestep) {
        writer.printf("%s simulation timestep (model parameter for iterated-map models) in seconds = %.3f%n",
                FileOutputBase.COMMENT_CHAR, timestep);
        writer.printf(OUTPUT_HEADING);
    }

    private void writeConstDistance(String format, double simulationTimestep, final String label,
            LongitudinalModelBase accModel) {
        writer = createWriter(String.format(format, label));
        writeHeader(simulationTimestep);

        final double s = CONST_DISTANCE;

        double vMin = QuantityRange.V.minValue;
        double vMax = QuantityRange.V.maxValue;

        double dvMin = QuantityRange.DV.minValue;
        double dvMax = QuantityRange.DV.maxValue;

        double v = vMin;
        while (v <= vMax) {
            double dv = dvMin;
            while (dv <= dvMax) {
                double acc = accModel.calcAccSimple(s, v, dv);
                writer.printf(OUTPUT_FORMAT, s, v, dv, acc);
                dv += STEPWIDTH;
            }
            v += STEPWIDTH;
            writer.printf("%n");
        }
        writer.close();
    }

    private void writeConstRelSpeed(String format, double simulationTimestep, final String label,
            LongitudinalModelBase accModel) {
        writer = createWriter(String.format(format, label));
        writeHeader(simulationTimestep);

        final double dv = CONST_APPROACHING_RATE;

        double sMin = QuantityRange.S.minValue;
        double sMax = QuantityRange.S.maxValue;

        double vMin = QuantityRange.V.minValue;
        double vMax = QuantityRange.V.maxValue;

        double v = vMin;
        while (v <= vMax) {
            double s = sMin;
            while (s <= sMax) {
                double acc = accModel.calcAccSimple(s, v, dv);
                writer.printf(OUTPUT_FORMAT, s, v, dv, acc);
                s += STEPWIDTH;
            }
            v += STEPWIDTH;
            writer.printf("%n");
        }
        writer.close();
    }

    private void writeConstSpeed(String format, double simulationTimestep, final String label,
            LongitudinalModelBase accModel) {
        writer = createWriter(String.format(format, label));
        writeHeader(simulationTimestep);

        final double v = CONST_SPEED;

        double sMin = QuantityRange.S.minValue;
        double sMax = QuantityRange.S.maxValue;

        double dvMin = QuantityRange.DV.minValue;
        double dvMax = QuantityRange.DV.maxValue;

        double s = sMin;
        while (s <= sMax) {
            double dv = dvMin;
            while (dv <= dvMax) {
                double acc = accModel.calcAccSimple(s, v, dv);
                writer.printf(OUTPUT_FORMAT, s, v, dv, acc);
                dv += STEPWIDTH;
            }
            s += STEPWIDTH;
            writer.printf("%n");
        }
        writer.close();
    }

    private void writeConstSpeedLeader(String format, double simulationTimestep, final String label,
            LongitudinalModelBase accModel) {
        writer = createWriter(String.format(format, label));
        writeHeader(simulationTimestep);

        final double vLead = CONST_SPEED_LEADER;

        double sMin = QuantityRange.S.minValue;
        double sMax = QuantityRange.S.maxValue;

        double dvMin = QuantityRange.DV.minValue;
        double dvMax = QuantityRange.DV.maxValue;

        double s = sMin;
        while (s <= sMax) {
            double dv = dvMin;
            while (dv <= dvMax) {
                double v = vLead + dv;
                double acc = accModel.calcAccSimple(s, v, dv);
                writer.printf(OUTPUT_FORMAT, s, v, dv, acc);
                dv += STEPWIDTH;
            }
            s += STEPWIDTH;
            writer.printf("%n");
        }
        writer.close();
    }

}
