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
package org.movsim.consumption.input;

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
import org.movsim.consumption.ConsumptionMain;
import org.movsim.utilities.FileNameUtils;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsumptionCommandLine{

    private static Logger logger = LoggerFactory.getLogger(ConsumptionCommandLine.class);

    private final CommandLineParser parser;
    private Options options;
    private final ConsumptionMetadata metaData;
    

    public static void parse(ConsumptionMetadata metaData, String[] args) {
        final ConsumptionCommandLine commandLine = new ConsumptionCommandLine(metaData);
        try {
            commandLine.createAndParse(args);
        } catch (ParseException e) {
            logger.error("Parsing failed.  Reason: {}", e.getMessage());
            commandLine.optionHelp();
        }
    }

    private ConsumptionCommandLine(ConsumptionMetadata metaData) {
        this.metaData = metaData;
        createOptions();
        parser = new GnuParser();
    }

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
        options.addOption("l", "log", false, "writes the file " + ConsumptionMetadata.getLog4jFilename() +
                "\" to file to adjust the logging properties on an individual level");

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
        
        optionOutputPath(cmdline);
        requiredOptionSimulation(cmdline);
    }

    /**
     * @param cmdline
     */
    private void optionOutputPath(CommandLine cmdline) {
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
        metaData.setOutputPath(FileUtils.getCanonicalPath(outputPath));
    }

    /**
     * Option: writes log4j.properties to local filesystem
     */
    private static void optWriteLoggingProperties() {
        final String resource = ConsumptionMetadata.getLog4jFilenameWithPath();
        final InputStream is = ConsumptionMain.class.getResourceAsStream(resource); 
        FileUtils.resourceToFile(is, ConsumptionMetadata.getLog4jFilename());
        logger.info("logger properties file written to {}", ConsumptionMetadata.getLog4jFilename());
      
        System.exit(0);
    }

    /**
     * Option: writes multiModelTrafficSimulatirInput.dtd to file system
     */
    private void optionWriteDtd() {
        final String resource = metaData.getDtdFilenameWithPath();
        final InputStream is = ConsumptionMain.class.getResourceAsStream(resource);
        FileUtils.resourceToFile(is, metaData.getDtdFilename());
        logger.info("dtd file written to {}", metaData.getDtdFilenameWithPath());

        System.exit(0);
    }

    /**
     * Option: write internal xml (without simulation).
     */
    private void optionInternalXml() {
        metaData.setWriteInternalXml(true);
    }

    /**
     * Option: parse xml input file for validation (without simulation).
     */
    private void optionValidation() {
        metaData.setOnlyValidation(true);
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

        final boolean isXml = FileNameUtils.validateFileName(filename, ConsumptionMetadata.getConfigFileEnding());
        if (isXml) {
            final String name = FileNameUtils.getName(filename);
            metaData.setProjectName(name.substring(0, name.indexOf(ConsumptionMetadata.getConfigFileEnding())));
            metaData.setPathToConsumptionFile(FileUtils.getCanonicalPathWithoutFilename(filename));
        } else {
            System.err.println("movsim consumption configuration file " + filename + " is not a valid xml.");
            System.exit(-1);
        }
    }


    /**
     * Option help.
     */
    private void optionHelp() {
        System.out.println("option -h. Exit Programm");

        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Movsim's Consumption Model", options);
        System.exit(0);
    }

   
}

