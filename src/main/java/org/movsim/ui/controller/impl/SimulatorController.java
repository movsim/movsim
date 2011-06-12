package org.movsim.ui.controller.impl;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.App;
import org.movsim.simulator.Simulator;
import org.movsim.ui.controller.Controller;

public class SimulatorController implements Controller {

    private Simulator model;
    private Thread simThread;

    public SimulatorController(Simulator model) {
        this.model = model;

        initLocalizationAndLogger();
        
        model.initialize();
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

    /**
     * Inits the localization and logger.
     */
    private static void initLocalizationAndLogger() {
        Locale.setDefault(Locale.US);
        final File file = new File("log4j.properties");
        if (file.exists() && file.isFile()) {
            PropertyConfigurator.configure("log4j.properties");
        } else {
            final URL log4jConfig = App.class.getResource("/sim/log4j.properties");
            PropertyConfigurator.configure(log4jConfig);
        }
        // Log Levels: DEBUG < INFO < WARN < ERROR;
    }

}
