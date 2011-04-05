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

/**
 * 
 * @author Ralph Germ, Arne Kesting
 * 
 *
 */
public class App extends JApplet implements AppletStub {

    // Define a static logger variable
    // Logging with slf4j, a facade for log4j
    final static Logger logger = LoggerFactory.getLogger(App.class);

    final static String xmlDefault = "sim/startStop_IDM.xml";
    
   

    protected AppletStub appletstub;

    /**
     * Only called if programm is running as application. If it as run as an
     * applet the method init() is called.
     * 
     * @param args
     */
    public static void main(String[] args) {

        App universalprogram = new App();
        universalprogram.runAsAplication(args);

    }

    /**
     * Called when this applet is loaded into a browser or AppletViewer.
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

        Thread appletThread = new Thread(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub
                try {
                    Class appletClass = Class.forName(appletToLoad);
                    JApplet realApplet = (JApplet) appletClass.newInstance();
                    realApplet.setStub(appletstub);
                    setLayout(new GridLayout(1, 0));
                    add(realApplet);
                    realApplet.init();
                    realApplet.start();
                } catch (Exception e) {
                    System.out.println(e);
                }
                validate();

            }
        });

        appletThread.run();

        super.init();
    }

    /**
     * @param args
     * 
     */
    private void runAsAplication(String[] args) {

        initLocalizationAndLogger();

        // CommandLine args options Parser
        String xmlFilename;
        SimCommandLine cmdline = new SimCommandLineImpl(args);
        if (cmdline.isWithSimulation()) {
            xmlFilename = cmdline.getSimulationFilename();
        } else {
            xmlFilename = xmlDefault;
        }

        InputDataImpl inputData = new InputDataImpl();

        // parse xmlFile and set values in InputBean
        XmlReaderSimInput xmlReader = new XmlReaderSimInput(xmlFilename, inputData);

        Simulator simulator = new SimulatorImpl(cmdline.isGui(), inputData);
        
        if (cmdline.isGui()) {
            GUISwing gui = new GUISwing(inputData, simulator);
        } else {
            // without graphics 
            simulator.run();
        }

    }

    /**
     * 
     */
    private static void initLocalizationAndLogger() {
        Locale.setDefault(Locale.US);

        // BasicConfigurator for log4j replaced with PropertyConfigurator.
        PropertyConfigurator.configure("sim/log4j.properties");

        // Log Levels: DEBUG < INFO < WARN < ERROR
        logger.info("Copyright '\u00A9' by Arne Kesting  <mail@akesting.de>, Martin Treiber <treibi@mtreiber.de> and Ralph Germ <ralph@ralphgerm.de> (2010) ]");
    }

    public void paint(Graphics g) {
        g.drawString("Loading the BIG ONE ...", 30, 30);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.applet.AppletStub#appletResize(int, int)
     */
    public void appletResize(int width, int height) {
        resize(width, height);
    }

}
