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

import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.movsim.utilities.ObservableImpl;
import org.movsim.utilities.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SpatioTemporalImpl.
 */
public class SpatioTemporal extends ObservableImpl implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SpatioTemporal.class);

    private final double dtOut;
    private final double dxOut;
    private final Route route;
    private final double routeLength;

    private int size;
    private double[] density;
    private double[] averageSpeed;
    private double[] flow;
    private double timeOffset;

    /**
     * Constructor.
     * 
     * @param input
     * @param route
     */
    public SpatioTemporal(SpatioTemporalInput input, Route route) {

        dtOut = input.getDt();
        dxOut = input.getDx();

        this.route = route;
        routeLength = route.getLength();

        initialize();
    }

    /**
     * Initialize.
     */
    private void initialize() {
        timeOffset = 0;
        final int nxOut = (int) (routeLength / dxOut);
        density = new double[nxOut + 1];
        averageSpeed = new double[nxOut + 1];
        flow = new double[nxOut + 1];
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if ((simulationTime - timeOffset) >= dtOut) {
            timeOffset = simulationTime;
            // TODO quick hack for multi-lane compatibility
            calcData(simulationTime);
            notifyObservers(simulationTime);
        }
    }

    /**
     * Calculate data.
     * 
     * @param time
     *            the time
     * @param vehContainer
     *            the vehicle container
     */
    private void calcData(double time) {
        // as first implementation, just use the first roadSegment in the route
        // TODO - deal with multiple road segments in a route
        final RoadSegment roadSegment = route.iterator().next();
        assert roadSegment != null;
        final LaneSegment laneSegment = roadSegment.laneSegment(MovsimConstants.MOST_RIGHT_LANE);
        size = laneSegment.vehicleCount();
        if (size == 0) {
            return;
        }
        final double[] localDensity = new double[size];
        final double[] vMicro = new double[size];
        final double[] xMicro = new double[size];

        for (int i = 0; i < size; i++) {
            vMicro[i] = laneSegment.getVehicle(i).getSpeed();
            xMicro[i] = laneSegment.getVehicle(i).getMidPosition();
        }

        // calculate density
        localDensity[0] = 0;
        for (int i = 1; i < size; i++) {
            final double dist = xMicro[i - 1] - xMicro[i];
            final double length = laneSegment.getVehicle(i - 1).getLength();
            localDensity[i] = (dist > length) ? 1 / dist : 1 / length;
        }

        for (int j = 0; j < density.length; j++) {
            final double x = j * dxOut;
            density[j] = Tables.intpextp(xMicro, localDensity, x, true);
            averageSpeed[j] = Tables.intpextp(xMicro, vMicro, x, true);
            flow[j] = density[j] * averageSpeed[j];
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
}
