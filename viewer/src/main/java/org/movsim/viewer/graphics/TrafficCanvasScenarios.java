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

package org.movsim.viewer.graphics;

import java.awt.Graphics2D;

import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;

/**
 * Traffic Canvas subclass that setups up the actual road network and traffic simulation scenarios.
 * 
 */
public class TrafficCanvasScenarios extends TrafficCanvas {

    static final long serialVersionUID = 1L;

    public static enum Scenario {
        NONE, ONRAMPFILE, STARTSTOPFILE, CLOVERLEAFFILE
    }

    private Scenario scenario = Scenario.NONE;

    // speed boost to get vehicles onto network quickly
    private boolean inInitialSpeedUp;
    private double speedupEndTime;
    private int sleepTimeSave;

    public TrafficCanvasScenarios(SimulationRunnable simulationRunnable, Simulator simulator) {
        super(simulationRunnable, simulator);

        simulationRunnable.addUpdateStatusCallback(this);
        setStatusControlCallbacks(statusControlCallbacks);

        final TrafficCanvasMouseWheelListener mousewheel = new TrafficCanvasMouseWheelListener(this);
        addMouseWheelListener(mousewheel);
    }

    public void setMessageStrings(String popupString, String popupStringExitEndRoad, String trafficInflowString,
            String perturbationRampingFinishedString, String perturbationAppliedString) {
        setMessageStrings(popupString, popupStringExitEndRoad, trafficInflowString);
    }

    @Override
    public void start() {
        assert scenario != Scenario.NONE;
        super.start();
    }

    @Override
    public void updateStatus(double simulationTime) {
        // System.out.println("TrafficCanvas.updateStatus:" + simulationTime);
        if (inInitialSpeedUp && simulationTime > speedupEndTime) {
            inInitialSpeedUp = false;
            setSleepTime(sleepTimeSave);
        }
    }

    /**
     * After the vehicles have been drawn, update any simulation , such as density diagrams and traffic lights.
     */
    @Override
    protected void drawAfterVehiclesMoved(Graphics2D g, double simulationTime, long iterationCount) {

    }

    @SuppressWarnings("nls")
    // private void displayInstrumentation() {
    // // RoadXMLWriter.externalizeRoadNetwork(roadNetwork);
    // // OpenDriveWriter.saveRoadNetwork(roadNetwork);
    // final long totalSimulationTime =
    // simulationRunnable.totalSimulationTime();
    // final long iterationCount = simulationRunnable.iterationCount();
    // final int vehicleCount = 0;
    // int roadSectionCount = 0;
    // for (final RoadSection roadSection :
    // movsimViewerFacade.getRoadSections()) {
    // // vehicleCount += roadSection.getVehContainer().size();
    // roadSectionCount++;
    // }
    // System.out.println("\nRoad network:");
    // // System.out.println("  source/sink count: " +
    // // TrafficFlowBase.count());
    // System.out.println("  road segment count: " + roadSectionCount);
    // // System.out.println("  lcm count: " + LaneChangeModel.count());
    // System.out.println("  active vehicle count: " + vehicleCount);
    // // System.out.println("  total vehicle count: " + (Vehicle.count()));
    // System.out.println("  iterationCount: " + iterationCount);
    // if (iterationCount != 0) {
    // System.out.println("  average simTime(ms): " + (double)
    // totalSimulationTime / iterationCount);
    // System.out.println("  average aniTime(ms): " + (double)
    // totalAnimationTime / iterationCount);
    // System.out.println("  average simTime(ms) per vehicle: " + (double)
    // totalSimulationTime
    // / (iterationCount * vehicleCount));
    // System.out.println("  average aniTime(ms) per vehicle: " + (double)
    // totalAnimationTime
    // / (iterationCount * vehicleCount));
    // }
    // }
    /**
     * Pause the animation.
     */
    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void reset() {
        super.reset();
        setSleepTime(INITIAL_SLEEP_TIME);
        initialScale = 1.0;
        inInitialSpeedUp = false;
        vehicleToHighlightId = -1;
        forceRepaintBackground();
    }

    /**
     * Sets up the given traffic scenario.
     * 
     * @param scenario
     */
    public void setupTrafficScenario(Scenario scenario) {

        reset();

        if (this.scenario == scenario) {
            return; // TODO proper restart
        }

        switch (scenario) {
        case ONRAMPFILE:
            simulator.loadScenarioFromXml("onramp_IDM", "../sim/buildingBlocks/");
            // initialScale = 1;
            // setScale(initialScale);
            // inInitialSpeedUp = false;
            break;
        case STARTSTOPFILE:
            simulator.loadScenarioFromXml("startStop_IDM", "../sim/bookScenarioStartStop/");
            break;
        case CLOVERLEAFFILE:
            simulator.loadScenarioFromXml("cloverleaf_ACC", "../sim/buildingBlocks/");
            break;
        default:
            return;
        }
        // if (statusControlCallbacks != null) {
        ////                    statusControlCallbacks.showStatusMessage(""); //$NON-NLS-1$
        // // statusControlCallbacks.stateChanged();
        // }
        forceRepaintBackground();
        this.scenario = scenario;
        start();
    }
}
