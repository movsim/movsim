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

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.ElevationProfile.Elevation;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The Class Slopes.
 */

// TODO refactoring, usage for car models
class Slopes implements Iterable<Slope> {

    private static final Logger LOG = LoggerFactory.getLogger(Slopes.class);

    private double[] positions;
    private double[] elevations;
    private double[] gradients;
    private final Collection<Slope> slopes;

    /**
     * Constructor.
     */
    Slopes(List<Elevation> elevationRecords) {
        Preconditions.checkNotNull(elevationRecords);
        slopes = new LinkedList<>();
        generateSpaceSeriesData(elevationRecords);
    }

    /**
     * Generate space series data.
     * 
     * @param elevationRecords
     *            the data
     */
    private void generateSpaceSeriesData(List<Elevation> elevationRecords) {
        final int size = elevationRecords.size() + 1;
        positions = new double[size];
        gradients = new double[size];
        elevations = new double[size];
        positions[0] = 0;
        elevations[0] = 0;
        gradients[0] = 0;
        for (int i = 1; i < size; i++) {
            final double pos = elevationRecords.get(i - 1).getS(); // position in m
            positions[i] = pos;
            Preconditions.checkArgument(i > 0 && positions[i] >= positions[i - 1],
                    "road elevation not given in increasing order");

            final double roadElevation = elevationRecords.get(i - 1).getA(); // elevation in m
            elevations[i] = roadElevation;
            
            double deltaElevation = roadElevation - elevations[i - 1];
            double deltaPosition = pos - positions[i-1];
            final double gradient = (deltaPosition > 0) ? deltaElevation / deltaPosition : 0;
            // if(LOG.isDebugEnabled()){
            LOG.info(String.format("calculated gradient from=%.2fm to %.2fm: gradient=%.5f.", pos,
                        positions[i - 1], gradient));
            // }
            gradients[i - 1] = gradient; // !!!
            slopes.add(new Slope(positions[i - 1], gradients[i - 1]));
        }
        // add last point
        slopes.add(new Slope(positions[size - 1], 0));
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
        LOG.debug("pos={} --> slope gradient={}", pos, slope);
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