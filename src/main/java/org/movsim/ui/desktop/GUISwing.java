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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.movsim.input.impl.InputDataImpl;
import org.movsim.simulator.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class GUISwing.
 */
public class GUISwing extends JFrame implements WindowListener {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(GUISwing.class);

    /**
     * Instantiates a new gUI swing.
     * 
     * @param simInput
     *            the sim input
     * @param simulator
     *            the simulator
     */
    public GUISwing(InputDataImpl simInput, Simulator simulator) {
        super();
        this.addWindowListener(this);

        setLocation(20, 60);

        final MenuSwing menuMain = new MenuSwing();

//        final ControlPanel controlPanel = new ControlPanel();

        final RoadPanel roadPanel = new RoadPanel();

        final StatusPanel statusPanel = new StatusPanel();

//        add(controlPanel, BorderLayout.NORTH);
        add(roadPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
     */
    @Override
    public void windowOpened(WindowEvent e) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosed(WindowEvent e) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    @Override
    public void windowIconified(WindowEvent e) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
     * )
     */
    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    @Override
    public void windowActivated(WindowEvent e) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
     * )
     */
    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    /**
     * The Class MenuSwing.
     */
    public class MenuSwing extends JPanel {

        /**
         * Instantiates a new menu swing.
         */
        public MenuSwing() {

            final JMenuBar menuBar = new JMenuBar();

            final JMenu menuFile = new JMenu("File");
            final JMenu menuEdit = new JMenu("Edit");
            final JMenu menuHelp = new JMenu("Help");

            menuFile.setMnemonic('F');
            menuEdit.setMnemonic('E');
            menuHelp.setMnemonic('H');

            final Action exitAction = new AbstractAction("Exit") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };

            menuFile.addSeparator();
            menuFile.add(exitAction);

            menuBar.add(menuFile);
            menuBar.add(menuEdit);
            menuBar.add(menuHelp);

            setJMenuBar(menuBar);
        }
    }

}
