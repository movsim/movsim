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

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.viewer.graphics.TrafficCanvas.VehicleColorMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficCanvasController {
    final static Logger logger = LoggerFactory.getLogger(TrafficCanvasController.class);
    final TrafficCanvas trafficCanvas;
    protected final RoadNetwork roadNetwork;

    public TrafficCanvasController(TrafficCanvas trafficCanvas, RoadNetwork roadNetwork) {
        this.trafficCanvas = trafficCanvas;
        this.roadNetwork = roadNetwork;
    }

    /**
     * Toggles the pause state.
     */
    public void commandTogglePause() {
        if (trafficCanvas.isPaused()) {
            if (trafficCanvas.vehicleTipWindow != null) {
                trafficCanvas.vehicleTipWindow.setVisible(false);
            }
            trafficCanvas.resume();
        } else if (trafficCanvas.isStopped() == false) {
            trafficCanvas.pause();
        }
        if (trafficCanvas.statusControlCallbacks != null) {
            trafficCanvas.statusControlCallbacks.stateChanged();
        }
    }

    public void commandZoomIn() {
        final double zoomFactor = Math.sqrt(2.0);
        trafficCanvas.setScale(trafficCanvas.scale() * zoomFactor);
        trafficCanvas.forceRepaintBackground();
    }

    public void commandZoomOut() {
        final double zoomFactor = Math.sqrt(2.0);
        trafficCanvas.setScale(trafficCanvas.scale() / zoomFactor);
        trafficCanvas.forceRepaintBackground();
    }

    public void commandRecenter() {
        trafficCanvas.resetScaleAndOffset();
        trafficCanvas.forceRepaintBackground();
    }

    public void commandFaster() {
        int sleepTime = trafficCanvas.sleepTime();
        sleepTime -= sleepTime <= 5 ? 1 : 5;
        if (sleepTime < 0) {
            sleepTime = 0;
        }
        trafficCanvas.setSleepTime(sleepTime);
        logger.debug("sleeptime: {}", trafficCanvas.sleepTime());
    }

    public void commandSlower() {
        int sleepTime = trafficCanvas.sleepTime();
        sleepTime += sleepTime < 5 ? 1 : 5;
        if (sleepTime > 400) {
            sleepTime = 400;
        }
        trafficCanvas.setSleepTime(sleepTime);
        logger.debug("sleeptime: {}", trafficCanvas.sleepTime());
    }

    public void commandReset() {
        trafficCanvas.stop();
        trafficCanvas.roadNetwork.clear();
        trafficCanvas.reset();
        trafficCanvas.start();
    }

    public void commandCycleVehicleColors() {
        if (trafficCanvas.accelerationColors == null) {
            trafficCanvas.setAccelerationColors();
        }
        // Cycle through the first ... vehicle color modes. This is the only place
        // where the use of an enum for the color mode is somewhat awkward.
        int vcmOrdinal = trafficCanvas.vehicleColorMode.ordinal() + 1;
        if (vcmOrdinal > TrafficCanvas.VehicleColorMode.EXIT_COLOR.ordinal()) {
            vcmOrdinal = 0;
        }
        trafficCanvas.vehicleColorMode = VehicleColorMode.values()[vcmOrdinal];
        logger.info("VehicleColorMode: {}", trafficCanvas.vehicleColorMode);
        trafficCanvas.repaint();
    }

    public void commandToggleVehicleColorMode(VehicleColorMode mode) {
        if (trafficCanvas.accelerationColors == null) {
            trafficCanvas.setAccelerationColors();
        }
        if (trafficCanvas.vehicleColorMode == mode) {
            trafficCanvas.vehicleColorMode = trafficCanvas.vehicleColorModeSave;
        } else {
            trafficCanvas.vehicleColorModeSave = trafficCanvas.vehicleColorMode;
            trafficCanvas.vehicleColorMode = mode;
        }
        logger.debug("VehicleColorMode: {}", trafficCanvas.vehicleColorMode); //$NON-NLS-1$
        trafficCanvas.repaint();
    }

    void commandReduceInflow() {
    }

    void commandIncreaseInflow() {
    }

    void commandToogleDrawJunctions() {
        trafficCanvas.drawRoadId = !trafficCanvas.drawRoadId;
        trafficCanvas.forceRepaintBackground();
    }

    void commandLowMemory() {
        trafficCanvas.pause();
        roadNetwork.onLowMemory();
        trafficCanvas.resume();
    }
}
