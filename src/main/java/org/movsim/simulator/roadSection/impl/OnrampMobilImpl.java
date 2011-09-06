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
import java.util.LinkedList;
import java.util.List;

import org.movsim.input.model.simulation.RampData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.InflowTimeSeries;
import org.movsim.simulator.roadSection.Onramp;
import org.movsim.simulator.roadSection.UpstreamBoundary;
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
public class OnrampMobilImpl extends AbstractRoadSection implements Onramp {

    /** The lane for entering the mainroad  
     *  only MOST_RIGHT_LANE possible to enter*/
    private final static int LANE_TO_MERGE_ON_MAINROAD = Constants.MOST_RIGHT_LANE; 

    
    private static final String extensionFormat = ".S%d_log.csv";
    private static final String outputHeading = Constants.COMMENT_CHAR
            + "     t[s], lane,  xEnter[m],    v[km/h],   qBC[1/h],  count,  queue\n";
    private static final String outputFormat = "%10.2f, %4d, %10.2f, %10.2f, %10.2f, %6d, %6d%n";

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(OnrampImpl.class);

    /** The Constant MINSPACE_MERGE_M. */
    final static double MINSPACE_MERGE_M = 2.0;

    /** The Constant RAMP_VEL_REDUCEFACTOR. */
    final static double RAMP_VEL_REDUCEFACTOR = 0.6;
    
    
    final static int N_LANES = 1;
    
    /** The veh container list (for each lane). */
    private List<VehicleContainer> vehContainers;


    /** The main veh container. */
    private final VehicleContainer mainVehContainer;

    /** The inflow time series. */
   // private final InflowTimeSeries inflowTimeSeries;

    /** The vehicle queue. */
    //private final LinkedList<Vehicle> vehicleQueue;

    /** The x center position of the ramp. */
    private final double xCenter;

    /** The length of the ramp. */
    private final double length;

    /** The x up ramp marks the start of ther ramp. */
    private final double xUpRamp;

