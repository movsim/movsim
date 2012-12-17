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
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;
import org.movsim.utilities.Units;
import org.movsim.viewer.util.StringHelper;
import org.movsim.viewer.util.SwingHelper;

public class StatusPanel extends JPanel implements SimulationRunnable.UpdateStatusCallback {

    private static final long serialVersionUID = 6663769351758390561L;

    private final Simulator simulator;
    private final SimulationRunnable simulationRunnable;

    private JProgressBar progressBar;
    
    private boolean withProgressBar = true;

    boolean isWithFiniteDurationAndProgressBar() {
        return withProgressBar && simulationRunnable.isFiniteDuration();
    }

    public void setWithProgressBar(boolean withProgressBar) {
        this.withProgressBar = withProgressBar;
    }

    private JLabel lblSimTime;
    private JLabel lblTimeDisplay;
    private JLabel lblDeltaTime;
    private JLabel lblDeltaTimeDisplay;
    private JLabel lblTimeWarp;
    private JLabel lblTimeWarpDisplay;

    private double time;

    private final ResourceBundle resourceBundle;

    private JLabel lblScenario;

    private JLabel lblCurrentScenario;

    private JLabel lblVehicleCount;

    private JLabel lblVehicleCountDisplay;

    private JLabel lblVehiclesMeanSpeed;

    private JLabel lblVehiclesMeanSpeedDisplay;
    
    private JLabel lblVehiclesStopped;

    private JLabel lblVehiclesStoppedDisplay;

    public StatusPanel(ResourceBundle resourceBundle, Simulator simulator) {
        this.resourceBundle = resourceBundle;
        this.simulator = simulator;
        this.simulationRunnable = simulator.getSimulationRunnable();
        this.setLayout(new FlowLayout());

        simulationRunnable.addUpdateStatusCallback(this);

        createStatusViews();
        addStatusView();
    }

    private void createStatusViews() {

        final Font font = new Font("Dialog", Font.PLAIN, 11);

        // current scenario
        lblScenario = new JLabel(resourceBundle.getString("lblScenario"));
        lblScenario.setFont(font);
        lblCurrentScenario = new JLabel("");
        lblCurrentScenario.setFont(font);
        
        lblCurrentScenario.setText(simulator.getProjectMetaData().hasProjectName() ? simulator.getProjectMetaData()
                .getProjectName() : resourceBundle.getString("lblUnspecifiedScenarioName"));
        lblCurrentScenario.setPreferredSize(new Dimension(100, 22));

        if (isWithFiniteDurationAndProgressBar()) {
            progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
            progressBar.setStringPainted(true);
            progressBar.setVisible(true);
        }

        // simulation time
        final String simTimeTooltip = resourceBundle.getString("simTimeTooltip");
        lblSimTime = new JLabel(resourceBundle.getString("lblSimTime"));
        lblSimTime.setFont(font);
        lblSimTime.setToolTipText(simTimeTooltip);

        lblTimeDisplay = new JLabel("0:00:00");
        lblTimeDisplay.setFont(font);
        lblTimeDisplay.setToolTipText(simTimeTooltip);
        SwingHelper.setComponentSize(lblTimeDisplay, 68, 22);

        // update time
        final String deltaTimeTooltip = resourceBundle.getString("deltaTimeTooltip");
        lblDeltaTime = new JLabel(resourceBundle.getString("lblDeltaTime"));
        lblDeltaTime.setFont(font);
        lblDeltaTime.setToolTipText(deltaTimeTooltip);

        lblDeltaTimeDisplay = new JLabel();
        lblDeltaTimeDisplay.setFont(font);
        lblDeltaTimeDisplay.setToolTipText(deltaTimeTooltip);
        SwingHelper.setComponentSize(lblDeltaTimeDisplay, 38, 22);

        // timewarp
        final String timeWarpTooltip = resourceBundle.getString("timeWarpTooltip");
        lblTimeWarp = new JLabel(resourceBundle.getString("lblTimeWarp"));
        lblTimeWarp.setFont(font);
        lblTimeWarp.setToolTipText(timeWarpTooltip);

        lblTimeWarpDisplay = new JLabel(String.valueOf(String.format("%.1f", simulationRunnable.getSmoothedTimewarp())));
        lblTimeWarpDisplay.setFont(font);
        lblTimeWarpDisplay.setToolTipText(timeWarpTooltip);
        lblTimeWarpDisplay.setPreferredSize(new Dimension(36, 22));

        // vehicle count
        final String vehicleCountTooltip = resourceBundle.getString("vehicleCountTooltip");
        lblVehicleCount = new JLabel(resourceBundle.getString("lblVehicleCount"));
        lblVehicleCount.setFont(font);
        lblVehicleCount.setToolTipText(vehicleCountTooltip);

        lblVehicleCountDisplay = new JLabel(String.valueOf(vehicleCount()));
        lblVehicleCountDisplay.setFont(font);
        lblVehicleCountDisplay.setToolTipText(vehicleCountTooltip);
        lblVehicleCountDisplay.setPreferredSize(new Dimension(36, 22));
        
        // average speed of vehicles
        final String vehiclesSpeedTooltip = resourceBundle.getString("vehiclesMeanSpeedTooltip");
        lblVehiclesMeanSpeed = new JLabel(resourceBundle.getString("lblVehiclesMeanSpeed"));
        lblVehiclesMeanSpeed.setFont(font);
        lblVehiclesMeanSpeed.setToolTipText(vehiclesSpeedTooltip);
        
        lblVehiclesMeanSpeedDisplay = new JLabel();
        lblVehiclesMeanSpeedDisplay.setFont(font);
        lblVehiclesMeanSpeedDisplay.setToolTipText(vehiclesSpeedTooltip);
        lblVehiclesMeanSpeedDisplay.setPreferredSize(new Dimension(36, 22));
        
        // number of stopped vehicles
        final String vehiclesStopppedTooltip = resourceBundle.getString("vehiclesStoppedTooltip");
        lblVehiclesStopped = new JLabel(resourceBundle.getString("lblVehiclesStopped"));
        lblVehiclesStopped.setFont(font);
        lblVehiclesStopped.setToolTipText(vehiclesStopppedTooltip);
        lblVehiclesStoppedDisplay = new JLabel();
        lblVehiclesStoppedDisplay.setFont(font);
        lblVehiclesStoppedDisplay.setToolTipText(vehiclesStopppedTooltip);
        lblVehiclesStoppedDisplay.setPreferredSize(new Dimension(36, 22));
        
    }

