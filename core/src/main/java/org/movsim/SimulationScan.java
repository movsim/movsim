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
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.xml.MovsimInputLoader;
import org.xml.sax.SAXException;

public final class SimulationScan {

    public static void invokeSimulationScan() throws JAXBException, SAXException {

	Movsim inputData = MovsimInputLoader.getInputData(ProjectMetaData.getInstance().getInputFile());

        // TODO quick hack
        double uncertaintyMin = 0;
        double uncertaintyMax = 2;
        double uncertaintyStep = 0.2;

        double fractionMin = 0.0;
        double fractionMax = 1.0;
        double fractionStep = 0.1;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            Simulator simRun = MovsimCoreMain.invokeSingleSimulation(inputData);
            sb.append(i).append(" ").append(simRun.getRoadNetwork().totalVehicleTravelTime()).append("\n");
        }

        // for (double fraction = fractionMin; fraction <= fractionMax; fraction = fraction + fractionStep) {
        // for (double uncertainty = uncertaintyMin; uncertainty <= uncertaintyMax; uncertainty = uncertainty
        // + uncertaintyStep) {
        // inputData.getScenario().getSimulation().getTrafficComposition().getVehicleType().get(0)
        // .setFraction(fraction);
        //
        // Preconditions.checkArgument(Math.abs(inputData.getScenario().getSimulation().getTrafficComposition().getVehicleType().get(0).getFraction()
        // -fraction)<0.0001);
        //
        //
        //
        // inputData.getScenario().getSimulation().getTrafficComposition().getVehicleType().get(1)
        // .setFraction((1 - fraction));
        // inputData.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getDecisionPoints()
        // .setUncertainty(uncertainty);
        //
        // Simulator simRun = MovsimCoreMain.invokeSingleSimulation(inputData);
        //
        // sb.append(fraction).append(" ").append(uncertainty).append(" ").append(simRun.getRoadNetwork().totalVehicleTravelTime()).append("\n");
        // }
        // }

        writeFile(sb.toString(), "totalVehicleTravelTime.dat");

        // inputData.getScenario().getSimulation().getTrafficComposition().getVehicleType().get(0).setFraction(0.0);
        // Simulator simRun = MovsimCoreMain.invokeSingleSimulation(inputData);
        // System.out.println("result = " + simRun.getRoadNetwork().totalVehicleTravelTime());
    }

    private static void writeFile(String text, String outputFile) {

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
