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
package org.movsim.viewer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.movsim.viewer.graphics.TrafficCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanvasPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(CanvasPanel.class);

    private static final long serialVersionUID = 1L;

    private TrafficCanvas trafficCanvas;

    public CanvasPanel(final ResourceBundle resourceBundle, final TrafficCanvas trafficCanvas) {
        // TODO check if needed anymore. Seems working fine with linux. Check windows and mac!
        // SwingHelper.makeLightWeightComponentsVisible();
        try {
            // Execute a job on the event-dispatching thread; creating this applet's GUI.
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    initApp(resourceBundle, trafficCanvas);
                }
            });
        } catch (final Exception e) {
            LOG.error("initApp didn't complete successfully", e); //$NON-NLS-1$
        }
    }

    public void initApp(ResourceBundle resourceBundle, TrafficCanvas trafficCanvas1) {
        this.trafficCanvas = trafficCanvas1;
        initStrings(resourceBundle);
        layoutAndAddCanvasToPanel();
    }

    /**
     * Handle component resized event.
     */
    public void resized() {
        final int width = this.getSize().width;
        final int height = this.getSize().height;
        trafficCanvas.setPreferredSize(new Dimension(width, height));
        trafficCanvas.setSize(width, height);
        trafficCanvas.requestFocusInWindow(); // give the canvas the keyboard focus
    }

    private void initStrings(ResourceBundle resourceBundle) {
        trafficCanvas.setMessageStrings((String) resourceBundle.getObject("VehiclePopup"), //$NON-NLS-1$
                (String) resourceBundle.getObject("VehiclePopupNoExit"), //$NON-NLS-1$
                (String) resourceBundle.getObject("TrafficInflow"), //$NON-NLS-1$
                (String) resourceBundle.getObject("RampingFinished"), //$NON-NLS-1$
                (String) resourceBundle.getObject("PerturbationApplied"),
                (String) resourceBundle.getObject("SimulationFinished")); //$NON-NLS-1$
    }

    private void layoutAndAddCanvasToPanel() {
        final int width = this.getSize().width;
        final int height = this.getSize().height;
        trafficCanvas.setPreferredSize(new Dimension(width, height));
        add(trafficCanvas, BorderLayout.CENTER);
        trafficCanvas.setSize(width, height);
        repaint();
    }

    public void quit() {
        if (trafficCanvas.isStopped()) {
            trafficCanvas.stop();
        }
    }
}
