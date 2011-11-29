/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.facades;

import java.net.URL;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.MovsimMain;
import org.movsim.input.InputData;
import org.movsim.input.ProjectMetaData;
import org.movsim.output.SimObservables;
import org.movsim.simulator.Simulator;
import org.movsim.utilities.impl.XYDataPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class MovsimViewerFacade.
 */
public class MovsimViewerFacade {

    /**
     * Initializaton on demand holder idiom. Lazy loaded singleton.
     */
    private static class Holder {
        private static final MovsimViewerFacade INSTANCE = new MovsimViewerFacade();
    }

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(MovsimViewerFacade.class);

    /**
     * Inits the localization and logger.
     */
    public void initLocalizationAndLogger() {
        final URL log4jConfig = MovsimMain.class.getResource("/sim/log4j.properties");
        PropertyConfigurator.configure(log4jConfig);
    }

    private final Simulator model;

    private final InputData inputData;

    private final ProjectMetaData projectMetaData;

    /**
     * Instantiates a new movsim viewer facade. Singleton pattern
     */
    private MovsimViewerFacade() {
        System.out.println("create movsim viewer facade");
        model = Simulator.getInstance();

        initLocalizationAndLogger();

        inputData = model.getSimInput();
        projectMetaData = inputData.getProjectMetaData();

        projectMetaData.setInstantaneousFileOutput(false);
        projectMetaData.setXmlFromResources(true);

    }

    public static MovsimViewerFacade getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Load scenario from xml.
     * 
     * @param scenario
     *            the scenario
     */
    public void loadScenarioFromXml(String scenario, String path, String xodrFilename, String xodrPath) {
        projectMetaData.setProjectName(scenario);
        projectMetaData.setPathToProjectXmlFile(path);
        projectMetaData.setOutputPath(path);
        projectMetaData.setXodrFilename(xodrFilename);
        projectMetaData.setXodrPath(xodrPath);
        model.initialize();
    }

    /**
     * Gets the duration of the simulation
     * 
     * @return the max sim time
     */
    public double getMaxSimTime() {
        return inputData.getSimulationInput().getMaxSimTime();
    }

    public List<List<XYDataPoint>> getTravelTimeEmas() {
        return model.getSimObservables().getTravelTimes().getTravelTimeEmas();
    }

    public List<Double> getTravelTimeDataEMAs(double time) {
        final double tauEMA = 40;
        return model.getSimObservables().getTravelTimes().getTravelTimesEMA(time, tauEMA);
    }

    public Simulator getSimulator() {
        return model;
    }

    public SimObservables getSimObservables() {
        return model.getSimObservables();
    }

    public ProjectMetaData getProjectMetaData() {
        return projectMetaData;
    }
}
