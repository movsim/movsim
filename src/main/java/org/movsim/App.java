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
package org.movsim;

import java.applet.AppletStub;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.Locale;

import javax.swing.JApplet;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.input.commandline.SimCommandLine;
import org.movsim.input.commandline.impl.SimCommandLineImpl;
import org.movsim.input.impl.InputDataImpl;
import org.movsim.input.impl.XmlReaderSimInput;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.impl.SimulatorImpl;
import org.movsim.ui.desktop.GUISwing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class App.
 */
public class App extends JApplet implements AppletStub {

    // Define a static logger variable
    // Logging with slf4j, a facade for log4j
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(App.class);

    /** The Constant xmlDefault. */
    final static String xmlDefault = "sim/startStop_IDM.xml";

    /** The appletstub. */
    protected AppletStub appletstub;

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {

        final App universalprogram = new App();
        universalprogram.runAsAplication(args);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.applet.Applet#init()
     */
    @Override
    public void init() {

        appletstub = this;

        // initLocalizationAndLogger();

        // InputBean mit Default-Werten erstellen
        // SimInputDataImpl simInput = new SimInputDataImpl();

        // parse xmlFile and set values in InputBean
        // XmlReaderSimInput xmlReader = new XmlReaderSimInput(xmlDefault,
        // simInput);

        // String appletToLoad = getParameter("appletToLoad");
        final String appletToLoad = "org.movsim.ui.japplet.GUIJApplet";
        setBackground(Color.white);

        final Thread appletThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final Class appletClass = Class.forName(appletToLoad);
                    final JApplet realApplet = (JApplet) appletClass.newInstance();
                    realApplet.setStub(appletstub);
                    setLayout(new GridLayout(1, 0));
                    add(realApplet);
                    realApplet.init();
                    realApplet.start();
                } catch (final Exception e) {
                    System.out.println(e);
                }
                validate();

            }
        });

        appletThread.run();

        super.init();
    }

    /**
     * Run as aplication.
     * 
     * @param args
     *            the args
     */
    private void runAsAplication(String[] args) {

        initLocalizationAndLogger();

        // CommandLine args options Parser
        String xmlFilename;
        final SimCommandLine cmdline = new SimCommandLineImpl(args);
        if (cmdline.isWithSimulation()) {
            xmlFilename = cmdline.getSimulationFilename();
        } else {
            xmlFilename = xmlDefault;
        }

        final InputDataImpl inputData = new InputDataImpl();

        // parse xmlFile and set values in InputBean
        final XmlReaderSimInput xmlReader = new XmlReaderSimInput(xmlFilename, inputData);

        final Simulator simulator = new SimulatorImpl(cmdline.isGui(), inputData);

        if (cmdline.isGui()) {
            final GUISwing gui = new GUISwing(inputData, simulator);
        } else {
            // without graphics
            simulator.run();
        }

    }

    /**
     * Inits the localization and logger.
     */
    private static void initLocalizationAndLogger() {
        Locale.setDefault(Locale.US);

        // BasicConfigurator for log4j replaced with PropertyConfigurator.
        PropertyConfigurator.configure("sim/log4j.properties");

        // Log Levels: DEBUG < INFO < WARN < ERROR
        logger.info("Copyright '\u00A9' by Arne Kesting, Martin Treiber, Ralph Germ and  Martin Budden (2010, 2011) ]");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Container#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        g.drawString("Loading the BIG ONE ...", 30, 30);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.applet.AppletStub#appletResize(int, int)
     */
    @Override
    public void appletResize(int width, int height) {
        resize(width, height);
    }

}
