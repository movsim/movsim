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
package org.movsim.simulator.vehicles;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.movsim.consumption.FuelConsumption;
import org.movsim.input.InputData;
import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputData;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataACC;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKrauss;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNewell;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataOVM_VDIFF;
import org.movsim.output.fileoutput.FileFundamentalDiagram;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.vehicles.lanechanging.LaneChangingModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.ACC;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.Gipps;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.IDM;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.KKW;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.Krauss;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.NSM;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.Newell;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.OVM_VDIFF;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelAbstract.ModelName;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumACC;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumGipps;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumIDM;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumKKW;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumKrauss;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumNSM;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumNewell;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumOVM_VDIFF;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumProperties;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class VehicleGeneratorImpl.
 */
public class VehicleGeneratorImpl implements VehicleGenerator {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(VehicleGeneratorImpl.class);

    // Aufwand mit prototypes wg. einmaliger berechnung des FD
    // Und ggf. einmaliger Neuberechnung FD nach Parameteraenderung !!

    /** The project name. */
    private final String projectName;

    // enthaelt die Menge der definierte Models ... notwendig fuer GUI
    // private HashMap<String, AccelerationModel> longModels;

    // enthaelt die Heterogenitaet der tatsaechlich simulierten Fahrzeuge
    /** The prototypes. */
    private final HashMap<String, VehiclePrototype> prototypes;

    /** The simulation timestep. */
    private double simulationTimestep;

    /** The is with reaction times. */
    private final boolean isWithReactionTimes;

    private boolean instantaneousFileOutput;
    
    private ConsumptionModeling fuelConsumptionModels;

    /**
     * Instantiates a new vehicle generator impl. And writes fundamental diagram
     * to file system if the param instantaneousFileOutput is true.
     * 
     * @param simInput
     *            the sim input
     */
    public VehicleGeneratorImpl(final InputData simInput, final List<TrafficCompositionInputData> heterogenInputData, boolean isWithWriteFundamentalDiagrams) {

        // TODO avoid access of simInput, heterogenInputData is from Simulation *or* from Road 
        this.projectName = simInput.getProjectMetaData().getProjectName();
        this.instantaneousFileOutput = simInput.getProjectMetaData().isInstantaneousFileOutput();

        // create vehicle prototyps according to traffic composition
        // (heterogeneity)
        prototypes = new HashMap<String, VehiclePrototype>();
        final double sumFraction = createPrototypes(simInput, heterogenInputData);

        // normalize heterogeneity fractions
        normalizeFractions(sumFraction);

        // output fundamental diagrams
        if (instantaneousFileOutput && isWithWriteFundamentalDiagrams) {
            FileFundamentalDiagram.writeFundamentalDiagrams(projectName, prototypes);
        }

        isWithReactionTimes = checkForReactionTimes();

        fuelConsumptionModels = new ConsumptionModeling(simInput.getFuelConsumptionInput());
    }

