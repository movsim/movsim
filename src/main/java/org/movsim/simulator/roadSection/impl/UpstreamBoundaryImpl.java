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

import java.io.PrintWriter;

import org.movsim.input.model.simulation.UpstreamBoundaryData;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.InflowTimeSeries;
import org.movsim.simulator.roadSection.UpstreamBoundary;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class UpstreamBoundaryImpl.
 */
public class UpstreamBoundaryImpl implements UpstreamBoundary {
    final static Logger logger = LoggerFactory.getLogger(UpstreamBoundaryImpl.class);

    private double nWait;

    private final VehicleGenerator vehGenerator;

    private final VehicleContainer vehContainer;

    private final InflowTimeSeries inflowTimeSeries;

    // logging
    private PrintWriter fstrLogging;

    private int enteringVehCounter;

    // status of last merging vehicle for logging to file
    private double xEnterLast;
    private double vEnterLast;
    private int laneEnterLast;

    /**
     * Instantiates a new upstream boundary impl.
     * 
     * @param vehGenerator
     *            the veh generator
     * @param vehContainer
     *            the veh container
     * @param upstreamBoundaryData
     *            the upstream boundary data
     * @param projectName
     *            the project name
     */
    public UpstreamBoundaryImpl(VehicleGenerator vehGenerator, VehicleContainer vehContainer,
            UpstreamBoundaryData upstreamBoundaryData, String projectName) {
        this.vehGenerator = vehGenerator;
        this.vehContainer = vehContainer;
        nWait = 0;

        inflowTimeSeries = new InflowTimeSeriesImpl(upstreamBoundaryData.getInflowTimeSeries());

        if (upstreamBoundaryData.withLogging()) {
            enteringVehCounter = 1;
            final String filename = projectName + ".upBC_log";
            fstrLogging = FileUtils.getWriter(filename);
            fstrLogging.printf(Constants.COMMENT_CHAR
                    + " time[s]  count[i]  xEnter[m]  vEnter[km/h]  laneEnter[1]  queue[1]  qBC[1/h]%n");
        }
    }

    /**
     * Adds the vehicle.
     * 
     * @param vehPrototype
     *            the veh prototype
     * @param xEnter
     *            the x enter
     * @param vEnter
     *            the v enter
     * @param laneEnter
     *            the lane enter
     */
    private void addVehicle(VehiclePrototype vehPrototype, double xEnter, double vEnter, int laneEnter) {
        final Vehicle veh = vehGenerator.createVehicle(vehPrototype);
        vehContainer.add(veh, xEnter, vEnter, laneEnter);
        // status variables of entering vehicle for logging
        enteringVehCounter++;
        xEnterLast = xEnter;
        vEnterLast = vEnter;
        laneEnterLast = laneEnter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.UpstreamBoundary#update(int,
     * double, double)
     */
    @Override
    public void update(int itime, double dt, double time) {
        // integrate inflow demand
        final double qBC = inflowTimeSeries.getFlow(time);
        nWait += qBC * dt;
        if (nWait >= 1) {
            // try to insert new vehicle at inflow
            final boolean isEntered = tryEnteringNewVehicle(time, qBC);
            if (isEntered) {
                nWait--;
                if (fstrLogging != null) {
                    fstrLogging.printf("%10.1f %6d  %8.2f  %8.2f  %5d  %6.1f  %8.2f%n", time, enteringVehCounter,
                            xEnterLast, 3.6 * vEnterLast, laneEnterLast, nWait, 3600 * qBC);
                    fstrLogging.flush();
                }
            }
        }
    }

    /**
     * Try entering new vehicle.
     * 
     * @param time
     *            the time
     * @param qBC
     *            the q bc
     * @return true, if successful
     */
    private boolean tryEnteringNewVehicle(double time, double qBC) {

        final VehiclePrototype vehPrototype = vehGenerator.getVehiclePrototype(); // type
                                                                                  // of
                                                                                  // new
                                                                                  // vehicle
        final Vehicle leader = vehContainer.getMostUpstream();

        // (1) empty road
        if (leader == null) {
            enterVehicleOnEmptyRoad(time, vehPrototype);
            return true;
        }
        // (2) check if gap to leader is sufficiently large
        final double netGapToLeader = leader.position() - leader.length(); // origin
                                                                           // of
                                                                           // road
                                                                           // section
                                                                           // is
                                                                           // assumed
                                                                           // to
                                                                           // be
                                                                           // zero
        double gapAtQMax = 1. / vehPrototype.getRhoQMax();
        if (vehPrototype.getLongModel().modelName().equalsIgnoreCase("")) {
            final double tau = 1;
            gapAtQMax = leader.speed() * tau;
        }

        final double minRequiredGap = 0.8 * gapAtQMax; // minimal distance set
                                                       // to 80 % of 1/rho at
                                                       // flow maximum in
                                                       // fundamental
                                                       // diagram
        if (netGapToLeader > minRequiredGap) {
            enterVehicle(time, minRequiredGap, vehPrototype, leader);
            return true;
        }
        // no entering possible
        return false;
    }

    /**
     * Enter vehicle on empty road.
     * 
     * @param time
     *            the time
     * @param vehPrototype
     *            the veh prototype
     */
    private void enterVehicleOnEmptyRoad(double time, VehiclePrototype vehPrototype) {
        final double xEnter = 0;
        final double vEnter = inflowTimeSeries.getSpeed(time);
        final int laneEnter = Constants.MOST_RIGHT_LANE;
        addVehicle(vehPrototype, xEnter, vEnter, laneEnter);
        // logger.debug("add vehicle from upstream boundary to empty road: xEnter={}, vEnter={}",
        // xEnter, vEnter);
    }

    /**
     * Enter vehicle.
     * 
     * @param time
     *            the time
     * @param sFreeMin
     *            the s free min
     * @param vehPrototype
     *            the veh prototype
     * @param leader
     *            the leader
     */
    private void enterVehicle(double time, double sFreeMin, VehiclePrototype vehPrototype, Vehicle leader) {
        final double sFree = leader.position() - leader.length();
        final double xLast = leader.position();
        final double vLast = leader.speed();
        final double aLast = leader.acc();

        final double speedDefault = inflowTimeSeries.getSpeed(time);
        final double vEnterTest = Math.min(speedDefault, 1.5 * vLast);
        final double lengthLast = leader.length();

        final double qBC = inflowTimeSeries.getFlow(time);
        final double xEnter = Math.min(vEnterTest * nWait / Math.max(qBC, 0.001), xLast - sFreeMin - lengthLast);
        final double rhoEnter = 1. / (xLast - xEnter);
        final double vMaxEq = vehPrototype.getEquilibriumSpeed(0.5 * rhoEnter);
        final double bMax = 4; // max. kinematic deceleration at boundary
        final double bEff = Math.max(0.1, bMax + aLast);
        final double vMaxKin = vLast + Math.sqrt(2 * sFree * bEff);
        final double vEnter = Math.min(Math.min(vEnterTest, vMaxEq), vMaxKin);
        final int laneEnter = Constants.MOST_RIGHT_LANE;

        addVehicle(vehPrototype, xEnter, vEnter, laneEnter);
        // logger.debug("add vehicle from upstream boundary: xEnter={}, vEnter={}",
        // xEnter, vEnter);
        // System.out.printf("add vehicle from upstream boundary: xLast=%.2f, vLast=%.2f, xEnter=%.2f, vEnter=%.2f, rhoEnter=%.2f, vMaxEq=%.2f, vMaxKin=%.2f %n",
        // xLast, vLast, xEnter, vEnter, rhoEnter, vMaxEq, vMaxKin );
    }

}
