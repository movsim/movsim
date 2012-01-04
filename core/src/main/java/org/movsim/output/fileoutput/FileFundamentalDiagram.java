/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                             <movsim.org@gmail.com>
 * ---------------------------------------------------------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MovSim.
 *  If not, see <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 *  
 * ---------------------------------------------------------------------------------------------------------------------
 */
package org.movsim.output.fileoutput;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumProperties;
import org.movsim.utilities.FileUtils;

/**
 * The Class FileFundamentalDiagram.
 */
public class FileFundamentalDiagram {

    /**
     * Instantiates a new file fundamental diagram.
     */
    private FileFundamentalDiagram() {
    }

    /**
     * Write fundamental diagrams.
     * 
     * @param prototypes
     *            the prototypes
     */
    public static void writeFundamentalDiagrams(HashMap<String, VehiclePrototype> prototypes) {
        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        final String path = projectMetaData.getOutputPath();
        final String baseFilename = projectMetaData.getProjectName();
        for (final Map.Entry<String, VehiclePrototype> entry : prototypes.entrySet()) {
            final String key = entry.getKey();
            final VehiclePrototype prototype = entry.getValue();
            if (prototype.fraction() > 0) {
                // avoid writing fundamental diagram of "obstacles"
                final String filename = path + File.separator + baseFilename + ".fund_" + key + ".csv";
                final EquilibriumProperties equilibriumProperties = prototype.getEquilibriumProperties();
                writeFundamentalDiagram(equilibriumProperties, filename);
            }
        }
    }

    /**
     * Write output.
     * 
     * @param filename
     *            the filename
     */
    private static void writeFundamentalDiagram(EquilibriumProperties equilibriumProperties, String filename) {
        final PrintWriter fstr = FileUtils.getWriter(filename);
        fstr.printf(FileOutputBase.COMMENT_CHAR + " rho at max Q = %8.3f%n", 1000 * equilibriumProperties.getRhoQMax());
        fstr.printf(FileOutputBase.COMMENT_CHAR + " max Q        = %8.3f%n", 3600 * equilibriumProperties.getQMax());
        fstr.printf(FileOutputBase.COMMENT_CHAR + " rho[1/km],  s[m],vEq[km/h], Q[veh/h]%n");
        final int count = equilibriumProperties.getVEqCount();
        for (int i = 0; i < count; i++) {
            final double rho = equilibriumProperties.getRho(i);
            final double s = equilibriumProperties.getNetDistance(rho);
            final double vEq = equilibriumProperties.getVEq(i);
            fstr.printf("%8.2f, %8.2f, %8.2f, %8.2f%n", 1000 * rho, s, 3.6 * vEq, 3600 * rho * vEq);
        }
        fstr.close();
    }
}
