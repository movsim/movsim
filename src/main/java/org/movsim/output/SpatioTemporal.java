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
package org.movsim.output;

import org.movsim.utilities.ObservableInTime;

// TODO: Auto-generated Javadoc
/**
 * The Interface SpatioTemporal.
 */
public interface SpatioTemporal extends ObservableInTime {

    /**
     * Gets the dt out.
     * 
     * @return the dt out
     */
    double getDtOut();

    /**
     * Gets the dx out.
     * 
     * @return the dx out
     */
    double getDxOut();

    /**
     * Gets the density.
     * 
     * @return the density
     */
    double[] getDensity();

    /**
     * Gets the average speed.
     * 
     * @return the average speed
     */
    double[] getAverageSpeed();

    /**
     * Gets the flow.
     * 
     * @return the flow
     */
    double[] getFlow();

    /**
     * Gets the time offset.
     * 
     * @return the time offset
     */
    double getTimeOffset();
}