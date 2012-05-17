package org.movsim.viewer.ui;

import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.movsim.viewer.graphics.TrafficCanvasScenarios;
import org.movsim.viewer.util.SwingHelper;

@SuppressWarnings("synthetic-access")
public class AppletMenu extends MovSimMenuBase {
    private static final long serialVersionUID = -1741830983719200790L;

    private StatusPanel statusPanel;

    public AppletMenu(CanvasPanel canvasPanel, TrafficCanvasScenarios trafficCanvas, StatusPanel statusPanel, ResourceBundle resourceBundle) {
        super(canvasPanel, trafficCanvas, resourceBundle);
        this.statusPanel = statusPanel;
    }

    public void initMenus(Applet frame) {
        final JMenuBar menuBar = new JMenuBar();

        menuBar.add(scenarioMenu());
        menuBar.add(viewMenu());
        menuBar.add(helpMenu());

        frame.setJMenuBar(menuBar);
    }

    private JMenu scenarioMenu() {
        final JMenu scenarioMenu = new JMenu((String) resourceBundle.getObject("ScenarioMenu"));
        final JMenuItem menuItemOnRamp = new JMenuItem(new AbstractAction(resourceBundle.getString("OnRamp")) {
            private static final long serialVersionUID = 7705041304742695628L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.ONRAMPFILE, "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemOnRamp);

        final JMenuItem menuItemOffRamp = new JMenuItem(new AbstractAction(resourceBundle.getString("OffRamp")) {
            private static final long serialVersionUID = -2548920811907898064L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.OFFRAMPFILE, "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemOffRamp);

        final JMenuItem menuItemFlowConservingBottleNeck = new JMenuItem(new AbstractAction(
                resourceBundle.getString("FlowConservingBottleNeck")) {
            private static final long serialVersionUID = -8349549625085281487L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.FLOWCONSERVINGBOTTLENECK, "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemFlowConservingBottleNeck);

        final JMenuItem menuItemSpeedLimit = new JMenuItem(new AbstractAction(resourceBundle.getString("SpeedLimit")) {
            private static final long serialVersionUID = -1498474459807551133L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.SPEEDLIMITFILE, "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemSpeedLimit);

        final JMenuItem menuItemTrafficLight = new JMenuItem(new AbstractAction(resourceBundle.getString("TrafficLight")) {
            private static final long serialVersionUID = 2511854387728111343L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.TRAFFICLIGHTFILE, "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemTrafficLight);

        final JMenuItem menuItemLaneClosing = new JMenuItem(new AbstractAction(resourceBundle.getString("LaneClosing")) {
            private static final long serialVersionUID = -5359478839829791298L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.LANECLOSINGFILE, "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemLaneClosing);

        final JMenuItem menuItemCloverLeaf = new JMenuItem(new AbstractAction(resourceBundle.getString("CloverLeaf")) {
            private static final long serialVersionUID = 8504921708742771452L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.CLOVERLEAFFILE, "/sim/buildingBlocks/");
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

        final JMenuItem menuItemCityInterSection = new JMenuItem(new AbstractAction(resourceBundle.getString("CityInterSection")) {
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
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.RINGROADONELANEFILE, "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemRingRoad);

        final JMenuItem menuItemRingRoadTwoLanes = new JMenuItem(new AbstractAction(resourceBundle.getString("RingRoad2Lanes")) {
            private static final long serialVersionUID = 4633365854029111923L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.RINGROADTWOLANESFILE, "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemRingRoadTwoLanes);

        final JMenuItem menuItemGameRampMetering = new JMenuItem(new AbstractAction(resourceBundle.getString("GameRampMetering")) {
            private static final long serialVersionUID = 4633365854029111923L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.RAMPMETERING, "/sim/games/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemGameRampMetering);

        final JMenuItem menuItemGameRouting = new JMenuItem(new AbstractAction(resourceBundle.getString("GameRouting")) {
            private static final long serialVersionUID = 4633365854029111923L;
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario(TrafficCanvasScenarios.ROUTING, "/sim/games/");
                uiDefaultReset();
            }
        });
        scenarioMenu.add(menuItemGameRouting);

        menuItemRoundAbout.setEnabled(false);
        menuItemCityInterSection.setEnabled(false);

        return scenarioMenu;
    }

    private JMenu viewMenu() {
        final JMenu viewMenu = new JMenu((String) resourceBundle.getObject("ViewMenu"));
        // final JMenu colorVehicles = new JMenu(resourceBundle.getString("VehicleColors"));
        //
        // final JMenuItem menuItemVehicleColorSpeedDependant = new JMenuItem(
        // resourceBundle.getString("VehicleColorSpeedDependant"));
        // final JMenuItem menuItemVehicleColorRandom = new JMenuItem(resourceBundle.getString("VehicleColorRandom"));
        // colorVehicles.add(menuItemVehicleColorSpeedDependant);
        // colorVehicles.add(menuItemVehicleColorRandom);
        //
        // menuItemVehicleColorSpeedDependant.setEnabled(false);
        // menuItemVehicleColorRandom.setEnabled(false);
        //
        // viewMenu.add(colorVehicles);
        //
        // viewMenu.addSeparator();
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

    private void handleQuit(EventObject event) {
        // canvasPanel.quit();
        // frame.dispose();
        // System.exit(0); // also kills all existing threads
    }

    private void uiDefaultReset() {
        statusPanel.setWithProgressBar(false);
        statusPanel.reset();
        trafficCanvas.start();
    }
}