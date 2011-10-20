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
package org.movsim.input.model.vehicle.laneChanging.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;

// TODO: Auto-generated Javadoc
/**
 * The Class LaneChangingMobilModelDataImpl.
 */
public class LaneChangingMobilModelDataImpl implements LaneChangingMobilData {
    
    private double safeDeceleration;
    private double minimumGap;
    private double thresholdAcceleration;
    private double rightBiasAcceleration; 
    private double politeness; 
     


    /**
     * Instantiates a new lane changing mobil model data impl.
     */
    public LaneChangingMobilModelDataImpl(){
	
    }
    
//    public LaneChangingMobilModelDataImpl(Map<String, String> map){
//        init(map);
//    }
    
    
    /* (non-Javadoc)
 * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData#init(java.util.Map)
 */
public void init(final Map<String, String> map){
	safeDeceleration = Double.parseDouble(map.get("b_safe"));
        minimumGap = Double.parseDouble(map.get("s_min"));
        thresholdAcceleration = Double.parseDouble(map.get("threshold"));
        rightBiasAcceleration = Double.parseDouble(map.get("bias_right"));
        politeness = Double.parseDouble(map.get("politeness"));
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData#getSafeDeceleration()
     */
    @Override
    public double getSafeDeceleration() {
        return safeDeceleration;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData#getMinimumGap()
     */
    @Override
    public double getMinimumGap() {
        return minimumGap;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData#getThresholdAcceleration()
     */
    @Override
    public double getThresholdAcceleration() {
        return thresholdAcceleration;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData#getRightBiasAcceleration()
     */
    @Override
    public double getRightBiasAcceleration() {
        return rightBiasAcceleration;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData#getPoliteness()
     */
    @Override
    public double getPoliteness() {
        return politeness;
    }
}
