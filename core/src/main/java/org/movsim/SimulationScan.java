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

import java.io.FileWriter;
import java.io.PrintWriter;

import javax.xml.bind.JAXBException;

import org.movsim.autogen.Movsim;
import org.movsim.simulator.Simulator;
import org.xml.sax.SAXException;

public final class SimulationScan {

    private SimulationScan() {
        throw new IllegalStateException("do not instanciate");
    }


    public static void invokeSimulationScan(final Movsim inputData) throws JAXBException, SAXException {

        // TODO quick hack here
        int uncertaintyMin = 0;
        int uncertaintyMax = 20;
        int uncertaintyStep = 2;

        int fractionMin = 0;
        int fractionMax = 100;
        int fractionStep = 10;

        StringBuilder sb = new StringBuilder();

        // for (int i = 0; i < 10; i++) {
        // Simulator simRun = MovsimCoreMain.invokeSingleSimulation(inputData);
        // sb.append(i).append(" ").append(simRun.getRoadNetwork().totalVehicleTravelTime()).append("\n");
        // }

        for (int fraction = fractionMin; fraction <= fractionMax; fraction = fraction + fractionStep) {
            for (int uncertainty = uncertaintyMin; uncertainty <= uncertaintyMax; uncertainty = uncertainty
                    + uncertaintyStep) {
                inputData.getScenario().getSimulation().getTrafficComposition().getVehicleType().get(0)
                        .setFraction(fraction/100.0);

                inputData.getScenario().getSimulation().getTrafficComposition().getVehicleType().get(1)
                        .setFraction((1 - fraction/100.0));
                inputData.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0)
                        .getPersonalNavigationDevice().setUncertainty(uncertainty*6);

                Simulator simRun = MovsimCoreMain.invokeSingleSimulation(inputData);

                sb.append(fraction/100.0).append(" ").append(uncertainty/10.0).append(" ")
                        .append(simRun.getRoadNetwork().totalVehicleTravelTime()).append("\n");
            }
        }

        writeFile(sb.toString(), "totalVehicleTravelTime.dat");

        // inputData.getScenario().getSimulation().getTrafficComposition().getVehicleType().get(0).setFraction(0.0);
        // Simulator simRun = MovsimCoreMain.invokeSingleSimulation(inputData);
        // System.out.println("result = " + simRun.getRoadNetwork().totalVehicleTravelTime());
    }

    public static void writeFile(String text, String outputFile) {

        FileWriter outFile;
        try {
            outFile = new FileWriter(outputFile);
            PrintWriter out = new PrintWriter(outFile);
            out.println(text);
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
