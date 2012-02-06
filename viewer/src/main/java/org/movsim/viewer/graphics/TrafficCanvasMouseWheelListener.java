/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                             <movsim.org@gmail.com>
 * ---------------------------------------------------------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MovSim.
 *  If not, see <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 *  
 * ---------------------------------------------------------------------------------------------------------------------
 */
package org.movsim.viewer.graphics;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficCanvasMouseWheelListener implements MouseWheelListener {

    final static Logger logger = LoggerFactory.getLogger(TrafficCanvasMouseWheelListener.class);

    private final TrafficCanvas trafficCanvas;

    /**
     * @param trafficCanvas
     */
    public TrafficCanvasMouseWheelListener(TrafficCanvas trafficCanvas) {
        this.trafficCanvas = trafficCanvas;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event. MouseWheelEvent)
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        String message;
        final int notches = e.getWheelRotation();
        if (notches < 0) {
            message = "Mouse wheel moved UP " + -notches + " notch(es)";
            commandZoomIn();
        } else {
            message = "Mouse wheel moved DOWN " + notches + " notch(es)";
            commandZoomOut();
        }
        saySomething(message, e);
    }

    /**
     * @param message
     * @param e
     */
    private void saySomething(String message, MouseWheelEvent e) {
        logger.info(message);
    }

    private void commandZoomIn() {
        final double zoomFactor = Math.sqrt(2.0);
        trafficCanvas.setScale(trafficCanvas.scale() * zoomFactor);
        trafficCanvas.forceRepaintBackground();
    }

    private void commandZoomOut() {
        final double zoomFactor = Math.sqrt(2.0);
        trafficCanvas.setScale(trafficCanvas.scale() / zoomFactor);
        trafficCanvas.forceRepaintBackground();
    }
}
