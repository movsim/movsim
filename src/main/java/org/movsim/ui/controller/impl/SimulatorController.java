/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.ui.controller.impl;

import org.movsim.simulator.Simulator;
import org.movsim.ui.controller.Controller;

// TODO: Auto-generated Javadoc
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

        // initLocalizationAndLogger(); // already initialized in SimCommanLine
        initializeModel();

        simThread = new Thread((Runnable) model);
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
