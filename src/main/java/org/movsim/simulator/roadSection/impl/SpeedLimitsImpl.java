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
package org.movsim.simulator.roadSection.impl;

import java.util.List;

import org.movsim.input.model.simulation.SpeedLimitDataPoint;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.SpeedLimits;
import org.movsim.utilities.Tables;

// TODO: Auto-generated Javadoc
/**
 * The Class SpeedLimitsImpl.
 */
public class SpeedLimitsImpl implements SpeedLimits {

    // final static Logger logger = LoggerFactory.getLogger(SpeedLimits.class);

    /** The pos values. */
    private double[] posValues;
    
    /** The speed values. */
    private double[] speedValues;

    /**
     * Instantiates a new speed limits impl.
     * 
     * @param speedLimitInputDataPoints
     *            the speed limit input data points
     */
    public SpeedLimitsImpl(List<SpeedLimitDataPoint> speedLimitInputDataPoints) {
        generateSpaceSeriesData(speedLimitInputDataPoints);
    }

    /**
     * Generate space series data.
     * 
     * @param data
     *            the data
     */
    private void generateSpaceSeriesData(List<SpeedLimitDataPoint> data) {
        final int size = data.size()+1;  
        posValues = new double[size];
        speedValues = new double[size];
        // add constant maximum speed at origin x=0 for correct extrapolation
        posValues[0] = 0;
        speedValues[0] = Constants.MAX_VEHICLE_SPEED;
        for (int i = 1; i < size; i++) {
            posValues[i] = data.get(i-1).getPosition();
            speedValues[i] = data.get(i-1).getSpeedlimit();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.SpeedLimits#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return (speedValues.length == 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.SpeedLimits#calcSpeedLimit(double)
     */
    @Override
    public double calcSpeedLimit(double x) {
        return (speedValues.length == 0) ? Constants.MAX_VEHICLE_SPEED : Tables.stepExtrapolation(posValues,
                speedValues, x);
    }

}
