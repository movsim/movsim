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
package org.movsim.input.model.vehicle.longModel;

// TODO: Auto-generated Javadoc
/**
 * The Interface AccelerationModelInputDataNSM.
 */
public interface AccelerationModelInputDataNSM extends AccelerationModelInputData{

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    double getV0();

    /**
     * Gets the slowdown.
     * 
     * @return the slowdown
     */
    double getSlowdown();

    /**
     * Gets the slow to start.
     * 
     * @return the slow to start
     */
    double getSlowToStart();
    
    
    double getV0Default();

    double getpSlowdown();

    double getpSlowdownDefault();

    double getpSlowToStart();

    double getpSlowToStartDefault();

    void setV0(double v0);

    void setpSlowdown(double pSlowdown);

    void setpSlowToStart(double pSlowToStart);

}