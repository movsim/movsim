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
package org.movsim.simulator.roadnetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.movsim.autogen.Inflow;
import org.movsim.utilities.Tables;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class InflowTimeSeries.
 */
public class InflowTimeSeries {

    /** The Constant LOG. */
    final static Logger logger = LoggerFactory.getLogger(InflowTimeSeries.class);

    private double[] timeValues;

    private double[] flowValues;

    private double[] speedValues;

    private double constantFlowPerLane = -1;

    /** constant init speed = 80 km/h */
    private final double constantInitSpeed = 80 / 3.6;

    /**
     * Instantiates a new inflow time series.
     * 
     * @param inflowTimeSeries
     *            the inflow time series
     */
    public InflowTimeSeries(List<Inflow> inflow) {
        generateTimeSeriesData(inflow);
    }

    /**
     * Generate time series data.
     * 
     * @param inflowTimeSeries
     *            the inflow time series
     */
    private void generateTimeSeriesData(List<Inflow> inflow) {
        List<InflowDataPoint> sortedInflowDataPoints = getSortedInflowDataPoints(inflow);

        final int size = sortedInflowDataPoints.size();

        logger.info(" inflowDataPoint.size = {}", size);
        if (sortedInflowDataPoints.isEmpty()) {
            logger.info("no inflow data points --> no inflow.");
        }
        timeValues = new double[size];
        flowValues = new double[size];
        speedValues = new double[size];
        for (int i = 0; i < size; i++) {
            final InflowDataPoint inflowDataPoint = sortedInflowDataPoints.get(i);
            timeValues[i] = inflowDataPoint.getTime();
            flowValues[i] = inflowDataPoint.getFlow();
            speedValues[i] = inflowDataPoint.getSpeed();
            logger.debug("add data: flow={}, speed={}", flowValues[i], speedValues[i]);
        }
    }

    private List<InflowDataPoint> getSortedInflowDataPoints(List<Inflow> inflow) {
        List<InflowDataPoint> dataPoints = new ArrayList<>();
        for (final Inflow inflowDataPoint : inflow) {
            dataPoints.add(new InflowDataPoint(inflowDataPoint));
        }
        Collections.sort(dataPoints, new Comparator<InflowDataPoint>() {
            @Override
            public int compare(InflowDataPoint o1, InflowDataPoint o2) {
                final Double time1 = new Double((o1).getTime());
                final Double time2 = new Double((o2).getTime());
                return time1.compareTo(time2); // sort with increasing t
            }
        });
        return dataPoints;
    }

    /**
     * Gets the flow.
     * 
     * @param time
     *            the time
     * @return the flow
     */
    public double getFlowPerLane(double time) {
        if (constantFlowPerLane >= 0) {
            return constantFlowPerLane;
        }
        return Tables.intpextp(timeValues, flowValues, time);
    }

    /**
     * Gets the speed.
     * 
     * @param time
     *            the time
     * @return the speed
     */
    public double getSpeed(double time) {
        if (constantFlowPerLane >= 0) {
            return constantInitSpeed;
        }
        return Tables.intpextp(timeValues, speedValues, time);
    }

    private final class InflowDataPoint {

        /** The time in seconds */
        private final double time;

        /** The flow in 1/s. */
        private final double flow;

        /** The speed in m/s. */
        private final double speed;

        public InflowDataPoint(Inflow inflowDataPoint) {
            this(inflowDataPoint.getT(), inflowDataPoint.getQPerHour(), inflowDataPoint.getV());
        }

        /**
         * Constructor.
         * 
         * @param time
         * @param flowPerHour
         * @param speed
         */
        public InflowDataPoint(double time, double flowPerHour, double speed) {
            this.time = time;
            this.flow = flowPerHour * Units.INVH_TO_INVS;
            this.speed = speed; // given in m/s
        }

        public double getTime() {
            return time;
        }

        public double getFlow() {
            return flow;
        }

        public double getSpeed() {
            return speed;
        }

    }

}
