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

import org.movsim.autogen.Trajectories;
import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The Class FileTrajectories.
 */
public class FileTrajectories extends FileOutputBase implements SimulationTimeStep {

    private static final String extensionFormat = ".traj.route_%s.csv";
    private static final String outputHeading = COMMENT_CHAR
            + "     t[s], lane,       x[m],     v[m/s],   a[m/s^2],     gap[m],    dv[m/s], label,           id,  roadId, originId, infoComment, absTime, xWithOffset[m]";
    private static final String outputFormat = "%10.2f, %4d, %10.1f, %10.4f, %10.5f, %10.2f, %10.6f,  %s, %12d, %8d, %8d, %s, %10.4f, %s%n";

    /** The Constant LOG. */
    private final static Logger LOG = LoggerFactory.getLogger(FileTrajectories.class);

    private final double positionIntervalStart;
    private final double positionIntervalEnd;
    private double time;
    private double lastUpdateTime = 0;
    private final Route route;

    private final Trajectories traj;

    /**
     * Instantiates a new trajectories.
     * 
     * @param traj
     *            the trajectories input
     */
    public FileTrajectories(Trajectories traj, Route route) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        this.traj = Preconditions.checkNotNull(traj);
        this.route = Preconditions.checkNotNull(route);
        positionIntervalStart = 0;
        positionIntervalEnd = route.getLength();

        LOG.info("interval for output: timeStart=" + (traj.isSetStartTime() ? traj.getStartTime() : "--")
                + ", timeEnd=" + (traj.isSetEndTime() ? traj.getEndTime() : "--"));
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
        if (isLargerThanStartTimeInterval() && isSmallerThanEndTimeInterval()) {
            if (iterationCount % 1000 == 0) {
                LOG.info("time = {}, timestep= {}", time, dt);
            }
            if ((time - lastUpdateTime + MovsimConstants.SMALL_VALUE) >= traj.getDt()) {
                lastUpdateTime = time;
                String formattedTime = ProjectMetaData.getInstance().getFormatedTimeWithOffset(simulationTime);
                writeTrajectories(formattedTime);
            }
        }
    }

    private boolean isLargerThanStartTimeInterval() {
        if (!traj.isSetStartTime()) {
            return true;
        }
        return time >= traj.getStartTime();
    }

    private boolean isSmallerThanEndTimeInterval() {
        if (!traj.isSetEndTime()) {
            return true;
        }
        return time <= traj.getEndTime();
    }

    /**
     * Write trajectories.
     * 
     * @param roadSegment
     */
    private void writeTrajectories(String formattedTime) {
        double positionOnRoute = 0.0;
        for (final RoadSegment roadSegment : route) {
            for (LaneSegment laneSegment : roadSegment.laneSegments()) {
                for (final Vehicle vehicle : laneSegment) {
                    if (vehicle.type() == Vehicle.Type.OBSTACLE) {
                        continue;
                    }
                    if (!traj.isSetRandomFraction() || vehicle.getRandomFix() < traj.getRandomFraction()) {
                        if (vehicle.getFrontPosition() >= positionIntervalStart
                                && vehicle.getFrontPosition() <= positionIntervalEnd) {
                            writeVehicleData(vehicle, positionOnRoute, laneSegment.frontVehicle(vehicle), formattedTime);
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
    private void writeVehicleData(Vehicle me, double positionOnRoute, Vehicle frontVehicle, String formattedTime) {
        final double pos = me.getFrontPosition() + positionOnRoute;
        final double s = (frontVehicle == null || frontVehicle.type() == Vehicle.Type.OBSTACLE) ? 0 : me
                .getNetDistance(frontVehicle);
        final double dv = (frontVehicle == null || frontVehicle.type() == Vehicle.Type.OBSTACLE) ? 0 : me
                .getRelSpeed(frontVehicle);
        write(outputFormat, time, me.lane(), pos, me.getSpeed(), me.getAcc(), s, dv, me.getLabel(), me.getId(),
                me.roadSegmentId(), me.originRoadSegmentId(), formattedTime, pos + traj.getOffsetPosition(), me
                        .getUserData().getString(","));
    }
}
