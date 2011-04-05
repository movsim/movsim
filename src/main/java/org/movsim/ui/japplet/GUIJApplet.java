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

import org.movsim.ui.GUI;
import org.movsim.ui.components.ComponentPanel;


/**
 * @author ralph
 * 
 */
public class GUIJApplet extends JApplet implements GUI {
    public GUIJApplet() {
    }

//    final static Logger logger = LoggerFactory.getLogger(GUIJApplet.class);
    
    // The bounds of the preferred Displaydevice
    private Rectangle bounds;

    /**
     * Called when this applet is loaded into the browser.
     */
    public void init() {
//        Locale.setDefault(Locale.US);
//
//        // BasicConfigurator for log4j replaced with PropertyConfigurator.
//        PropertyConfigurator.configure("sim/log4j.properties");
        
        System.out.println("Second Applet");
        
        // Execute a job on the event-dispatching thread:
        // creating this applet's GUI.
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) {
//            logger.error("createGUI() did ot succesfully complete!");
        }
    }

    /**
     * 
     */
    protected void createGUI() {

        // Screen
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Use the getScreenDevices() method to pull out the screen devices from
        // the GraphicsEnvironment object
        GraphicsDevice[] gs = ge.getScreenDevices();

        for (int j = 0; j < gs.length; j++) {
            System.out.println("Checking Device " + j);
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            System.out.println("DefaultConfiguration for Device[" + "] has bounds of "
                    + gd.getDefaultConfiguration().getBounds() + "\n and color model of "
                    + gd.getDefaultConfiguration().getColorModel());
            bounds = gd.getDefaultConfiguration().getBounds();
        }

        System.out.println("bounds height: "+ bounds.height);
        System.out.println("bounds width: " + bounds.width);

        Container contentPane = getContentPane();
        
        getContentPane().setLayout(new BorderLayout());
        
        ComponentPanel componentPanel = new ComponentPanel();
        
        contentPane.add(componentPanel);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        componentPanel.add(tabbedPane, BorderLayout.CENTER);


    }

    /**
     * The start() method is always called whenever the applet becomes visible.
     * It is called immediately after the execution of init() on the first
     * occasion, and then subsequently when the applet reappears after scrolling
     * or browsing, for example.
     */
    @Override
    public void start() {
        super.start();
    }

    /**
     * The stop() method is always called by a browser whenever the applet
     * becomes invisible. This allows any applet code producing effects such as
     * animation to be stopped.
     */
    @Override
    public void stop() {
        super.stop();
    }

    /**
     * The destroy() method is called by a browser at some convenient point when
     * it decides to remove the resources of the applet. It thus allows the
     * applet a last chance to clean up before it is removed.
     */
    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * This is called by the browser each time the panel's visible area is
     * affected and is supplied with a Graphics object that facilitates drawing
     * on its surface. Because paint() overrides the superclass method, a call
     * of super.paint() is advisable since it ensures that any other components
     * of the superclass are painted.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawString("Paint() method draws String", 100, 350);
    }
}
