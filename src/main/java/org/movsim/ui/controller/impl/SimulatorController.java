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
package org.movsim.ui.controller.impl;

import org.movsim.simulator.Simulator;
import org.movsim.ui.controller.Controller;

/**
 * The Class SimulatorController.
 */
public class SimulatorController extends Controller {

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

        initializeModel();

        simThread = new Thread((Runnable) model);
        simThread.setName("movsim-thread");
        start();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.desktop.ControllerInterface#start()
     */
    @Override
    public void start() {
        simThread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.desktop.ControllerInterface#stop()
     */
    @Override
    public void reset() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.desktop.ControllerInterface#pause()
     */
    @Override
    public void pause() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.controller.Controller#initializeModel()
     */
    @Override
    public void initializeModel() {
        model.initialize();
    }

}
