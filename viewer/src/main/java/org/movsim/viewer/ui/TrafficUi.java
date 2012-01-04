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

package org.movsim.viewer.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import org.movsim.facades.MovsimViewerFacade;
import org.movsim.viewer.control.SimulationRunnable;
import org.movsim.viewer.graphics.GraphicsConfigurationParameters;
import org.movsim.viewer.graphics.TrafficCanvasKeyListener;
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.graphics.TrafficCanvasScenarios;
import org.movsim.viewer.graphics.TrafficCanvasScenarios.Scenario;
import org.movsim.viewer.util.LocalizationStrings;

/**
 * Traffic Simulation UI, contains the TrafficCanvas object which runs and draws the simulation. This UI can be used by either a Java Applet
 * or a Java MovsimMain.
 */
public class TrafficUi extends Component {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("nls")
    String helpText;

    private final Container container;
    TrafficCanvasScenarios trafficCanvas;
    SimulationRunnable simulationRunnable;
    MovsimViewerFacade movsimViewerFacade;

    String scenarioFilename;
    transient TrafficCanvas.StatusControlCallbacks statusCallbacks;
    private StatusPanel statusPanel;

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    // private InflowOutFlowControlPanel inflowControl;
    private JPanel controlPanel;
    private TrafficCanvasKeyListener controller;
    private ResourceBundle resourceBundle;

    public TrafficCanvasKeyListener getController() {
        return controller;
    }

    public TrafficUi(Container container) {
        super();
        this.container = container;
        this.setBackground(GraphicsConfigurationParameters.BACKGROUND_COLOR_SIM);
    }

    /**
     * Handle component resized event.
     */
    public void resized() {
        final Dimension dimensionStatusPanel = statusPanel.getPreferredSize();
        final int width = container.getSize().width;
        final int height = container.getSize().height - dimensionStatusPanel.height;
        trafficCanvas.setPreferredSize(new Dimension(width, height));
        trafficCanvas.setSize(width, height);
        trafficCanvas.requestFocusInWindow(); // give the canvas the keyboard focus
    }

    /**
     * Stop the simulation thread.
     */
    public void stop() {
        trafficCanvas.stop();
    }

    private void createGui(SimulationRunnable simulationRunnable, MovsimViewerFacade movsimViewerFacade) {

        this.simulationRunnable = simulationRunnable;
        this.movsimViewerFacade = movsimViewerFacade;

        initTrafficCanvas();

        this.controller = new TrafficCanvasKeyListener(trafficCanvas);

    }

    private void initTrafficCanvas() {

        assert movsimViewerFacade != null;
        assert simulationRunnable != null;

        trafficCanvas = new TrafficCanvasScenarios(simulationRunnable, movsimViewerFacade);
    }

    /**
     * Creates a GUI suitable for use by a Java Applet. The buttonpanel has two rows of buttons, the first row allows the user to control
     * the simulation (faster, slower etc), the second allows the user to choose a new traffic scenario.
     * 
     */
    public void createGuiForApplet(SimulationRunnable simulationRunnable, MovsimViewerFacade movsimViewerFacade) {

        createGui(simulationRunnable, movsimViewerFacade);

        resourceBundle = ResourceBundle.getBundle(LocalizationStrings.class.getName(), Locale.getDefault());
        initStrings(resourceBundle);

        layoutMainPanelAndCanvas();

        // first scenario
        trafficCanvas.setupTrafficScenario(Scenario.STARTSTOPFILE);
        getStatusPanel().setWithTravelTimes(false);
        getStatusPanel().setWithProgressBar(true);
        // removeInflowOutFlowControls();

        statusPanel.reset();
        // inflowControl.reset();

        statusPanel.setProgressBarDuration();

    }

    private void initStrings(ResourceBundle resourceBundle) {
        trafficCanvas.setMessageStrings((String) resourceBundle.getObject("VehiclePopup"), //$NON-NLS-1$
                (String) resourceBundle.getObject("VehiclePopupNoExit"), //$NON-NLS-1$
                (String) resourceBundle.getObject("TrafficInflow"), //$NON-NLS-1$
                (String) resourceBundle.getObject("RampingFinished"), //$NON-NLS-1$
                (String) resourceBundle.getObject("PerturbationApplied")); //$NON-NLS-1$
    }

    private void layoutMainPanelAndCanvas() {

        controlPanel = new JPanel();
        // inflowControl = new InflowOutFlowControlPanel();

        controlPanel.setLayout(new BorderLayout());

        controlPanel.setBackground(GraphicsConfigurationParameters.BACKGROUND_COLOR_SIM);

        statusPanel = new StatusPanel(resourceBundle);

        container.add(controlPanel, BorderLayout.NORTH);
        addStatusPanel();

        // controlPanel.add(inflowControl, BorderLayout.SOUTH);

        this.repaint();
    }

    // public void removeInflowOutFlowControls() {
    // controlPanel.remove(inflowControl);
    // }

    // public void addInFlowOutFlowControls() {
    // inflowControl = new InflowOutFlowControlPanel();
    // controlPanel.add(inflowControl, BorderLayout.SOUTH);
    // }

    public void quit() {
        if (trafficCanvas.isStopped() == false) {
            trafficCanvas.stop();
            // statusCallbacks.stateChanged();
        }
    }

    public void setDrawRoadId(boolean drawRoadId) {
        trafficCanvas.setDrawRoadId(drawRoadId);
    }

    public void removeStatusPanel() {
        container.remove(statusPanel);
        final Dimension dimensionControlPanel = controlPanel.getPreferredSize();

        final int width = container.getSize().width;
        final int height = container.getSize().height - dimensionControlPanel.height;
        trafficCanvas.setPreferredSize(new Dimension(width, height));
        container.add(trafficCanvas, BorderLayout.CENTER);
        trafficCanvas.setSize(width, height);
    }

    public void addStatusPanel() {
        final Dimension dimensionControlPanel = controlPanel.getPreferredSize();

        final Dimension dimensionStatusPanel = statusPanel.getPreferredSize();
        final int width = container.getSize().width;
        final int height = container.getSize().height - dimensionControlPanel.height - dimensionStatusPanel.height;
        trafficCanvas.setPreferredSize(new Dimension(width, height));
        container.add(trafficCanvas, BorderLayout.CENTER);
        trafficCanvas.setSize(width, height);
        container.setSize(width, height + dimensionControlPanel.height + dimensionStatusPanel.height);
        container.setVisible(true);
        container.add(statusPanel, BorderLayout.SOUTH);
        container.validate();
    }

}
