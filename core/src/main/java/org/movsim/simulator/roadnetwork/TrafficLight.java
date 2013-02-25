/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TrafficLight.
 */
// TODO refactor. Use jaxb input class for members.
public class TrafficLight {

    public enum TrafficLightStatus {
        GREEN, GREEN_RED, RED, RED_GREEN;

        @Override
        public String toString() {
            return name();
        }
    }

    /** The Constant logger. */
    public static final Logger logger = LoggerFactory.getLogger(TrafficLight.class);

    /** The position. */
    // private final double position;

    private final String id;

    /** The status. */
    private TrafficLightStatus status;
    /** The old status. */
    private TrafficLightStatus oldStatus;

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
    public TrafficLight(String id, double greenTime, double greenRedTime, double redTime, double redGreenTime,
            double phaseShift, int initStatusOrdinal) {
        // this.position = position;
        this.id = id;
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

        TrafficLightStatus initStatus = (initStatusOrdinal < 0 || initStatusOrdinal >= TrafficLightStatus.values().length) ? TrafficLightStatus.GREEN
                : TrafficLightStatus.values()[initStatusOrdinal];
        initialize(initStatus);

        // if (position < 0) {
        // logger.error("inconsistent input data: position of trafficlight at={} must be >= 0", position);
        // System.exit(-1);
        // }
    }

    /**
     * Constructor.
     * 
     */
    public TrafficLight(org.movsim.core.autogen.TrafficLight tlData) {
        this(tlData.getId(), tlData.getGreenTime(), tlData.getGreenRedTime(), tlData.getRedTime(), tlData
                .getRedGreenTime(), tlData.getPhaseShift(), tlData.getInit().intValue());
    }

    /**
     * Initialize.
     */
    private void initialize(TrafficLightStatus initStatus) {
        status = initStatus;
        totalCycleTime = redTimePeriod + greenTimePeriod + greenRedTimePeriod + redGreenTimePeriod;
        currentCycleTime = -phaseShift;
        logger.debug("initialize traffic light id={} with status={}", id, status.toString());
    }

    /**
     * Return the number of lights this traffic light has, can be 1, 2 or 3.
     * 
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
            status = TrafficLightStatus.GREEN_RED;
        }
        if (greenRedTimePeriod > 0.0 && currentCycleTime > greenTimePeriod + greenRedTimePeriod) {
            status = TrafficLightStatus.RED;
        }
        if (redTimePeriod > 0.0 && currentCycleTime > greenTimePeriod + greenRedTimePeriod + redTimePeriod) {
            status = TrafficLightStatus.RED_GREEN;
        }
        if (redGreenTimePeriod > 0.0 && currentCycleTime >= totalCycleTime) {
            status = TrafficLightStatus.GREEN;
            currentCycleTime -= totalCycleTime;
        }

        lastUpdateTime = simulationTime;
    }

    public void nextState() {
        oldStatus = status;
        switch (status) {
        case GREEN:
            if (hasGreenRedStatus == true) {
                status = TrafficLightStatus.GREEN_RED;
                currentCycleTime = greenTimePeriod;
            } else {
                status = TrafficLightStatus.RED;
                currentCycleTime = greenTimePeriod + greenRedTimePeriod;
            }
            break;
        case GREEN_RED:
            status = TrafficLightStatus.RED;
            currentCycleTime = greenTimePeriod + greenRedTimePeriod;
            break;
        case RED:
            if (hasGreenRedStatus == true) {
                status = TrafficLightStatus.RED_GREEN;
                currentCycleTime = greenTimePeriod + greenRedTimePeriod + redTimePeriod;
            } else {
                status = TrafficLightStatus.GREEN;
                currentCycleTime = 0.0;
            }
            break;
        case RED_GREEN:
            status = TrafficLightStatus.GREEN;
            currentCycleTime = 0.0;
            break;
        }
    }

    /**
     * Position.
     * 
     * @return the double
     */
    public double position() {
        // TODO
        return 0; // position;
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
        if (status == TrafficLightStatus.GREEN || status == TrafficLightStatus.GREEN_RED) {
            return (greenTimePeriod + alpha * greenRedTimePeriod - currentCycleTime);
        }
        if (status == TrafficLightStatus.RED || status == TrafficLightStatus.RED_GREEN) {
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
    public TrafficLightStatus status() {
        return status;
    }

    public double getRelativeRedPhase() {
        return redTimePeriod / (redTimePeriod + greenTimePeriod);
    }

}
