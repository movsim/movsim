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

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;

import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadSegment;

/**
 * Traffic Canvas subclass that setups up the actual road network and traffic simulation scenarios.
 * 
 */
public class TrafficCanvasScenarios extends TrafficCanvas {

    static final long serialVersionUID = 1L;

    public static enum Scenario {
        NONE, ONRAMPFILE, STARTSTOPFILE, CLOVERLEAFFILE, OFFRAMPFILE, LANECLOSINGFILE, TRAFFICLIGHTFILE, SPEEDLIMITFILE, RINGROADONELANEFILE, RINGROADTWOLANESFILE, FLOWCONSERVINGBOTTLENECK, VASALOPPET
    }

    private Scenario scenario = Scenario.NONE;

    // speed boost to get vehicles onto network quickly
    private boolean isInitialSpeedUp;
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
        setMessageStrings(popupString, popupStringExitEndRoad);
    }

    @Override
    public void start() {
        assert scenario != Scenario.NONE;
        super.start();
    }

    @Override
    public void updateStatus(double simulationTime) {
        if (isInitialSpeedUp && simulationTime > speedupEndTime) {
            isInitialSpeedUp = false;
            setSleepTime(sleepTimeSave);
        }
    }

    /**
     * After the vehicles have been drawn, update any simulation , such as density diagrams and traffic lights.
     */
    @Override
    protected void drawAfterVehiclesMoved(Graphics2D g, double simulationTime, long iterationCount) {

    }

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
        isInitialSpeedUp = false;
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
        final String path;
        switch (scenario) {
        case ONRAMPFILE: // TODO rg path
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("onramp", path);
            // initialScale = 1;
            // setScale(initialScale);
            // inInitialSpeedUp = false;
            break;
        case OFFRAMPFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("offramp", path);
            break;
        case STARTSTOPFILE:
            path = ".." + File.separator + "sim" + File.separator + "bookScenarioStartStop" + File.separator;
            simulator.loadScenarioFromXml("startStop_IDM", path);
            break;
        case CLOVERLEAFFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("cloverleaf", path);
            break;
        case LANECLOSINGFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("laneclosure", path);
            break;
        case TRAFFICLIGHTFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("trafficlight", path);
            break;
        case SPEEDLIMITFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("speedlimit", path);
            break;
        case RINGROADONELANEFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("ringroad_1lane", path);
            break;
        case RINGROADTWOLANESFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("ringroad_2lanes", path);
            break;
        case FLOWCONSERVINGBOTTLENECK:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            simulator.loadScenarioFromXml("flow_conserving_bottleneck", path);
            break;
        case VASALOPPET:
            path = ".." + File.separator + "sim" + File.separator + "examples" + File.separator;
            simulator.loadScenarioFromXml("vasa_CCS", path);
            setSleepTime(0);
            setVmaxForColorSpectrum(40);
            setxOffset(400);
            setyOffset(700);
            roadLineColor = Color.LIGHT_GRAY;
            roadEdgeColor = Color.DARK_GRAY;
            backgroundColor = Color.WHITE;
            initialScale = 0.8;
            setScale(initialScale);
            for (RoadSegment segment: roadNetwork) {
                segment.roadMapping().setRoadColor(Color.WHITE);
            }
            break;
        default:
            return;
        }

        forceRepaintBackground();
        this.scenario = scenario;
        start();
    }

}
