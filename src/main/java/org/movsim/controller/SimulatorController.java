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
package org.movsim.controller;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.MovsimMain;
import org.movsim.simulator.Simulator;

/**
 * The Class SimulatorController.
 */
public class SimulatorController  {

    private Simulator model;
    private Thread simThread;

    /**
     * Instantiates a new simulator controller.
     * 
     * @param model
     *            the model
     */
    public SimulatorController(Simulator model) {
        this.model = model;

        model.initialize();

        simThread = new Thread(model);
        simThread.setName("movsim-thread");
        start();

    }
    
    /**
     * Inits the localization and logger.
     */
    public void initLocalizationAndLogger() {
        Locale.setDefault(Locale.US);

        final File file = new File("log4j.properties");
        if (file.exists() && file.isFile()) {
            PropertyConfigurator.configure("log4j.properties");
        } else {
            final URL log4jConfig = MovsimMain.class.getResource("/sim/log4j.properties");
            PropertyConfigurator.configure(log4jConfig);
        }

        // Log Levels: DEBUG < INFO < WARN < ERROR;
    }

    public void start() {
        simThread.start();
    }
}
