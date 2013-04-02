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
package org.movsim.output.detector;

import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.utilities.Units;

/**
 * The Class FileDetector.
 */
public class FileDetector extends FileOutputBase {

    private static final String extensionFormat = ".det.road_%s.x_%d.csv";

    private static final String outputHeadingTime = String.format("%s%10s,", COMMENT_CHAR, "t[s]");
    private static final String outputHeadingLaneAverage = String.format("%10s,%10s,%10s,%10s,%10s,%10s,%10s,",
            "nVehTotal[1]", "nTotalAccum[1]", "V[km/h]", "flow[1/h/lane]", "occup[1]", "1/<1/v>[km/h]",
            "<1/Tbrut>[1/s]");
    private static final String outputHeadingLane = String.format("%10s,%10s,%10s,%10s,%10s,%10s,%10s,", "nVeh[1]",
            "nAccum[1]", "V[km/h]", "flow[1/h]", "occup[1]", "1/<1/v>[km/h]", "<1/Tbrut>[1/s]");

    // note: number before decimal point is total width of field, not width of
    // integer part
    private static final String outputFormatTime = "%10.1f, ";
    private static final String outputFormat = "%10d, %10d, %10.3f, %10.1f, %10.7f, %10.3f, %10.5f, ";

    private final LoopDetector detector;
    private int laneCount;
    private final boolean loggingLanes;

    /**
     * Instantiates a new file detector.
     * 
     * @param detector
     *            the detector
     * @param laneCount
     */
    public FileDetector(LoopDetector detector, String roadId, int laneCount, boolean loggingLanes) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        final int xDetectorInt = (int) detector.getDetPosition();
        this.detector = detector;
        this.laneCount = laneCount;
        this.loggingLanes = (loggingLanes || laneCount == 1) ? true : false;

        writer = createWriter(String.format(extensionFormat, roadId, xDetectorInt));
        writeHeader();
    }

    /**
     * Writes the header.
     * 
     */
    private void writeHeader() {
        writer.printf(COMMENT_CHAR + " number of lanes = %d. (most inner lane is = %d and increasing to outer lanes)%n",
                laneCount, Lanes.MOST_INNER_LANE);
        writer.printf(COMMENT_CHAR + " dtSample in seconds = %-8.4f%n", detector.getDtSample());
        writer.printf(COMMENT_CHAR + " logging lanes = %s%n", loggingLanes);
        writer.printf(outputHeadingTime);
        if (laneCount > 1) {
            write(outputHeadingLaneAverage);
        }
        if (loggingLanes) {
            for (int i = 0; i < laneCount; i++) {
                write(outputHeadingLane);
            }
        }
        writer.printf("%n");
        writer.flush();
    }

    /**
     * Pulls data and writes aggregated data to output file.
     * 
     * @param time
     *            the time
     */
    protected void writeAggregatedData(double time) {
        writer.printf(outputFormatTime, time);
        if (laneCount > 1) {
            writeLaneAverages();
        }
        if (loggingLanes) {
            writeQuantitiesPerLane();
        }
        writer.printf("%n");
    }

    /**
     * Writes out the values per lane.
     * 
     * @param time
     */
    private void writeQuantitiesPerLane() {
        for (int i = 0; i < laneCount; i++) {
            write(outputFormat, detector.getVehCountOutput(i), detector.getVehCumulatedCountOutput(i), Units.MS_TO_KMH
                    * detector.getMeanSpeed(i), Units.INVS_TO_INVH * detector.getFlow(i), detector.getOccupancy(i),
                    Units.MS_TO_KMH * detector.getMeanSpeedHarmonic(i), detector.getMeanTimegapHarmonic(i));
        }
    }

    /**
     * Writes out the values over all lanes.
     * 
     * @param time
     */
    private void writeLaneAverages() {
        write(outputFormat, detector.getVehCountOutputAllLanes(), detector.getVehCumulatedCountOutputAllLanes(),
                Units.MS_TO_KMH * detector.getMeanSpeedAllLanes(), Units.INVS_TO_INVH * detector.getFlowAllLanes(),
                detector.getOccupancyAllLanes(), Units.MS_TO_KMH * detector.getMeanSpeedHarmonicAllLanes(),
                detector.getMeanTimegapHarmonicAllLanes());
    }

}