    /**
     * Creates the prototypes.
     * 
     * @param simInput
     *            the sim input
     * @return the double
     */
    private double createPrototypes(final InputData simInput, List<TrafficCompositionInputData> heterogenInputData) {

        // default for continuous micro models
        simulationTimestep = simInput.getSimulationInput().getTimestep();

        final Map<String, VehicleInput> vehInputMap = simInput.createVehicleInputDataMap();
        
        addObstacleSystemVehicleType(heterogenInputData);
        
        double sumFraction = 0;
        for (final TrafficCompositionInputData heterogen : heterogenInputData) {
            final String keyName = heterogen.getKeyName();
            logger.debug("key name={}", keyName);
            if (!vehInputMap.containsKey(keyName)) {
                logger.error("no corresponding vehicle found. check vehicle input with label={}", keyName);
                System.exit(-1);
            }
            final VehicleInput vehInput = vehInputMap.get(keyName);
            final double vehLength = vehInput.getLength();
            final AccelerationModel longModel = longModelFactory(vehInput.getAccelerationModelInputData(), vehLength);

            final EquilibriumProperties fundDia = fundDiagramFactory(vehLength, longModel);

            final double fraction = heterogen.getFraction();
            logger.debug("fraction = {}", fraction);

            sumFraction += fraction;
            final double relRandomizationV0 = heterogen.getRelativeRandomizationDesiredSpeed();
            final VehiclePrototype vehProto = new VehiclePrototype(keyName, fraction, longModel, fundDia, vehInput, relRandomizationV0);
            prototypes.put(keyName, vehProto);

        }
        return sumFraction;
    }
    
    
    // add Obstacle as permanent Vehicle_Type
    // first check if Obstacle is already part of user defined heterogeneity input 
    /**
     * Adds the obstacle system vehicle type.
     *
     * @param heterogenInputData the heterogen input data
     */
    private void addObstacleSystemVehicleType(List<TrafficCompositionInputData> heterogenInputData) {
        boolean obstacleEntryIsContained = false;
        for (TrafficCompositionInputData het : heterogenInputData) {
            if (het.getKeyName().equals(MovsimConstants.OBSTACLE_KEY_NAME)) {
                obstacleEntryIsContained = true;
            }
        }
        
        if (obstacleEntryIsContained) {
            logger.info("vehicle system type with keyname = {} for Obstacle in Heterogeneity already defined by user. do not overwrite", MovsimConstants.OBSTACLE_KEY_NAME);
        }
        else{
            logger.info("vehicle system type with keyname = {} for Obstacle will be automatically defined in Heterogeneity", MovsimConstants.OBSTACLE_KEY_NAME);
            final Map<String, String> mapEntryObstacle = new HashMap<String, String>();
            mapEntryObstacle.put("label", MovsimConstants.OBSTACLE_KEY_NAME);
            mapEntryObstacle.put("fraction", "0");
            mapEntryObstacle.put("relative_v0_randomization", "0");
            heterogenInputData.add(new TrafficCompositionInputData(mapEntryObstacle));
        } 
    }
    
    /**
     * Fund diagram factory.
     * 
     * @param vehLength
     *            the veh length
     * @param longModel
     *            the long model
     * @return the equilibrium properties
     */
    private EquilibriumProperties fundDiagramFactory(double vehLength, AccelerationModel longModel) {
        if (longModel.modelName() == ModelName.IDM)
            return new EquilibriumIDM(vehLength, (IDM) longModel);
        else if (longModel.modelName() == ModelName.ACC)
            return new EquilibriumACC(vehLength, (ACC) longModel);
        else if (longModel.modelName() == ModelName.OVM_VDIFF)
            return new EquilibriumOVM_VDIFF(vehLength, (OVM_VDIFF) longModel);
        else if (longModel.modelName() == ModelName.GIPPS)
            return new EquilibriumGipps(vehLength, (Gipps) longModel);
        else if (longModel.modelName() == ModelName.KRAUSS)
            return new EquilibriumKrauss(vehLength, (Krauss) longModel);
        else if (longModel.modelName() == ModelName.NEWELL)
            return new EquilibriumNewell(vehLength, (Newell) longModel);
        else if (longModel.modelName() == ModelName.NSM)
            return new EquilibriumNSM(vehLength, (NSM) longModel);
        else if (longModel.modelName() == ModelName.KKW)
            return new EquilibriumKKW(vehLength, (KKW) longModel);
        else {
            logger.error("no fundamental diagram constructed for model {}. exit.", longModel.modelName().name());
            System.exit(0);
        }
        return null; // should not be reached after exit

    }

