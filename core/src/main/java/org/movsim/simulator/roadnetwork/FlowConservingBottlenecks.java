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

import java.util.List;
import java.util.Map;

import org.movsim.autogen.FlowConservingInhomogeneities;
import org.movsim.autogen.Inhomogeneity;
import org.movsim.utilities.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FlowConservingBottlenecksImpl.
 */
public class FlowConservingBottlenecks {

    final static Logger logger = LoggerFactory.getLogger(FlowConservingBottlenecks.class);

    /** The pos values. */
    private double[] posValues;

    /** The alpha t values. */
    private double[] alphaTValues;

    /** The alpha v0 values. */
    private double[] alphaV0Values;

    /**
     * Instantiates a new flow conserving bottlenecks impl.
     * 
     * @param flowConservingInhomogeneities
     *            the flow cons data points
     */
    public FlowConservingBottlenecks(FlowConservingInhomogeneities flowConservingInhomogeneities) {
        generateSpaceSeriesData(flowConservingInhomogeneities.getInhomogeneity());
    }

    /**
     * Generate space series data.
     * 
     * @param list
     *            the data
     */
    private void generateSpaceSeriesData(List<Inhomogeneity> list) {
        final int size = list.size();
        posValues = new double[size];
        alphaTValues = new double[size];
        alphaV0Values = new double[size];
        for (int i = 0; i < size; i++) {
            posValues[i] = list.get(i).getPosition();
            alphaTValues[i] = list.get(i).getAlphaT();
            alphaV0Values[i] = list.get(i).getAlphaV0();
            // LOG.debug("add data: alphaT={}, alphaV0={}", alphaTValues[i],
            // alphaV0Values[i]);
        }
    }

    /**
     * Alpha t.
     * 
     * @param x
     *            the x
     * @return the double
     */
    public double alphaT(double x) {
        return (alphaTValues.length == 0) ? 1 : Tables.intpextp(posValues, alphaTValues, x);
    }

    /**
     * Alpha v0.
     * 
     * @param x
     *            the x
     * @return the double
     */
    public double alphaV0(double x) {
        return (alphaV0Values.length == 0) ? 1 : Tables.intpextp(posValues, alphaV0Values, x);
    }

    private final class FlowConservingBottleneckDataPoint {

        /** The position in m */
        private final double x;

        /** The alpha t. */
        private final double alphaT;

        /** The alpha v0. */
        private final double alphaV0;

        /**
         * Instantiates a new flow conserving bottleneck data point impl.
         * 
         * @param map
         *            the map
         */
        public FlowConservingBottleneckDataPoint(Map<String, String> map) {
            this.x = Double.parseDouble(map.get("x"));
            this.alphaT = Double.parseDouble(map.get("alpha_T"));
            this.alphaV0 = Double.parseDouble(map.get("alpha_v0"));
        }

        public double getPosition() {
            return x;
        }

        public double getAlphaT() {
            return alphaT;
        }

        public double getAlphaV0() {
            return alphaV0;
        }
    }

}
