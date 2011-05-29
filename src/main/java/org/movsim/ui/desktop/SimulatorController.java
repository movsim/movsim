/**
 * 
 * Copyright (C) 2010 by Ralph Germ (http://www.ralphgerm.de)
 * 
 */
package org.movsim.ui.desktop;

import org.movsim.simulator.Simulator;

/**
 * @author ralph
 * 
 */
public class SimulatorController implements ControllerInterface {

    private SimulatorView view;
    private Simulator model;

    public SimulatorController(Simulator model) {
        this.model = model;
        view = new SimulatorView(model, this);
        view.createControls();
        view.createOutputViews();
        // model.initialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.desktop.ControllerInterface#start()
     */
    @Override
    public void start() {
        view.disableStart();
        view.enablePause();
        view.enableStop();
//        model.restart();
        model.run();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.desktop.ControllerInterface#stop()
     */
    @Override
    public void stop() {
        view.disableStop();
        view.enableStart();
        view.disablePause();
        // model.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.desktop.ControllerInterface#pause()
     */
    @Override
    public void pause() {
        view.enableStart();
        view.enableStop();
        view.disablePause();
    }

  

}
