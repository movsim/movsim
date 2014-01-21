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
package org.movsim.viewer.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.graphics.TrafficCanvas.StatusControlCallbacks;
import org.movsim.viewer.graphics.TrafficCanvasController;
import org.movsim.viewer.util.SwingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovSimToolBar extends JToolBar implements ActionListener {

    private static final long serialVersionUID = 1L;

    final static Logger logger = LoggerFactory.getLogger(MovSimToolBar.class);

    protected String newline = "\n";
    private final TrafficCanvasController controller;
    static final private String START = "toogle start/pause/resume";
    static final private String ZOOM_IN = "zoom in";
    static final private String ZOOM_OUT = "zoom out";
    static final private String FASTER = "faster";
    static final private String SLOWER = "slower";
    static final private String RECENTER = "recenter";
    static final private String VEHICLE_COLORS = "vehicle colors";
    static final private String RESET = "reset";

    JButton buttonStart;
    private final StatusPanel statusPanel;
    private final StatusControlCallbacks statusCallbacks;

    public MovSimToolBar(StatusPanel statusPanel, final TrafficCanvas trafficCanvas, ResourceBundle resourceBundle) {
        super(resourceBundle.getString("ToolBarTitle"));
        this.statusPanel = statusPanel;

        setRollover(true);
        this.controller = trafficCanvas.controller();
        addButtons(this, resourceBundle);
        addSeparator();
        add(statusPanel);

        statusCallbacks = new TrafficCanvas.StatusControlCallbacks() {
            @Override
            public void showStatusMessage(String message) {
            }

            @Override
            public void stateChanged() {
                if (trafficCanvas.isStopped()) {
                    buttonStart.setIcon(SwingHelper.createImageIcon(this.getClass(), "/images/" + "button_play"
                            + ".png", 32, 32));
                } else if (trafficCanvas.isPaused()) {
                    buttonStart.setIcon(SwingHelper.createImageIcon(this.getClass(), "/images/" + "button_play"
                            + ".png", 32, 32));
                } else {
                    buttonStart.setIcon(SwingHelper.createImageIcon(this.getClass(), "/images/" + "button_pause"
                            + ".png", 32, 32));
                }
            }
        };

        trafficCanvas.setStatusControlCallbacks(statusCallbacks);
    }

    protected void addButtons(JToolBar toolBar, ResourceBundle resourceBundle) {
        buttonStart = createButton(resourceBundle, "button_pause", START, "StartTip", "Start");
        toolBar.add(buttonStart);

        toolBar.add(createButton(resourceBundle, "button_rew", SLOWER, "SlowerTip", "Slower"));
        toolBar.add(createButton(resourceBundle, "button_ffw", FASTER, "FasterTip", "Faster"));

        toolBar.add(createButton(resourceBundle, "button_repeat", RESET, "ResetTip", "Reset"));

        toolBar.addSeparator(new Dimension(20, 0));

        toolBar.add(createButton(resourceBundle, "locate", RECENTER, "RecenterTip", "Recenter"));

        toolBar.add(createButton(resourceBundle, "zoom_in", ZOOM_IN, "ZoomInTip", "ZoomIn"));
        toolBar.add(createButton(resourceBundle, "zoom_out", ZOOM_OUT, "ZoomOutTip", "ZoomOut"));

        toolBar.add(createButton(resourceBundle, "button_vehicle_colors", VEHICLE_COLORS, "VehicleColorsTip",
                "VehicleColors"));
    }

    protected JButton createButton(ResourceBundle resourceBundle, String imageName, String actionCommand,
            String toolTipResource, String textResource) {
        // Look for the image.
        final String imgLocation = "/images/" + imageName + ".png";
        final URL imageURL = MovSimToolBar.class.getResource(imgLocation);

        // Create and initialize the button.
        final JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(resourceBundle.getString(toolTipResource));
        button.addActionListener(this);

        if (imageURL != null) { // image found
            button.setIcon(SwingHelper.createImageIcon(this.getClass(), imgLocation, 32, 32));
        } else { // no image found
            button.setText(resourceBundle.getString(textResource));
            logger.error("Resource not found: ", imgLocation);
        }
        return button;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        logger.info("action command: {}", e.getActionCommand());
        if (e.getActionCommand().equals(ZOOM_IN)) {
            controller.commandZoomIn();
        } else if (e.getActionCommand().equals(ZOOM_OUT)) {
            controller.commandZoomOut();
        } else if (e.getActionCommand().equals(FASTER)) {
            controller.commandFaster();
        } else if (e.getActionCommand().equals(SLOWER)) {
            controller.commandSlower();
        } else if (e.getActionCommand().equals(RECENTER)) {
            controller.commandRecenter();
        } else if (e.getActionCommand().equals(START)) {
            controller.commandTogglePause();
        } else if (e.getActionCommand().equals(VEHICLE_COLORS)) {
            controller.commandCycleVehicleColors();
        } else if (e.getActionCommand().equals(RESET)) {
            statusPanel.reset();
            controller.commandReset();
            statusCallbacks.stateChanged();
        }
    }
}
