package org.movsim.viewer.ui;

import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.movsim.viewer.graphics.GraphicsConfigurationParameters;
import org.movsim.viewer.util.SwingHelper;

public class AppletMenu extends JPanel {
    private static final long serialVersionUID = -1741830983719200790L;
    private final Applet frame;
    final CanvasPanel canvasPanel;
    private final ResourceBundle resourceBundle;

    private LogWindow logWindow;
    private StatusPanel statusPanel;

    // protected TravelTimeDiagram travelTimeDiagram;
    // private DetectorsView detectorsDiagram;
    // private SpatioTemporalView spatioTemporalDiagram;
    // private FloatingCarsAccelerationView fcAcc;
    // private FloatingCarsSpeedView fcSpeed;
    // private FloatingCarsTrajectoriesView fcTrajectories;

    public AppletMenu(Applet mainFrame, CanvasPanel canvasPanel, StatusPanel statusPanel, ResourceBundle resourceBundle) {
        this.frame = mainFrame;
        this.canvasPanel = canvasPanel;
        this.statusPanel = statusPanel;
        this.resourceBundle = resourceBundle;
    }

    public void initMenus() {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu scenarioMenu = scenarioMenu();
        final JMenu helpMenu = helpMenu();
        final JMenu modelMenu = modelMenu();
        final JMenu viewMenu = viewMenu();
        final JMenu outputMenu = outputMenu();

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
                canvasPanel.simulator.loadScenarioFromXml("onramp", "/sim/buildingBlocks/");
                canvasPanel.trafficCanvas.reset();
                canvasPanel.trafficCanvas.start();
                statusPanel.reset();
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemOnRamp);

        final JMenuItem menuItemOffRamp = new JMenuItem(new AbstractAction(resourceBundle.getString("OffRamp")) {

            private static final long serialVersionUID = -2548920811907898064L;

            @Override
            public void actionPerformed(ActionEvent e) {
                canvasPanel.simulator.loadScenarioFromXml("offramp", "/sim/buildingBlocks/");
                canvasPanel.trafficCanvas.reset();
                canvasPanel.trafficCanvas.start();
                statusPanel.reset();
                uiDefaultReset();
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
                canvasPanel.simulator.loadScenarioFromXml("speedlimit", "/sim/buildingBlocks/");
                canvasPanel.trafficCanvas.reset();
                canvasPanel.trafficCanvas.start();
                statusPanel.reset();
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemSpeedLimit);

        final JMenuItem menuItemTrafficLight = new JMenuItem(new AbstractAction(
                resourceBundle.getString("TrafficLight")) {

            private static final long serialVersionUID = 2511854387728111343L;

            @Override
            public void actionPerformed(ActionEvent e) {
                canvasPanel.simulator.loadScenarioFromXml("trafficlight", "/sim/buildingBlocks/");
                canvasPanel.trafficCanvas.reset();
                canvasPanel.trafficCanvas.start();
                statusPanel.reset();
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemTrafficLight);

        final JMenuItem menuItemLaneClosing = new JMenuItem(
                new AbstractAction(resourceBundle.getString("LaneClosing")) {

                    private static final long serialVersionUID = -5359478839829791298L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        canvasPanel.simulator.loadScenarioFromXml("laneclosure", "/sim/buildingBlocks/");
                        canvasPanel.trafficCanvas.reset();
                        canvasPanel.trafficCanvas.start();
                        statusPanel.reset();
                        uiDefaultReset();
                    }
                });
        scenarioMenu.add(menuItemLaneClosing);

        final JMenuItem menuItemCloverLeaf = new JMenuItem(new AbstractAction(resourceBundle.getString("CloverLeaf")) {

            private static final long serialVersionUID = 8504921708742771452L;

            @Override
            public void actionPerformed(ActionEvent e) {
                canvasPanel.simulator.loadScenarioFromXml("cloverleaf", "/sim/buildingBlocks/");
                canvasPanel.trafficCanvas.reset();
                canvasPanel.trafficCanvas.start();
                statusPanel.reset();
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
                canvasPanel.simulator.loadScenarioFromXml("ringroad_1lane", "/sim/buildingBlocks/");
                canvasPanel.trafficCanvas.reset();
                canvasPanel.trafficCanvas.start();
                statusPanel.reset();
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemRingRoad);

        final JMenuItem menuItemRingRoadTwoLanes = new JMenuItem(new AbstractAction(
                resourceBundle.getString("RingRoad2Lanes")) {

            private static final long serialVersionUID = 4633365854029111923L;

            @Override
            public void actionPerformed(ActionEvent e) {
                canvasPanel.simulator.loadScenarioFromXml("ringroad_2lanes", "/sim/buildingBlocks/");
                canvasPanel.trafficCanvas.reset();
                canvasPanel.trafficCanvas.start();
                statusPanel.reset();
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemRingRoadTwoLanes);

        menuItemFlowConservingBottleNeck.setEnabled(false);
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

        cbSpeedLimits.setSelected(GraphicsConfigurationParameters.DRAWSPEEDLIMITS);
        cbDrawRoadIds.setSelected(GraphicsConfigurationParameters.DRAW_ROADID);
        cbSources.setSelected(GraphicsConfigurationParameters.DRAWSOURCES);
        cbSinks.setSelected(GraphicsConfigurationParameters.DRAWSINKS);
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

    public void startbuttonToPauseAtScenarioChange() {
        if (canvasPanel.simulator.getSimulationRunnable().isPaused()) {
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
        // canvasPanel.quit();
        // frame.dispose();
        // System.exit(0); // also kills all existing threads
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
        if (cb.isSelected()) {
            canvasPanel.trafficCanvas.setDrawRoadId(true);
        } else {
            canvasPanel.trafficCanvas.setDrawRoadId(false);
        }
        canvasPanel.trafficCanvas.forceRepaintBackground();
    }

    protected void handleDrawSources(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        if (cb.isSelected()) {
            canvasPanel.trafficCanvas.setDrawSources(true);
        } else {
            canvasPanel.trafficCanvas.setDrawSources(false);
        }
        canvasPanel.trafficCanvas.forceRepaintBackground();
    }

    protected void handleDrawSinks(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        if (cb.isSelected()) {
            canvasPanel.trafficCanvas.setDrawSinks(true);
        } else {
            canvasPanel.trafficCanvas.setDrawSinks(false);
        }
        canvasPanel.trafficCanvas.forceRepaintBackground();
    }

    protected void handleDrawSpeedLimits(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        if (cb.isSelected()) {
            canvasPanel.trafficCanvas.setDrawSpeedLimits(true);
        } else {
            canvasPanel.trafficCanvas.setDrawSpeedLimits(false);
        }
        canvasPanel.trafficCanvas.forceRepaintBackground();
    }

    public void uiDefaultReset() {
        startbuttonToPauseAtScenarioChange();
        statusPanel.setWithTravelTimes(false);
        statusPanel.setWithProgressBar(false);
        statusPanel.reset();
    }

}