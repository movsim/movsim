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

import org.movsim.facades.MovsimViewerFacade;
import org.movsim.viewer.control.SimulationRunnable;
import org.movsim.viewer.graphics.GraphicsConfigurationParameters;
import org.movsim.viewer.util.StringHelper;
import org.movsim.viewer.util.SwingHelper;

/**
 * @author ralph
 * 
 */
public class StatusPanel extends JPanel implements SimulationRunnable.UpdateStatusPanelCallback {

    private static final long serialVersionUID = 6663769351758390561L;

    private final MovsimViewerFacade movsimViewerFacade;
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

    public StatusPanel(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        this.movsimViewerFacade = MovsimViewerFacade.getInstance();
        this.simulationRunnable = SimulationRunnable.getInstance();
        this.setLayout(new FlowLayout());
        this.setBackground(GraphicsConfigurationParameters.BACKGROUND_COLOR_SIM);

        simulationRunnable.setUpdateStatusPanelCallback(this);

        createStatusViews();
        addStatusView();
    }

    private void createStatusViews() {
        // current scenario
        lblScenario = new JLabel(resourceBundle.getString("lblScenario"));
        lblCurrentScenario = new JLabel("");
        lblCurrentScenario.setText(movsimViewerFacade.getProjectMetaData().getProjectName());
        lblCurrentScenario.setPreferredSize(new Dimension(200, 22));

        if (isWithProgressBar) {
            progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
            progressBar.setStringPainted(true);
            progressBar.setVisible(true);
        }

        final Font font = new Font("Dialog", Font.BOLD, 12);

        // simulation time
        final String simTimeTooltip = resourceBundle.getString("simTimeTooltip");
        lblSimTime = new JLabel(resourceBundle.getString("lblSimTime"));
        lblSimTime.setToolTipText(simTimeTooltip);

        lblTimeDisplay = new JLabel("0:00:00");
        lblTimeDisplay.setFont(font);
        lblTimeDisplay.setToolTipText(simTimeTooltip);
        SwingHelper.setComponentSize(lblTimeDisplay, 68, 22);

        // update time
        final String deltaTimeTooltip = resourceBundle.getString("deltaTimeTooltip");
        lblDeltaTime = new JLabel(resourceBundle.getString("lblDeltaTime"));
        lblDeltaTime.setToolTipText(deltaTimeTooltip);

        lblDeltaTimeDisplay = new JLabel(simulationRunnable.timeStep() + " s");
        lblDeltaTimeDisplay.setFont(font);
        lblDeltaTimeDisplay.setToolTipText(deltaTimeTooltip);
        SwingHelper.setComponentSize(lblDeltaTimeDisplay, 40, 22);

        // timewarp
        final String timeWarpTooltip = resourceBundle.getString("timeWarpTooltip");
        lblTimeWarp = new JLabel(resourceBundle.getString("lblTimeWarp"));
        lblTimeWarp.setToolTipText(timeWarpTooltip);

        lblTimeWarpDisplay = new JLabel(String.valueOf(simulationRunnable.getSmoothedTimewarp()));
        lblTimeWarpDisplay.setFont(font);
        lblTimeWarpDisplay.setToolTipText(timeWarpTooltip);
        lblTimeWarpDisplay.setPreferredSize(new Dimension(42, 22));

        if (withTravelTimes) {
            createTravelTimeLabels(font);
        }

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

        if (isWithProgressBar) {
            add(progressBar);
        }

        add(Box.createRigidArea(new Dimension(6, 22)));

        add(lblSimTime);
        add(lblTimeDisplay);

        add(Box.createRigidArea(new Dimension(6, 22)));
        add(lblTimeWarp);
        add(lblTimeWarpDisplay);

        add(Box.createRigidArea(new Dimension(6, 22)));

        add(lblDeltaTime);
        add(lblDeltaTimeDisplay);

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
            final int maxSimTime = (int) movsimViewerFacade.getMaxSimTime();
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
            lblTimeWarpDisplay.setText(String.valueOf(simulationRunnable.getSmoothedTimewarp()));

            // die TravelTimes haben eigentlich einen anderen notifier
            if (withTravelTimes) {
                final List<Double> dataTT = movsimViewerFacade.getTravelTimeDataEMAs(time);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.web.appletroad.control.SimulationRunnable.UpdateStatusPanelCallback#updateTravelTime(double)
     */
    @Override
    public void updateStatusPanel(double simulationTime) {
        notifyObserver(simulationTime);
    }
}
