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
package org.movsim.facades;

import java.net.URL;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.MovsimMain;
import org.movsim.input.impl.InputDataImpl;
import org.movsim.input.impl.ProjectMetaDataImpl;
import org.movsim.output.SimObservables;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.impl.SimulatorImpl;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.utilities.impl.XYDataPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class MovsimViewerFacade.
 */
public class MovsimViewerFacade {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(MovsimViewerFacade.class);

    // singleton pattern
    private static MovsimViewerFacade instance = null;
    
    /**
     * Inits the localization and logger.
     */
    public void initLocalizationAndLogger() {
        final URL log4jConfig = MovsimMain.class.getResource("/sim/log4j.properties");
        PropertyConfigurator.configure(log4jConfig);
    }

    private final Simulator model;

    private InputDataImpl inputData;

    private ProjectMetaDataImpl projectMetaDataImpl;

    /**
     * Instantiates a new movsim viewer facade.
     * Singleton pattern
     */
    
    private MovsimViewerFacade() {

        model = SimulatorImpl.getInstance();

        // init Logger
        initLocalizationAndLogger();

        inputData = (InputDataImpl) model.getSimInput();
        projectMetaDataImpl = inputData.getProjectMetaDataImpl();

        // TODO set project config
        projectMetaDataImpl.setInstantaneousFileOutput(false);
        projectMetaDataImpl.setXmlFromResources(true);

    }

    
    public static MovsimViewerFacade getInstance(){
        if(instance == null){
            instance = new MovsimViewerFacade();
        }
        return instance;
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.controller.Controller#initializeModel()
     */
    /**
     * Initialize model.
     */
    public void initializeModel() {
        model.initialize();
    }

    /**
     * Load scenario from xml.
     *
     * @param scenario the scenario
     */
    public void loadScenarioFromXml(String scenario) {
        String xmlFileName = "/sim/" + scenario + ".xml";
        inputData.setProjectName(xmlFileName);
        initializeModel();
    }

    /**
     * Reset.
     */
    public void reset() {
        model.reset();
    }

    /**
     * Update.
     */
    public void update() {
        model.update();
    }
    
    /**
     * Checks if is simulation run finished.
     *
     * @return true, if is simulation run finished
     */
    public boolean isSimulationRunFinished(){
        return model.isSimulationRunFinished();
    }
    
    /**
     * Find road by id.
     *
     * @param id the id
     * @return the road section
     */
    public RoadSection findRoadById(long id) {
        return model.findRoadById(id);
    }

    /**
     * Gets the road sections.
     *
     * @return the road sections
     */
    public List<RoadSection> getRoadSections() {
        return model.getRoadSections();
    }

    /**
     * Gets the timestep.
     *
     * @return the timestep
     */
    public double getTimestep() {
        return model.timestep();
    }

    /**
     * Gets the iteration count.
     *
     * @return the iteration count
     */
    public long getIterationCount() {
        return model.iterationCount();
    }

    /**
     * Gets the simulation time.
     *
     * @return the simulation time
     */
    public double getSimulationTime() {
        return model.time();
    }

    /**
     * Gets the max sim time.
     *
     * @return the max sim time
     */
    public double getMaxSimTime() {
        return inputData.getSimulationInput().getMaxSimTime();
    }

    // public double getMainroadInflow() {
    // // TODO Auto-generated method stub
    // return 0;
    // }
    //
    //
    // public double getRampInflow() {
    // // TODO Auto-generated method stub
    // return 0;
    // }
    
//    public List<List<XYDataPoint>> getTravelTimeDataRoutes(){
//        return model.getSimObservables().getTravelTimes().getTravelTimeDataRoutes();
//    }
    
    public List<Double> getTravelTimeDataEMAs(double time){
        final double tauEMA = 40;
        return model.getSimObservables().getTravelTimes().getTravelTimesEMA(time, tauEMA);
    }
    
    public SimObservables getSimObservables(){
        return model.getSimObservables();
    }
    

}
