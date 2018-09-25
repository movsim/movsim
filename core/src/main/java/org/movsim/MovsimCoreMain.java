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

import org.movsim.autogen.Movsim;
import org.movsim.input.MovsimCommandLine;
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.xml.InputLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.util.Locale;

/**
 * MovSim core command line interface.
 */
public class MovsimCoreMain {

    private static final Logger LOG = LoggerFactory.getLogger(MovsimCoreMain.class);

    /**
     * The main method.
     *
     * @param args the command line arguments
     * @throws SAXException
     * @throws JAXBException
     */
    public static void main(String[] args) throws JAXBException, SAXException {
        Locale.setDefault(Locale.US);
        org.movsim.logging.Logger.initializeLogger();
        MovsimCommandLine.parse(args);

        ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        if (!projectMetaData.hasProjectName()) {
            throw new IllegalArgumentException("no xml simulation configuration file provided.");
        }

        // FIXME not working
        // LogFileAppender.initialize(projectMetaData);

        // unmarshall movsim configuration file
        Movsim movsimInput = InputLoader.unmarshallMovsim(projectMetaData.getInputFile());
        if (projectMetaData.isScanMode()) {
            LOG.info("scanning mode");
            SimulationScan.invokeSimulationScan(movsimInput);
        } else {
            invokeSingleSimulation(movsimInput);
        }
    }

    public static Simulator invokeSingleSimulation(Movsim inputData) throws JAXBException, SAXException {
        Simulator simulator = new Simulator(inputData);
        simulator.initialize();
        simulator.runToCompletion();
        return simulator;
    }

}
