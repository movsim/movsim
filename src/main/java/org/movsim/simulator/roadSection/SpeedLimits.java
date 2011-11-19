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
package org.movsim.simulator.roadSection;

import java.util.LinkedList;
import java.util.List;

import org.movsim.input.model.simulation.SpeedLimitDataPoint;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadSection.SpeedLimit;
import org.movsim.utilities.impl.Tables;

/**
 * The Class SpeedLimits.
 */
public class SpeedLimits {

    // final static Logger logger = LoggerFactory.getLogger(SpeedLimits.class);

    /** The pos values. */
    private double[] posValues;

    /** The speed values. */
    private double[] speedValues;
    
   
    private List<SpeedLimit> speedLimits;

    /**
     * Instantiates a new speed limits impl.
     * 
     * @param speedLimitInputDataPoints
     *            the speed limit input data points
     */
    public SpeedLimits(List<SpeedLimitDataPoint> speedLimitInputDataPoints) {
        speedLimits = new LinkedList<SpeedLimit>();
        generateSpaceSeriesData(speedLimitInputDataPoints);
    }

    /**
     * Generate space series data.
     * 
     * @param data
     *            the data
     */
    private void generateSpaceSeriesData(List<SpeedLimitDataPoint> data) {
        final int size = data.size() + 1;
        posValues = new double[size];
        speedValues = new double[size];
        // add constant maximum speed at origin x=0 for correct extrapolation
        posValues[0] = 0;
        speedValues[0] = MovsimConstants.MAX_VEHICLE_SPEED;
        for (int i = 1; i < size; i++) {
            final double pos = data.get(i - 1).getPosition();
            posValues[i] = pos;
            final double speed = data.get(i - 1).getSpeedlimit();
            speedValues[i] = speed; 
            speedLimits.add(new SpeedLimit(pos, speed));
        }
    }

    /**
     * Checks if is empty.
     * 
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return (speedValues.length == 0);
    }

    /**
     * Calc speed limit.
     * 
     * @param x
     *            the x
     * @return the double
     */
    public double calcSpeedLimit(double x) {
        return (speedValues.length == 0) ? MovsimConstants.MAX_VEHICLE_SPEED : Tables.stepExtrapolation(posValues,
                speedValues, x);
    }

    public List<SpeedLimit> getSpeedLimits() {
        return speedLimits;
    }
}
