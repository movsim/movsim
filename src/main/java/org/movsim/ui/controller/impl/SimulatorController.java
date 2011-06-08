package org.movsim.ui.controller.impl;

import org.movsim.simulator.Simulator;
import org.movsim.ui.controller.Controller;

public class SimulatorController implements Controller {

    private Simulator model;
    private Thread simThread;

    public SimulatorController(Simulator model) {
        this.model = model;

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
    public void stop() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.desktop.ControllerInterface#pause()
     */
    @Override
    public void pause() {

    }

}
