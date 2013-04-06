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
import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.App;
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.util.SwingHelper;

@SuppressWarnings("synthetic-access")
public class AppFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final CanvasPanel canvasPanel;
    final StatusPanel statusPanel;
    private MovSimToolBar toolBar;

    public AppFrame(ResourceBundle resourceBundle, ProjectMetaData projectMetaData, Properties properties) {
        super(resourceBundle.getString("FrameName"));

        SwingHelper.activateWindowClosingAndSystemExitButton(this);

        final Simulator simulator = new Simulator();
        initLookAndFeel();

        final TrafficCanvas trafficCanvas = new TrafficCanvas(simulator, properties);
        canvasPanel = new CanvasPanel(resourceBundle, trafficCanvas);
        statusPanel = new StatusPanel(resourceBundle, simulator);
        toolBar = new MovSimToolBar(statusPanel, trafficCanvas, resourceBundle);

        addMenu(resourceBundle, simulator, trafficCanvas, properties);
        add(canvasPanel, BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                canvasPanel.resized();
                canvasPanel.repaint();
            }
        });

        initFrameSize(properties);

        if (projectMetaData.hasProjectName()) {
            trafficCanvas.setupTrafficScenario(projectMetaData.getProjectName(),
                    projectMetaData.getPathToProjectFile());
        } else {
            System.out.println("try to load default");
            final String path = "sim/buildingBlocks/";
            URL project = App.class.getClassLoader().getResource(path+"onramp.xml");
            URL projectPath = App.class.getClassLoader().getResource(path);
            File file = new File(project.getFile());  // TODO use file, working in applet?
            System.out.println("file exists = "+file.exists());
            System.out.println("project = "+project.getFile());
            trafficCanvas.setupTrafficScenario(file.getName(), projectPath.getFile());
        }
        
        statusPanel.reset();
        trafficCanvas.start();
        setVisible(true);

        boolean isGame = Boolean.parseBoolean(properties.getProperty("isGame"));
        if (isGame) {
            HighscoreFrame.initialize(resourceBundle, simulator, properties);
        }
    }

    private void initFrameSize(Properties properties) {
        int xPixSize = Integer.parseInt(properties.getProperty("xPixSizeWindow"));
        int yPixSize = Integer.parseInt(properties.getProperty("yPixSizeWindow"));
        if (xPixSize < 0 || yPixSize < 0) {
            setExtendedState(Frame.MAXIMIZED_BOTH);
        } else {
            setSize(xPixSize, yPixSize);
        }
    }

    private void addMenu(ResourceBundle resourceBundle, Simulator simulator, TrafficCanvas trafficCanvas,
            Properties properties) {
        final AppMenu trafficMenus = new AppMenu(this, simulator, canvasPanel, trafficCanvas, resourceBundle,
                properties);
        trafficMenus.initMenus();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
