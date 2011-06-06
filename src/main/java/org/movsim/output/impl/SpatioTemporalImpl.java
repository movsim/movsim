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

import java.util.List;

import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.output.SpatioTemporal;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
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
     * Instantiates a new macro3 d impl.
     *
     * @param input the input
     * @param roadSection the road section
     */
    public SpatioTemporalImpl(SpatioTemporalInput input, RoadSection roadSection) {

        dtOut = input.getDt();
        dxOut = input.getDx();

        roadlength = roadSection.roadLength();

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

    public void update(int it, double time, RoadSection roadSection) {
        if ((time - timeOffset) >= dtOut) {
            // logger.info("update: write to file. time = {}h", time / 60.,
            // time/3600.);
            timeOffset = time;
            calcData(time, roadSection.vehContainer());
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
    private void calcData(double time, VehicleContainer vehContainer) {
        final List<Vehicle> vehicles = vehContainer.getVehicles();
        final int size = vehicles.size();
        final double[] localDensity = new double[size];
        final double[] vMicro = new double[size];
        final double[] xMicro = new double[size];

        for (int i = 0; i < size; i++) {
            vMicro[i] = vehicles.get(i).getSpeed();
            xMicro[i] = vehicles.get(i).getPosition();
        }

        // calculate density
        localDensity[0] = 0;
        for (int i = 1; i < size; i++) {
            final double dist = xMicro[i - 1] - xMicro[i];
            final double length = vehicles.get(i - 1).length();
            localDensity[i] = (dist > length) ? 1 / dist : 1 / length;
        }

        for (int j = 0; j < density.length; j++) {
            final double x = j * dxOut;
            density[j] = Tables.intpextp(xMicro, localDensity, x, true);
            averageSpeed[j] = Tables.intpextp(xMicro, vMicro, x, true);
            flow[j] = density[j] * averageSpeed[j];
        }
    }

    public double getDtOut() {
        return dtOut;
    }

    public double getDxOut() {
        return dxOut;
    }

    public double[] getDensity() {
        return density;
    }

    public double[] getAverageSpeed() {
        return averageSpeed;
    }

    public double[] getFlow() {
        return flow;
    }

    public double getTimeOffset() {
        return timeOffset;
    }



}
