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

package org.movsim.viewer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.movsim.facades.MovsimViewerFacade;
import org.movsim.viewer.control.SimulationRunnable;
import org.movsim.viewer.ui.LogWindow;
import org.movsim.viewer.ui.MovSimMenu;
import org.movsim.viewer.ui.MovSimToolBar;
import org.movsim.viewer.ui.TrafficUi;
import org.movsim.viewer.util.LocalizationStrings;
import org.movsim.viewer.util.SwingHelper;

/**
 * MovSim traffic simulation applet.
 * 
 */
public class ViewerApp extends JApplet {

    static final long serialVersionUID = 1L;

    final static String FRAME_NAME = "Movsim - multi-model open-source traffic simulator";

    static TrafficUi trafficUi;

    private Frame frame;

    private LookAndFeelInfo[] lookAndFeelArray;

    private static final int INIT_FRAME_SIZE_WIDTH = 1400;

    private static final int INIT_FRAME_SIZE_HEIGHT = 640;

    /**
     * Returns basic applet description.
     */
    @Override
    public String getAppletInfo() {
        return "MovSim Traffic Simulation Applet."; //$NON-NLS-1$
    }

    /**
     * Start the traffic simulation. This function is called by the framework
     * after <code>init()<code>.
     */
    @Override
    public void start() {
    }

    @Override
    public void stop() {
        trafficUi.stop();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * Create and initialize the applet's GUI. Called by the framework when this
     * applet is loaded into the browser.
     */
    @Override
    public void init() {
        setLayout(new BorderLayout());
        initLafAndGetFrame();
        this.setSize(INIT_FRAME_SIZE_WIDTH, INIT_FRAME_SIZE_HEIGHT);

        try {
            // Execute a job on the event-dispatching thread; creating this
            // applet's GUI.
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    initApp();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println("initApp didn't complete successfully"); //$NON-NLS-1$
        }
        super.init();
    }

    protected void initApp() {

        trafficUi = new TrafficUi(this);

        final MovsimViewerFacade movsimViewerFacade = MovsimViewerFacade.getInstance();
        // Applet Controller/Runnable
        final SimulationRunnable simulationRunnable = SimulationRunnable.getInstance();

        trafficUi.createGuiForApplet(simulationRunnable, movsimViewerFacade);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                trafficUi.resized();
                trafficUi.repaint();
            }
        });

    }

    protected void initLafAndGetFrame() {
        frame = SwingHelper.getFrame(this);
        try {

            // get the look-and-feels available on the system
            lookAndFeelArray = UIManager.getInstalledLookAndFeels();
            for (int i = 0; i < lookAndFeelArray.length; i++) {
                System.out.println(lookAndFeelArray[i].getName());
                if (lookAndFeelArray[i].getName().equals("Nimbus")) {
                    UIManager.setLookAndFeel(lookAndFeelArray[i].getClassName());
                }
            }

        } catch (final Exception exc) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.out.println("set to system LaF");
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            } catch (final InstantiationException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            } catch (final UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }
        SwingUtilities.updateComponentTreeUI(frame);
    }

    public static void main(String[] args) {

        final JFrame frame = new JFrame(FRAME_NAME);

        frame.setLocation(0, 20);

        SwingHelper.activateWindowClosingAndSystemExitButton(frame);
        SwingHelper.makeLightWeightComponentsVisible();

        frame.setSize(INIT_FRAME_SIZE_WIDTH, INIT_FRAME_SIZE_HEIGHT);
        frame.setVisible(true);

        final JTextArea logArea = new JTextArea();
        LogWindow.setupLog4JAppender(logArea);

        final ViewerApp trafficApplet = new ViewerApp();

        // Add the applet to the frame
        frame.add(trafficApplet, BorderLayout.CENTER);
        // Initialize and start the applet
        trafficApplet.init(); // simulate browser call(1)

        final ResourceBundle resourceBundle = ResourceBundle.getBundle(LocalizationStrings.class.getName(),
                Locale.getDefault());

//        final MovSimMenu trafficMenus = new MovSimMenu(trafficUi, frame, resourceBundle);

        final boolean isWithToolbar = true;
//        if (isWithToolbar) {
//            frame.add(new MovSimToolBar(trafficUi, resourceBundle), BorderLayout.NORTH);
//        }

//        trafficMenus.initMenus();

        trafficApplet.validate();
        trafficApplet.repaint();
    }
}
