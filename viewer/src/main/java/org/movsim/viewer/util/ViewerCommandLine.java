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
package org.movsim.viewer.util;

import org.apache.commons.cli.CommandLine;
import org.movsim.input.ProjectMetaData;
import org.movsim.input.SimCommandLine;
import org.movsim.utilities.FileUtils;

public class ViewerCommandLine extends SimCommandLine {

    public static void parse(ProjectMetaData projectMetaData, String[] args) {
        final ViewerCommandLine commandLine = new ViewerCommandLine(projectMetaData, args);
        commandLine.parse(args);
    }

    public ViewerCommandLine(ProjectMetaData projectMetaData, String[] args) {
        super(projectMetaData, args);
    }
    /**
     * Option simulation.
     * 
     * @param cmdline
     *            the cmdline
     */
    @Override
    public void optSimulation(CommandLine cmdline) {
        final String filename = cmdline.getOptionValue('f');
        if (filename == null || !FileUtils.fileExists(filename)) {
            System.out.println("No xml configuration file! Please specify via the option -f. Fall back to default.");
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
}
