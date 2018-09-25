/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim.org@gmail.com>
 * ----------------------------------------------------------------------------------------- This file is part of MovSim - the
 * multi-model open-source vehicular-traffic simulator. MovSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.roadnetwork.boundaries;

import org.movsim.autogen.Inflow;
import org.movsim.utilities.LinearInterpolatedFunction;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// TODO time format not aligned with complex date format (as string)
public class InflowTimeSeries {

    private static final Logger LOG = LoggerFactory.getLogger(InflowTimeSeries.class);

    private static final double CONSTANT_FLOW_PER_LANE = 1000 * Units.INVH_TO_INVS;

    private static final double CONSTANT_INIT_SPEED = 80 / 3.6;

    private LinearInterpolatedFunction flowFunction;

    private LinearInterpolatedFunction speedFunction;

    /**
     * Instantiates a new inflow time series.
     */
    public InflowTimeSeries(List<Inflow> inflow) {
        LOG.info(" inflowDataPoint.size = {}", inflow.size());
        if (!inflow.isEmpty()) {
            generateTimeSeriesData(inflow);
        }
    }

    private void generateTimeSeriesData(List<Inflow> inflow) {
        final int size = inflow.size();
        double[] timeValues = new double[size];
        double[] flowValues = new double[size];
        double[] speedValues = new double[size];
        for (int i = 0; i < size; i++) {
            final Inflow dataPoint = inflow.get(i);
            timeValues[i] = dataPoint.getT();
            // convert flow per lane per hour in 1/s
            flowValues[i] = dataPoint.getQPerHour() * Units.INVH_TO_INVS;
            speedValues[i] = dataPoint.getV(); // in m/s
            LOG.debug("add data: flow={}, speed={}", flowValues[i], speedValues[i]);
        }

        flowFunction = new LinearInterpolatedFunction(timeValues, flowValues);
        speedFunction = new LinearInterpolatedFunction(timeValues, speedValues);
    }

    public double getFlowPerLane(double time) {
        if (flowFunction == null) {
            return CONSTANT_FLOW_PER_LANE;
        }
        return flowFunction.value(time);
    }

    public double getSpeed(double time) {
        if (flowFunction == null) {
            return CONSTANT_INIT_SPEED;
        }
        return speedFunction.value(time);
    }

}
