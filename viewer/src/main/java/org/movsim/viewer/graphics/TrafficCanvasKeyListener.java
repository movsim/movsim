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
package org.movsim.viewer.graphics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.viewer.graphics.TrafficCanvas.VehicleColorMode;

public class TrafficCanvasKeyListener extends TrafficCanvasController implements KeyListener {

    public TrafficCanvasKeyListener(TrafficCanvas trafficCanvas, RoadNetwork roadNetwork) {
        super(trafficCanvas, roadNetwork);
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
            // 'laneIndex Change'
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
