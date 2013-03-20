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
package org.movsim.output.route;

import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;

/**
 * The Class FileSpatioTemporal.
 */
class FileSpatioTemporal extends FileOutputBase {

    private static final String extensionFormat = ".st.route_%s.csv";
    private static final String outputHeading = COMMENT_CHAR + "     t[s],       x[m],     v[m/s],   a[m/s^2]\n";
    private static final String outputFormat = "%10.2f, %10.1f, %10.4f, %10.4f%n";

    FileSpatioTemporal(String routeLabel) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        writer = createWriter(String.format(extensionFormat, routeLabel));
        writer.printf(outputHeading);
        writer.flush();
    }

    void writeOutput(SpatioTemporal spatioTemporal, double simulationTime) {
        final int count = spatioTemporal.size();
        final double dx = spatioTemporal.getDxOutput();
        for (int i = 0; i < count; i++) {
            final double x = i * dx;
            writer.printf(outputFormat, simulationTime, x, spatioTemporal.getAverageSpeed(i),
                    spatioTemporal.getAverageAcceleration(i));
        }
        write("%n"); // block ends
    }

}
