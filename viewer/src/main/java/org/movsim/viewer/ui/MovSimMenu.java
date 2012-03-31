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

import org.movsim.simulator.Simulator;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.CCS;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.CCS.Waves;
import org.movsim.utilities.FileUtils;
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.graphics.TrafficCanvasScenarios;
import org.movsim.viewer.graphics.TrafficCanvasScenarios.Scenario;
import org.movsim.viewer.util.SwingHelper;

@SuppressWarnings("synthetic-access")
public class MovSimMenu extends JPanel {
    private static final long serialVersionUID = -1741830983719200790L;
    private final Simulator simulator;
    private final MainFrame frame;
    private final CanvasPanel canvasPanel;
    private final TrafficCanvasScenarios trafficCanvas;
    private final ResourceBundle resourceBundle;

    private LogWindow logWindow;

    public MovSimMenu(MainFrame mainFrame, Simulator simulator, CanvasPanel canvasPanel, TrafficCanvasScenarios trafficCanvas, ResourceBundle resourceBundle) {
        this.frame = mainFrame;
        this.simulator = simulator;
        this.canvasPanel = canvasPanel;
        this.trafficCanvas = trafficCanvas;
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
                trafficCanvas.setupTrafficScenario(Scenario.ONRAMPFILE);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemOnRamp);

        final JMenuItem menuItemOffRamp = new JMenuItem(new AbstractAction(resourceBundle.getString("OffRamp")) {

            private static final long serialVersionUID = -2548920811907898064L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.OFFRAMPFILE);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemOffRamp);

        final JMenuItem menuItemFlowConservingBottleNeck = new JMenuItem(new AbstractAction(
                resourceBundle.getString("FlowConservingBottleNeck")) {

            private static final long serialVersionUID = -8349549625085281487L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.FLOWCONSERVINGBOTTLENECK);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemFlowConservingBottleNeck);

        final JMenuItem menuItemSpeedLimit = new JMenuItem(new AbstractAction(resourceBundle.getString("SpeedLimit")) {

            private static final long serialVersionUID = -1498474459807551133L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.SPEEDLIMITFILE);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemSpeedLimit);

        final JMenuItem menuItemTrafficLight = new JMenuItem(new AbstractAction(
                resourceBundle.getString("TrafficLight")) {

            private static final long serialVersionUID = 2511854387728111343L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.TRAFFICLIGHTFILE);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemTrafficLight);

        final JMenuItem menuItemLaneClosing = new JMenuItem(
                new AbstractAction(resourceBundle.getString("LaneClosing")) {

                    private static final long serialVersionUID = -5359478839829791298L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        trafficCanvas.setupTrafficScenario(Scenario.LANECLOSINGFILE);
                        uiDefaultReset();
                    }
                });
        scenarioMenu.add(menuItemLaneClosing);

        final JMenuItem menuItemCloverLeaf = new JMenuItem(new AbstractAction(resourceBundle.getString("CloverLeaf")) {

            private static final long serialVersionUID = 8504921708742771452L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.CLOVERLEAFFILE);
                uiDefaultReset();
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
                trafficCanvas.setupTrafficScenario(Scenario.RINGROADONELANEFILE);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemRingRoad);

        final JMenuItem menuItemRingRoadTwoLanes = new JMenuItem(new AbstractAction(
                resourceBundle.getString("RingRoad2Lanes")) {

            private static final long serialVersionUID = 4633365854029111923L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.RINGROADTWOLANESFILE);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemRingRoadTwoLanes);

        scenarioMenu.addSeparator();
        final JMenuItem menuItemGameRampMetering = new JMenuItem(new AbstractAction(
                resourceBundle.getString("GameRampMetering")) {

            private static final long serialVersionUID = 4633365854029111923L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.RAMPMETERING);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemGameRampMetering);

        final JMenuItem menuItemGameRouting = new JMenuItem(new AbstractAction(
                resourceBundle.getString("GameRouting")) {

            private static final long serialVersionUID = 4633365854029111923L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.ROUTING);
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemGameRouting);

        scenarioMenu.addSeparator();
        final JMenuItem menuItemVasaLoppet = new JMenuItem(new AbstractAction(resourceBundle.getString("Vasaloppet")) {

            private static final long serialVersionUID = 4633365854029111923L;

            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(Scenario.VASALOPPET);
                uiDefaultReset();
                CCS.setWave(Waves.FOURWAVES);
            }
        });
        scenarioMenu.add(menuItemVasaLoppet);

        menuItemRoundAbout.setEnabled(false);
        menuItemCityInterSection.setEnabled(false);

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
                        handleDrawSources(actionEvent);
                    }
                });
        viewMenu.add(cbSources);

        final JCheckBoxMenuItem cbSinks = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawSinks")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDrawSinks(actionEvent);
                    }
                });
        viewMenu.add(cbSinks);

        final JCheckBoxMenuItem cbSpeedLimits = new JCheckBoxMenuItem(new AbstractAction(
                (String) resourceBundle.getObject("DrawSpeedLimits")) {//$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDrawSpeedLimits(actionEvent);
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
        cbflowConservingBootleNecks.setEnabled(false);
        cbRoutesSpatioTemporal.setEnabled(false);
        cbRoutesTravelTimes.setEnabled(false);

        cbSpeedLimits.setSelected(trafficCanvas.isDrawSpeedLimits());
        cbDrawRoadIds.setSelected(trafficCanvas.isDrawRoadId());
        cbSources.setSelected(trafficCanvas.isDrawSources());
        cbSinks.setSelected(trafficCanvas.isDrawSinks());
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

        menuFile.add(new JMenuItem(new OpenAction((String) resourceBundle.getObject("FileMenuOpen"))));

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

    private void handleAbout(EventObject event) {
        final String titleString = (String) resourceBundle.getObject("AboutTitle"); //$NON-NLS-1$
        final String aboutString = (String) resourceBundle.getObject("AboutText"); //$NON-NLS-1$
        JOptionPane.showMessageDialog(canvasPanel, aboutString, titleString, JOptionPane.INFORMATION_MESSAGE);
    }

    private void handlePreferences(EventObject event) {
        ViewerPreferences viewerPreferences = new ViewerPreferences(resourceBundle);
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

    protected void handleDrawRoadIds(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        trafficCanvas.setDrawRoadId(cb.isSelected());
    }

    protected void handleDrawSources(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        trafficCanvas.setDrawSources(cb.isSelected());
    }

    protected void handleDrawSinks(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        trafficCanvas.setDrawSinks(cb.isSelected());
    }

    protected void handleDrawSpeedLimits(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        trafficCanvas.setDrawSpeedLimits(cb.isSelected());
    }

    public void uiDefaultReset() {
        trafficCanvas.setVmaxForColorSpectrum(Double.parseDouble(TrafficCanvas.getProperties().getProperty(
                "vmaxForColorSpectrum", "140")));
        frame.statusPanel.setWithProgressBar(true);
        frame.statusPanel.reset();
        trafficCanvas.start();
    }

    // --------------------------------------------------------------------------------------
    class OpenAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public OpenAction(String title) {
            super(title);
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
                if (file != null && file.isFile()) {
                    // if the user has selected a file, then load it
                    simulator.loadScenarioFromXml(FileUtils.getProjectName(file),
                            FileUtils.getCanonicalPathWithoutFilename(file));
                    uiDefaultReset();
                    trafficCanvas.forceRepaintBackground();
                }
            }
        }
    }
}
