package org.movsim.viewer.ui;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.util.SwingHelper;

@SuppressWarnings({ "synthetic-access", "serial" })
public class AppletMenu extends MovSimMenuBase {
    private static final long serialVersionUID = -1741830983719200790L;

    private StatusPanel statusPanel;

    public AppletMenu(CanvasPanel canvasPanel, TrafficCanvas trafficCanvas, StatusPanel statusPanel,
            ResourceBundle resourceBundle) {
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

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("OnRamp")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("onramp", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("OffRamp")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("offramp", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("FlowConservingBottleNeck")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("flow_conserving_bottleneck", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("SpeedLimitOUTDATED")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("speedlimit", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("TrafficLight")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("trafficlight", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("LaneClosing")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("laneclosure", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("CloverLeaf")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("cloverleaf", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("RoundAbout")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        })).setEnabled(false);

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("CityInterSection")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        })).setEnabled(false);

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("RingRoad")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("ringroad_1lane", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("RingRoad2Lanes")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("ringroad_2lanes", "/sim/buildingBlocks/");
                uiDefaultReset();
            }
        }));

        scenarioMenu.addSeparator();

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("GameRampMetering")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("ramp_metering", "/sim/games/");
                trafficCanvas.setVehicleColorMode(TrafficCanvas.VehicleColorMode.EXIT_COLOR);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("GameRouting")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("routing", "/sim/games/");
                trafficCanvas.setVehicleColorMode(TrafficCanvas.VehicleColorMode.EXIT_COLOR);
                uiDefaultReset();
            }
        }));

        return scenarioMenu;
    }

    private JMenu viewMenu() {
        final JMenu viewMenu = new JMenu((String) resourceBundle.getObject("ViewMenu"));

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawRoadIds")) {//$NON-NLS-1$
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDrawRoadIds(actionEvent);
                    }
                })).setSelected(trafficCanvas.isDrawRoadId());

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawSources")) {//$NON-NLS-1$
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDrawSources(actionEvent);
                    }
                })).setSelected(trafficCanvas.isDrawSources());

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawSinks")) {//$NON-NLS-1$
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDrawSinks(actionEvent);
                    }
                })).setSelected(trafficCanvas.isDrawSinks());

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawSpeedLimits")) {//$NON-NLS-1$
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleDrawSpeedLimits(actionEvent);
                    }
                })).setSelected(trafficCanvas.isDrawSpeedLimits());

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawFlowConservingBootleNecks")) {//$NON-NLS-1$
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                })).setEnabled(false);

        viewMenu.addSeparator();

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawRoutesTravelTime")) {//$NON-NLS-1$
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                })).setEnabled(false);

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawRoutesSpatioTemporal")) {//$NON-NLS-1$
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                    }
                })).setEnabled(false);

        return viewMenu;
    }

    private void uiDefaultReset() {
        statusPanel.setWithProgressBar(false);
        statusPanel.reset();
        trafficCanvas.start();
    }
}