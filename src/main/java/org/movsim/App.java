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

import org.movsim.input.impl.SimCommandLine;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.impl.SimulatorImpl;
import org.movsim.ui.controller.Controller;
import org.movsim.ui.controller.impl.SimulatorController;

/**
 * The Class App.
 */
public class App {


    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        
        // CommandLine args options Parser
        // Results are set in ProjectMetaDataImpl
        final SimCommandLine cmdline = new SimCommandLine(args);

        final Simulator simulator = new SimulatorImpl();

        final Controller controller = new SimulatorController(simulator);
    }

}
