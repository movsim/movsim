package org.movsim.ui.controller.impl;

import org.movsim.simulator.Simulator;
import org.movsim.ui.controller.Controller;
import org.movsim.ui.controller.GUIContoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorGUIController implements Controller, GUIContoller {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulatorGUIController.class);

    // private SimulatorView view;
    private Simulator model;
    private Thread simThread;

    public SimulatorGUIController(Simulator model) {
        this.model = model;
        // view = new SimulatorView(this);

        // view.createControls();
        // view.createOutputViews();

        System.out.println("Not yet implemented. Use app without option -g. EXIT.");
        logger.error("Not yet implemented. Use app without option -g. EXIT.");
        System.exit(-1);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.desktop.ControllerInterface#start()
     */
    @Override
    public void start() {
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
