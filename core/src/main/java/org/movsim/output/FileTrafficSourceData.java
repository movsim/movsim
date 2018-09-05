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
package org.movsim.output;

import org.movsim.input.ProjectMetaData;
import org.movsim.io.FileOutputBase;
import org.movsim.simulator.roadnetwork.boundaries.TrafficSourceMacro;

public class FileTrafficSourceData extends FileOutputBase implements TrafficSourceMacro.RecordDataCallback {

    private static final String EXTENSION_FORMAT = ".source.road_%s.csv";
    private static final String OUTPUT_HEADING =
            COMMENT_CHAR + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],    count,      queue\n";
    private static final String OUTPUT_FORMAT = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %8d, %10.5f%n";

    public FileTrafficSourceData(String roadId) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        writer = createWriter(String.format(EXTENSION_FORMAT, roadId));
        writer.printf(OUTPUT_HEADING);
    }

    @Override
    public void recordData(double simulationTime, int laneEnter, double xEnter, double vEnter, double totalInflow,
            int enteringVehCounter, double nWait) {
        writer.printf(OUTPUT_FORMAT, simulationTime, laneEnter, xEnter, 3.6 * vEnter, 3600 * totalInflow,
                enteringVehCounter, nWait);
        writer.flush();
    }
}
