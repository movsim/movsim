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
package org.movsim.input.model;

import org.movsim.input.model.vehicle.behavior.MemoryInputData;
import org.movsim.input.model.vehicle.behavior.NoiseInputData;
import org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputData;

// TODO: Auto-generated Javadoc
/**
 * The Interface VehicleInput.
 */
public interface VehicleInput {

    /**
     * Gets the label.
     * 
     * @return the label
     */
    String getLabel();

    /**
     * Gets the length.
     * 
     * @return the length
     */
    double getLength();

    /**
     * Gets the max deceleration.
     * 
     * @return the max deceleration
     */
    double getMaxDeceleration();

    /**
     * Gets the reaction time.
     * 
     * @return the reaction time
     */
    double getReactionTime();

    /**
     * Gets the model input data.
     * 
     * @return the model input data
     */
    AccelerationModelInputData getAccelerationModelInputData();
    
    
    /**
     * Gets the lane changing input data.
     *
     * @return the lane changing input data
     */
    LaneChangingInputData getLaneChangingInputData();

    /**
     * Checks if is with memory.
     * 
     * @return true, if is with memory
     */
    boolean isWithMemory();

    /**
     * Gets the memory input data.
     * 
     * @return the memory input data
     */
    MemoryInputData getMemoryInputData();

    /**
     * Checks if is with noise.
     * 
     * @return true, if is with noise
     */
    boolean isWithNoise();

    /**
     * Gets the noise input data.
     * 
     * @return the noise input data
     */
    NoiseInputData getNoiseInputData();

}