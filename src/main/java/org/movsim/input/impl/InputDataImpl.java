/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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


public class InputDataImpl implements InputData {
    
    final static Logger logger = LoggerFactory.getLogger(InputDataImpl.class);

	private String projectName;

	private boolean withFileOutputScenario = false; // default gesetzt

	private List<VehicleInput> vehicleInputData;
	
	private OutputInput outputInput;

    private SimulationInput simulationInput;
	
	
	public InputDataImpl() {
	    // empty constructor
	}
//
//	public boolean isWithFileOutputScenario() {
//		return withFileOutputScenario;
//	}
//
//	public void setWithFileOutputScenario(boolean withFileOutputScenario) {
//		this.withFileOutputScenario = withFileOutputScenario;
//	}


	protected void setProjectName(String projectname) {
		this.projectName = projectname;
		logger.debug("Projectname in Bean: {}", projectname);
	}

	public String getProjectName() {
		return projectName;
	}

    public void setVehicleInputData(List<VehicleInput> vehicleInputData){
        this.vehicleInputData = vehicleInputData;
    }
    
    public List<VehicleInput> getVehicleInputData() {
        return vehicleInputData; 
    }

    public OutputInput getOutputInput() {
        return outputInput;
    }

    public SimulationInput getSimulationInput() {
        return simulationInput;
    }
    
    public void setOutputInput(OutputInput outputInput) {
        this.outputInput = outputInput;
    }

    public void setSimulationInput(SimulationInput simulationInput) {
        this.simulationInput = simulationInput;
    }

}
