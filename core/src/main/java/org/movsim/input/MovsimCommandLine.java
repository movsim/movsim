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
package org.movsim.input;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.movsim.MovsimCoreMain;
import org.movsim.logging.Logger;
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.FileUtils;

/**
 * The Class MovsimCommandLine. MovSim console command line parser. Sets the ProjectMetaData. Initializes the logger.
 */
public class MovsimCommandLine {

    final CommandLineParser parser;
    private Options options;
    protected final ProjectMetaData projectMetaData;

    public static void parse(ProjectMetaData projectMetaData, String[] args) {
        final MovsimCommandLine commandLine = new MovsimCommandLine(projectMetaData);
        commandLine.parse(args);
    }

    /**
     * Constructor.
     * 
     * Intitializes logger and localization.
     * 
     * Parses command line and sets results in ProjectMetaData.
     * 
     * @param args
     *            the args
     */
    public MovsimCommandLine(ProjectMetaData projectMetaData) {
        Logger.initializeLogger();
        this.projectMetaData = projectMetaData;
        createOptions();
        parser = new GnuParser();
    }

    /**
     * Parse the command line.
     * 
     * @param args
     *            the args
     */
    public void parse(String[] args) {
        // create the parser
        try {
            // parse the command line arguments
            final CommandLine cmdline = parser.parse(options, args);
            parse(cmdline);
        } catch (final ParseException exp) {
            // something went wrong
            System.out.printf("Parsing failed.  Reason: %s %n", exp.getMessage());
            optHelp();
        }
        final String projectName = projectMetaData.getProjectName();
        if (projectMetaData.isWriteInternalXml() && projectName.isEmpty()) {
            System.err.println("no xml file for simulation configuration found!");
            System.exit(-1);
        }
    }

    /**
     * Creates the options.
     */
    private void createOptions() {
        options = new Options();
        options.addOption("h", "help", false, "prints this message");
        options.addOption("d", "validate", false, "parses xml input file for validation (without simulation)");
        options.addOption("i", "internal_xml", false,
                "Writes internal xml (the simulation configuration) after validation from dtd. No simulation");
        options.addOption("w", "write dtd", false, "writes dtd file to file");
        options.addOption("l", "log", false,
                "writes the file \"log4j.properties\" to file to adjust the logging properties on an individual level");
        options.addOption("v", "version", false, "prints version number of this movsim release");

        OptionBuilder.withArgName("file");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("argument has to be a xml file specifing the configuration of the simulation");
        final Option xmlSimFile = OptionBuilder.create("f");
        options.addOption(xmlSimFile);

        OptionBuilder.withArgName("directory");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("argument is the output path relative to calling directory");
        final Option outputPathOption = OptionBuilder.create("o");
        options.addOption(outputPathOption);
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
        if (cmdline.hasOption("d")) {
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
        optOutputPath(cmdline);
        optSimulation(cmdline);
    }

    /**
     * @param cmdline
     */
    private void optOutputPath(CommandLine cmdline) {
        String outputPath = cmdline.getOptionValue('o');

        if (outputPath == null || outputPath.equals("") || outputPath.isEmpty()) {
            outputPath = ".";
            System.out.println("No output path provided via option. Set output path to current directory!");
        }
        System.out.println("output path: " + outputPath);
        final boolean outputPathExits = FileUtils.dirExists(outputPath, "dir exits");
        if (!outputPathExits) {
            FileUtils.createDir(outputPath, "");
        }
        projectMetaData.setOutputPath(FileUtils.getCanonicalPath(outputPath));
    }

    /**
     * Option: prints the version number of this Movsim release.
     */
    private static void optPrintVersion() {
        System.out.println("movsim release version: " + MovsimConstants.RELEASE_VERSION);

        System.exit(0);
    }

    /**
     * Option: writes log4j.properties to local filesystem
     */
    private static void optWriteLoggingProperties() {
        final String resource = File.separator + "config" + File.separator + "log4j.properties";
        final String filename = "log4j.properties";
        final InputStream is = MovsimCoreMain.class.getResourceAsStream(resource); 
        FileUtils.resourceToFile(is, filename);
        System.out.println("logger properties file written to " + filename);
      
        System.exit(0);
    }

    /**
     * Option: writes multiModelTrafficSimulatirInput.dtd to file system
     */
    private void optWriteDtd() {
        final String resource = File.separator + projectMetaData.getDtdPath() + File.separator
                + projectMetaData.getDdtFilename();
        final InputStream is = MovsimCoreMain.class.getResourceAsStream(resource);
        FileUtils.resourceToFile(is, projectMetaData.getDdtFilename());
        System.out.println("dtd file written to " + projectMetaData.getDdtFilename());

        System.exit(0);
    }

    /**
     * Option: write internal xml (without simulation).
     */
    private void optInternalXml() {
        projectMetaData.setWriteInternalXml(true);
    }

    /**
     * Option: parse xml input file for validation (without simulation).
     */
    private void optValidation() {
        projectMetaData.setOnlyValidation(true);
    }

    /**
     * Option simulation.
     * 
     * @param cmdline
     *            the cmdline
     */
    public void optSimulation(CommandLine cmdline) {
        final String filename = cmdline.getOptionValue('f');
        if (filename == null || !FileUtils.fileExists(filename)) {
            System.err.println("No xml configuration file! Please specify via the option -f.");
            System.exit(-1);
        } else {
            final boolean isXml = validateSimulationFileName(filename);
            if (isXml) {
                final String name = FileUtils.getName(filename);
                projectMetaData.setProjectName(name.substring(0, name.indexOf(".xml")));
                projectMetaData.setPathToProjectXmlFile(FileUtils.getCanonicalPathWithoutFilename(filename));
            } else {
                System.exit(-1);
            }
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
    protected static boolean validateSimulationFileName(String filename) {
        final int i = filename.lastIndexOf(".xml");
        if (i < 0) {
            System.out
                    .println("Please provide simulation file with ending \".xml\" as argument with option -f, exit. ");
            return false;
        }
        return true;

    }


}
