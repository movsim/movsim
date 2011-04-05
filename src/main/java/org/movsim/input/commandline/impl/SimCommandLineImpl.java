/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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
package org.movsim.input.commandline.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.movsim.input.commandline.SimCommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimCommandLineImpl implements SimCommandLine {

    private static Logger logger = LoggerFactory.getLogger(SimCommandLineImpl.class);
    private Options options;
    private String simulationFilename;
    private boolean withSimulation = false;
    private boolean gui = false;

    /**
     * 
     */
    public SimCommandLineImpl(String[] args) {

        logger.debug("Begin CommandLine Parser");

        createOptions();
        createParserAndParse(args);

        logger.debug("End CommandLine Parser");
    }

    /**
     * 
     */
    private void createOptions() {

        options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("d", "default", false, "simulate with default simulation xmlfile ");
        options.addOption("g", "gui", false, "start a Desktop GUI");
        
        // options.addOption("f", "simulation", true,
        // "argument has to be a xml file specifing the simulation");

        Option xmlSimFile = OptionBuilder.withArgName("file").hasArg()
                .withDescription("argument has to be a xml file specifing the simulation").create("f");
        options.addOption(xmlSimFile);
    }

    /**
     * @param args
     */
    private void createParserAndParse(String[] args) {
        // create the parser
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine cmdline = parser.parse(options, args);
            parse(cmdline);
        } catch (ParseException exp) {
            // oops, something went wrong
            logger.error("Parsing failed.  Reason: {}", exp.getMessage());
            System.out.printf("Parsing failed.  Reason: %s %n", exp.getMessage());
            optHelp();
        }
    }

    /**
     * @param cmdline
     */
    private void parse(CommandLine cmdline) {
        if (cmdline.hasOption("h")) {
            optHelp();
        }
        if (cmdline.hasOption("d")) {
            optDefault();
        }
        if (cmdline.hasOption("f")) {
            optSimulation(cmdline);
        }
        if (cmdline.hasOption("g")) {
            optGUI();
        }
    }

    /**
     * 
     */
    private void optGUI() {
        logger.debug("option --gui");
        gui = true;
    }

    /**
     * @param cmdline
     */
    private void optSimulation(CommandLine cmdline) {
        simulationFilename = cmdline.getOptionValue('f');
        if (simulationFilename == null) {
            logger.warn("No configfile as option passed. Start Simulation with default.");
            optDefault();
        } else {
            withSimulation = validateSimulationFileName(simulationFilename);
        }

    }

    /**
     * 
     */
    private void optDefault() {
        logger.debug("option --default");
    }

    /**
     * 
     */
    private void optHelp() {
        logger.debug("option -h. Exit Programm");

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("App", options);

        System.exit(0);
    }

    /**
     * @param filename
     */
    private boolean validateSimulationFileName(String filename) {
        int i = filename.lastIndexOf(".xml");
        if (i < 0) {
            logger.error("Please provide simulation file with ending \".xml\" as argument with option -s, exit. ");
            System.exit(1);
        }
        logger.info("projectName = " + filename.substring(0, i));
        return true;

    }

    public boolean isWithSimulation() {
        return withSimulation;
    }

    public String getSimulationFilename() {
        return simulationFilename;
    }

    public boolean isGui() {
        return gui;
    }

}
