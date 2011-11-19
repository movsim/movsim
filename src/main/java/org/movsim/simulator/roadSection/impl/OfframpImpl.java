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

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.model.RoadInput;
import org.movsim.output.LoopDetector;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.SpeedLimits;
import org.movsim.simulator.roadSection.TrafficLights;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.impl.VehicleContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class OfframpImpl.
 */
public class OfframpImpl extends AbstractRoadSection implements RoadSection {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(OfframpImpl.class);
    
    private final double mergeLength;

    /** The x up ramp marks the start of the ramp. */
    private final double xDownRamp;

    /** The x down ramp marks the end of the ramp. */
    // private final double xDownRamp;

    private final double xToMain;
    
    
    /**
     * Instantiates a new offramp impl.
     *
     * @param roadData the ramp data
     */
    public OfframpImpl(final RoadInput roadData){
        super(roadData);

        // vehicles start at initial position
        mergeLength = roadData.getRampMergingLength();
        xToMain = roadData.getRampStartPosition();
        xDownRamp = roadLength - mergeLength; // rampData.getRampStartPosition();

        // create vehicle container for onramp lane
        vehContainers = new ArrayList<VehicleContainer>();
        vehContainers.add(new VehicleContainerImpl(id, MovsimConstants.MOST_RIGHT_LANE));
        
        speedlimits = new SpeedLimits(roadData.getSpeedLimitInputData());
        
        System.out.println("speed limit at offramp: x=1400 = "+speedlimits.calcSpeedLimit(1400));
        
        trafficLights = new TrafficLights(null, roadData.getTrafficLightsInput());
    }
    
    

    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.AbstractRoadSection#updateDownstreamBoundary()
     */
    @Override
    // identical to RoadSectionImpl
    public void updateDownstreamBoundary() {
        for (VehicleContainer vehContainerLane : vehContainers) {
            vehContainerLane.removeVehiclesDownstream(roadLength);
        }
    }

    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.AbstractRoadSection#getRampMergingLength()
     */
    @Override
    public double getRampMergingLength() {
        return mergeLength;
    }

    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.AbstractRoadSection#getRampPositionToMainroad()
     */
    @Override
    public double getRampPositionToMainroad() {
        return xToMain;
    }

   

    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.RoadSection#getLoopDetectors()
     */
    @Override
    public List<LoopDetector> getLoopDetectors() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.RoadSection#updateDetectors(long, double, double)
     */
    @Override
    public void updateDetectors(long iterationCount, double dt, double simulationTime) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.impl.AbstractRoadSection#updateUpstreamBoundary(long, double, double)
     */
    @Override 
    public void updateUpstreamBoundary(long iterationCount, double dt, double time) {
        //upstreamBoundary.update(iterationCount, dt, time);
    }



    @Override
    public void setFractionOfLeavingVehicles(double newFraction) {
        // TODO Auto-generated method stub
        
    }



    @Override
    public void laneChangingToOfframpsAndFromOnramps(RoadSection connectedRoadSection, long iterationCount, double dt,
            double time) {
        // TODO Auto-generated method stub
        
    }
    
    
    
    
    
    
}
