package org.movsim.ui.controller.impl;

import org.movsim.simulator.Simulator;
import org.movsim.ui.controller.Controller;

public class SimulatorController extends Controller {

    private Simulator model;
    private Thread simThread;

    public SimulatorController(Simulator model) {
        this.model = model;

        initLocalizationAndLogger();
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

    /* (non-Javadoc)
     * @see org.movsim.ui.controller.Controller#initializeModel()
     */
    @Override
    public void initializeModel() {
        model.initialize();        
    }

}
