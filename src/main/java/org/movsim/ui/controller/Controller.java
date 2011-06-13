package org.movsim.ui.controller;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.App;

public abstract class Controller {


    public void initLocalizationAndLogger() {
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
    
    public abstract void initializeModel();
    
    public abstract void start();

    public abstract void stop();

    public abstract void pause();

    // void reset();
    
    //void update();

}
