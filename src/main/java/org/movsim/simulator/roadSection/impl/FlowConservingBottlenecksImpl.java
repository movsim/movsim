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

import org.movsim.input.model.simulation.FlowConservingBottleneckDataPoint;
import org.movsim.simulator.roadSection.FlowConservingBottlenecks;
import org.movsim.utilities.impl.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FlowConservingBottlenecksImpl.
 */
public class FlowConservingBottlenecksImpl implements FlowConservingBottlenecks {
    
     final static Logger logger = LoggerFactory.getLogger(FlowConservingBottlenecksImpl.class);

    /** The pos values. */
    private double[] posValues;

    /** The alpha t values. */
    private double[] alphaTValues;

    /** The alpha v0 values. */
    private double[] alphaV0Values;

    /**
     * Instantiates a new flow conserving bottlenecks impl.
     * 
     * @param flowConsDataPoints
     *            the flow cons data points
     */
    public FlowConservingBottlenecksImpl(List<FlowConservingBottleneckDataPoint> flowConsDataPoints) {
        generateSpaceSeriesData(flowConsDataPoints);
    }

    /**
     * Generate space series data.
     * 
     * @param data
     *            the data
     */
    private void generateSpaceSeriesData(List<FlowConservingBottleneckDataPoint> data) {
        final int size = data.size();
        posValues = new double[size];
        alphaTValues = new double[size];
        alphaV0Values = new double[size];
        for (int i = 0; i < size; i++) {
            posValues[i] = data.get(i).getPosition();
            alphaTValues[i] = data.get(i).getAlphaT();
            alphaV0Values[i] = data.get(i).getAlphaV0();
            // logger.debug("add data: alphaT={}, alphaV0={}", alphaTValues[i],
            // alphaV0Values[i]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.roadSection.FlowConservingBottlenecks#alphaT(double)
     */
    @Override
    public double alphaT(double x) {
        return (alphaTValues.length == 0) ? 1 : Tables.intpextp(posValues, alphaTValues, x);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.roadSection.FlowConservingBottlenecks#alphaV0(double
     * )
     */
    @Override
    public double alphaV0(double x) {
        return (alphaV0Values.length == 0) ? 1 : Tables.intpextp(posValues, alphaV0Values, x);
    }

}
