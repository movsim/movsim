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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.util.SwingHelper;

@SuppressWarnings("serial")
public class MovSimMenuBase extends JPanel {

    private static final long serialVersionUID = -2588408479627239336L;

    final CanvasPanel canvasPanel;
    final TrafficCanvas trafficCanvas;
    final ResourceBundle resourceBundle;
    private LogWindow logWindow;

    public MovSimMenuBase(CanvasPanel canvasPanel, TrafficCanvas trafficCanvas, ResourceBundle resourceBundle) {
        this.canvasPanel = canvasPanel;
        this.trafficCanvas = trafficCanvas;
        this.resourceBundle = resourceBundle;
    }

    final String resourceString(String string) {
        return resourceBundle.getString(string);
    }

    JMenu helpMenu() {
        final JMenu helpMenu = new JMenu(resourceBundle.getString("HelpMenu")); //$NON-NLS-1$

        helpMenu.add(new JMenuItem(new AbstractAction(resourceBundle.getString("HelpMenuAbout")) {//$NON-NLS-1$
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        handleAbout();
                    }
                }));

        return helpMenu;
    }

    void handleAbout() {
        final String titleString = (String) resourceBundle.getObject("AboutTitle"); //$NON-NLS-1$
        final String aboutString = (String) resourceBundle.getObject("AboutText"); //$NON-NLS-1$
        JOptionPane.showMessageDialog(canvasPanel, aboutString, titleString, JOptionPane.INFORMATION_MESSAGE);
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
        trafficCanvas.forceRepaintBackground();
    }

    protected void handleDrawSources(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        trafficCanvas.setDrawSources(cb.isSelected());
        trafficCanvas.forceRepaintBackground();
    }

    protected void handleDrawSinks(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        trafficCanvas.setDrawSinks(cb.isSelected());
        trafficCanvas.forceRepaintBackground();
    }

    protected void handleDrawSpeedLimits(ActionEvent actionEvent) {
        final JCheckBoxMenuItem cb = (JCheckBoxMenuItem) actionEvent.getSource();
        trafficCanvas.setDrawSpeedLimits(cb.isSelected());
        trafficCanvas.forceRepaintBackground();
    }
}
