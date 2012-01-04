/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <movsim@akesting.de>
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
package org.movsim.viewer.graphics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.movsim.viewer.graphics.TrafficCanvas.VehicleColorMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficCanvasKeyListener implements KeyListener {

    final static Logger logger = LoggerFactory.getLogger(TrafficCanvasKeyListener.class);

    private final TrafficCanvas trafficCanvas;

    public TrafficCanvasKeyListener(TrafficCanvas trafficCanvas) {
        this.trafficCanvas = trafficCanvas;
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
        trafficCanvas.setScale(trafficCanvas.initialScale);
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
        trafficCanvas.reset();

    }

    public void commandVehicleChange() {
        trafficCanvas.toggleWithTreibisCars();
        trafficCanvas.repaint();
    }

    public void commandCycleVehicleColors() {
        if (trafficCanvas.velocities == null) {
            trafficCanvas.setVelocityColors();
        }
        // Cycle through the first four vehicle color modes. This is the only
        // place
        // where the use of an enum for the color mode is somewhat awkward.
        int vcmOrdinal = trafficCanvas.vehicleColorMode.ordinal() + 1;
        if (vcmOrdinal >= 4) {
            vcmOrdinal = 0;
        }
        trafficCanvas.vehicleColorMode = VehicleColorMode.values()[vcmOrdinal];
        logger.debug("VehicleColorMode: {}", trafficCanvas.vehicleColorMode);
        trafficCanvas.repaint();
    }

    public void commandToggleVehicleColorMode(VehicleColorMode mode) {
        if (trafficCanvas.velocities == null) {
            trafficCanvas.setVelocityColors();
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
        // roadNetwork.onLowMemory();
        trafficCanvas.resume();
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // mnemonics are given in comments for each key
        final char c = Character.toUpperCase(e.getKeyChar());
        if (c == KeyEvent.CHAR_UNDEFINED)
            return;
        switch (c) {
        case 'I':
        case '+':
        case '=':
            if (trafficCanvas.zoomingAllowed) {
                commandZoomIn();
            }
            break;
        case 'O':
        case '-':
        case '_':
            if (trafficCanvas.zoomingAllowed) {
                commandZoomOut();
            }
            break;
        case '0':
            if (trafficCanvas.zoomingAllowed) {
                commandRecenter();
            }
            break;
        case 'H':
            // 'highlight' vehicle
            commandToggleVehicleColorMode(VehicleColorMode.HIGHLIGHT_VEHICLE);
            break;
        case 'E':
            // 'exit color'
            commandToggleVehicleColorMode(VehicleColorMode.EXIT_COLOR);
            break;
        case 'V':
            // 'vehicle'
            commandCycleVehicleColors();
            break;
        case 'C':
            // 'lane Change'
            commandToggleVehicleColorMode(VehicleColorMode.LANE_CHANGE);
            break;
        case 'J':
            // 'junction'
            commandToogleDrawJunctions();
            break;
        case 'L':
            // 'less'
            commandReduceInflow();
            break;
        case 'M':
            // 'more'
            commandIncreaseInflow();
            break;
        case 'F':
            // 'faster'
            commandFaster();
            break;
        case 'S':
            // 'slower'
            commandSlower();
            break;
        case 'P':
            // 'pause'
            commandTogglePause();
            break;
        case 'Z':
            commandLowMemory();
            break;
        }
        e.consume();
    }
}
