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
package org.movsim.output;

import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.ObservableImpl;
import org.movsim.utilities.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SpatioTemporalImpl.
 */
public class SpatioTemporal extends ObservableImpl implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SpatioTemporal.class);

    private final double dxOut;
    private final double dtOut;
    private final Route route;

    private final int size;
    private final double[] density;
    private final double[] averageSpeed;
    private final double[] flow;
    private double timeOffset;

    /**
     * Constructor.
     * 
     * @param input
     * @param routes
     */
    public SpatioTemporal(double dxOut, double dtOut, Route route) {

        this.dxOut = dxOut;
        this.dtOut = dtOut;
        this.route = route;

        timeOffset = 0;
        size = (int) (route.getLength() / dxOut) + 1;
        density = new double[size];
        averageSpeed = new double[size];
        flow = new double[size];
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if ((simulationTime - timeOffset) >= dtOut) {
            timeOffset = simulationTime;
            calcData();
            notifyObservers(simulationTime);
        }
    }

    /**
     * Calculate data.
     */
    private void calcData() {
        // TODO - deal with multiple lanes in a road segment
        int vehicleCount = 0;
        for (final RoadSegment roadSegment : route) {
            final LaneSegment laneSegment = roadSegment.laneSegment(MovsimConstants.MOST_RIGHT_LANE);
            vehicleCount += laneSegment.vehicleCount();
        }
        if (vehicleCount == 0) {
            return;
        }
        final double[] localDensity = new double[vehicleCount];
        final double[] vMicro = new double[vehicleCount];
        final double[] xMicro = new double[vehicleCount];
        final double[] lengths = new double[vehicleCount];

        int i = 0;
        for (final RoadSegment roadSegment : route) {
            final LaneSegment laneSegment = roadSegment.laneSegment(MovsimConstants.MOST_RIGHT_LANE);
            final int laneVehicleCount = laneSegment.vehicleCount();
            for (int j = 0; j < laneVehicleCount; ++j) {
                final Vehicle vehicle = laneSegment.getVehicle(j);
                vMicro[i] = vehicle.getSpeed();
                xMicro[i] = vehicle.getFrontPosition();
                lengths[i] = vehicle.getLength();
                ++i;
            }
        }

        // calculate density
        localDensity[0] = 0;
        for (i = 1; i < vehicleCount; ++i) {
            final double dist = xMicro[i - 1] - xMicro[i];
            final double length = lengths[i - 1];
            localDensity[i] = (dist > length) ? 1.0 / dist : 1.0 / length;
        }

        for (i = 0; i < size; ++i) {
            final double x = i * dxOut;
            density[i] = Tables.intpextp(xMicro, localDensity, x, true);
            averageSpeed[i] = Tables.intpextp(xMicro, vMicro, x, true);
            flow[i] = density[i] * averageSpeed[i];
        }
    }

    /**
     * Gets the dt out.
     * 
     * @return the dt out
     */
    public double getDtOut() {
        return dtOut;
    }

    /**
     * Gets the dx out.
     * 
     * @return the dx out
     */
    public double getDxOut() {
        return dxOut;
    }

    /**
     * Returns the size of the storage arrays.
     * 
     * @return the size of the arrays
     */
    public int size() {
        return size;
    }

    /**
     * Gets the density.
     * 
     * @return the density
     */
    public double getDensity(int index) {
        return density[index];
    }

    /**
     * Gets the average speed.
     * 
     * @return the average speed
     */
    public double getAverageSpeed(int index) {
        return averageSpeed[index];
    }

    /**
     * Gets the flow.
     * 
     * @return the flow
     */
    public double getFlow(int index) {
        return flow[index];
    }

    /**
     * Gets the time offset.
     * 
     * @return the time offset
     */
    public double getTimeOffset() {
        return timeOffset;
    }

    /**
     * @return the route
     */
    public Route getRoute() {
        return route;
    }
}
