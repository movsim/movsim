/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim@akesting.de>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */

package org.movsim.viewer.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EventObject;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.movsim.facades.MovsimViewerFacade;
import org.movsim.viewer.graphics.GraphicsConfigurationParameters;
import org.movsim.viewer.graphics.TrafficCanvasScenarios.Scenario;
import org.movsim.viewer.util.SwingHelper;

public class MovSimMenu extends JPanel {
    private static final long serialVersionUID = -1741830983719200790L;
    CanvasPanel canvasPanel;
    private final ResourceBundle resourceBundle;
    // protected TravelTimeDiagram travelTimeDiagram;
    private LogWindow logWindow;
    private MainFrame frame;

    // private DetectorsView detectorsDiagram;
    // private SpatioTemporalView spatioTemporalDiagram;
    // private FloatingCarsAccelerationView fcAcc;
    // private FloatingCarsSpeedView fcSpeed;
    // private FloatingCarsTrajectoriesView fcTrajectories;

    public MovSimMenu(MainFrame mainFrame, CanvasPanel canvasPanel, ResourceBundle resourceBundle) {
        this.frame = mainFrame;
        this.canvasPanel = canvasPanel;
        this.resourceBundle = resourceBundle;
    }

    public void initMenus() {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu menuFile = fileMenu();
        final JMenu scenarioMenu = scenarioMenu();
        final JMenu helpMenu = helpMenu();
        final JMenu modelMenu = modelMenu();
        final JMenu viewMenu = viewMenu();
        final JMenu outputMenu = outputMenu();

        menuBar.add(menuFile);
        menuBar.add(scenarioMenu);
        menuBar.add(modelMenu);
        menuBar.add(outputMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);
    }

