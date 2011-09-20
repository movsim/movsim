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
package org.movsim.simulator.roadSection;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.simulation.RampData;
import org.movsim.output.LoopDetector;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.impl.AbstractRoadSection;
import org.movsim.simulator.roadSection.impl.OnrampImpl;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
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
     * @param rampData the ramp data
     */
    public OfframpImpl(final RampData rampData){
        super(rampData);
        

        // vehicles start at initial position
        mergeLength = rampData.getRampMergingLength();
        xToMain = rampData.getRampStartPosition();
        xDownRamp = roadLength - mergeLength; // rampData.getRampStartPosition();

        // create vehicle container for onramp lane
        vehContainers = new ArrayList<VehicleContainer>();
        vehContainers.add(new VehicleContainerImpl(roadLength, Constants.MOST_RIGHT_LANE));
    }
    
    

    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.AbstractRoadSection#updateRoadConditions(long, double)
     */
    @Override
    public void updateRoadConditions(long iterationCount, double time) {
        // TODO Auto-generated method stub
        
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
     * @see org.movsim.simulator.roadSection.RoadSection#getTrafficLights()
     */
    @Override
    public List<TrafficLight> getTrafficLights() {
        // TODO Auto-generated method stub
        return null;
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
     * @see org.movsim.simulator.roadSection.RoadSection#rampFactory(org.movsim.input.InputData)
     */
    @Override
    public List<RoadSection> rampFactory(InputData inputData) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.impl.AbstractRoadSection#updateUpstreamBoundary(long, double, double)
     */
    @Override 
    public void updateUpstreamBoundary(long iterationCount, double dt, double time) {
        //upstreamBoundary.update(iterationCount, dt, time);
    }



    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.RoadSection#laneChangingToOfframps(java.util.List, long, double, double)
     */
    @Override
    public void laneChangingToOfframps(List<RoadSection> ramps, long iterationCount, double dt, double time) {
        // TODO Auto-generated method stub
        
    }
    
    
    
    
    
    
}
