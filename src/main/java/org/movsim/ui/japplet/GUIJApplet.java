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
package org.movsim.ui.japplet;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JApplet;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.movsim.ui.GUI;
import org.movsim.ui.components.ComponentPanel;

// TODO: Auto-generated Javadoc
/**
 * The Class GUIJApplet.
 */
public class GUIJApplet extends JApplet implements GUI {

    /**
     * Instantiates a new gUIJ applet.
     */
    public GUIJApplet() {
    }

    // final static Logger logger = LoggerFactory.getLogger(GUIJApplet.class);

    // The bounds of the preferred Displaydevice
    private Rectangle bounds;

    /*
     * (non-Javadoc)
     * 
     * @see java.applet.Applet#init()
     */
    @Override
    public void init() {
        // Locale.setDefault(Locale.US);
        //
        // // BasicConfigurator for log4j replaced with PropertyConfigurator.
        // PropertyConfigurator.configure("sim/log4j.properties");

        System.out.println("Second Applet");

        // Execute a job on the event-dispatching thread:
        // creating this applet's GUI.
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    createGUI();
                }
            });
        } catch (final Exception e) {
            // logger.error("createGUI() did ot succesfully complete!");
        }
    }

    /**
     * Creates the gui.
     */
    protected void createGUI() {

        // Screen

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Use the getScreenDevices() method to pull out the screen devices from
        // the GraphicsEnvironment object
        final GraphicsDevice[] gs = ge.getScreenDevices();

        for (int j = 0; j < gs.length; j++) {
            System.out.println("Checking Device " + j);
            final GraphicsDevice gd = gs[j];
            final GraphicsConfiguration[] gc = gd.getConfigurations();
            System.out.println("DefaultConfiguration for Device[" + "] has bounds of "
                    + gd.getDefaultConfiguration().getBounds() + "\n and color model of "
                    + gd.getDefaultConfiguration().getColorModel());
            bounds = gd.getDefaultConfiguration().getBounds();
        }

        System.out.println("bounds height: " + bounds.height);
        System.out.println("bounds width: " + bounds.width);

        final Container contentPane = getContentPane();

        getContentPane().setLayout(new BorderLayout());

        final ComponentPanel componentPanel = new ComponentPanel();

        contentPane.add(componentPanel);

        final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        componentPanel.add(tabbedPane, BorderLayout.CENTER);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.applet.Applet#start()
     */
    @Override
    public void start() {
        super.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.applet.Applet#stop()
     */
    @Override
    public void stop() {
        super.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.applet.Applet#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Container#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawString("Paint() method draws String", 100, 350);
    }
}
