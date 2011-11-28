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
package org.movsim.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.consumption.FuelConsumptionInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InputData {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(InputData.class);

    private List<VehicleInput> vehicleInputData;

    private SimulationInput simulationInput;
    
    private FuelConsumptionInput fuelConsumptionInput;

    private ProjectMetaData projectMetaData;

    
    /**
     * Instantiates a new inputdata.
     */
    public InputData() {
        projectMetaData = ProjectMetaData.getInstance();
    }

    public void setProjectName(String projectname) {
        this.projectMetaData.setProjectName(projectname);
    }

    public void setVehicleInputData(List<VehicleInput> vehicleInputData) {
        this.vehicleInputData = vehicleInputData;
    }

    public List<VehicleInput> getVehicleInputData() {
        return vehicleInputData;
    }

    public Map<String, VehicleInput> createVehicleInputDataMap() {
        final HashMap<String, VehicleInput> map = new HashMap<String, VehicleInput>();
        for (final VehicleInput vehInput : vehicleInputData) {
            final String keyName = vehInput.getLabel();
            map.put(keyName, vehInput);
        }
        return map;
    }

    public SimulationInput getSimulationInput() {
        return simulationInput;
    }

    public void setSimulationInput(SimulationInput simulationInput) {
        this.simulationInput = simulationInput;
    }

    public ProjectMetaData getProjectMetaData() {
        return projectMetaData;
    }

    public void setFuelConsumptionInput(FuelConsumptionInput fuelConsumptionInput) {
        this.fuelConsumptionInput = fuelConsumptionInput;
    }

    public FuelConsumptionInput getFuelConsumptionInput() {
        return fuelConsumptionInput;
    }

}
