/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.ui.controller;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.App;

// TODO: Auto-generated Javadoc
/**
 * The Class Controller.
 */
public abstract class Controller {

    /**
     * Inits the localization and logger.
     */
    public void initLocalizationAndLogger() {
        Locale.setDefault(Locale.US);

        final File file = new File("log4j.properties");
        if (file.exists() && file.isFile()) {
            PropertyConfigurator.configure("log4j.properties");
        } else {
            final URL log4jConfig = App.class.getResource(File.separator + "sim" + File.separator + "log4j.properties"); //TODO check for Windos
            PropertyConfigurator.configure(log4jConfig);
        }

        // Log Levels: DEBUG < INFO < WARN < ERROR;
    }

    /**
     * Initialize model.
     */
    public abstract void initializeModel();

    /**
     * Start.
     */
    public abstract void start();

    /**
     * Reset.
     */
    public abstract void reset();

    /**
     * Pause.
     */
    public abstract void pause();

    // void update();

}
