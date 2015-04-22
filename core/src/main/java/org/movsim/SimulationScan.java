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
package org.movsim;

import java.io.PrintWriter;

import javax.xml.bind.JAXBException;

import org.movsim.autogen.Movsim;
import org.movsim.autogen.VehiclePrototypeConfiguration;
import org.movsim.autogen.VehicleType;
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public final class SimulationScan {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SimulationScan.class);

    private SimulationScan() {
        throw new IllegalStateException("do not instanciate");
    }

    static final String OUTPUT_NAME = ".totalAvgTravelTime.csv";

    public static void invokeSimulationScan(final Movsim inputData) throws JAXBException, SAXException {

        // 50x50 grid scan
        double uncertaintyMin = 0;
        double uncertaintyMax = 300;
        double uncertaintyStep = 30; // 6;

        double fractionMin = 0;
        double fractionMax = 1;
        double fractionStep = 0.1; // 0.02;

        String filename = ProjectMetaData.getInstance().getProjectName() + OUTPUT_NAME;
        PrintWriter writer = FileUtils.getWriter(filename);
        double fraction = fractionMin;
        double uncertainty = uncertaintyMin;
        while (fraction <= fractionMax) {
            while (uncertainty <= uncertaintyMax) {
                modifyInput(inputData, fraction, uncertainty);
                Simulator simRun = MovsimCoreMain.invokeSingleSimulation(inputData);
                writeOutput(writer, fraction, uncertainty, simRun);
                if (uncertainty < uncertaintyMax && uncertainty + uncertaintyStep > uncertaintyMax) {
                    // handle boundary explicitly, not elegant
                    uncertainty = uncertaintyMax;
                } else {
                    uncertainty += uncertaintyStep;
                }
            }
            writer.println();
            uncertainty = uncertaintyMin;
            if (fraction < fractionMax && fraction + fractionStep > fractionMax) {
                // handle boundary explicitly
                fraction = fractionMax;
            } else {
                fraction += fractionStep;
            }
        }
        writer.close();
    }

    private static void writeOutput(PrintWriter writer, double fraction, double uncertainty, Simulator simRun) {
        StringBuilder sb = new StringBuilder();
        double avgTravelTime = simRun.getRoadNetwork().totalVehicleTravelTime()
                / simRun.getRoadNetwork().totalVehiclesRemoved();
        sb.append(String.format("%.3f", fraction)).append(", ").append(String.format("%.3f", uncertainty)).append(", ")
                .append(String.format("%.3f", avgTravelTime));
        writer.println(sb.toString());
        writer.flush();
    }

    private static void modifyInput(final Movsim inputData, double fraction, double uncertainty) {
        Preconditions.checkArgument(inputData.getScenario().getSimulation().getTrafficComposition().getVehicleType()
                .size() == 2);
        VehicleType equippedVehicleType = inputData.getScenario().getSimulation().getTrafficComposition()
                .getVehicleType().get(0);
        Preconditions.checkArgument(equippedVehicleType.getLabel().equals("Equipped"));
        equippedVehicleType.setFraction(fraction);

        VehicleType nonEquippedVehicleType = inputData.getScenario().getSimulation().getTrafficComposition()
                .getVehicleType().get(1);
        Preconditions.checkArgument(nonEquippedVehicleType.getLabel().equals("NonEquipped"));
        nonEquippedVehicleType.setFraction(1 - fraction);

        VehiclePrototypeConfiguration equippedVehPrototypeConfig = inputData.getVehiclePrototypes()
                .getVehiclePrototypeConfiguration().get(0);
        Preconditions.checkArgument(equippedVehPrototypeConfig.getLabel().equals("Equipped"));
        Preconditions.checkArgument(equippedVehPrototypeConfig.getPersonalNavigationDevice().isSetServiceProvider());
        equippedVehPrototypeConfig.getPersonalNavigationDevice().setUncertainty(uncertainty);
        Preconditions.checkArgument(inputData.getServiceProviders().getServiceProvider().size() == 1);
        inputData.getServiceProviders().getServiceProvider().get(0).getDecisionPoints().setUncertainty(uncertainty);
        LOG.info("### modified input: uncertainty={}, fraction={}", uncertainty, fraction);
    }

}
