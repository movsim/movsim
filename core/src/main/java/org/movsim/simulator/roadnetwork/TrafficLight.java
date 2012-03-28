/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.roadnetwork;

import org.movsim.input.model.simulation.TrafficLightData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO code review, test scenario: test_trafficlights.xml
/**
 * The Class TrafficLight.
 */
public class TrafficLight {

    // cycle is GREEN --> GREEN_RED --> RED --> RED_GREEN --> GREEN
    /** The GREE n_ light. */
    public static final int GREEN_LIGHT = 0;

    /** The GREE n_ re d_ light. */
    public static final int GREEN_RED_LIGHT = 1;

    /** The RE d_ light. */
    public static final int RED_LIGHT = 2;

    /** The RE d_ gree n_ light. */
    public static final int RED_GREEN_LIGHT = 3;

    /** The Constant logger. */
    public static final Logger logger = LoggerFactory.getLogger(TrafficLight.class);

    // transfered parameters:
    /** The position. */
    private final double position;

    /** The status. */
    private int status;
    /** The old status. */
    private int oldStatus;

    private int lightCount;
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
    private final double greenRedTimePeriodInit;
    private boolean hasGreenRedStatus;

    /** The red green time period. */
    private double redGreenTimePeriod;
    private final double redGreenTimePeriodInit;
    private boolean hasRedGreenStatus;

    /** The phase shift. */
    private final double phaseShift;

    /** The current cycle time. */
    private double currentCycleTime = 0;

    /** The last update time. */
    private double lastUpdateTime = 0;

    /**
     * Constructor.
     */
    public TrafficLight(double position, double greenTime, double greenRedTime, double redTime, double redGreenTime, double phaseShift) {
        this.position = position;
        this.greenTimePeriod = greenTime;
        this.redTimePeriod = redTime;
        this.greenRedTimePeriod = greenRedTime;
        greenTimePeriodInit = greenTimePeriod;
        redTimePeriodInit = redTimePeriod;
        greenRedTimePeriodInit = greenRedTimePeriod;
        redGreenTimePeriodInit = redGreenTimePeriod;
        hasGreenRedStatus = true;
        if (greenRedTimePeriod < 0.0) {
            greenRedTimePeriod = 0.0;
            hasGreenRedStatus = false;
        }
        hasRedGreenStatus = true;
        this.redGreenTimePeriod = redGreenTime;
        if (redGreenTimePeriod < 0.0) {
            redGreenTimePeriod = 0.0;
            hasRedGreenStatus = false;
        }
        lightCount = hasGreenRedStatus == false && hasRedGreenStatus == false ? 2 : 3;
        this.phaseShift = phaseShift;
        initialize();
    }

