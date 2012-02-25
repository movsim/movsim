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

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.movsim.viewer.util.SwingHelper;

/**
 * @author ralph
 * 
 */
public class ViewerPreferences extends JFrame {

    private static final long serialVersionUID = -7133363889525927714L;

    /**
     * @param resourceBundle
     */
    public ViewerPreferences(ResourceBundle resourceBundle) {
        super(resourceBundle.getString("TitlePreferences"));
        setLayout(new BorderLayout());
        SwingHelper.activateWindowClosingButton(this);
        final JLabel lblFeatures = new JLabel(
                "initial scale, initial speeduptime, initial speedup factor, initial sleep, vehicles, vehicle color, canvas area, window size, language, draw road ids, show log window, show diagrams");

        final JPanel prefPanel = new JPanel();
        prefPanel.add(lblFeatures, BorderLayout.CENTER);

        add(prefPanel);
        pack();
        setVisible(true);
    }

}
