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
package org.movsim.output.fileoutput;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileTrajectories.
 */
public class FileTrajectories extends FileOutputBase {

    private static final String extensionFormat = ".traj.road_%s.csv";
    private static final String outputHeading = COMMENT_CHAR
            + "     t[s], lane,       x[m],     v[m/s],   a[m/s^2],     gap[m],    dv[m/s], label,           id";
    private static final String outputFormat = "%10.2f, %4d, %10.1f, %10.4f, %10.5f, %10.2f, %10.6f,  %s, %12d%n";

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FileTrajectories.class);

    private final double dtOut;
    private final double t_start_interval;
    private final double t_end_interval;
    private final double x_start_interval;
    private final double x_end_interval;
    private final HashMap<String, PrintWriter> fileHandles;
    private double time = 0;
    private double lastUpdateTime = 0;
    private final RoadNetwork roadNetwork;

    /**
     * Instantiates a new trajectories.
     * 
     * @param trajectoriesInput
     *            the trajectories input
     * @param roadSection
     *            the road section
     */
    public FileTrajectories(TrajectoriesInput trajectoriesInput, RoadNetwork roadNetwork) {
        super();
        logger.info("Constructor");

        dtOut = trajectoriesInput.getDt();
        t_start_interval = trajectoriesInput.getStartTime();
        t_end_interval = trajectoriesInput.getEndTime();
        x_start_interval = trajectoriesInput.getStartPosition();
        x_end_interval = trajectoriesInput.getEndPosition();

        this.roadNetwork = roadNetwork;

        fileHandles = new HashMap<String, PrintWriter>();
        logger.info("interval for output: timeStart={}, timeEnd={}", t_start_interval, t_end_interval);
    }

    /**
     * Creates the file handles.
     */
    private void createFileHandles() {
    	for (final RoadSegment roadSegment : roadNetwork) {
    		fileHandles.put(roadSegment.userId(), createWriter(String.format(extensionFormat, roadSegment.userId())));
    	}
//        final String filenameMainroad = path + File.separator + baseFilename
//                + String.format(extensionFormat, roadSegment.id());
//        logger.info("filenameMainroad={}, id={}", filenameMainroad, roadSegment.id());
//        fileHandles.put(roadSegment.id(), createWriter(String.format(extensionFormat, roadSegment.id())));

        
//        // onramps
//		int counter = 1;
//		for (IOnRamp rmp : mainroad.onramps()) {
//			final String filename = projectName + ".onr_" + Integer.toString(counter) + endingFile;
//			fileHandles.put(rmp.roadIndex(), FileUtils.getWriter(filename));
//			counter++;
//		}
//		// offramps
//		counter = 1;
//		for (IStreet rmp : mainroad.offramps()) {
//			final String filename = projectName + ".offr_" + Integer.toString(counter) + endingFile;
//			fileHandles.put(rmp.roadIndex(), FileUtils.getWriter(filename));
//			counter++;
//		}

        // write headers
        final Iterator<String> it = fileHandles.keySet().iterator();
        while (it.hasNext()) {
            final String id = it.next();
            final PrintWriter writer = fileHandles.get(id);
            writer.println(outputHeading);
            writer.flush();
        }
    }

    /**
     * Update.
     * 
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     */
    public void update(double simulationTime, long iterationCount) {

        if (fileHandles.isEmpty()) {
            // cannot initialize earlier because onramps and offramps are
            // constructed after constructing mainroad
            createFileHandles();
        }

        this.time = simulationTime;
        // check time interval for output:
        if (time >= t_start_interval && time <= t_end_interval) {

            if (iterationCount % 1000 == 0) {
                logger.info("time = {}, timestep= {}", time);
            }

            if ((time - lastUpdateTime + MovsimConstants.SMALL_VALUE) >= dtOut) {

                lastUpdateTime = time;

                for (final RoadSegment roadSegment : roadNetwork) {
                    writeTrajectories(fileHandles.get(roadSegment.userId()), roadSegment);
                }
                /*
                 * // onramps for(IOnRamp rmp : mainroad.onramps()){ writeTrajectories(fileHandles.get(rmp.roadIndex()),
                 * rmp.vehContainer()); } // offramps for(IStreet rmp : mainroad.offramps()){
                 * writeTrajectories(fileHandles.get(rmp.roadIndex()), rmp.vehContainer()); }
                 */
            } // of if
        }
    }

    /**
     * Write trajectories.
     * 
     * @param writer
     *            the fstr
     * @param roadSegment
     */
    private void writeTrajectories(PrintWriter writer, RoadSegment roadSegment) {
        final int laneCount = roadSegment.laneCount();
        for (int lane = 0; lane < laneCount; ++lane) {
            final LaneSegment laneSegment = roadSegment.laneSegment(lane);
            final int N = laneSegment.vehicleCount();
            for (int i = 0; i < N; i++) {
                final Vehicle me = laneSegment.getVehicle(i);
                if ((me.getMidPosition() >= x_start_interval && me.getMidPosition() <= x_end_interval)) {
                    final Vehicle frontVeh = laneSegment.frontVehicle(me);
                    writeCarData(writer, i, me, frontVeh);
                }
            }
        }
    }

    /**
     * Write car data.
     * 
     * @param writer
     *            the writer
     * @param index
     *            the index
     * @param me
     *            the me
     * @param frontVeh
     *            the front veh
     */
    private void writeCarData(PrintWriter writer, int index, final Vehicle me, final Vehicle frontVeh) {
        final double s = (frontVeh == null) ? 0 : me.getNetDistance(frontVeh);
        final double dv = (frontVeh == null) ? 0 : me.getRelSpeed(frontVeh);
        writer.printf(outputFormat, time, me.getLane(), me.getMidPosition(), me.getSpeed(), me.getAcc(), s, dv,
                me.getLabel(), me.getId());
        writer.flush();
    }
}