    /**
     * Constructor.
     * 
     * @param inputData
     *            the input data
     */
    public TrafficLight(TrafficLightData inputData) {
        this(inputData.getX(), 
            inputData.getGreenTime(),
            inputData.getGreenRedTimePeriod(),
            inputData.getRedTime(),
            inputData.getRedGreenTimePeriod(),
            inputData.getPhaseShift());
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

    /**
     * Return the number of lights this traffic light has, can be 1, 2 or 3.
     * @return
     */
    public int lightCount() {
        return lightCount;
    }
    /**
     * Update.
     * 
     * @param simulationTime
     *            current simulation time, seconds
     */
    public void update(double simulationTime) {
        oldStatus = status;
        currentCycleTime += simulationTime - lastUpdateTime;

        // logger.debug("update at time = {}, status = {}", time, status);
        // logger.debug("   actualCycleTime = {}, lastUpdateTime={}", currentCycleTime, lastUpdateTime);

        // if any color time period is zero then the light will not automatically change from that color
        if (greenTimePeriod > 0.0 && currentCycleTime > greenTimePeriod) {
            status = GREEN_RED_LIGHT;
        }
        if (greenRedTimePeriod > 0.0 && currentCycleTime > greenTimePeriod + greenRedTimePeriod) {
            status = RED_LIGHT;
        }
        if (redTimePeriod > 0.0 && currentCycleTime > greenTimePeriod + greenRedTimePeriod + redTimePeriod) {
            status = RED_GREEN_LIGHT;
        }
        if (redGreenTimePeriod > 0.0 && currentCycleTime >= totalCycleTime) {
            status = GREEN_LIGHT;
            currentCycleTime -= totalCycleTime;
        }

        lastUpdateTime = simulationTime;
    }

    /**
     * Set the traffic light to its next state.
     * 
     * @param simulationTime
     *            current simulation time, seconds
     */
    public void nextState() {
        oldStatus = status;
        switch (status) {
        case GREEN_LIGHT:
            if (hasGreenRedStatus == true) {
                status = GREEN_RED_LIGHT;
                currentCycleTime = greenTimePeriod;
            } else {
                status = RED_LIGHT;
                currentCycleTime = greenTimePeriod + greenRedTimePeriod;
            }
            break;
        case GREEN_RED_LIGHT:
            status = RED_LIGHT;
            currentCycleTime = greenTimePeriod + greenRedTimePeriod;
            break;
        case RED_LIGHT:
            if (hasGreenRedStatus == true) {
                status = RED_GREEN_LIGHT;
                currentCycleTime = greenTimePeriod + greenRedTimePeriod + redTimePeriod;
            } else {
                status = GREEN_LIGHT;
                currentCycleTime = 0.0;
            }
            break;
        case RED_GREEN_LIGHT:
            status = GREEN_LIGHT;
            currentCycleTime = 0.0;
            break;
        }
    }

    // boolean redLightJustReleased(){
    // if(oldStatus==RED_LIGHT && (isGreen() || isRedGreen())) return true;
    // return false;
    // }
    //

    /**
     * Position.
     * 
     * @return the double
     */
    public double position() {
        return position;
    }

    /**
     * Gets the crit time for next main phase.
     * 
     * @param alpha
     *            the alpha
     * @return the crit time for next main phase
     */
    public double getCritTimeForNextMainPhase(double alpha) {
        // Zeit bis zum naechsten rot bzw. gruen
        // periode startet bei gruen
        // restliche period time + alpha*yellowPhase
        if (status == GREEN_LIGHT || status == GREEN_RED_LIGHT) {
            return (greenTimePeriod + alpha * greenRedTimePeriod - currentCycleTime);
        }
        if (status == RED_LIGHT || status == RED_GREEN_LIGHT) {
            return (greenTimePeriod + greenRedTimePeriod + redTimePeriod + alpha * redGreenTimePeriod - currentCycleTime);
        }
        return 0;
    }

    /**
     * Gets the current cycle time.
     * 
     * @return the current cycle time
     */
    public double getCurrentCycleTime() {
        return this.currentCycleTime;
    }

    /**
     * Gets the cycle time.
     * 
     * @return the cycle time
     */
    public double getCycleTime() {
        return this.totalCycleTime;
    }

    /**
     * Gets the time for next green.
     * 
     * @param alpha
     *            the alpha
     * @return the time for next green
     */
    public double getTimeForNextGreen(double alpha) {
        double dt = totalCycleTime - currentCycleTime - (1 - alpha) * redGreenTimePeriod;
        if (dt < 0) {
            dt += totalCycleTime;
        }
        return dt;
    }

    /**
     * Gets the time for next red.
     * 
     * @param alpha
     *            the alpha
     * @return the time for next red
     */
    public double getTimeForNextRed(double alpha) {
        double dt = greenTimePeriod + alpha * greenRedTimePeriod - currentCycleTime;
        if (dt < 0) {
            dt += totalCycleTime;
        }
        return dt;
    }

    /**
     * Gets the time for next green.
     * 
     * @return the time for next green
     */
    public double getTimeForNextGreen() {
        double dt = totalCycleTime - currentCycleTime;
        if (dt < 0) {
            dt += totalCycleTime;
        }
        return dt;
    }

    /**
     * Gets the time for next red.
     * 
     * @return the time for next red
     */
    public double getTimeForNextRed() {
        double dt = greenTimePeriod + greenRedTimePeriod - currentCycleTime;
        if (dt < 0) {
            dt += totalCycleTime;
        }
        return dt;
    }

    /**
     * Status.
     * 
     * @return the status
     */
    public int status() {
        return status;
    }

    public void setRelativeRedPhase(double initRelativeRedPhase) {
        redTimePeriod = initRelativeRedPhase * redTimePeriodInit;
        greenTimePeriod = (1 - initRelativeRedPhase) * greenTimePeriodInit;

        int oldStatus = status;
        if (initRelativeRedPhase >= 1 || initRelativeRedPhase <= 0) {
            greenRedTimePeriod = 0;
            // redGreenTimePeriod = 0;
            System.out.println("++++ initRel " + initRelativeRedPhase + " and set to zero");
            oldStatus = (initRelativeRedPhase >= 1) ? RED_LIGHT : GREEN_LIGHT;
        } else {
            greenRedTimePeriod = greenRedTimePeriodInit;
            // redGreenTimePeriod = redGreenTimePeriodInit;
        }

        initialize();
        status = oldStatus;
    }

    public double getRelativeRedPhase() {
        return redTimePeriod / (redTimePeriod + greenTimePeriod);
    }

}
