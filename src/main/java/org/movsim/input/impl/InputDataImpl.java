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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.input.InputData;
import org.movsim.input.ProjectMetaData;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.consumption.FuelConsumptionInput;
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

    /** The vehicle input data. */
    private List<VehicleInput> vehicleInputData;

    /** The simulation input. */
    private SimulationInput simulationInput;
    
    /** The fuel consumption input. */
    private FuelConsumptionInput fuelConsumptionInput;

    private ProjectMetaDataImpl projectMetaDataImpl;

    
    /**
     * Gets the project meta data impl.
     * 
     * @return the project meta data impl
     */
    public ProjectMetaDataImpl getProjectMetaDataImpl() {
        return projectMetaDataImpl;
    }

    /**
     * Instantiates a new input data impl.
     */
    public InputDataImpl() {
        projectMetaDataImpl = ProjectMetaDataImpl.getInstanceImpl();
    }

    /**
     * Sets the project name.
     * 
     * @param projectname
     *            the new project name
     */
    public void setProjectName(String projectname) {
        this.projectMetaDataImpl.setProjectName(projectname);
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

    @Override
    public List<VehicleInput> getVehicleInputData() {
        return vehicleInputData;
    }

    @Override
    public Map<String, VehicleInput> createVehicleInputDataMap() {
        final HashMap<String, VehicleInput> map = new HashMap<String, VehicleInput>();
        for (final VehicleInput vehInput : vehicleInputData) {
            final String keyName = vehInput.getLabel();
            map.put(keyName, vehInput);
        }
        return map;
    }

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

    @Override
    public ProjectMetaData getProjectMetaData() {
        return projectMetaDataImpl;
    }

    public void setFuelConsumptionInput(FuelConsumptionInput fuelConsumptionInput) {
        this.fuelConsumptionInput = fuelConsumptionInput;
    }

    @Override
    public FuelConsumptionInput getFuelConsumptionInput() {
        return fuelConsumptionInput;
    }

}
