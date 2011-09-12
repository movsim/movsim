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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.simulation.FlowConservingBottleneckDataPoint;
import org.movsim.input.model.simulation.RampData;
import org.movsim.output.LoopDetector;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.impl.VehicleContainerImpl;
import org.movsim.utilities.impl.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OnrampImpl.
 */
public class OnrampMobilImpl extends AbstractRoadSection implements RoadSection {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(OnrampImpl.class);

    
    /** The lane for entering the mainroad  
     *  only MOST_RIGHT_LANE possible to enter*/
    private final static int LANE_TO_MERGE_ON_MAINROAD = Constants.MOST_RIGHT_LANE; 

    
    private static final String extensionFormat = ".S%d_log.csv";
    private static final String outputHeading = Constants.COMMENT_CHAR
            + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],  count,  queue\n";
    private static final String outputFormat = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %6d, %6d%n";

    
    /** The Constant MINSPACE_MERGE_M. */
    final static double MINSPACE_MERGE_M = 2.0;

    /** The Constant RAMP_VEL_REDUCEFACTOR. */
    final static double RAMP_VEL_REDUCEFACTOR = 0.6;
    
    
    final static int N_LANES = 1;
    
    /** The main veh container. */
    private final VehicleContainer mainVehContainer;

    /** The x center position of the ramp. */
    //private final double xCenter;

    private final double mergeLength;

    /** The x up ramp marks the start of ther ramp. */
    private final double xUpRamp;

    /** The x down ramp marks the end of the ramp. */
    //private final double xDownRamp;
    
    private final double xOffsetMain;

    /** The n wait. */
    private double nWait;

    /** The fstr logging. */
    PrintWriter fstrLogging;

    /** The x enter last merge. status of last merging vehicle */
    private double xEnterLastMerge;

    /** The v enter last merge. */
    private double vEnterLastMerge;

    
    /** The merge count. */
    private int mergeCount;
    
    
    private final boolean isWithCrashExit = true;
    
    
    /**
     * Instantiates a new onramp impl.
     * 
     * @param rampData
     *            the ramp data
     * @param vehGenerator
     *            the veh generator
     * @param mainVehContainerMostRightLane
     *            the main veh container
     * @param projectName
     *            the project name
     * @param rampIndex
     *            the ramp index
     */
    
    
    // TODO: create from Simulator
    
    // TODO assume only *one* lane here !!!
    
    public OnrampMobilImpl(final RampData rampData, final VehicleGenerator vehGenerator, final VehicleContainer mainVehContainerMostRightLane,
            String projectName, int rampIndex) {

        super(rampData, vehGenerator);
        
        
        
        
        // local coordinate system
        // ramp from x=0 to x=roadLength
        // merging possible from x=roadLength-mergeLength to x=roadLength
        // offset to main road: rampData.getRampStartPosition() - xUpRamp

        // vehicles start at initial position 
        mergeLength = rampData.getRampMergingLength();
        
        xUpRamp = roadLength - mergeLength; //rampData.getRampStartPosition();
        //xCenter = rampData.getRampStartPosition() + 0.5 * mergeLength;
        //xDownRamp = xUpRamp + mergeLength;
        
        xOffsetMain = rampData.getRampStartPosition()-xUpRamp;
        
        logger.debug("xOffsetMain = {}", xOffsetMain);
        if(xOffsetMain<0){
            logger.error("xOffsetMain = {}. negative values not allowed.", xOffsetMain);
        }
        
        this.mainVehContainer = mainVehContainerMostRightLane;  // container of mainroad's most-right lane
        // create vehicle container for onramp lane
        vehContainers = new ArrayList<VehicleContainer>();
        vehContainers.add(new VehicleContainerImpl(Constants.MOST_RIGHT_LANE));
        setObstacleAtEndOfLane(); // 
        
        // TODO only dummy here for RoadSection interface
        flowConsBottlenecks = new FlowConservingBottlenecksImpl(new ArrayList<FlowConservingBottleneckDataPoint>());
        
        upstreamBoundary = new UpstreamBoundaryImpl(vehGenerator, vehContainers, rampData.getUpstreamBoundaryData(),
                projectName);

        
        mergeCount = 0;
        if (rampData.withLogging()) {
            final int roadCount = 1; // assuming only one road in the scenario
                                     // for the moment
            final String filename = projectName + String.format(extensionFormat, rampIndex + roadCount);
            fstrLogging = FileUtils.getWriter(filename);
            fstrLogging.printf(outputHeading);
            fstrLogging.flush();
        }

        nWait = 0;

        
    }
    
    
    @Override
    public void laneChanging(long iterationCount, double dt, double time) {

	stagedVehicles.clear();
	
	assert vehContainers.size()==1;
	final VehicleContainer vehContainer = vehContainers.get(0);
	
	// loop over on-ramp veh (i=0 is obstacle !! )
        // ignore Obstacle as first vehicle !!!
	for (Vehicle veh : vehContainer.getVehicles()) {
	    if( !veh.getLabel().equals(Constants.OBSTACLE_KEY_NAME) && tryToMergeToMainroad(veh) ){
		stagedVehicles.add(veh);
	    }
	}
	
	// assign staged vehicles to new lanes
	for(final Vehicle veh : stagedVehicles){
	    vehContainer.removeVehicle(veh);
	    mainVehContainer.addFromRamp(veh);
	}
	            
    }
    
    private boolean tryToMergeToMainroad(final Vehicle veh) {
	final double pos = veh.getPosition();
	if (pos > xUpRamp ) { 
	    final double newPos = pos+xOffsetMain;  // position on main road
	    veh.setPosition(newPos);  // important mapping to coordinate system of mainroad !!!
	    logger.debug("mergeToMainroad: veh in ramp region! pos = {}, positionOnMainraod = {}", pos, newPos);
	    final boolean isSafeChange = veh.getLaneChangingModel().checkLaneChangeFromRamp(dt, mainVehContainer);

	    //if (veh.getLaneChangingModel().laneChanging()) {
	    if(isSafeChange){
		logger.debug("safeChange --> pos = {}, positionOnMainraod = {}", pos, newPos);
		return true;  
	    }
	    else{
		// reset vehicle's position to ramp coordinates
//		System.out.println("mergeToMainroad: not safeChange .. ");
		veh.setPosition(pos);
	    }
        } 
	return false;
    }

    
    private void setObstacleAtEndOfLane() {
        final Vehicle obstacle = vehGenerator.createVehicle(Constants.OBSTACLE_KEY_NAME);
        final double posInit = roadLength;
        final double speedInit = 0;
        vehContainers.get(0).add(obstacle, posInit, speedInit);
        logger.debug("set obstacle at pos={} with length={}", posInit, obstacle.getLength());
    }

   

    @Override
    public void updateRoadConditions(long iterationCount, double time) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateDownstreamBoundary() {
        // TODO Auto-generated method stub
    }

    @Override
    public List<TrafficLight> getTrafficLights() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<LoopDetector> getLoopDetectors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateDetectors(long iterationCount, double dt, double simulationTime) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<RoadSection> onrampFactory(InputData inputData) {
        // TODO Auto-generated method stub
        return null;
    }

   


   


}