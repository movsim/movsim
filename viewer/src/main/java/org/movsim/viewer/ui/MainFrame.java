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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.graphics.TrafficCanvasScenarios.Scenario;
import org.movsim.viewer.util.SwingHelper;

@SuppressWarnings("synthetic-access")
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Scenario defaultScenario = Scenario.CLOVERLEAFFILE;
    private final int INIT_FRAME_SIZE_WIDTH = 1400;
    private final int INIT_FRAME_SIZE_HEIGHT = 640;

    final StatusPanel statusPanel;
    private final CanvasPanel canvasPanel;
    private MovSimToolBar toolBar;

    public MainFrame(ResourceBundle resourceBundle, ProjectMetaData projectMetaData) {
        super(resourceBundle.getString("FrameName"));

        SwingHelper.activateWindowClosingAndSystemExitButton(this);

        final Simulator simulator = new Simulator(projectMetaData);
        initLookAndFeel();

        canvasPanel = new CanvasPanel(resourceBundle, simulator);
        statusPanel = new StatusPanel(resourceBundle, simulator);

        addToolBar(resourceBundle);
        addMenu(resourceBundle);

        add(canvasPanel, BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                canvasPanel.resized();
                canvasPanel.repaint();
            }
        });

        setLocation(0, 20);
        setSize(INIT_FRAME_SIZE_WIDTH, INIT_FRAME_SIZE_HEIGHT);
        setVisible(true);


        // first scenario
        if (projectMetaData.getProjectName().equals("")) {
             canvasPanel.trafficCanvas.setupTrafficScenario(defaultScenario);
        } else {
            simulator.loadScenarioFromXml(projectMetaData.getProjectName(),
                    projectMetaData.getPathToProjectXmlFile());
            canvasPanel.trafficCanvas.reset();
            canvasPanel.trafficCanvas.start();
        }
        statusPanel.reset();
    }

    /**
     * @param resourceBundle
     */
    private void addToolBar(ResourceBundle resourceBundle) {
        toolBar = new MovSimToolBar(statusPanel, canvasPanel, resourceBundle);
    }

    /**
     * @param resourceBundle
     */
    private void addMenu(ResourceBundle resourceBundle) {
        final MovSimMenu trafficMenus = new MovSimMenu(this, canvasPanel, resourceBundle);
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
