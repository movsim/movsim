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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.graphics.TrafficCanvas.StatusControlCallbacks;
import org.movsim.viewer.graphics.TrafficCanvasKeyListener;
import org.movsim.viewer.util.SwingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovSimToolBar extends JToolBar implements ActionListener {

    private static final long serialVersionUID = 1L;

    final static Logger logger = LoggerFactory.getLogger(MovSimToolBar.class);

    protected String newline = "\n";
    private final TrafficCanvasKeyListener controller;
    static final private String START = "toogle start/pause/resume";
    static final private String ZOOM_IN = "zoom in";
    static final private String ZOOM_OUT = "zoom out";
    static final private String FASTER = "faster";
    static final private String SLOWER = "slower";
    static final private String RECENTER = "recenter";
    static final private String VEHICLE_COLORS = "vehicle colors";
    static final private String RESET = "reset";
    static final private String VEHICLE_CHANGE = "vehicle change";

    JButton buttonStart;
    private final ResourceBundle resourceBundle;
    private StatusPanel statusPanel;

    public MovSimToolBar(StatusPanel statusPanel, final CanvasPanel canvasPanel, final ResourceBundle resourceBundle) {
        super(resourceBundle.getString("ToolBarTitle"));
        this.statusPanel = statusPanel;
        this.resourceBundle = resourceBundle;
        
        setRollover(true);
        controller = canvasPanel.controller;
        addButtons(this);
        addSeparator();
        add(statusPanel);

        final StatusControlCallbacks statusCallbacks = new TrafficCanvas.StatusControlCallbacks() {
            @Override
            public void showStatusMessage(String message) {
                // showStatus(message);
            }

            @Override
            @SuppressWarnings({ "synthetic-access" })
            public void stateChanged() {
                // final String buttonString;
                if (canvasPanel.trafficCanvas.isStopped()) {
                    // buttonString = resourceBundle.getString("Start");
                    buttonStart.setIcon(SwingHelper.createImageIcon(this.getClass(), "/images/" + "button_play"
                            + ".png", 32, 32));
                } else if (canvasPanel.trafficCanvas.isPaused()) {
                    // buttonString = resourceBundle.getString("Resume");
                    buttonStart.setIcon(SwingHelper.createImageIcon(this.getClass(), "/images/" + "button_play"
                            + ".png", 32, 32));
                } else {
                    // buttonString = resourceBundle.getString("Pause");
                    buttonStart.setIcon(SwingHelper.createImageIcon(this.getClass(), "/images/" + "button_pause"
                            + ".png", 32, 32));
                }
                // buttonStart.setText(buttonString);
            }
        };

        canvasPanel.trafficCanvas.setStatusControlCallbacks(statusCallbacks);
    }

    protected void addButtons(JToolBar toolBar) {
        JButton button = null;

        buttonStart = makeNavigationButton("button_pause", START, resourceBundle.getString("StartTip"),
                resourceBundle.getString("Start"));
        toolBar.add(buttonStart);

        button = makeNavigationButton("button_rew", SLOWER, resourceBundle.getString("SlowerTip"),
                resourceBundle.getString("Slower"));
        toolBar.add(button);
        button = makeNavigationButton("button_ffw", FASTER, resourceBundle.getString("FasterTip"),
                resourceBundle.getString("Faster"));
        toolBar.add(button);

        button = makeNavigationButton("button_repeat", RESET, resourceBundle.getString("ResetTip"),
                resourceBundle.getString("Reset"));
        toolBar.add(button);

        toolBar.addSeparator(new Dimension(20, 0));

        button = makeNavigationButton("locate", RECENTER, resourceBundle.getString("RecenterTip"),
                resourceBundle.getString("Recenter"));
        toolBar.add(button);

        button = makeNavigationButton("zoom_in", ZOOM_IN, resourceBundle.getString("ZoomInTip"),
                resourceBundle.getString("ZoomIn"));
        toolBar.add(button);
        button = makeNavigationButton("zoom_out", ZOOM_OUT, resourceBundle.getString("ZoomOutTip"),
                resourceBundle.getString("ZoomOut"));
        toolBar.add(button);

        toolBar.addSeparator(new Dimension(20, 0));

        button = makeNavigationButton("colors", VEHICLE_COLORS, resourceBundle.getString("VehicleColorsTip"),
                resourceBundle.getString("VehicleColors"));
        toolBar.add(button);
//        button = makeNavigationButton("vehicles", VEHICLE_CHANGE, resourceBundle.getString("VehiclesTip"),
//                resourceBundle.getString("Vehicles"));
//        toolBar.add(button);
    }

    protected JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText) {
        // Look for the image.
        final String imgLocation = "/images/" + imageName + ".png";
        final URL imageURL = MovSimToolBar.class.getResource(imgLocation);

        // Create and initialize the button.
        final JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);

        if (imageURL != null) { // image found
            button.setIcon(SwingHelper.createImageIcon(this.getClass(), imgLocation, 32, 32));
        } else { // no image found
            button.setText(altText);
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
        }
    }
}
