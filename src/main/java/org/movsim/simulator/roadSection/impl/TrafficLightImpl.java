/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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


import org.movsim.input.model.simulation.TrafficLightData;
import org.movsim.simulator.roadSection.TrafficLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



// TODO code review, test scenario: test_trafficlights.xml
public class TrafficLightImpl implements TrafficLight{

	final static Logger logger = LoggerFactory.getLogger(TrafficLightImpl.class);
	  
    // transfered parameters:
    private double position;
    
    
    private int status;
    private int oldStatus;

    //parameter: cycle times
    private double totalCycleTime; 
    
    private double greenTimePeriod;
    private double redTimePeriod;
    private double greenRedTimePeriod;
    private double redGreenTimePeriod; 
    private double phaseShift;
    
    private double currentCycleTime=0;
    private double lastUpdateTime=0;

    public TrafficLightImpl(TrafficLightData inputData){
        position = inputData.getX();
        greenTimePeriod = inputData.getGreenTime();
        redTimePeriod = inputData.getRedTime();
        greenRedTimePeriod = inputData.getGreenRedTimePeriod();
        redGreenTimePeriod = inputData.getRedGreenTimePeriod();
        phaseShift = inputData.getPhaseShift();
        
        initialize();
    }
    
    private void initialize(){
		status = RED_LIGHT; //GREEN_LIGHT; // init 
		totalCycleTime = redTimePeriod+greenTimePeriod+greenRedTimePeriod+redGreenTimePeriod;
		currentCycleTime = -phaseShift;  
		logger.debug("initialize traffic light at pos = {}", position);
	}
    
    
    public void update(double time){
		oldStatus=status;
		currentCycleTime += time-lastUpdateTime;
		
//		logger.debug("update at time = {}, status = {}", time, status);
//	    logger.debug("   actualCycleTime = {}, lastUpdateTime={}", currentCycleTime, lastUpdateTime);
	
		if(currentCycleTime > greenTimePeriod){
			status=GREEN_RED_LIGHT;
		}
		if(currentCycleTime > greenTimePeriod+greenRedTimePeriod){
			status=RED_LIGHT;
		}
		if(currentCycleTime > greenTimePeriod+greenRedTimePeriod+redTimePeriod){
			status=RED_GREEN_LIGHT;
		}
		if(currentCycleTime >= totalCycleTime){
			status = GREEN_LIGHT;
			currentCycleTime -= totalCycleTime;
		}
		
		lastUpdateTime=time;
	}
    
//    boolean redLightJustReleased(){
//		if(oldStatus==RED_LIGHT && (isGreen() || isRedGreen())) return true;
//		return false;
//	}
//	

    public double position(){ return position; }
    
	public double getCritTimeForNextMainPhase(double alpha){
		//  Zeit bis zum naechsten rot bzw. gruen
		// periode startet bei gruen
		//restliche period time + alpha*yellowPhase
		if(status==GREEN_LIGHT || status==GREEN_RED_LIGHT) return (greenTimePeriod+alpha*greenRedTimePeriod-currentCycleTime);
		if(status==RED_LIGHT  || status==RED_GREEN_LIGHT) return (greenTimePeriod+greenRedTimePeriod+redTimePeriod+alpha*redGreenTimePeriod-currentCycleTime);
		return 0;
	}
	
    public double getCurrentCycleTime(){ return this.currentCycleTime; }
	public double getCycleTime(){ return this.totalCycleTime; }	
	
	public double getTimeForNextGreen(double alpha){
		double dt = totalCycleTime-currentCycleTime-(1-alpha)*redGreenTimePeriod;
		if(dt<0) dt += totalCycleTime;
		return dt;
	}
	
	public double getTimeForNextRed(double alpha){
		double dt = greenTimePeriod+alpha*greenRedTimePeriod-currentCycleTime;
		if(dt<0) dt +=totalCycleTime;
		return dt;
	}
	
	public double getTimeForNextGreen(){
		double dt = totalCycleTime-currentCycleTime;
		if(dt<0) dt += totalCycleTime;
		return dt;
	}
	
	public double getTimeForNextRed(){
		double dt = greenTimePeriod+greenRedTimePeriod-currentCycleTime;
		if(dt<0) dt +=totalCycleTime;
		return dt;
	}
	
	
    
    public boolean isGreen(){
		return (status==GREEN_LIGHT);
	}

	public boolean isGreenRed(){
		return (status==GREEN_RED_LIGHT);
	}

	public boolean isRed(){
		return (status==RED_LIGHT);
	}
	
	public boolean isRedGreen(){
		return (status==RED_GREEN_LIGHT);
	}
	
	public int status(){
	    return status; 
	} 

	String getStatus(){
		switch(status){
		case GREEN_LIGHT : return "green";
		case GREEN_RED_LIGHT : return "green_red";
		case RED_LIGHT   : return "red";
		case RED_GREEN_LIGHT  : return "red_green";
		}
		return "error: not defined!";
	}
	
    
    
}
