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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.movsim.input.model.simulation.SlopeDataPoint;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Slopes.
 */
public class Slopes implements Iterable<Slope> {

    final static Logger logger = LoggerFactory.getLogger(Slopes.class);

    private double[] positions;
    private double[] gradients;
    private final Collection<Slope> slopes;

    /**
     * Constructor.
     */
    public Slopes(List<SlopeDataPoint> slopesInputDataPoints) {
        slopes = new LinkedList<Slope>();
        generateSpaceSeriesData(slopesInputDataPoints);
    }

    /**
     * Generate space series data.
     * 
     * @param data
     *            the data
     */
    private void generateSpaceSeriesData(List<SlopeDataPoint> data) {
        final int size = data.size() + 1;
        positions = new double[size];
        gradients = new double[size];
        positions[0] = 0;
        gradients[0] = 0;
        for (int i = 1; i < size; i++) {
            final double pos = data.get(i - 1).getPosition();
            positions[i] = pos;
            final double gradient = data.get(i - 1).getGradient();
            gradients[i] = gradient;
            slopes.add(new Slope(pos, gradient));
        }
    }

    /**
     * Checks if is empty.
     * 
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return gradients.length == 0;
    }

    /**
     * Apply the slope to a vehicle
     * @param vehicle
     */
    public void apply(Vehicle vehicle) {
        final double pos = vehicle.getFrontPosition();
        final double slope = calcSlope(pos);
        vehicle.setSlope(slope);
        logger.debug("pos={} --> slope gradient={}", pos, slope);
    }

    public double calcSlope(double position) {
        return gradients.length == 0 ? 0 :
            Tables.stepExtrapolation(positions, gradients, position);
    }

    @Override
    public Iterator<Slope> iterator() {
        return slopes.iterator();
    }
}