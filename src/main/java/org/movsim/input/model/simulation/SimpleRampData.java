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
package org.movsim.input.model.simulation;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface SimpleRampData.
 */
public interface SimpleRampData {

    /**
     * Gets the inflow time series.
     * 
     * @return the inflow time series
     */
    List<InflowDataPoint> getInflowTimeSeries();

    /**
     * Gets the center position.
     * 
     * @return the center position
     */
    double getCenterPosition();

    /**
     * Gets the ramp length.
     * 
     * @return the ramp length
     */
    double getRampLength();

    /**
     * With logging.
     * 
     * @return true, if successful
     */
    boolean withLogging();

}