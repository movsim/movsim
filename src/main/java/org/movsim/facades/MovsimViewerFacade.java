package org.movsim.facades;

import java.net.URL;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.App;
import org.movsim.input.impl.InputDataImpl;
import org.movsim.input.impl.ProjectMetaDataImpl;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.impl.SimulatorImpl;
import org.movsim.simulator.roadSection.RoadSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovsimViewerFacade {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(MovsimViewerFacade.class);

    public void initLocalizationAndLogger() {
        final URL log4jConfig = App.class.getResource("/sim/log4j.properties");
        PropertyConfigurator.configure(log4jConfig);
    }

    // private SimulatorView view;
    private final Simulator model;

    // private Thread simThread;

    private InputDataImpl inputData;

    private ProjectMetaDataImpl projectMetaDataImpl;

    public MovsimViewerFacade() {

        model = new SimulatorImpl();

        // init Logger
        initLocalizationAndLogger();

        inputData = (InputDataImpl) model.getSimInput();
        projectMetaDataImpl = inputData.getProjectMetaDataImpl();

        // TODO set project config
        projectMetaDataImpl.setInstantaneousFileOutput(false);
        projectMetaDataImpl.setXmlFromResources(true);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.ui.controller.Controller#initializeModel()
     */
    public void initializeModel() {
        model.initialize();
    }

    public void loadScenarioFromXml(String scenario) {
        String xmlFileName = "/sim/" + scenario + ".xml";
        inputData.setProjectName(xmlFileName);
        initializeModel();
    }

    public void reset() {
        model.reset();
    }

    public void update() {
        model.update();
    }
    
    public boolean isSimulationRunFinished(){
        return model.isSimulationRunFinished();
    }
    
    public RoadSection findRoadById(long id) {
        return model.findRoadById(id);
    }

    public List<RoadSection> getRoadSections() {
        return model.getRoadSections();
    }

    public double getTimestep() {
        return model.timestep();
    }

    public long getIterationCount() {
        return model.iterationCount();
    }

    public double getSimulationTime() {
        return model.time();
    }

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

}