    /** The x down ramp marks the end of the ramp. */
    private final double xDownRamp;

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
    public OnrampMobilImpl(final RampData rampData, final VehicleGenerator vehGenerator, final VehicleContainer mainVehContainerMostRightLane,
            String projectName, int rampIndex) {

        super(rampData, vehGenerator);
        this.mainVehContainer = mainVehContainerMostRightLane;  // container of mainroad's most-right lane
        //vehicleQueue = new LinkedList<Vehicle>();
        
        // create vehicle container for onramp lane
        vehContainers = new ArrayList<VehicleContainer>();
        vehContainers.add(new VehicleContainerImpl(Constants.MOST_RIGHT_LANE));
        
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

        this.length = rampData.getRampLength();
        this.xCenter = rampData.getRampStartPosition() + 0.5 * length;

        xUpRamp = rampData.getRampStartPosition();
        xDownRamp = xUpRamp + length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.Onramp#update(int, double, double)
     */
    @Override
    public void update(long iterationCount, double dt, double time) {


        // check for crashes
        checkForInconsistencies(iterationCount, time, isWithCrashExit);

        //updateRoadConditions(iterationCount, time);

        // vehicle accelerations
        accelerate(iterationCount, dt, time);

        // vehicle pos/speed
        updatePositionAndSpeed(iterationCount, dt, time);

        //updateDownstreamBoundary();

        updateUpstreamBoundary(iterationCount, dt, time);

        //detectors.update(iterationCount, time, dt, vehContainers);
        
                
//                if (fstrLogging != null) {
//                    final double qBC  = upstreamBoundary.getTotalInflow(time);
//                    fstrLogging.printf(outputFormat, time, LANE_TO_MERGE_ON_MAINROAD, xEnterLastMerge, 3.6 * vEnterLastMerge,
//                            3600 * qBC, mergeCount, vehicleQueue.size());
//                    fstrLogging.flush();
//                }

    }

    /**
     * Adds the vehicle from ramp.
     * 
     * @param vehToEnter
     *            the veh to enter
     * @param xEnter
     *            the x enter
     * @param vEnter
     *            the v enter
     * @param laneEnter
     *            the lane enter
     */
    private void addVehicleFromRamp(Vehicle vehToEnter, double xEnter, double vEnter) {
        mainVehContainer.addFromRamp(vehToEnter, xEnter, vEnter, LANE_TO_MERGE_ON_MAINROAD);
        // update status for last merge
        mergeCount++;
        xEnterLastMerge = xEnter;
        vEnterLastMerge = vEnter;
        //LANE_TO_MERGE_ON_MAINROAD = laneEnter; 
    }

    /**
     * Speed to enter.
     * 
     * @param speedLeader
     *            the speed leader
     * @return the double
     */
    private double speedToEnter(double speedLeader) {
        final double vCrit = 10.;
        final double reduceFactor = 1. - (1 - RAMP_VEL_REDUCEFACTOR) * Math.min(1., speedLeader / vCrit);
        return reduceFactor * speedLeader;
    }

    /**
     * Find down index within ramp.
     * 
     * @param mainVehicles
     *            the main vehicles
     * @return the int
     */
    private int findDownIndexWithinRamp(List<Vehicle> mainVehicles) {
        // for search in upstream direction increase index
        if (mainVehicles.isEmpty()) {
            logger.error("cannot operate on empty list !");
            System.exit(-1);
        }
        for (int i = 0, N = mainVehicles.size(); i < N; i++) {
            final double pos = mainVehicles.get(i).getPosition();
            if (pos <= xDownRamp && pos >= xUpRamp)
                return i; // first vehicle in down direction
        }
        return -1;
    }

    /**
     * Find up index within ramp.
     * 
     * @param mainVehicles
     *            the main vehicles
     * @return the int
     */
    private int findUpIndexWithinRamp(List<Vehicle> mainVehicles) {
        // for search in downstream direction DECREASE index
        if (mainVehicles.isEmpty()) {
            logger.error("cannot operate on empty list !");
            System.exit(-1);
        }
        final int N = mainVehicles.size();
        for (int i = N - 1; i > -0; i--) {
            final double pos = mainVehicles.get(i).getPosition();
            if (pos <= xDownRamp && pos >= xUpRamp)
                return i; // first vehicle in down direction
        }
        return -1;
    }

    /**
     * Try merge.
     * 
     * @param vehToEnter
     *            the veh to enter
     * @param mainVehContainer
     *            the main veh container
     * @return true, if successful
     */
    private boolean tryMerge(Vehicle vehToEnter, VehicleContainer mainVehContainer) {

        logger.debug("");
        final double xUp = xUpRamp;
        final double xDown = xUpRamp + length;

        final List<Vehicle> mainVehicles = mainVehContainer.getVehicles();

        final int laneEnter = Constants.MOST_RIGHT_LANE; // single-lane
                                                         // simulation

        final int mainVehSize = mainVehicles.size();
        if (mainVehSize == 0) {
            logger.debug("empty road: merge anyway. mainVeh.size() = {}", mainVehSize);
            final double xEnter = xCenter;
            final double vEnter = speedToEnter(vehToEnter.getDesiredSpeedParameter()); // no
                                                                                       // leader
            addVehicleFromRamp(vehToEnter, xEnter, vEnter);
            return true;
        } else if (mainVehContainer.getMostDownstream().getPosition() <= xCenter) {
            // most downstream mainroad vehicle is upstream of onramp
            final Vehicle mainVehDown = mainVehContainer.getMostDownstream();
            final double xEnter = Math.max(xCenter, mainVehDown.getPosition() + 0.5 * length);
            final double vEnter = speedToEnter(vehToEnter.getDesiredSpeedParameter()); // no
                                                                                       // leader
            logger.debug("most downstream veh is still upstream of ramp center. mainVeh.size() = {}. posMostDown = {}",
                    mainVehSize, mainVehDown.getPosition());
            addVehicleFromRamp(vehToEnter, xEnter, vEnter);
            return true;
        } else if (mainVehContainer.getMostUpstream().getPosition() >= xCenter) {
            // most upstream mainroad vehicle has already passed onramp
            final Vehicle mainVehUp = mainVehContainer.getMostUpstream();
            final double xEnter = Math.min(xCenter, mainVehUp.getPosition() - 0.5 * length);
            final double vEnter = speedToEnter(mainVehUp.getSpeed());
            logger.debug("most upstream veh is already downstream of ramp center. mainVeh.size() = {}, posMostUp = {}",
                    mainVehSize, mainVehUp.getPosition());
            addVehicleFromRamp(vehToEnter, xEnter, vEnter);
            return true;
        } else {
            logger.debug("okay, try merge mainVeh.size() = {}", mainVehSize);

            // logger.debug("mainVehContainer.getMostDownstream().position() = {} ",
            // mainVehContainer.getMostDownstream().position());
            // logger.debug("mainVehContainer.getMostUpstream().position() = {} ",
            // mainVehContainer.getMostUpstream().position());

            final int indexUp = findUpIndexWithinRamp(mainVehicles);
            final int indexDown = findDownIndexWithinRamp(mainVehicles);
            logger.debug("indexUp = {}, indexDown = {}", indexUp, indexDown);

            // scan through vehicle list on mainroad in upstream direction
            // (increase index)

            if (indexUp == -1 && indexDown == -1) {
                for (int i = 0; i < mainVehSize; i++) {
                    logger.debug("i = {}, position() = {} ", i, mainVehicles.get(i).getPosition());
                }
                // enter in center
                final double xEnter = xCenter;
                final double vEnter = speedToEnter(vehToEnter.getDesiredSpeedParameter());
                addVehicleFromRamp(vehToEnter, xEnter, vEnter);
                return true;
            }

            for (int i = indexDown; i <= indexUp; i++) {
                logger.debug("i = {}, position() = {} ", i, mainVehicles.get(i).getPosition());
            }
            double xEnter = -1;
            double vEnter = -1;

            double minGap = -1; // init

            // compare
            for (int i = indexDown; i <= Math.min(indexUp + 1, mainVehSize - 1); i++) {
                final Vehicle actualVeh = mainVehicles.get(i);
                final double posFront = (i > 0) ? mainVehicles.get(i - 1).getPosition() : Double.MAX_VALUE; // abfrage
                final double xEnterTest = Math.min(Math.max(xUp, 0.5 * (actualVeh.getPosition() + posFront)), xDown);
                double netGap = xEnterTest - actualVeh.getPosition() - 0.5 * (actualVeh.getLength() + vehToEnter.getLength());
                if (i == indexUp + 1) {
                    netGap = mainVehicles.get(i - 1).getPosition() - xEnterTest - 0.5
                            * (mainVehicles.get(i - 1).getLength() + vehToEnter.getLength());
                }

                logger.debug("netGap = {}, xEnterTest = {}", netGap, xEnterTest);
                if (netGap > MINSPACE_MERGE_M && netGap > minGap) {
                    // new !!
                    minGap = netGap;
                    xEnter = xEnterTest;
                    vEnter = (i > 0) ? speedToEnter(mainVehicles.get(i - 1).getSpeed()) : speedToEnter(vehToEnter
                            .getDesiredSpeedParameter());
                }
            }
            // check between indexUp+1 and indexUp
            if (indexUp == mainVehSize) {
                // compare directly. assume position at 0
                final double posFront = mainVehicles.get(indexUp).getPosition();
                final double xEnterTest = Math.min(Math.max(xUp, 0.5 * (0 + posFront)), xDown);
                final double netGap = posFront - xEnterTest - vehToEnter.getLength();
                logger.debug("test gap for most upstream  without back veh: netGap = {}, xEnterTest = {}", netGap,
                        xEnterTest);
                if (netGap > MINSPACE_MERGE_M && netGap > minGap) {
                    minGap = netGap;
                    xEnter = xEnterTest;
                    vEnter = speedToEnter(mainVehicles.get(indexUp).getSpeed());
                }
            }

            if (xEnter > 0) {
                logger.debug("enter from ramp. squeeze in at x = {} with speed = {}", xEnter, vEnter);
                addVehicleFromRamp(vehToEnter, xEnter, vEnter);
                return true;
            }
        }
        return false;
    }


}