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
import org.movsim.utilities.impl.FileUtils;

/**
 * The Class SimCommandLineImpl. MovSim console command line parser.
 */
public class SimCommandLineImpl implements SimCommandLine {
    
    final static String releaseVersion = "1.0";

    /** The Constant xmlDefault. */
    final static String xmlDefault = "sim/onramp_IDM.xml";

    /** The options. */
    private Options options;

    /** The simulation filename. */
    private String simulationFilename;

    /** The flag for only validatiion of xml input file without simulation */
    private boolean onlyValidation = false;

    /**
     * The flag for writing xml config file of the simulation input after
     * validation from dtd
     */
    private boolean writeInternalXml;


    /**
     * Instantiates a new sim command line impl.
     * 
     * @param args
     *            the args
     */
    public SimCommandLineImpl(String[] args) {

        createOptions();
        createParserAndParse(args);
    }

    /**
     * Creates the options.
     */
    private void createOptions() {

        options = new Options();
        options.addOption("h", "help", false, "prints this message");
        options.addOption("v", "validate", false, "parses xml input file for validation (without simulation)");
        options.addOption("i", "internal_xml", false,
                "Writes internal xml (the simulation configuration) after validation from dtd. No simulation");
        options.addOption("w", "write dtd", false, "writes dtd file to file");
        options.addOption("l", "log", false,
                "writes the file \"log4j.properties\" to file to adjust the logging properties on an individual level");
        options.addOption("v", "version", false, "prints version number of this MovSim release");;
        OptionBuilder.withArgName("file");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("argument has to be a xml file specifing the configuration of the simulation");
        final Option xmlSimFile = OptionBuilder.create("f");
        options.addOption(xmlSimFile);
    }

    /**
     * Creates the parser and parse.
     * 
     * @param args
     *            the args
     */
    private void createParserAndParse(String[] args) {
        // create the parser
        final CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            final CommandLine cmdline = parser.parse(options, args);
            parse(cmdline);
        } catch (final ParseException exp) {
            // something went wrong
            System.out.printf("Parsing failed.  Reason: %s %n", exp.getMessage());
            optHelp();
        }
    }

    /**
     * Parses the command line.
     * 
     * @param cmdline
     *            the cmdline
     */
    private void parse(CommandLine cmdline) {
        if (cmdline.hasOption("h")) {
            optHelp();
        }
        if (cmdline.hasOption("v")) {
            optValidation();
        }
        if (cmdline.hasOption("i")) {
            optInternalXml();
        }
        if (cmdline.hasOption("w")) {
            optWriteDtd();
        }
        if (cmdline.hasOption("l")) {
            optWriteLoggingProperties();
        }
        if (cmdline.hasOption("v")) {
            optPrintVersion();
        }
        optSimulation(cmdline);
    }


    /**
     * Option: prints the version number of this Movsim release 
     */
    private void optPrintVersion() {
        System.out.println("MovSim release: " + releaseVersion);
    }

    /**
     * Option: writes log4j.properties to local filesystem
     */
    private void optWriteLoggingProperties() {
        String resource = "/sim/log4j.properties";
        String filename = "log4j.properties";
        FileUtils.resourceToFile(resource, filename);
    }

    /**
     * Option: writes multiModelTrafficSimulatirInput.dtd to file system
     */
    private void optWriteDtd() {
        String resource = "/sim/multiModelTrafficSimulatorInput.dtd";
        String filename = "multiModelTrafficSimulatorInput.dtd";
        FileUtils.resourceToFile(resource, filename);
    }

    /**
     * Option: write internal xml (without simulation)
     */
    private void optInternalXml() {
        writeInternalXml = true;
    }

    /**
     * Option: parse xml input file for validation (without simulation)
     */
    private void optValidation() {
        onlyValidation = true;
    }

    /**
     * Option simulation.
     * 
     * @param cmdline
     *            the cmdline
     */
    private void optSimulation(CommandLine cmdline) {
        simulationFilename = cmdline.getOptionValue('f');
        if (simulationFilename == null) {
            System.out.println("No configfile as option passed. Start Simulation with default.");
            simulationFilename = xmlDefault;
        } else {
            validateSimulationFileName(simulationFilename);
        }

    }

    /**
     * Option help.
     */
    private void optHelp() {
        System.out.println("option -h. Exit Programm");

        final HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("movsim", options);

        System.exit(0);
    }

    /**
     * Validate simulation file name.
     * 
     * @param filename
     *            the filename
     * @return true, if successful
     */
    private boolean validateSimulationFileName(String filename) {
        final int i = filename.lastIndexOf(".xml");
        if (i < 0) {
            System.out
                    .println("Please provide simulation file with ending \".xml\" as argument with option -f, exit. ");
            System.exit(1);
        }
        return true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.commandline.SimCommandLine#getSimulationFilename()
     */
    @Override
    public String getSimulationFilename() {
        return simulationFilename;
    }


    public boolean isOnlyValidation() {
        return onlyValidation;
    }

    public boolean isWriteInternalXml() {
        return writeInternalXml;
    }

}
