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

import org.movsim.input.model.simulation.InflowDataPoint;
import org.movsim.simulator.roadSection.InflowTimeSeries;
import org.movsim.utilities.impl.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class InflowTimeSeriesImpl.
 */
public class InflowTimeSeriesImpl implements InflowTimeSeries {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(InflowTimeSeriesImpl.class);

    /** The time values. */
    private double[] timeValues;

    /** The flow values. */
    private double[] flowValues;

    /** The speed values. */
    private double[] speedValues;

    /**
     * Instantiates a new inflow time series impl.
     * 
     * @param inflowTimeSeries
     *            the inflow time series
     */
    public InflowTimeSeriesImpl(List<InflowDataPoint> inflowTimeSeries) {
        generateTimeSeriesData(inflowTimeSeries);
    }

    /**
     * Generate time series data.
     * 
     * @param inflowTimeSeries
     *            the inflow time series
     */
    private void generateTimeSeriesData(List<InflowDataPoint> inflowTimeSeries) {
        final int size = inflowTimeSeries.size();

        logger.info(" inflowDataPoint.size = {} (if ==0. no inflow)", size);
        timeValues = new double[size];
        flowValues = new double[size];
        speedValues = new double[size];
        for (int i = 0; i < size; i++) {
            timeValues[i] = inflowTimeSeries.get(i).getTime();
            flowValues[i] = inflowTimeSeries.get(i).getFlow();
            speedValues[i] = inflowTimeSeries.get(i).getSpeed();
            logger.debug("add data: flow={}, speed={}", flowValues[i], speedValues[i]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.InflowTimeSeries#getFlow(double)
     */
    @Override
    public double getFlow(double time) {
        return Tables.intpextp(timeValues, flowValues, time);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.InflowTimeSeries#getSpeed(double)
     */
    @Override
    public double getSpeed(double time) {
        return Tables.intpextp(timeValues, speedValues, time);
    }

}
