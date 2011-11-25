/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.output.impl;


import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.output.SpatioTemporal;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.utilities.impl.ObservableImpl;
import org.movsim.utilities.impl.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SpatioTemporalImpl.
 */
public class SpatioTemporalImpl extends ObservableImpl implements SpatioTemporal {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SpatioTemporalImpl.class);

    /** The dt out. */
    private final double dtOut;

    /** The dx out. */
    private final double dxOut;

    private double[] density;

    private double[] averageSpeed;

    private double[] flow;

    /** The roadlength. */
    private final double roadlength;

    /** The time offset. */
    private double timeOffset;

    /**
     * Instantiates a new spatio temporal impl.
     * 
     * @param input
     *            the input
     * @param roadSection
     *            the road section
     */
    public SpatioTemporalImpl(SpatioTemporalInput input, RoadSegment roadSegment) {

        dtOut = input.getDt();
        dxOut = input.getDx();

        roadlength = roadSegment.roadLength();

        initialize();

    }

    /**
     * Initialize.
     */
    private void initialize() {
        timeOffset = 0;
        final int nxOut = (int) (roadlength / dxOut);
        density = new double[nxOut + 1];
        averageSpeed = new double[nxOut + 1];
        flow = new double[nxOut + 1];
    }

    /**
     * Update.
     * 
     * @param it
     *            the it
     * @param time
     *            the time
     * @param roadSection
     *            the road section
     */
    public void update(long it, double time, RoadSegment roadSegment) {
        if ((time - timeOffset) >= dtOut) {
            timeOffset = time;
            // TODO quick hack for multi-lane compatibility
            calcData(time, roadSegment.laneSegment(MovsimConstants.MOST_RIGHT_LANE));
            notifyObservers(time);
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
    private void calcData(double time, LaneSegment laneSegment) {
        final int size = laneSegment.vehicleCount();
        final double[] localDensity = new double[size];
        final double[] vMicro = new double[size];
        final double[] xMicro = new double[size];

        for (int i = 0; i < size; i++) {
            vMicro[i] = laneSegment.getVehicle(i).getSpeed();
            xMicro[i] = laneSegment.getVehicle(i).getPosition();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.output.SpatioTemporal#getDtOut()
     */
    @Override
    public double getDtOut() {
        return dtOut;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.output.SpatioTemporal#getDxOut()
     */
    @Override
    public double getDxOut() {
        return dxOut;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.output.SpatioTemporal#getDensity()
     */
    @Override
    public double[] getDensity() {
        return density;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.output.SpatioTemporal#getAverageSpeed()
     */
    @Override
    public double[] getAverageSpeed() {
        return averageSpeed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.output.SpatioTemporal#getFlow()
     */
    @Override
    public double[] getFlow() {
        return flow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.output.SpatioTemporal#getTimeOffset()
     */
    @Override
    public double getTimeOffset() {
        return timeOffset;
    }

}
