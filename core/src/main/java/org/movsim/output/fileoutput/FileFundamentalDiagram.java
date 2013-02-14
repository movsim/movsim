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
package org.movsim.output.fileoutput;

import org.movsim.input.model.vehicle.VehicleInput;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.EquilibriumProperties;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.EquilibriumPropertiesFactory;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelFactory;
import org.movsim.utilities.Units;

/**
 * The Class FileFundamentalDiagram.
 */
public class FileFundamentalDiagram extends FileOutputBase {
    
    private static final String extensionFormat = ".fund_%s.csv";
    private static final String outputHeading = String.format("%s %8s, %8s, %8s, %8s%n", FileOutputBase.COMMENT_CHAR,
            "rho[1/km]", "s[m]", "vEq[km/h]", "Q[veh/h]");
    private static final String outputFormat = "%8.2f, %8.2f, %8.2f, %8.2f%n";

    public static void writeToFile(double simulationTimestep, VehicleInput vehicleInput) {
        new FileFundamentalDiagram(simulationTimestep, vehicleInput);
    }

    /** Simulation timestep is model parameter for iterated map models (and cellular automata) */
    private FileFundamentalDiagram(double simulationTimestep, VehicleInput vehicleInput) {
        super();
        final String label = vehicleInput.getLabel();

        final double vehicleLength = vehicleInput.getLength();

        final LongitudinalModelBase longModel = LongitudinalModelFactory.create(vehicleLength,
                vehicleInput.getAccelerationModelInputData(), simulationTimestep);
        final EquilibriumProperties eqProperties = EquilibriumPropertiesFactory.create(vehicleLength, longModel);

        writer = createWriter(String.format(extensionFormat, label));
        writeHeader(simulationTimestep, eqProperties);
        writeFundamentalDiagram(eqProperties);
        writer.close();
    }

    private void writeHeader(double timestep, EquilibriumProperties equilibriumProperties) {
        writer.printf("%s rho at max Q = %8.3f%n", FileOutputBase.COMMENT_CHAR,
                1000 * equilibriumProperties.getRhoQMax());
        writer.printf("%s max Q        = %8.3f%n", FileOutputBase.COMMENT_CHAR, Units.INVS_TO_INVH
                * equilibriumProperties.getQMax());
        writer.printf("%s simulation timestep (model parameter for iterated map models) = %.3f%n",
                FileOutputBase.COMMENT_CHAR, timestep);
        writer.printf(outputHeading);
    }

    private void writeFundamentalDiagram(EquilibriumProperties equilibriumProperties) {
        final int count = equilibriumProperties.getVEqCount();
        for (int i = 0; i < count; i++) {
            final double rho = equilibriumProperties.getRho(i);
            final double s = equilibriumProperties.getNetDistance(rho);
            final double vEq = equilibriumProperties.getVEq(i);
            writer.printf(outputFormat, Units.INVM_TO_INVKM * rho, s,
                    Units.MS_TO_KMH * vEq, Units.INVS_TO_INVH * rho * vEq);
        }
    }
}
