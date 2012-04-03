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

import javax.swing.JOptionPane;

import org.movsim.input.model.VehiclesInput;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traffic Canvas subclass that setups up the actual road network and traffic simulation scenarios.
 * 
 */
public class TrafficCanvasScenarios extends TrafficCanvas {

    @SuppressWarnings("hiding")
    static final long serialVersionUID = 1L;
    final static Logger logger = LoggerFactory.getLogger(TrafficCanvasScenarios.class);

    public static enum Scenario {
        NONE, ONRAMPFILE, STARTSTOPFILE, CLOVERLEAFFILE, OFFRAMPFILE, LANECLOSINGFILE, TRAFFICLIGHTFILE,
        SPEEDLIMITFILE, RINGROADONELANEFILE, RINGROADTWOLANESFILE, FLOWCONSERVINGBOTTLENECK,
        RAMPMETERING, ROUTING,
        VASALOPPET
    }

    private Scenario scenario = Scenario.NONE;

    // speed boost to get vehicles onto network quickly
    private boolean isInitialSpeedUp;
    private double speedupEndTime;
    private int sleepTimeSave;
    String simulationFinished;

    public TrafficCanvasScenarios(Simulator simulator) {
        super(simulator);

        simulationRunnable.addUpdateStatusCallback(this);
        setStatusControlCallbacks(statusControlCallbacks);

        final TrafficCanvasMouseWheelListener mousewheel = new TrafficCanvasMouseWheelListener(this);
        addMouseWheelListener(mousewheel);
    }

    public void setMessageStrings(String popupString, String popupStringExitEndRoad, String trafficInflowString,
            String perturbationRampingFinishedString, String perturbationAppliedString, String simulationFinished) {
        setMessageStrings(popupString, popupStringExitEndRoad);
        this.simulationFinished = simulationFinished;
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
        if (simulator.isFinished() && simulationFinished != null) {
            JOptionPane.showMessageDialog(null, String.format(simulationFinished, (int) simulationTime));
            simulationRunnable.stop();
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
        isInitialSpeedUp = false;
        vehicleToHighlightId = -1;
        initGraphicSettings();
        forceRepaintBackground();
    }

    /**
     * Returns the current traffic scenario.
     * @return the current traffic scenario
     */
    Scenario scenario() {
        return scenario;
    }

    /**
     * Sets up the given traffic scenario.
     * 
     * @param scenario
     */
    public void setupTrafficScenario(Scenario scenario) {

        final String path;
        switch (scenario) {
        case ONRAMPFILE: // TODO rg path
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case OFFRAMPFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case STARTSTOPFILE:
            path = ".." + File.separator + "sim" + File.separator + "bookScenarioStartStop" + File.separator;
            break;
        case CLOVERLEAFFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case LANECLOSINGFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case TRAFFICLIGHTFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case SPEEDLIMITFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case RINGROADONELANEFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case RINGROADTWOLANESFILE:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case FLOWCONSERVINGBOTTLENECK:
            path = ".." + File.separator + "sim" + File.separator + "buildingBlocks" + File.separator;
            break;
        case RAMPMETERING:
            path = ".." + File.separator + "sim" + File.separator + "games" + File.separator;
            break;
        case ROUTING:
            path = ".." + File.separator + "sim" + File.separator + "games" + File.separator;
            break;
        case VASALOPPET:
            path = ".." + File.separator + "sim" + File.separator + "examples" + File.separator;
            break;
        default:
            path = "";
        }
        setupTrafficScenario(scenario, path);
    }

    /**
     * Sets up the given traffic scenario.
     * 
     * @param scenario
     */
    public void setupTrafficScenario(Scenario scenario, String path) {

        reset();

        if (this.scenario == scenario) {
            return; // TODO proper restart
        }
        switch (scenario) {
        case ONRAMPFILE: // TODO rg path
            simulator.loadScenarioFromXml("onramp", path);
            break;
        case OFFRAMPFILE:
            simulator.loadScenarioFromXml("offramp", path);
            break;
        case STARTSTOPFILE:
            simulator.loadScenarioFromXml("startStop_IDM", path);
            break;
        case CLOVERLEAFFILE:
            simulator.loadScenarioFromXml("cloverleaf", path);
            break;
        case LANECLOSINGFILE:
            simulator.loadScenarioFromXml("laneclosure", path);
            break;
        case TRAFFICLIGHTFILE:
            simulator.loadScenarioFromXml("trafficlight", path);
            break;
        case SPEEDLIMITFILE:
            simulator.loadScenarioFromXml("speedlimit", path);
            break;
        case RINGROADONELANEFILE:
            simulator.loadScenarioFromXml("ringroad_1lane", path);
            break;
        case RINGROADTWOLANESFILE:
            simulator.loadScenarioFromXml("ringroad_2lanes", path);
            break;
        case FLOWCONSERVINGBOTTLENECK:
            simulator.loadScenarioFromXml("flow_conserving_bottleneck", path);
            break;
        case RAMPMETERING:
            simulator.loadScenarioFromXml("ramp_metering", path);
            vehicleColorMode = TrafficCanvas.VehicleColorMode.EXIT_COLOR;
            break;
        case ROUTING:
            simulator.loadScenarioFromXml("routing", path);
            vehicleColorMode = TrafficCanvas.VehicleColorMode.EXIT_COLOR;
            break;
        case VASALOPPET:
            simulator.loadScenarioFromXml("vasa_CCS", path);
            break;
        default:
            // nothing to do
        }
        initGraphicSettings();
        forceRepaintBackground();
        this.scenario = scenario;
    }

    private void initGraphicSettings() {
        setProperties(loadProperties());
        initGraphicConfigFieldsFromProperties();
        resetScaleAndOffset();

        for (final RoadSegment segment : roadNetwork) {
            segment.roadMapping().setRoadColor(roadColor);
        }

        VehiclesInput vehiclesInput = simulator.getVehiclesInput();
        if (vehiclesInput == null) {
            System.out.println("vehiclesInput is null. cannot set vehicles' labelColors."); //$NON-NLS-1$
        } else {
            for (String vehicleTypeLabel : vehiclesInput.getVehicleInputMap().keySet()) {
                int r = (int) (Math.random() * 256);
                int g = (int) (Math.random() * 256);
                int b = (int) (Math.random() * 256);
                Color color = new Color(r, g, b);

                // final float hue = random.nextFloat();
                // final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
                // final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
                // Color color = Color.getHSBColor(hue, saturation, luminance);

                logger.info("set color for vehicle label={}", vehicleTypeLabel);
                labelColors.put(vehicleTypeLabel, color);
            }
        }
    }
}