    /**
     * Long model factory with vehicle length vehicle length is only needed for
     * KKW (explicit model parameter).
     * 
     * @param modelInputData
     *            the model input data
     * @param vehLength
     *            the veh length
     * @return the acceleration model
     */
    private AccelerationModel longModelFactory(AccelerationModelInputData modelInputData, double vehLength) {
        final ModelName modelName = modelInputData.getModelName();
        AccelerationModel longModel = null;
        // logger.debug("modelName = {}", modelName);
        if (modelName == ModelName.IDM) {
            longModel = new IDM( (AccelerationModelInputDataIDM) modelInputData);
        } else if (modelName == ModelName.ACC) {
            longModel = new ACC((AccelerationModelInputDataACC) modelInputData);
        } else if (modelName == ModelName.OVM_VDIFF) {
            longModel = new OVM_VDIFF((AccelerationModelInputDataOVM_VDIFF) modelInputData);
        } else if (modelName == ModelName.GIPPS) {
            longModel = new Gipps(simulationTimestep, (AccelerationModelInputDataGipps) modelInputData);
        } else if (modelName == ModelName.KRAUSS) {
            longModel = new Krauss(simulationTimestep, (AccelerationModelInputDataKrauss) modelInputData);
        } else if (modelName == ModelName.NEWELL) {
            return new Newell(simulationTimestep, (AccelerationModelInputDataNewell) modelInputData);
        } else if (modelName == ModelName.NSM) {
            longModel = new NSM((AccelerationModelInputDataNSM) modelInputData);
        } else if (modelName == ModelName.KKW) {
            longModel = new KKW((AccelerationModelInputDataKKW) modelInputData, vehLength);
        } else {
            logger.error("create model by inputParameter: Model {} not known !", modelName);
            System.exit(0);
        }
        return longModel;
    }

    /**
     * Normalize fractions.
     * 
     * @param sumFraction
     *            the sum fraction
     */
    private void normalizeFractions(double sumFraction) {
        final Iterator<String> it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            final double fraction = prototypes.get(key).fraction();
            prototypes.get(key).setFraction(fraction / sumFraction);
        }
    }

    /**
     * Check for reaction times.
     * 
     * @return true, if successful
     */
    private boolean checkForReactionTimes() {
        final Iterator<String> it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            final VehiclePrototype prototype = prototypes.get(key);
            if (prototype.hasReactionTime())
                return true;
        }
        return false;
    }

     /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.VehicleGenerator#getVehiclePrototype()
     */
    @Override
    public VehiclePrototype getVehiclePrototype() {
        final double randomNumber = MyRandom.nextDouble();
        double sumFraction = 0;
        final Iterator<String> it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            sumFraction += prototypes.get(key).fraction();
            if (sumFraction >= randomNumber)
                return prototypes.get(key);
        }
        logger.error("no vehicle prototype found for randomNumber= {}", randomNumber);
        System.exit(-1);
        return null; // not reached after exit
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.VehicleGenerator#createVehicle(org.movsim
     * .simulator.vehicles.VehiclePrototype)
     */
    @Override
    public Vehicle createVehicle(VehiclePrototype prototype) {
        final int vehID = MyRandom.nextInt(); // for veh index
        // final double length = prototype.length();
        // final double reactionTime = prototype.reactionTime();
        final VehicleInput vehInput = prototype.getVehicleInput();
        final AccelerationModel longModel = longModelFactory(vehInput.getAccelerationModelInputData(),
                prototype.length());
        
        longModel.setRelativeRandomizationV0(prototype.getRelativeRandomizationV0());
        
        // TODO lane-changing model impl
        final LaneChangingModel lcModel = new LaneChangingModel(vehInput.getLaneChangingInputData());
        final FuelConsumption fuelModel = fuelConsumptionModels.getFuelConsumptionModel(vehInput.getFuelConsumptionLabel());
        final Vehicle veh = new Vehicle(prototype.getLabel(), vehID, longModel, vehInput, null, lcModel, fuelModel);
        return veh;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.VehicleGenerator#createVehicle()
     */
    @Override
    public Vehicle createVehicle() {
        final VehiclePrototype prototype = getVehiclePrototype();
        return createVehicle(prototype);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.VehicleGenerator#createVehicle(java.lang
     * .String)
     */
    @Override
    public Vehicle createVehicle(String typeLabel) {
        if (!prototypes.containsKey(typeLabel)) {
            logger.error("cannot create vehicle. label = {} not defined. exit. ", typeLabel);
            System.exit(-1);
        }
        final VehiclePrototype prototype = prototypes.get(typeLabel);
        logger.debug("create vehicle with label = {}", typeLabel);
        return createVehicle(prototype);
    }

    

}
