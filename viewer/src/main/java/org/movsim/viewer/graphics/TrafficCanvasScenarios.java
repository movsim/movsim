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

    public final static String ONRAMPFILE= "onramp";
    public final static String STARTSTOPFILE ="startStop_IDM";
    public final static String CLOVERLEAFFILE ="cloverleaf";
    public final static String OFFRAMPFILE ="offramp";
    public final static String LANECLOSINGFILE ="laneclosure";
    public final static String TRAFFICLIGHTFILE ="trafficlight";
    public final static String SPEEDLIMITFILE ="speedlimit";
    public final static String RINGROADONELANEFILE ="ringroad_1lane";
    public final static String RINGROADTWOLANESFILE ="ringroad_2lanes";
    public final static String FLOWCONSERVINGBOTTLENECK ="flow_conserving_bottleneck";
    public final static String RAMPMETERING ="ramp_metering";
    public final static String ROUTING ="routing";
    public final static String VASALOPPET = "vasa_CCS";

    //private Scenario scenario = Scenario.NONE;

    // speed boost to get vehicles onto network quickly
    private boolean isInitialSpeedUp;
    private double speedupEndTime;
    private int sleepTimeSave;
    private String simulationFinished;

    public TrafficCanvasScenarios(Simulator simulator) {
        super(simulator);
    }

    public void setMessageStrings(String popupString, String popupStringExitEndRoad, String trafficInflowString,
            String perturbationRampingFinishedString, String perturbationAppliedString, String simulationFinished) {
        setMessageStrings(popupString, popupStringExitEndRoad);
        this.simulationFinished = simulationFinished;
    }

    @Override
    public void updateStatus(double simulationTime) {
        if (isInitialSpeedUp && simulationTime > speedupEndTime) {
            isInitialSpeedUp = false;
            setSleepTime(sleepTimeSave);
        }
        if (simulator.isFinished() && simulationFinished != null) {
            final double totalVehicleTravelTime = roadNetwork.totalVehicleTravelTime();
            final double totalVehicleTravelDistance = roadNetwork.totalVehicleTravelDistance() /1000.0;
            JOptionPane.showMessageDialog(null, String.format(simulationFinished, (int)simulationTime,
                    (int)totalVehicleTravelTime, (int)totalVehicleTravelDistance));
            simulationRunnable.stop();
        }
    }

    /**
     * After the vehicles have been drawn, update any simulation , such as density diagrams and traffic lights.
     */
    @Override
    protected void drawAfterVehiclesMoved(Graphics2D g, double simulationTime, long iterationCount) {

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
     * Sets up the given traffic scenario.
     * 
     * @param scenario
     */
    public void setupTrafficScenario(String scenario, String path) {
        reset();
        simulator.loadScenarioFromXml(scenario, path);
        initGraphicSettings();
        forceRepaintBackground();
    }

    private void initGraphicSettings() {
        setProperties(loadProperties());
        initGraphicConfigFieldsFromProperties();
        resetScaleAndOffset();
        for (final RoadSegment roadSegment : roadNetwork) {
            roadSegment.roadMapping().setRoadColor(roadColor);
        }
        VehiclesInput vehiclesInput = simulator.getVehiclesInput();
        if (vehiclesInput == null) {
            System.out.println("vehiclesInput is null. cannot set vehicles' labelColors."); //$NON-NLS-1$
        } else {
            for (String vehicleTypeLabel : vehiclesInput.getVehicleInputMap().keySet()) {
                final int r = (int) (Math.random() * 256);
                final int g = (int) (Math.random() * 256);
                final int b = (int) (Math.random() * 256);
                final Color color = new Color(r, g, b);
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