    private JMenu outputMenu() {
        final JMenu outputMenu = new JMenu((String) resourceBundle.getObject("OutputMenu"));

        final JCheckBoxMenuItem menuItemTravelTime = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("TravelTime")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleTravelTimeDiagram(actionEvent);
                    }
                });
        outputMenu.add(menuItemTravelTime);
        final JCheckBoxMenuItem menuItemDetectors = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("Detectors")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDetectorsDiagram(actionEvent);
                    }
                });
        outputMenu.add(menuItemDetectors);
        final JCheckBoxMenuItem menuItemFloatingCars = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("FloatingCars")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleFloatingCarsDiagram(actionEvent);
                    }
                });
        outputMenu.add(menuItemFloatingCars);
        final JCheckBoxMenuItem menuItemSpatioTemporalContour = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("SpatioTemporal")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleSpatioTemporalDiagram(actionEvent);
                    }
                });
        outputMenu.add(menuItemSpatioTemporalContour);
        final JCheckBoxMenuItem menuItemFuelConsumption = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("FuelConsumption")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleFuelConsumptionDiagram(actionEvent);
                    }
                });
        outputMenu.add(menuItemFuelConsumption);

        menuItemDetectors.setEnabled(false);
        menuItemFloatingCars.setEnabled(false);
        menuItemFuelConsumption.setEnabled(false);
        menuItemSpatioTemporalContour.setEnabled(false);
        menuItemTravelTime.setEnabled(false);

        return outputMenu;
    }

    private JMenu scenarioMenu() {
        final JMenu scenarioMenu = new JMenu((String) resourceBundle.getObject("ScenarioMenu"));
        final JMenuItem menuItemOnRamp = new JMenuItem(new AbstractAction(resourceBundle.getString("OnRamp")) {

            private static final long serialVersionUID = 7705041304742695628L;

            @Override
            public void actionPerformed(ActionEvent e) {
                canvasPanel.trafficCanvas.setupTrafficScenario(Scenario.ONRAMPFILE);
            }
        });
        scenarioMenu.add(menuItemOnRamp);

        final JMenuItem menuItemOffRamp = new JMenuItem(new AbstractAction(resourceBundle.getString("OffRamp")) {

            private static final long serialVersionUID = -2548920811907898064L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        });
        scenarioMenu.add(menuItemOffRamp);

        final JMenuItem menuItemFlowConservingBottleNeck = new JMenuItem(new AbstractAction(
                resourceBundle.getString("FlowConservingBottleNeck")) {

            private static final long serialVersionUID = -8349549625085281487L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        });
        scenarioMenu.add(menuItemFlowConservingBottleNeck);

        final JMenuItem menuItemSpeedLimit = new JMenuItem(new AbstractAction(resourceBundle.getString("SpeedLimit")) {

            private static final long serialVersionUID = -1498474459807551133L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        });
        scenarioMenu.add(menuItemSpeedLimit);

        final JMenuItem menuItemTrafficLight = new JMenuItem(new AbstractAction(
                resourceBundle.getString("TrafficLight")) {

            private static final long serialVersionUID = 2511854387728111343L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        });
        scenarioMenu.add(menuItemTrafficLight);

        final JMenuItem menuItemLaneClosing = new JMenuItem(
                new AbstractAction(resourceBundle.getString("LaneClosing")) {

                    private static final long serialVersionUID = -5359478839829791298L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SwingHelper.notImplemented(canvasPanel);
                    }
                });
        scenarioMenu.add(menuItemLaneClosing);

        final JMenuItem menuItemCloverLeaf = new JMenuItem(new AbstractAction(resourceBundle.getString("CloverLeaf")) {

            private static final long serialVersionUID = 8504921708742771452L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        });
        scenarioMenu.add(menuItemCloverLeaf);

        final JMenuItem menuItemRoundAbout = new JMenuItem(new AbstractAction(resourceBundle.getString("RoundAbout")) {

            private static final long serialVersionUID = 5468084978732943923L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        });
        scenarioMenu.add(menuItemRoundAbout);

        final JMenuItem menuItemCityInterSection = new JMenuItem(new AbstractAction(
                resourceBundle.getString("CityInterSection")) {

            private static final long serialVersionUID = 3606709421278067399L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        });
        scenarioMenu.add(menuItemCityInterSection);

        final JMenuItem menuItemRingRoad = new JMenuItem(new AbstractAction(resourceBundle.getString("RingRoad")) {

            private static final long serialVersionUID = 4633365854029111923L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        });
        scenarioMenu.add(menuItemRingRoad);

        menuItemOffRamp.setEnabled(false);
        menuItemFlowConservingBottleNeck.setEnabled(false);
        menuItemSpeedLimit.setEnabled(false);
        menuItemTrafficLight.setEnabled(false);
        menuItemLaneClosing.setEnabled(false);
        menuItemCloverLeaf.setEnabled(false);
        menuItemRoundAbout.setEnabled(false);
        menuItemCityInterSection.setEnabled(false);
        menuItemRingRoad.setEnabled(false);

        return scenarioMenu;
    }

    private JMenu viewMenu() {
        final JMenu viewMenu = new JMenu((String) resourceBundle.getObject("ViewMenu"));
        final JMenu colorVehicles = new JMenu(resourceBundle.getString("VehicleColors"));

        final JMenuItem menuItemVehicleColorSpeedDependant = new JMenuItem(
                resourceBundle.getString("VehicleColorSpeedDependant"));
        final JMenuItem menuItemVehicleColorRandom = new JMenuItem(resourceBundle.getString("VehicleColorRandom"));
        colorVehicles.add(menuItemVehicleColorSpeedDependant);
        colorVehicles.add(menuItemVehicleColorRandom);

        menuItemVehicleColorSpeedDependant.setEnabled(false);
        menuItemVehicleColorRandom.setEnabled(false);

        viewMenu.add(colorVehicles);
        viewMenu.addSeparator();

        final JCheckBoxMenuItem cbStatusPanel = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("StatusPanel")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDisplayStatusPanel(actionEvent);
                    }
                });
        cbStatusPanel.setSelected(true);
        viewMenu.add(cbStatusPanel);
        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction((String) resourceBundle.getObject("LogOutput")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleLogOutput(actionEvent);
                    }
                }));

        viewMenu.addSeparator();
        final JCheckBoxMenuItem cbDrawRoadIds = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawRoadIds")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDrawRoadIds(actionEvent);
                    }
                });
        viewMenu.add(cbDrawRoadIds);

        final JCheckBoxMenuItem cbSources = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawSources")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                });
        viewMenu.add(cbSources);

        final JCheckBoxMenuItem cbSinks = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawSinks")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                });
        viewMenu.add(cbSinks);

        final JCheckBoxMenuItem cbSpeedLimits = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawSpeedLimits")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                });
        viewMenu.add(cbSpeedLimits);

        final JCheckBoxMenuItem cbflowConservingBootleNecks = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawFlowConservingBootleNecks")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                });
        viewMenu.add(cbflowConservingBootleNecks);

        viewMenu.addSeparator();

        final JCheckBoxMenuItem cbRoutesTravelTimes = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawRoutesTravelTime")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                });
        viewMenu.add(cbRoutesTravelTimes);

        final JCheckBoxMenuItem cbRoutesSpatioTemporal = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawRoutesSpatioTemporal")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                });
        viewMenu.add(cbRoutesSpatioTemporal);

        cbRoutesSpatioTemporal.setEnabled(false);
        cbSources.setEnabled(false);
        cbSinks.setEnabled(false);
        cbflowConservingBootleNecks.setEnabled(false);
        cbRoutesSpatioTemporal.setEnabled(false);
        cbRoutesTravelTimes.setEnabled(false);
        cbSpeedLimits.setEnabled(false);

        cbDrawRoadIds.setSelected(GraphicsConfigurationParameters.DRAW_ROADID);
        return viewMenu;
    }

    private JMenu modelMenu() {
        final JMenu modelMenu = new JMenu((String) resourceBundle.getObject("ModelMenu"));
        final JMenuItem menuItemModelParameters = new JMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("ModelMenuViewParams")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        SwingHelper.notImplemented(canvasPanel);
                    }
                });
        modelMenu.add(menuItemModelParameters);

        menuItemModelParameters.setEnabled(false);
        return modelMenu;
    }

    private JMenu helpMenu() {
        final JMenu helpMenu = new JMenu((String) resourceBundle.getObject("HelpMenu")); //$NON-NLS-1$

        helpMenu.add(new JMenuItem(new AbstractAction((String) resourceBundle.getObject("HelpMenuAbout")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleAbout(actionEvent);
                    }
                }));

        final JMenuItem menuItemDocumentation = new JMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("HelpMenuDocumentation")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleAbout(actionEvent);
                    }
                });
        helpMenu.add(menuItemDocumentation);

        final JMenu language = new JMenu(resourceBundle.getString("LanguageChooser"));
        final JMenuItem menuItemEnglish = new JMenuItem(new AbstractAction(resourceBundle.getString("English")) {

            private static final long serialVersionUID = -2576202988909465055L;

            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        language.add(menuItemEnglish);

        final JMenuItem menuItemGerman = new JMenuItem(new AbstractAction(resourceBundle.getString("German")) {

            private static final long serialVersionUID = 7733985567454234949L;

            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        language.add(menuItemGerman);

        menuItemGerman.setEnabled(false);
        menuItemDocumentation.setEnabled(false);

        helpMenu.add(language);

        return helpMenu;
    }

    private JMenu fileMenu() {
        final JMenu menuFile = new JMenu((String) resourceBundle.getObject("FileMenu")); //$NON-NLS-1$
        // menuFile.setMnemonic(KeyEvent.VK_F); // not useful, because we have an KeyListener for f 'faster'

        menuFile.add(new JMenuItem(new OpenAction((String) resourceBundle.getObject("FileMenuOpen"), false)));
        menuFile.add(new JMenuItem(new OpenAction((String) resourceBundle.getObject("XmlEditor"), true)));
        // final JMenuItem menuItemXmlEditor = new JMenuItem(new AbstractAction(resourceBundle.getString("XmlEditor")) {
        //
        // private static final long serialVersionUID = -1280435957379756272L;
        //
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // // TODO Auto-generated method stub
        //
        // }
        // });
        // menuFile.add(menuItemXmlEditor);

        final JMenuItem menuItemPreferences = new JMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("FileMenuPreferences")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handlePreferences(actionEvent);
                    }
                });
        menuFile.add(menuItemPreferences);

        menuFile.addSeparator();
        menuFile.add(new AbstractAction((String) resourceBundle.getObject("FileMenuExit")) {//$NON-NLS-1$
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleQuit(actionEvent);
            }
        });

        menuItemPreferences.setEnabled(false);
        return menuFile;
    }

    public JMenuItem onrampAction(final String string) {
        return new JMenuItem(new AbstractAction((String) resourceBundle.getObject(string)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleExampleOnramp(actionEvent, string);
            }
        });
    }

    protected void handleExampleOnramp(ActionEvent actionEvent, String string) {
        // final MovsimViewerFacade movsimViewerFacade = MovsimViewerFacade.getInstance();
        // movsimViewerFacade.loadScenarioFromXml(string);
        // trafficUi.getController().commandOnrampFile();
        // uiDefaultReset();

    }

    public void startbuttonToPauseAtScenarioChange() {
        if (canvasPanel.simulationRunnable.isPaused()) {
            canvasPanel.controller.commandTogglePause();
        }
    }

    private void handleAbout(EventObject event) {
        final String titleString = (String) resourceBundle.getObject("AboutTitle"); //$NON-NLS-1$
        final String aboutString = (String) resourceBundle.getObject("AboutText"); //$NON-NLS-1$
        JOptionPane.showMessageDialog(canvasPanel, aboutString, titleString, JOptionPane.INFORMATION_MESSAGE);
    }

    private void handlePreferences(EventObject event) {
        new ViewerPreferences(resourceBundle);
    }

    protected void handleTravelTimeDiagram(ActionEvent actionEvent) {
        // final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        // if (trafficUi.getStatusPanel().isWithTravelTimes()) {
        // if (cb.isSelected()) {
        // travelTimeDiagram = new TravelTimeDiagram(resourceBundle, cb);
        // } else {
        // SwingHelper.closeWindow(travelTimeDiagram);
        // }
        // } else {
        // JOptionPane.showMessageDialog(frame, resourceBundle.getString("NoTravelTime"));
        // cb.setSelected(false);
        // }

    }

    protected void handleSpatioTemporalDiagram(ActionEvent actionEvent) {
        // final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        // if (cb.isSelected()) {
        // spatioTemporalDiagram = new SpatioTemporalView(resourceBundle, cb);
        // } else {
        // SwingHelper.closeWindow(spatioTemporalDiagram);
        // }
    }

    protected void handleFloatingCarsDiagram(ActionEvent actionEvent) {
        // final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        // if (cb.isSelected()) {
        // fcAcc = new FloatingCarsAccelerationView();
        // fcSpeed = new FloatingCarsSpeedView();
        // fcTrajectories = new FloatingCarsTrajectoriesView();
        // } else {
        // SwingHelper.closeWindow(fcAcc);
        // SwingHelper.closeWindow(fcSpeed);
        // SwingHelper.closeWindow(fcTrajectories);
        // }
    }

    protected void handleDetectorsDiagram(ActionEvent actionEvent) {
        // final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        // if (cb.isSelected()) {
        // detectorsDiagram = new DetectorsView(resourceBundle, cb);
        // } else {
        // SwingHelper.closeWindow(detectorsDiagram);
        // }
    }

    protected void handleFuelConsumptionDiagram(ActionEvent actionEvent) {
        SwingHelper.notImplemented(canvasPanel);
    }

    private void handleQuit(EventObject event) {
        canvasPanel.quit();
        frame.dispose();
        System.exit(0); // also kills all existing threads
    }

    protected void handleLogOutput(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cbMenu = (JCheckBoxMenuItem) actionEvent.getSource();
        if (cbMenu.isSelected()) {
            logWindow = new LogWindow(resourceBundle, cbMenu);
        } else {
            SwingHelper.closeWindow(logWindow);
        }
    }

    protected void handleDisplayStatusPanel(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        if (cb.isSelected()) {
//            frame.addStatusPanel();
        } else {
//            frame.removeStatusPanel();
        }
    }

    protected void handleDrawRoadIds(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        if (cb.isSelected()) {
            canvasPanel.setDrawRoadId(true);
        } else {
            canvasPanel.setDrawRoadId(false);
        }
    }

    public void uiDefaultReset() {
        startbuttonToPauseAtScenarioChange();
        frame.statusPanel.setWithTravelTimes(false);
        frame.statusPanel.setWithProgressBar(true);
        frame.statusPanel.reset();
    }

    // --------------------------------------------------------------------------------------
    class OpenAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private boolean editor;

        public OpenAction(String title, boolean editor) {
            super(title);
            this.editor = editor;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String cwd = ""; //$NON-NLS-1$
            try {
                cwd = new java.io.File(".").getCanonicalPath(); //$NON-NLS-1$
            } catch (final java.io.IOException e) {
            }
            // System.out.println("cwd = " + cwd); //$NON-NLS-1$
            final File path = new File(cwd);
            final JFileChooser fileChooser = new JFileChooser(path);
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().endsWith(".xml"); //$NON-NLS-1$
                }

                @Override
                public String getDescription() {
                    return "XML files";
                }
            });
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            final int ret = fileChooser.showOpenDialog(canvasPanel);
            if (ret == JFileChooser.APPROVE_OPTION) {
                final File file = fileChooser.getSelectedFile();
                if (file != null) {
                    // if the user has selected a file, then load it
                    // final String filename =
                    // FilenameUtils.removeExtension(file.getAbsolutePath());
                    // trafficUi.loadScenarioAndRun(filename);
                    if (editor) {
                        new Editor(resourceBundle, file);
                    } else {
                        final MovsimViewerFacade movsimViewerFacade = MovsimViewerFacade.getInstance();
                        canvasPanel.trafficCanvas.setupTrafficScenario(Scenario.ONRAMPFILE);
                        // movsimViewerFacade.loadScenarioFromXml(file);
                        //                        uiDefaultReset();
                    }
                }
            }
        }
    }
}