    private int vehicleCount() {
        return simulator.getRoadNetwork().vehicleCount();
    }
    
    private int stoppedVehicleCount() {
        return simulator.getRoadNetwork().getStoppedVehicleCount();
    }
    
    private int vehiclesMeanSpeedInKmh() {
        return (int)Math.round(Units.MS_TO_KMH*simulator.getRoadNetwork().vehiclesMeanSpeed());
    }

    public void addStatusView() {
        add(lblScenario);
        add(lblCurrentScenario);

        add(Box.createRigidArea(new Dimension(4, 22)));

        add(lblSimTime);
        add(lblTimeDisplay);

        add(Box.createRigidArea(new Dimension(4, 22)));
        add(lblTimeWarp);
        add(lblTimeWarpDisplay);

        add(Box.createRigidArea(new Dimension(4, 22)));

        add(lblDeltaTime);
        add(lblDeltaTimeDisplay);

        add(Box.createRigidArea(new Dimension(4, 22)));

        add(lblVehicleCount);
        add(lblVehicleCountDisplay);

        add(Box.createRigidArea(new Dimension(4, 22)));
        
        add(lblVehiclesMeanSpeed);
        add(lblVehiclesMeanSpeedDisplay);

        add(Box.createRigidArea(new Dimension(4, 22)));
        
        add(lblVehiclesStopped);
        add(lblVehiclesStoppedDisplay);

        add(Box.createRigidArea(new Dimension(4, 22)));

        if (isWithFiniteDurationAndProgressBar()) {
            add(progressBar);
        }
    }

    protected void setProgressBarDuration() {
        final int maxSimTime = (int) simulationRunnable.duration();
        if (withProgressBar && maxSimTime > 0) {
            progressBar.setMaximum(maxSimTime);
        }
    }
    
    @SuppressWarnings("hiding")
    public void notifyObserver(double time) {
        if (time != this.time) {
            final int intTime = (int) time;

            if (isWithFiniteDurationAndProgressBar()) {
                progressBar.setValue(intTime);
            }
            lblTimeDisplay.setText(StringHelper.getTime(time, true, true, true));
            lblDeltaTimeDisplay.setText(String.valueOf(String.format("%.1f", simulationRunnable.timeStep())));
            lblTimeWarpDisplay.setText(String.valueOf(String.format("%.1f", simulationRunnable.getSmoothedTimewarp())));

            lblVehicleCountDisplay.setText(String.valueOf(vehicleCount()));
            lblVehiclesMeanSpeedDisplay.setText(String.valueOf(vehiclesMeanSpeedInKmh()));
            
            lblVehiclesStoppedDisplay.setText(String.valueOf(stoppedVehicleCount()));

            this.time = time;
        }
    }

    public void reset() {
        removeAll();
        repaint();
        createStatusViews();
        addStatusView();
        setProgressBarDuration();
        notifyObserver(0);
        time = 0;
        validate(); // make visible after reset
    }

    @Override
    public void updateStatus(double simulationTime) {
        notifyObserver(simulationTime);
    }
}
