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

import org.movsim.input.model.simulation.TrafficLightData;
import org.movsim.simulator.roadSection.TrafficLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// TODO code review, test scenario: test_trafficlights.xml
/**
 * The Class TrafficLightImpl.
 */
public class TrafficLightImpl implements TrafficLight {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TrafficLightImpl.class);

    // transfered parameters:
    /** The position. */
    private final double position;

    /** The status. */
    private int status;

    /** The old status. */
    private int oldStatus;

    /** The total cycle time. */
    private double totalCycleTime;

    /** The green time period. */
    private double greenTimePeriod;
    
    private final double greenTimePeriodInit;

    /** The red time period. */
    private double redTimePeriod;
    
    private final double redTimePeriodInit;

    /** The green red time period. */
    private double greenRedTimePeriod;
    
    private double greenRedTimePeriodInit;

    /** The red green time period. */
    private double redGreenTimePeriod;
    
    private final double redGreenTimePeriodInit;

    /** The phase shift. */
    private final double phaseShift;

    /** The current cycle time. */
    private double currentCycleTime = 0;

    /** The last update time. */
    private double lastUpdateTime = 0;

    /**
     * Instantiates a new traffic light impl.
     * 
     * @param inputData
     *            the input data
     */
    public TrafficLightImpl(TrafficLightData inputData) {
        position = inputData.getX();
        greenTimePeriodInit = greenTimePeriod = inputData.getGreenTime();
        redTimePeriodInit = redTimePeriod = inputData.getRedTime();
        greenRedTimePeriodInit = greenRedTimePeriod = inputData.getGreenRedTimePeriod();
        redGreenTimePeriodInit = redGreenTimePeriod = inputData.getRedGreenTimePeriod();
        phaseShift = inputData.getPhaseShift();

        initialize();
    }

    /**
     * Initialize.
     */
    private void initialize() {
        status = RED_LIGHT; // GREEN_LIGHT; // init
        totalCycleTime = redTimePeriod + greenTimePeriod + greenRedTimePeriod + redGreenTimePeriod;
        currentCycleTime = -phaseShift;
        logger.debug("initialize traffic light at pos = {}", position);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#update(double)
     */
    @Override
    public void update(double time) {
        oldStatus = status;
        currentCycleTime += time - lastUpdateTime;

//        logger.debug("update at time = {}, status = {}", time, status);
//        logger.debug("   actualCycleTime = {}, lastUpdateTime={}", currentCycleTime, lastUpdateTime);

        
        if (currentCycleTime > greenTimePeriod) {
            status = GREEN_RED_LIGHT;
        }
        if (currentCycleTime > greenTimePeriod + greenRedTimePeriod) {
            status = RED_LIGHT;
        }
        if (currentCycleTime > greenTimePeriod + greenRedTimePeriod + redTimePeriod) {
            status = RED_GREEN_LIGHT;
        }
        if(currentCycleTime >= totalCycleTime) {
            status = GREEN_LIGHT;
            currentCycleTime -= totalCycleTime;
        }

        lastUpdateTime = time;
    }

    // boolean redLightJustReleased(){
    // if(oldStatus==RED_LIGHT && (isGreen() || isRedGreen())) return true;
    // return false;
    // }
    //

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#position()
     */
    @Override
    public double position() {
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.roadSection.TrafficLight#getCritTimeForNextMainPhase
     * (double)
     */
    @Override
    public double getCritTimeForNextMainPhase(double alpha) {
        // Zeit bis zum naechsten rot bzw. gruen
        // periode startet bei gruen
        // restliche period time + alpha*yellowPhase
        if (status == GREEN_LIGHT || status == GREEN_RED_LIGHT)
            return (greenTimePeriod + alpha * greenRedTimePeriod - currentCycleTime);
        if (status == RED_LIGHT || status == RED_GREEN_LIGHT)
            return (greenTimePeriod + greenRedTimePeriod + redTimePeriod + alpha * redGreenTimePeriod - currentCycleTime);
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#getCurrentCycleTime()
     */
    @Override
    public double getCurrentCycleTime() {
        return this.currentCycleTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#getCycleTime()
     */
    @Override
    public double getCycleTime() {
        return this.totalCycleTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.roadSection.TrafficLight#getTimeForNextGreen(double)
     */
    @Override
    public double getTimeForNextGreen(double alpha) {
        double dt = totalCycleTime - currentCycleTime - (1 - alpha) * redGreenTimePeriod;
        if (dt < 0) {
            dt += totalCycleTime;
        }
        return dt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.roadSection.TrafficLight#getTimeForNextRed(double)
     */
    @Override
    public double getTimeForNextRed(double alpha) {
        double dt = greenTimePeriod + alpha * greenRedTimePeriod - currentCycleTime;
        if (dt < 0) {
            dt += totalCycleTime;
        }
        return dt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#getTimeForNextGreen()
     */
    @Override
    public double getTimeForNextGreen() {
        double dt = totalCycleTime - currentCycleTime;
        if (dt < 0) {
            dt += totalCycleTime;
        }
        return dt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#getTimeForNextRed()
     */
    @Override
    public double getTimeForNextRed() {
        double dt = greenTimePeriod + greenRedTimePeriod - currentCycleTime;
        if (dt < 0) {
            dt += totalCycleTime;
        }
        return dt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#isGreen()
     */
    @Override
    public boolean isGreen() {
        return (status == GREEN_LIGHT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#isGreenRed()
     */
    @Override
    public boolean isGreenRed() {
        return (status == GREEN_RED_LIGHT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#isRed()
     */
    @Override
    public boolean isRed() {
        return (status == RED_LIGHT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#isRedGreen()
     */
    @Override
    public boolean isRedGreen() {
        return (status == RED_GREEN_LIGHT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.TrafficLight#status()
     */
    @Override
    public int status() {
        return status;
    }

    /**
     * Gets the status.
     * 
     * @return the status
     */
    String getStatus() {
        switch (status) {
        case GREEN_LIGHT:
            return "green";
        case GREEN_RED_LIGHT:
            return "green_red";
        case RED_LIGHT:
            return "red";
        case RED_GREEN_LIGHT:
            return "red_green";
        }
        return "error: not defined!";
    }

    @Override
    public void setRelativeRedPhase(double initRelativeRedPhase) {
        redTimePeriod = initRelativeRedPhase*redTimePeriodInit;
        greenTimePeriod = (1-initRelativeRedPhase)*greenTimePeriodInit;
        
        int oldStatus = status;
        if(initRelativeRedPhase>=1 || initRelativeRedPhase<=0){
            greenRedTimePeriod = 0;
            //redGreenTimePeriod = 0;
            System.out.println("++++ initRel "+ initRelativeRedPhase+ " and set to zero");
            oldStatus = (initRelativeRedPhase>=1) ? RED_LIGHT : GREEN_LIGHT;
        }
        else{
            greenRedTimePeriod = greenRedTimePeriodInit;
            //redGreenTimePeriod = redGreenTimePeriodInit;
        }

        
        initialize();
        status = oldStatus;
        
    }

    @Override
    public double getRelativeRedPhase() {
        return redTimePeriod/(redTimePeriod + greenTimePeriod);
    }

}
