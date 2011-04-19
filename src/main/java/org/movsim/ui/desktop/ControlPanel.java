/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.movsim.simulator.Simulator;

// TODO: Auto-generated Javadoc
/**
 * The Class ControlPanel.
 */
public class ControlPanel extends JPanel {

    private final Simulator simulator;

    /**
     * Instantiates a new control panel.
     * 
     * @param simulator
     *            the simulator
     */
    public ControlPanel(final Simulator simulator) {
        this.simulator = simulator;
        SwingHelper.setComponentSize(this, 1200, 80);
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(null);

        // Buttons
        final JButton startButton = new JButton("start");
        final JButton pauseButton = new JButton("pause");
        final JButton stopButton = new JButton("stop");

        startButton.setBounds(20, 20, 80, 22);
        pauseButton.setBounds(120, 20, 80, 22);
        stopButton.setBounds(220, 20, 80, 22);

        final Thread simThread = new Thread(new Runnable() {

            @Override
            public void run() {
                simulator.run();

            }
        });

        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                simThread.run();
            }
        });

        pauseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });

        stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });

        add(startButton);
        add(pauseButton);
        add(stopButton);
    }

}
