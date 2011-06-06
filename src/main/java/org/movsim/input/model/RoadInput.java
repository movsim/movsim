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

import java.util.List;

import org.movsim.input.model.simulation.DetectorInput;
import org.movsim.input.model.simulation.FlowConservingBottleneckDataPoint;
import org.movsim.input.model.simulation.HeterogeneityInputData;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.RampData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.input.model.simulation.SpeedLimitDataPoint;
import org.movsim.input.model.simulation.TrafficLightData;
import org.movsim.input.model.simulation.TrafficLightsInput;
import org.movsim.input.model.simulation.UpstreamBoundaryData;
import org.movsim.input.model.simulation.impl.TrafficLightsInputImpl;

// TODO: Auto-generated Javadoc
/**
 * The Interface RoadInput.
 */
public interface RoadInput {

    /**
     * Gets the road length.
     * 
     * @return the road length
     */
    double getRoadLength();

    /**
     * Gets the lanes.
     * 
     * @return the lanes
     */
    int getLanes();

    /**
     * Gets the id.
     * 
     * @return the id
     */
    long getId();

    /**
     * Checks if is with write fundamental diagrams.
     * 
     * @return true, if is with write fundamental diagrams
     */
    boolean isWithWriteFundamentalDiagrams();

    /**
     * Gets the heterogeneity input data.
     * 
     * @return the heterogeneity input data
     */
    List<HeterogeneityInputData> getHeterogeneityInputData();

    /**
     * Gets the ic macro data.
     * 
     * @return the ic macro data
     */
    List<ICMacroData> getIcMacroData();

    /**
     * Gets the ic micro data.
     * 
     * @return the ic micro data
     */
    List<ICMicroData> getIcMicroData();

    /**
     * Gets the upstream boundary data.
     * 
     * @return the upstream boundary data
     */
    UpstreamBoundaryData getUpstreamBoundaryData();

    /**
     * Gets the flow cons bottleneck input data.
     * 
     * @return the flow cons bottleneck input data
     */
    List<FlowConservingBottleneckDataPoint> getFlowConsBottleneckInputData();

    /**
     * Gets the speed limit input data.
     * 
     * @return the speed limit input data
     */
    List<SpeedLimitDataPoint> getSpeedLimitInputData();

    /**
     * Gets the ramps.
     * 
     * @return the ramps
     */
    List<RampData> getRamps();

    /**
     * Gets the simple ramps.
     * 
     * @return the simple ramps
     */
    List<SimpleRampData> getSimpleRamps();

    
    TrafficLightsInput getTrafficLightsInput();
    
    
    /**
     * Gets the detector input.
     * 
     * @return the detector input
     */
    DetectorInput getDetectorInput();
    
    OutputInput getOutputInput();
}
