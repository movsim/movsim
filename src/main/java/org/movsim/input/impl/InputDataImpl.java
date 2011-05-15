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
package org.movsim.input.impl;

import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.VehicleInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class InputDataImpl.
 * 
 * @author Arne, Ralph
 */
public class InputDataImpl implements InputData {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(InputDataImpl.class);

    /** The project name. */
    private String projectName;

//    private final boolean withFileOutputScenario = false; // default gesetzt

    /** The vehicle input data. */
private List<VehicleInput> vehicleInputData;


    /** The simulation input. */
    private SimulationInput simulationInput;

    /**
     * Instantiates a new input data impl.
     */
    public InputDataImpl() {
        // empty constructor
    }

    //
    // public boolean isWithFileOutputScenario() {
    // return withFileOutputScenario;
    // }
    //
    // public void setWithFileOutputScenario(boolean withFileOutputScenario) {
    // this.withFileOutputScenario = withFileOutputScenario;
    // }

    /**
     * Sets the project name.
     * 
     * @param projectname
     *            the new project name
     */
    protected void setProjectName(String projectname) {
        this.projectName = projectname;
        logger.debug("Projectname: {}", projectname);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.InputData#getProjectName()
     */
    @Override
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the vehicle input data.
     * 
     * @param vehicleInputData
     *            the new vehicle input data
     */
    public void setVehicleInputData(List<VehicleInput> vehicleInputData) {
        this.vehicleInputData = vehicleInputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.InputData#getVehicleInputData()
     */
    @Override
    public List<VehicleInput> getVehicleInputData() {
        return vehicleInputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.InputData#getSimulationInput()
     */
    @Override
    public SimulationInput getSimulationInput() {
        return simulationInput;
    }

    /**
     * Sets the simulation input.
     * 
     * @param simulationInput
     *            the new simulation input
     */
    public void setSimulationInput(SimulationInput simulationInput) {
        this.simulationInput = simulationInput;
    }

}
