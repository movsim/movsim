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
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.movsim.simulator.Simulator;
import org.movsim.viewer.graphics.TrafficCanvas;

@SuppressWarnings({"synthetic-access", "serial"})
public class AppMenu extends MovSimMenuBase {

    private static final long serialVersionUID = -1741830983719200790L;

    private final Simulator simulator;
    private final AppFrame frame;

    public AppMenu(AppFrame mainFrame, Simulator simulator, CanvasPanel canvasPanel,
                   TrafficCanvas trafficCanvas, ResourceBundle resourceBundle) {
        super(canvasPanel, trafficCanvas, resourceBundle);
        this.frame = mainFrame;
        this.simulator = simulator;
    }

    public void initMenus() {
        final JMenuBar menuBar = new JMenuBar();

        menuBar.add(viewMenu());
        menuBar.add(helpMenu());

        frame.setJMenuBar(menuBar);
    }

    private JMenu viewMenu() {
        final JMenu viewMenu = new JMenu(resourceString("ViewMenu"));

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("LogOutput")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleLogOutput(actionEvent);
            }
        }));

        viewMenu.addSeparator();

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

}
