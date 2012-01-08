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
package org.movsim.output;

import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.input.model.output.TravelTimesInput;
import org.movsim.output.fileoutput.FileFloatingCars;
import org.movsim.output.fileoutput.FileSpatioTemporal;
import org.movsim.output.fileoutput.FileTrajectories;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SimOutput.
 */
public class SimOutput implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimOutput.class);

    private SpatioTemporal spatioTemporal = null;
    private FileSpatioTemporal fileSpatioTemporal;
    private FloatingCars floatingCars = null;
    private FileFloatingCars fileFloatingCars;
    private FileTrajectories fileTrajectories = null;
    private final boolean writeOutput;
    private final RoadNetwork roadNetwork;
    private final RoadSegment roadSegment;
    private TravelTimes travelTimes;

    /**
     * Instantiates a new sim output.
     * 
     * @param simInput
     *            the sim input
     * @param roadSections
     *            the road sections
     */
    public SimOutput(InputData simInput, RoadNetwork roadNetwork) {
        this.roadNetwork = roadNetwork;
        roadSegment = roadNetwork.size() == 0 ? null : roadNetwork.iterator().next();
        // TODO - route is hardcoded for now
        final Route route = new Route();
        route.setName("rt1");
        route.add(roadSegment);
        if (roadSegment.userId().equals("1")) {
            RoadSegment nextRoadSegment = roadNetwork.findByUserId("2");
            if (nextRoadSegment != null) {
                route.add(nextRoadSegment);
                nextRoadSegment = roadNetwork.findByUserId("3");
                if (nextRoadSegment != null) {
                    route.add(nextRoadSegment);
                }
            }
        }

        // more restrictive than in other output classes TODO
        writeOutput = simInput.getProjectMetaData().isInstantaneousFileOutput();

        // SingleRoad quickhack! TODO
        final SimulationInput simulationInput = simInput.getSimulationInput();
        if (simulationInput == null) {
            return;
        }
        final OutputInput outputInput = simulationInput.getOutputInput();

        // TODO quick hack null treatment
        // travel times
        final TravelTimesInput travelTimesInput = outputInput.getTravelTimesInput();
        if (travelTimesInput != null) {
            travelTimes = new TravelTimes(travelTimesInput, roadNetwork);
        }

        // TODO hack: just *one* roadsection
        // access not robust to fetch mainroad
        // roadSection = roadSections.get(0);
        // Floating Car Output
        final FloatingCarInput floatingCarInput = outputInput.getFloatingCarInput();
        if (floatingCarInput.isWithFCD()) {
            floatingCars = new FloatingCars(roadSegment, floatingCarInput);
            if (writeOutput) {
                fileFloatingCars = new FileFloatingCars(floatingCars);
            }
        }

        final SpatioTemporalInput spatioTemporalInput = outputInput.getSpatioTemporalInput();
        if (spatioTemporalInput.isWithMacro() && roadSegment != null) {
            spatioTemporal = new SpatioTemporal(spatioTemporalInput, route);
            if (writeOutput) {
                fileSpatioTemporal = new FileSpatioTemporal(route.getName(), spatioTemporal);
            }
        }

        final TrajectoriesInput trajInput = outputInput.getTrajectoriesInput();
        if (trajInput.isInitialized()) {
            if (writeOutput) {
                fileTrajectories = new FileTrajectories(trajInput, roadNetwork);
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        if (floatingCars != null) {
            floatingCars.timeStep(dt, simulationTime, iterationCount);
        }
        if (spatioTemporal != null) {
            spatioTemporal.timeStep(dt, simulationTime, iterationCount);
        }

        if (fileTrajectories != null) {
            fileTrajectories.timeStep(dt, simulationTime, iterationCount);
        }

        if (travelTimes != null) {
            travelTimes.timeStep(dt, simulationTime, iterationCount);
        }

    }

    /**
     * Gets the spatio temporal.
     * 
     * @return the spatio temporal
     */
    public SpatioTemporal getSpatioTemporal() {
        return spatioTemporal;
    }

    /**
     * Gets the floating cars.
     * 
     * @return the floating cars
     */
    public FloatingCars getFloatingCars() {
        return floatingCars;
    }

    /**
     * Gets the loop detectors.
     * 
     * @return the loop detectors
     */
    public List<LoopDetector> getLoopDetectors() {
        return roadSegment.getLoopDetectors().getDetectors();
    }

    public TravelTimes getTravelTimes() {
        return travelTimes;
    }
}
