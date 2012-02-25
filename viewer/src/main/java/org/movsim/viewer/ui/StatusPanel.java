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
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.viewer.util.StringHelper;
import org.movsim.viewer.util.SwingHelper;

public class StatusPanel extends JPanel implements SimulationRunnable.UpdateStatusCallback {

    private static final long serialVersionUID = 6663769351758390561L;

    private final Simulator simulator;
    private final SimulationRunnable simulationRunnable;

    private boolean isWithProgressBar = true;

    public boolean isWithProgressBar() {
        return isWithProgressBar;
    }

    public void setWithProgressBar(boolean isWithProgressBar) {
        this.isWithProgressBar = isWithProgressBar;
    }

    private JProgressBar progressBar;

    private JLabel lblSimTime;
    private JLabel lblTimeDisplay;
    private JLabel lblDeltaTime;
    private JLabel lblDeltaTimeDisplay;
    private JLabel lblTimeWarp;
    private JLabel lblTimeWarpDisplay;

    // travel times for TT scenario
    boolean withTravelTimes = false;

    public void setWithTravelTimes(boolean withTravelTimes) {
        this.withTravelTimes = withTravelTimes;
    }

    public boolean isWithTravelTimes() {
        return withTravelTimes;
    }

    private List<JLabel> lblTravelTimes;
    private List<JLabel> lblTravelTimeDisplays;

    private double time;

    private final ResourceBundle resourceBundle;

    private JLabel lblScenario;

    private JLabel lblCurrentScenario;

    private JLabel lblVehicleCount;

    private JLabel lblVehicleCountDisplay;

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
        lblCurrentScenario.setText(simulator.getProjectMetaData().getProjectName());
        lblCurrentScenario.setPreferredSize(new Dimension(100, 22));

        if (isWithProgressBar) {
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

        lblDeltaTimeDisplay = new JLabel(simulationRunnable.timeStep() + " s");
        lblDeltaTimeDisplay.setFont(font);
        lblDeltaTimeDisplay.setToolTipText(deltaTimeTooltip);
        SwingHelper.setComponentSize(lblDeltaTimeDisplay, 40, 22);

        // timewarp
        final String timeWarpTooltip = resourceBundle.getString("timeWarpTooltip");
        lblTimeWarp = new JLabel(resourceBundle.getString("lblTimeWarp"));
        lblTimeWarp.setFont(font);
        lblTimeWarp.setToolTipText(timeWarpTooltip);

        lblTimeWarpDisplay = new JLabel(String.valueOf(String.format("%.1f",simulationRunnable.getSmoothedTimewarp())));
        lblTimeWarpDisplay.setFont(font);
        lblTimeWarpDisplay.setToolTipText(timeWarpTooltip);
        lblTimeWarpDisplay.setPreferredSize(new Dimension(42, 22));
        
        // vehicle count
        final String vehicleCountTooltip = resourceBundle.getString("vehicleCountTooltip");
        lblVehicleCount = new JLabel(resourceBundle.getString("lblVehicleCount"));
        lblVehicleCount.setFont(font);
        lblVehicleCount.setToolTipText(vehicleCountTooltip);


        lblVehicleCountDisplay = new JLabel(String.valueOf(vehicleCount()));
        lblVehicleCountDisplay.setFont(font);
        lblVehicleCountDisplay.setToolTipText(vehicleCountTooltip);
        lblVehicleCountDisplay.setPreferredSize(new Dimension(42, 22));

        if (withTravelTimes) {
            createTravelTimeLabels(font);
        }

    }

    private int vehicleCount() {
        int vehicleCount = 0;
        for (final RoadSegment roadSegment : simulator.getRoadNetwork()) {
            vehicleCount += roadSegment.totalVehicleCount();
        }
        return vehicleCount;
    }

    private void createTravelTimeLabels(final Font f) {
        // hack
        lblTravelTimes = new LinkedList<JLabel>();
        lblTravelTimeDisplays = new LinkedList<JLabel>();

        for (int i = 0; i < 2; i++) {
            final String traveltimeTooltip = resourceBundle.getString("traveltimeTooltip") + i + ".";
            final JLabel tt = new JLabel(resourceBundle.getString("traveltime")
                    + ((i == 0) ? resourceBundle.getString("highway") : resourceBundle.getString("detour")) + ":");
            tt.setToolTipText(traveltimeTooltip);
            lblTravelTimes.add(tt);

            final JLabel ttD = new JLabel(String.valueOf(0));
            ttD.setFont(f);
            ttD.setToolTipText(traveltimeTooltip);
            ttD.setPreferredSize(new Dimension(80, 22));
            lblTravelTimeDisplays.add(ttD);
        }
    }

    public void addStatusView() {
        add(lblScenario);
        add(lblCurrentScenario);

        add(Box.createRigidArea(new Dimension(6, 22)));

        add(lblSimTime);
        add(lblTimeDisplay);

        add(Box.createRigidArea(new Dimension(6, 22)));
        add(lblTimeWarp);
        add(lblTimeWarpDisplay);

        add(Box.createRigidArea(new Dimension(6, 22)));

        add(lblDeltaTime);
        add(lblDeltaTimeDisplay);
        
        add(Box.createRigidArea(new Dimension(6, 22)));

        add(lblVehicleCount);
        add(lblVehicleCountDisplay);

        add(Box.createRigidArea(new Dimension(6, 22)));

        if (isWithProgressBar) {
            add(progressBar);
        }

        if (withTravelTimes) {
            for (int i = 0, N = lblTravelTimeDisplays.size(); i < N; i++) {
                add(Box.createRigidArea(new Dimension(6, 22)));
                add(lblTravelTimes.get(i));
                add(lblTravelTimeDisplays.get(i));
            }
        }
    }

    protected void setProgressBarDuration() {
        if (isWithProgressBar) {
            final int maxSimTime = (int) simulationRunnable.duration();
            progressBar.setMaximum(maxSimTime);
        }
    }

    public void notifyObserver(double time) {
        if (time > this.time) {
            final int intTime = (int) time;

            if (isWithProgressBar) {
                progressBar.setValue(intTime);
            }
            lblTimeDisplay.setText(StringHelper.getTime(time, true, true, true));
            lblDeltaTimeDisplay.setText(String.valueOf(simulationRunnable.timeStep()) + " s");
            lblTimeWarpDisplay.setText(String.valueOf(String.format("%.1f",simulationRunnable.getSmoothedTimewarp())));

            lblVehicleCountDisplay.setText(String.valueOf(vehicleCount()));

            // die TravelTimes haben eigentlich einen anderen notifier
            if (withTravelTimes) {
                final List<Double> dataTT = simulator.getTravelTimeDataEMAs(time);
                for (int i = 0, N = lblTravelTimeDisplays.size(); i < N; i++) {
                    lblTravelTimeDisplays.get(i).setText(String.format("%.1f min", dataTT.get(i) / 60.));
                }
            }
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
