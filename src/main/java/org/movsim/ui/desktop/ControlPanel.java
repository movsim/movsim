/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.ui.desktop;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.movsim.simulator.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ControlPanel.
 */
public class ControlPanel extends JPanel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ControlPanel.class);
    final JButton startButton;
    final JButton pauseButton;
    final JButton stopButton;

    /**
     * Instantiates a new control panel.
     * 
     * @param simulator
     *            the simulator
     */
    public ControlPanel(ActionListener listener) {
        
//        SwingHelper.setComponentSize(this, 1200, 80);
//        setBorder(BorderFactory.createEtchedBorder());
//        setLayout(null);

        // Buttons
        startButton = new JButton("start");
        pauseButton = new JButton("pause");
        stopButton = new JButton("stop");

        startButton.setBounds(20, 20, 80, 22);
        pauseButton.setBounds(120, 20, 80, 22);
        stopButton.setBounds(220, 20, 80, 22);
        
        startButton.addActionListener(listener);
        pauseButton.addActionListener(listener);
        stopButton.addActionListener(listener);

        add(startButton);
        add(pauseButton);
        add(stopButton);
    }

}
