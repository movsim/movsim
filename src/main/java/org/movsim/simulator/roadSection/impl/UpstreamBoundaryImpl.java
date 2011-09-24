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
import java.util.List;

import org.movsim.input.model.simulation.TrafficSourceData;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.InflowTimeSeries;
import org.movsim.simulator.roadSection.UpstreamBoundary;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.utilities.impl.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class UpstreamBoundaryImpl.
 */
public class UpstreamBoundaryImpl implements UpstreamBoundary {

    // TODO the same output format is used in SimpleOnrampImpl. Consolidate. 

    private static final String extensionFormat = ".id%d_source_log.csv";
    private static final String outputHeading = Constants.COMMENT_CHAR
            + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],    count,      queue\n";
    private static final String outputFormat = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %8d, %10.5f%n";

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(UpstreamBoundaryImpl.class);

    /** The n wait. */
    private double nWait;

    /** The veh generator. */
    private final VehicleGenerator vehGenerator;

    /** The veh container. */
    private final List<VehicleContainer> vehContainers;

    /** The inflow time series. */
    private final InflowTimeSeries inflowTimeSeries;

    /** The fstr logging. */
    private PrintWriter fstrLogging;

    /** The entering veh counter. */
    private int enteringVehCounter;

    /** The x enter last. status of last merging vehicle for logging to file */
    private double xEnterLast;

    /** The v enter last. */
    private double vEnterLast;

    /** The lane enter last. */
    private int laneEnterLast;

    /**
     * Instantiates a new upstream boundary impl.
     *
     * @param vehGenerator the vehicle generator
     * @param vehContainers the veh containers
     * @param upstreamBoundaryData the upstream boundary data
     * @param projectName the project name
     */
    public UpstreamBoundaryImpl(long roadId, VehicleGenerator vehGenerator, List<VehicleContainer> vehContainers,
            TrafficSourceData upstreamBoundaryData, String projectName) {
        this.vehGenerator = vehGenerator;
        this.vehContainers = vehContainers;
        nWait = 0;

        inflowTimeSeries = new InflowTimeSeriesImpl(upstreamBoundaryData.getInflowTimeSeries());

        if (upstreamBoundaryData.withLogging()) {
            enteringVehCounter = 1;
            final String filename = projectName + String.format(extensionFormat, roadId);
            fstrLogging = FileUtils.getWriter(filename);
            fstrLogging.printf(outputHeading);
        }
    }

    /* (non-Javadoc)
     * @see org.movsim.simulator.roadSection.UpstreamBoundary#getEnteringVehCounter()
     */
    @Override
    public int getEnteringVehCounter() {
        return enteringVehCounter;
    }

    /**
     * Gets the new cyclic lane index for entering.
     *
     * @param iLane the i lane
     * @return the new cyclic lane index for entering
     */
    private int getNewCyclicLaneIndexForEntering(int iLane) {
        return (iLane == vehContainers.size() - 1 ? 0 : iLane + 1);
    }

    /**
     * Gets the total inflow.
     *
     * @param time the time
     * @return the total inflow
     */
    private double getTotalInflow(double time) {
        // inflow over all lanes
        final double qBC = inflowTimeSeries.getFlowPerLane(time);
        final int nLanes = vehContainers.size();
        return nLanes * qBC;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.UpstreamBoundary#update(int,
     * double, double)
     */
    @Override
    public void update(long itime, double dt, double time) {
        // integrate inflow demand
        final double totalInflow = getTotalInflow(time);
        nWait += totalInflow * dt;
        if (nWait >= 1) {
            // try to insert new vehicle at inflow
            // iterate periodically over n lanes
            int iLane = laneEnterLast;
            for (int i = 0, N = vehContainers.size(); i < N; i++) {
                iLane = getNewCyclicLaneIndexForEntering(iLane);
                final VehicleContainer vehContainerLane = vehContainers.get(iLane);
                // lane index is identical to vehicle's lane number
                final boolean isEntered = tryEnteringNewVehicle(vehContainerLane, time, totalInflow);
                if (isEntered) {
                    nWait--;
                    if (fstrLogging != null) {
                        fstrLogging.printf(outputFormat, time, laneEnterLast, xEnterLast, 3.6 * vEnterLast,
                                3600 * totalInflow, enteringVehCounter, nWait);
                        fstrLogging.flush();
                    }
                    return; // only one insert per simulation update
                }
            }
        }
    }

    /**
     * Try entering new vehicle.
     *
     * @param vehContainer the veh container
     * @param time the time
     * @param qBC the q bc
     * @return true, if successful
     */
    private boolean tryEnteringNewVehicle(final VehicleContainer vehContainer, double time, double qBC) {

        // type of new vehicle
        final VehiclePrototype vehPrototype = vehGenerator.getVehiclePrototype();
        final Vehicle leader = vehContainer.getMostUpstream();

        // (1) empty road
        if (leader == null) {
            enterVehicleOnEmptyRoad(vehContainer, time, vehPrototype);
            return true;
        }
        // (2) check if gap to leader is sufficiently large
        // origin of road section is assumed to be zero
        final double netGapToLeader = leader.getPosition() - leader.getLength();
        double gapAtQMax = 1. / vehPrototype.getRhoQMax();
        if (vehPrototype.getLongModel().modelName().equalsIgnoreCase("")) {
            final double tau = 1;
            gapAtQMax = leader.getSpeed() * tau;
        }
        // minimal distance set to 80% of 1/rho at flow maximum in fundamental
        // diagram
        double minRequiredGap = 0.8 * gapAtQMax;
        if (vehPrototype.getLongModel().isCA()) {
            final double tau = 1;
            minRequiredGap = leader.getSpeed() * tau;
        }
        if (netGapToLeader > minRequiredGap) {
            enterVehicle(vehContainer, time, minRequiredGap, vehPrototype, leader);
            return true;
        }
        // no entering possible
        return false;
    }

    /**
     * Enter vehicle on empty road.
     *
     * @param vehContainer the veh container
     * @param time the time
     * @param vehPrototype the veh prototype
     */
    private void enterVehicleOnEmptyRoad(final VehicleContainer vehContainer, double time, VehiclePrototype vehPrototype) {
        final double xEnter = 0;
        final double vEnter = inflowTimeSeries.getSpeed(time);
        addVehicle(vehContainer, vehPrototype, xEnter, vEnter);
        logger.debug("add vehicle from upstream boundary to empty road: xEnter={}, vEnter={}", xEnter, vEnter);
    }

    /**
     * Enter vehicle.
     *
     * @param vehContainer the veh container
     * @param time the time
     * @param sFreeMin the s free min
     * @param vehPrototype the veh prototype
     * @param leader the leader
     */
    private void enterVehicle(final VehicleContainer vehContainer, double time, double sFreeMin,
            VehiclePrototype vehPrototype, Vehicle leader) {

        final double speedDefault = inflowTimeSeries.getSpeed(time);

        final double sFree = leader.getPosition() - leader.getLength();
        final double xLast = leader.getPosition();
        final double vLast = leader.getSpeed();
        final double aLast = leader.getAcc();

        final double vEnterTest = Math.min(speedDefault, 1.5 * vLast);
        final double lengthLast = leader.getLength();

        final double qBC = inflowTimeSeries.getFlowPerLane(time);
        final double xEnter = Math.min(vEnterTest * nWait / Math.max(qBC, 0.001), xLast - sFreeMin - lengthLast);
        final double rhoEnter = 1. / (xLast - xEnter);
        final double vMaxEq = vehPrototype.getEquilibriumSpeed(0.5 * rhoEnter);
        final double bMax = 4; // max. kinematic deceleration at boundary
        final double bEff = Math.max(0.1, bMax + aLast);
        final double vMaxKin = vLast + Math.sqrt(2 * sFree * bEff);
        final double vEnter = Math.min(Math.min(vEnterTest, vMaxEq), vMaxKin);
        // final int laneEnter = Constants.MOST_RIGHT_LANE;

        addVehicle(vehContainer, vehPrototype, xEnter, vEnter);
        // logger.debug("add vehicle from upstream boundary: xEnter={}, vEnter={}",
        // xEnter, vEnter);

        // System.out.printf("add vehicle from upstream boundary: vehType=%s, xLast=%.2f, vLast=%.2f, xEnter=%.2f, vEnter=%.2f, lane=%d, rhoEnter=%.2f, vMaxEq=%.2f, vMaxKin=%.2f %n",
        // vehPrototype.getLabel(), xLast, vLast, xEnter, vEnter,
        // vehContainer.getLaneIndex(), rhoEnter, vMaxEq, vMaxKin );
    }

    /**
     * Adds the vehicle.
     *
     * @param vehContainer the veh container
     * @param vehPrototype the veh prototype
     * @param xEnter the x enter
     * @param vEnter the v enter
     */
    private void addVehicle(final VehicleContainer vehContainer, final VehiclePrototype vehPrototype, double xEnter,
            double vEnter) {
        final Vehicle veh = vehGenerator.createVehicle(vehPrototype);
        vehContainer.add(veh, xEnter, vEnter);
        // status variables of entering vehicle for logging
        enteringVehCounter++;
        xEnterLast = xEnter;
        vEnterLast = vEnter;
        laneEnterLast = vehContainer.getLaneIndex();
    }

    @Override
    public void setFlowPerLane(double newFlowPerLane) {
        logger.info("set new flow per lane={} per second and reset queue of waiting vehicles={}", newFlowPerLane,nWait);
        inflowTimeSeries.setConstantFlowPerLane(newFlowPerLane);
        nWait = 0;
    }

    @Override
    public double getFlowPerLane(double time) {
        return inflowTimeSeries.getFlowPerLane(time);
    }
}
