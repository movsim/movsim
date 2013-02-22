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
package org.movsim.output.fileoutput;

import org.movsim.core.autogen.Trajectories;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileTrajectories.
 */
public class FileTrajectories extends FileOutputBase implements SimulationTimeStep {

    private static final String extensionFormat = ".traj.route_%s.csv";
    private static final String outputHeading = COMMENT_CHAR
            + "     t[s], lane,       x[m],     v[m/s],   a[m/s^2],     gap[m],    dv[m/s], label,           id,  roadId, originId";
    private static final String outputFormat = "%10.2f, %4d, %10.1f, %10.4f, %10.5f, %10.2f, %10.6f,  %s, %12d, %8d, %8d%n";

    /** The Constant logger. */
    private final static Logger logger = LoggerFactory.getLogger(FileTrajectories.class);

    private final double dtOut;
    private final double randomFraction;
    private final double t_start_interval;
    private final double t_end_interval;
    private final double x_start_interval;
    private final double x_end_interval;
    private double time = 0;
    private double lastUpdateTime = 0;
    private final Route route;

    /**
     * Instantiates a new trajectories.
     * 
     * @param traj
     *            the trajectories input
     */
    public FileTrajectories(Trajectories traj, Route route) {
        super();

        dtOut = traj.getDt();
        randomFraction = (traj.getRandomFraction() < 0 || traj.getRandomFraction() > 1) ? 0 : traj.getRandomFraction();
        t_start_interval = traj.getStartTime();
        t_end_interval = traj.getEndTime();
        x_start_interval = 0;
        x_end_interval = route.getLength();

        this.route = route;

        logger.info("interval for output: timeStart={}, timeEnd={}", t_start_interval, t_end_interval);
        writer = createWriter(String.format(extensionFormat, route.getName()));
        writeHeader(route);
    }

    private void writeHeader(Route route) {
        writer.println(String.format("%s %s", COMMENT_CHAR, route.toString()));
        writer.println(outputHeading);
        writer.flush();
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        this.time = simulationTime;
        // check time interval for output:
        if (time >= t_start_interval && time <= t_end_interval) {
            if (iterationCount % 1000 == 0) {
                logger.info("time = {}, timestep= {}", time, dt);
            }
            if ((time - lastUpdateTime + MovsimConstants.SMALL_VALUE) >= dtOut) {
                lastUpdateTime = time;
                writeTrajectories();
            }
        }
    }

    /**
     * Write trajectories.
     * 
     * @param roadSegment
     */
    private void writeTrajectories() {
        double positionOnRoute = 0.0;
        for (final RoadSegment roadSegment : route) {
            final int laneCount = roadSegment.laneCount();
            for (int lane = 0; lane < laneCount; ++lane) {
                LaneSegment laneSegment = roadSegment.laneSegment(lane);
                for (final Vehicle vehicle : laneSegment) {
                    if (vehicle.type() == Vehicle.Type.OBSTACLE) {
                        continue;
                    }
                    if (randomFraction == 0 || vehicle.getRandomFix() < randomFraction) {
                        if (vehicle.getFrontPosition() >= x_start_interval
                                && vehicle.getFrontPosition() <= x_end_interval) {
                            writeVehicleData(vehicle, positionOnRoute, laneSegment.frontVehicle(vehicle));
                        }
                    }
                }
            }
            positionOnRoute += roadSegment.roadLength();
        }
    }

    /**
     * Write vehicle data.
     * 
     * @param me
     * @param positionOnRoute
     * @param frontVehicle
     */
    private void writeVehicleData(Vehicle me, double positionOnRoute, Vehicle frontVehicle) {
        final double pos = me.getFrontPosition() + positionOnRoute;
        final double s = (frontVehicle == null || (frontVehicle != null && frontVehicle.type() == Vehicle.Type.OBSTACLE)) ? 0
                : me.getNetDistance(frontVehicle);
        final double dv = (frontVehicle == null || (frontVehicle != null && frontVehicle.type() == Vehicle.Type.OBSTACLE)) ? 0
                : me.getRelSpeed(frontVehicle);
        write(outputFormat, time, me.getLane(), pos, me.getSpeed(), me.getAcc(), s, dv, me.getLabel(), me.getId(),
                me.roadSegmentId(), me.originRoadSegmentId());
    }
}
