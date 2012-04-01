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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.graphics.TrafficCanvasScenarios;
import org.movsim.viewer.graphics.TrafficCanvasScenarios.Scenario;
import org.movsim.viewer.util.SwingHelper;

@SuppressWarnings("synthetic-access")
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private Scenario defaultScenario = Scenario.CLOVERLEAFFILE;

    private final CanvasPanel canvasPanel;
    final StatusPanel statusPanel;
    private MovSimToolBar toolBar;

    public MainFrame(ResourceBundle resourceBundle, ProjectMetaData projectMetaData) {
        super(resourceBundle.getString("FrameName"));

        SwingHelper.activateWindowClosingAndSystemExitButton(this);

        final Simulator simulator = new Simulator(projectMetaData);
        initLookAndFeel();

        final TrafficCanvasScenarios trafficCanvas = new TrafficCanvasScenarios(simulator);
        canvasPanel = new CanvasPanel(resourceBundle, trafficCanvas);
        statusPanel = new StatusPanel(resourceBundle, simulator);

        addToolBar(resourceBundle, trafficCanvas);
        addMenu(resourceBundle, simulator, trafficCanvas);

        add(canvasPanel, BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                canvasPanel.resized();
                canvasPanel.repaint();
            }
        });

        this.setExtendedState(Frame.MAXIMIZED_BOTH);

        // first scenario
        if (projectMetaData.getProjectName().equals("")) {
            trafficCanvas.setupTrafficScenario(defaultScenario);
        } else {
            simulator.loadScenarioFromXml(projectMetaData.getProjectName(), projectMetaData.getPathToProjectXmlFile());
            trafficCanvas.reset();
        }
        statusPanel.reset();
        trafficCanvas.start();
        setVisible(true);
    }

    /**
     * @param resourceBundle
     */
    private void addToolBar(ResourceBundle resourceBundle, TrafficCanvasScenarios trafficCanvas) {
        toolBar = new MovSimToolBar(statusPanel, trafficCanvas, resourceBundle);
    }

    /**
     * @param resourceBundle
     */
    private void addMenu(ResourceBundle resourceBundle, Simulator simulator, TrafficCanvasScenarios trafficCanvas) {
        final MovSimMenu trafficMenus = new MovSimMenu(this, simulator, canvasPanel, trafficCanvas, resourceBundle);
        trafficMenus.initMenus();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("set to system LaF");
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);
    }
}
