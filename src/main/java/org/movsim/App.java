/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.input.commandline.SimCommandLine;
import org.movsim.input.commandline.impl.SimCommandLineImpl;
import org.movsim.input.impl.InputDataImpl;
import org.movsim.input.impl.XmlReaderSimInput;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.impl.SimulatorImpl;
import org.movsim.ui.controller.Controller;
import org.movsim.ui.controller.impl.SimulatorController;
import org.movsim.ui.controller.impl.SimulatorGUIController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class App.
 */
public class App {

    // Define a static logger variable
    // Logging with slf4j, a facade for log4j
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(App.class);

    /** The Constant xmlDefault. */
    final static String xmlDefault = "sim/onramp_IDM.xml";

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {

        final App universalprogram = new App();
        universalprogram.runAsAplication(args);

    }

    /**
     * Run as aplication.
     * 
     * @param args
     *            the args
     */
    private void runAsAplication(String[] args) {

        initLocalizationAndLogger();

        // CommandLine args options Parser
        String xmlFilename;
        final SimCommandLine cmdline = new SimCommandLineImpl(args);
        if (cmdline.isWithXmlSimulationConfigFile()) {
            xmlFilename = cmdline.getSimulationFilename();
        } else {
            xmlFilename = xmlDefault;
        }

        final InputDataImpl inputData = new InputDataImpl();

        // parse xmlFile and set values
        final XmlReaderSimInput xmlReader = new XmlReaderSimInput(xmlFilename, cmdline, inputData);

        final Simulator simulator = new SimulatorImpl(cmdline.isGui(), inputData);
        
        // TODO raus für release
        if (cmdline.isGui()) {
            Controller controller = new SimulatorGUIController(simulator);
        } else {
            // commandline tool
            final Controller controller = new SimulatorController(simulator); 
        }

    }

    /**
     * Inits the localization and logger.
     */
    private static void initLocalizationAndLogger() {
        Locale.setDefault(Locale.US);
            final File file = new File("log4j.properties");
            if (file.exists() && file.isFile()) {
            PropertyConfigurator.configure("log4j.properties");
        } else {
            final URL log4jConfig = App.class.getResource("/sim/log4j.properties");
            PropertyConfigurator.configure(log4jConfig);
        }
        // Log Levels: DEBUG < INFO < WARN < ERROR
        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and  Martin Budden (2010, 2011)");
    }

}
