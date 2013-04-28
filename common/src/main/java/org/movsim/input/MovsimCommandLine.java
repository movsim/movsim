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
package org.movsim.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.movsim.utilities.FileUtils;
import org.movsim.xml.MovsimInputLoader;

/**
 * MovSim console command line parser. Values from the command line are set to ProjectMetaData.
 */
public class MovsimCommandLine {

    final CommandLineParser parser;
    private Options options;

    public static void parse(String[] args) {
        final MovsimCommandLine commandLine = new MovsimCommandLine();
        try {
            commandLine.createAndParse(args);
        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
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
    private MovsimCommandLine() {
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
        options.addOption("v", "validate", false, "parses xml input file for validation (without simulation)");
        options.addOption("w", "write xsd", false,
                "writes xsd file to output (for convenience/lookup schema definitions)");
        options.addOption("l", "log", false,
                "writes the file \"log4j.properties\" to file to adjust the logging properties on an individual level");

        OptionBuilder.withArgName("file");
        OptionBuilder.hasArg();
        ProjectMetaData.getInstance();
        OptionBuilder.withDescription("movsim main configuration file (ending \""
                + ProjectMetaData.getMovsimConfigFileEnding()
                + "\" will be added automatically if not provided.");
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
        if (cmdline.hasOption("v")) {
            optionValidation();
        }
        if (cmdline.hasOption("w")) {
            optionWriteXsd();
        }
        if (cmdline.hasOption("l")) {
            optWriteLoggingProperties();
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
            System.out.println("No output path provided via option. Set output path to current directory!");
        }
        final boolean outputPathExits = FileUtils.dirExists(outputPath, "dir exits");
        if (!outputPathExits) {
            FileUtils.createDir(outputPath, "");
        }
        ProjectMetaData.getInstance().setOutputPath(FileUtils.getCanonicalPath(outputPath));
    }

    /**
     * Option: writes log4j.properties to local filesystem
     */
    private static void optWriteLoggingProperties() {
        final String resource = ProjectMetaData.getLog4jFilenameWithPath();
        final InputStream is = MovsimCommandLine.class.getResourceAsStream(resource);
        FileUtils.resourceToFile(is, ProjectMetaData.getLog4jFilename());
        System.out.println("logger properties file written to " + ProjectMetaData.getLog4jFilename());

        System.exit(0);
    }

    /**
     * Option: writes multiModelTrafficSimulatirInput.dtd to file system
     */
    private static void optionWriteXsd() {
        try {
            MovsimInputLoader.writeXsdToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void optionValidation() {
        System.out.println("Not working implemented!");
        System.exit(0);
    }

    /**
     * Option simulation.
     * 
     * @param cmdline
     *            the cmdline
     */
    private void requiredOptionSimulation(CommandLine cmdline) {
        String filename = cmdline.getOptionValue('f');
        if (filename == null || filename.isEmpty()) {
            System.err.println("No configuration file provided! Please specify a file via the option -f.");
            System.exit(-1);
        }
        if (!filename.endsWith(ProjectMetaData.getMovsimConfigFileEnding())) {
            filename = filename + ProjectMetaData.getMovsimConfigFileEnding();
        }
        if (!FileUtils.fileExists(filename)) {
            System.err.println("Configuration file \"" + filename + "\" not found!");
            System.exit(-1);
        }
        // final boolean isXml = FileNameUtils.validateFileName(filename, ProjectMetaData.getMovsimConfigFileEnding());
        File file = new File(filename);
        final String name = file.getName();
        ProjectMetaData.getInstance().setProjectName(
                name.substring(0, name.indexOf(ProjectMetaData.getMovsimConfigFileEnding())));
        ProjectMetaData.getInstance().setPathToProjectXmlFile(FileUtils.getCanonicalPathWithoutFilename(filename));
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
