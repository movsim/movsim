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
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.FileNameUtils;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MovSim console command line parser. Values from the command line are set to ProjectMetaData.
 */
public class MovsimCommandLine {

    private static Logger logger = LoggerFactory.getLogger(MovsimCommandLine.class);

    final CommandLineParser parser;
    private Options options;
    protected final ProjectMetaData projectMetaData;

    public static void parse(ProjectMetaData projectMetaData, String[] args) {
        final MovsimCommandLine commandLine = new MovsimCommandLine(projectMetaData);
        try {
            commandLine.createAndParse(args);
        } catch (ParseException e) {
            logger.error("Parsing failed.  Reason: {}", e.getMessage());
            commandLine.optionHelp();
        }
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
    private MovsimCommandLine(ProjectMetaData projectMetaData) {
        this.projectMetaData = projectMetaData;
        createOptions();
        parser = new GnuParser();
    }

    /**
     * Parse the command line.
     * 
     * @param args
     *            the args
     * @throws ParseException
     */
    private void createAndParse(String[] args) throws ParseException {
        final CommandLine cmdline = parser.parse(options, args);
        parse(cmdline);
    }

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
            optionHelp();
        }
        if (cmdline.hasOption("d")) {
            optionValidation();
        }
        if (cmdline.hasOption("i")) {
            optionInternalXml();
        }
        if (cmdline.hasOption("w")) {
            optionWriteDtd();
        }
        if (cmdline.hasOption("l")) {
            optWriteLoggingProperties();
        }
        if (cmdline.hasOption("v")) {
            optionPrintVersion();
        }
        requiredOptionOutputPath(cmdline);
        requiredOptionSimulation(cmdline);
    }

    /**
     * @param cmdline
     */
    private void requiredOptionOutputPath(CommandLine cmdline) {
        String outputPath = cmdline.getOptionValue('o');

        if (outputPath == null || outputPath.equals("") || outputPath.isEmpty()) {
            outputPath = ".";
            logger.info("No output path provided via option. Set output path to current directory!");
        }
        logger.info("output path: " + outputPath);
        final boolean outputPathExits = FileUtils.dirExists(outputPath, "dir exits");
        if (!outputPathExits) {
            FileUtils.createDir(outputPath, "");
        }
        projectMetaData.setOutputPath(FileUtils.getCanonicalPath(outputPath));
    }

    /**
     * Option: prints the version number of this Movsim release.
     */
    private static void optionPrintVersion() {
        logger.info("movsim release version: " + MovsimConstants.RELEASE_VERSION);

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
        logger.info("logger properties file written to {}", filename);
      
        System.exit(0);
    }

    /**
     * Option: writes multiModelTrafficSimulatirInput.dtd to file system
     */
    private void optionWriteDtd() {
        final String resource = File.separator + projectMetaData.getDtdPath() + File.separator
                + projectMetaData.getDdtFilename();
        final InputStream is = MovsimCoreMain.class.getResourceAsStream(resource);
        FileUtils.resourceToFile(is, projectMetaData.getDdtFilename());
        logger.info("dtd file written to {}", projectMetaData.getDdtFilename());

        System.exit(0);
    }

    /**
     * Option: write internal xml (without simulation).
     */
    private void optionInternalXml() {
        projectMetaData.setWriteInternalXml(true);
    }

    /**
     * Option: parse xml input file for validation (without simulation).
     */
    private void optionValidation() {
        projectMetaData.setOnlyValidation(true);
    }

    /**
     * Option simulation.
     * 
     * @param cmdline
     *            the cmdline
     */
    private void requiredOptionSimulation(CommandLine cmdline) {
        final String filename = cmdline.getOptionValue('f');
        if (filename == null || !FileUtils.fileExists(filename)) {
            logger.error("No xml configuration file! Please specify via the option -f.");
            return;
        }

        final boolean isXml = FileNameUtils.validateFileName(filename, ProjectMetaData.getMovsimConfigFileEnding());
        if (isXml) {
            final String name = FileUtils.getName(filename);
            projectMetaData
                    .setProjectName(name.substring(0, name.indexOf(ProjectMetaData.getMovsimConfigFileEnding())));
            projectMetaData.setPathToProjectXmlFile(FileUtils.getCanonicalPathWithoutFilename(filename));
        } else {
            System.err.println("movsim configuration file " + filename + " is not a valid xml.");
            System.exit(-1);
        }
    }


    /**
     * Option help.
     */
    private void optionHelp() {
        System.out.println("option -h. Exit Programm");

        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("movsim", options);
        System.exit(0);
    }

}
