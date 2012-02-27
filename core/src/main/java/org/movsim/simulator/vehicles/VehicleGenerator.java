/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.movsim.input.InputData;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputData;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataACC;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataCCS;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataGipps;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataIDM;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataKKW;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataKrauss;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNSM;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNewell;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataOVM_FVDM;
import org.movsim.simulator.vehicles.consumption.FuelConsumption;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.ACC;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.CCS;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.Gipps;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.IDM;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.KKW;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.Krauss;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.NSM;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.Newell;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.OVM_FVDM;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumACC;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumCCS;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumGipps;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumIDM;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumKKW;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumKrauss;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumNSM;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumNewell;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumOVM_FVDM;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumProperties;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class VehicleGenerator.
 */
public class VehicleGenerator {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(VehicleGenerator.class);

    private final HashMap<String, VehiclePrototype> defaultPrototypes;

    private final double simulationTimestep;

    private final boolean isWithReactionTimes;

    private final ConsumptionModeling fuelConsumptionModels;

    private HashMap<String, HashMap<String, VehiclePrototype>> allRoadPrototypes;

    /**
     * Instantiates a new vehicle generator. And writes fundamental diagram to file system if the param
     * instantaneousFileOutput is true.
     * 
     */
    public VehicleGenerator(double simulationTimestep, InputData simInput,
            List<TrafficCompositionInputData> defaultHeterogenInputData) {
        this.simulationTimestep = simulationTimestep;
        
        final List<VehicleInput> vehicleInputData = simInput.getVehicleInputData();
        final Map<String, VehicleInput> vehInputMap = InputData.createVehicleInputDataMap(vehicleInputData);
        
        allRoadPrototypes = createAllPrototypesForEachRoadWithTrafficComposition(simInput, vehInputMap);

        defaultPrototypes = createPrototypes(vehInputMap, defaultHeterogenInputData);

        isWithReactionTimes = checkForReactionTimes();

        fuelConsumptionModels = new ConsumptionModeling(simInput.getFuelConsumptionInput());
    }

    /**
     * @param simInput
     * @param vehInputMap
     */
    private HashMap<String, HashMap<String,VehiclePrototype>> createAllPrototypesForEachRoadWithTrafficComposition(InputData simInput,
            final Map<String, VehicleInput> vehInputMap) {
        HashMap<String, HashMap<String,VehiclePrototype>> allPrototypes = new HashMap<String, HashMap<String,VehiclePrototype>>();
        Set<String> keys = simInput.getSimulationInput().getRoadInput().keySet();
        for (String key: keys) {
            RoadInput roadInput = simInput.getSimulationInput().getRoadInput().get(key);
            List<TrafficCompositionInputData> trafficCompositionData = roadInput.getTrafficCompositionInputData();
            if (trafficCompositionData != null) {
                HashMap<String, VehiclePrototype> protoTypesForRoad = createPrototypes(vehInputMap, trafficCompositionData);
                allPrototypes.put(key, protoTypesForRoad);
            }
        }
        return allPrototypes;
    }

    /**
     * Creates the prototypes.
     * 
     * @return the double
     */
    private HashMap<String, VehiclePrototype> createPrototypes(Map<String, VehicleInput> vehInputMap,
            List<TrafficCompositionInputData> heterogenInputData) {
        final HashMap<String, VehiclePrototype> prototypes = new HashMap<String, VehiclePrototype>();

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
            final LongitudinalModelBase longModel = longitudinalModelFactory(vehInput.getAccelerationModelInputData(),
                    vehLength);

            final EquilibriumProperties fundDia = fundDiagramFactory(vehLength, longModel);

            final double fraction = heterogen.getFraction();
            logger.debug("fraction = {}", fraction);

            sumFraction += fraction;
            final double relRandomizationV0 = heterogen.getRelativeRandomizationDesiredSpeed();
            final VehiclePrototype vehProto = new VehiclePrototype(keyName, fraction, longModel, fundDia, vehInput,
                    relRandomizationV0);
            prototypes.put(keyName, vehProto);

        }
        // normalize heterogeneity fractions
        normalizeFractions(sumFraction, prototypes);
        return prototypes;
    }

    public HashMap<String, VehiclePrototype> prototypes() {
        return defaultPrototypes;
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
    private static EquilibriumProperties fundDiagramFactory(double vehLength, LongitudinalModelBase longModel) {
        if (longModel.modelName() == ModelName.IDM) {
            return new EquilibriumIDM(vehLength, (IDM) longModel);
        } else if (longModel.modelName() == ModelName.ACC) {
            return new EquilibriumACC(vehLength, (ACC) longModel);
        } else if (longModel.modelName() == ModelName.OVM_FVDM) {
            return new EquilibriumOVM_FVDM(vehLength, (OVM_FVDM) longModel);
        } else if (longModel.modelName() == ModelName.GIPPS) {
            return new EquilibriumGipps(vehLength, (Gipps) longModel);
        } else if (longModel.modelName() == ModelName.NEWELL) {
            return new EquilibriumNewell(vehLength, (Newell) longModel);
        } else if (longModel.modelName() == ModelName.KRAUSS) {
            return new EquilibriumKrauss(vehLength, (Krauss) longModel);
        } else if (longModel.modelName() == ModelName.NSM) {
            return new EquilibriumNSM(vehLength, (NSM) longModel);
        } else if (longModel.modelName() == ModelName.KKW) {
            return new EquilibriumKKW(vehLength, (KKW) longModel);
        } else if (longModel.modelName() == ModelName.CCS) {
            return new EquilibriumCCS(vehLength, (CCS) longModel);
        } else {
            logger.error("no fundamental diagram constructed for model {}. exit.", longModel.modelName().name());
            System.exit(0);
        }
        return null; // should not be reached after exit

    }

    /**
     * Long model factory with vehicle length vehicle length is only needed for KKW (explicit model parameter).
     * 
     * @param modelInputData
     *            the model input data
     * @param vehLength
     *            the vehicle length
     * @return the longitudinal model
     */
    private LongitudinalModelBase longitudinalModelFactory(LongitudinalModelInputData modelInputData, double vehLength) {
        final ModelName modelName = modelInputData.getModelName();
        LongitudinalModelBase longModel = null;
        // logger.debug("modelName = {}", modelName);
        if (modelName == ModelName.IDM) {
            longModel = new IDM((LongitudinalModelInputDataIDM) modelInputData);
        } else if (modelName == ModelName.ACC) {
            longModel = new ACC((LongitudinalModelInputDataACC) modelInputData);
        } else if (modelName == ModelName.OVM_FVDM) {
            longModel = new OVM_FVDM((LongitudinalModelInputDataOVM_FVDM) modelInputData);
        } else if (modelName == ModelName.GIPPS) {
            longModel = new Gipps(simulationTimestep, (LongitudinalModelInputDataGipps) modelInputData);
        } else if (modelName == ModelName.KRAUSS) {
            longModel = new Krauss(simulationTimestep, (LongitudinalModelInputDataKrauss) modelInputData);
        } else if (modelName == ModelName.NEWELL) {
            return new Newell(simulationTimestep, (LongitudinalModelInputDataNewell) modelInputData);
        } else if (modelName == ModelName.NSM) {
            longModel = new NSM((LongitudinalModelInputDataNSM) modelInputData);
        } else if (modelName == ModelName.KKW) {
            longModel = new KKW((LongitudinalModelInputDataKKW) modelInputData, vehLength);
        } else if (modelName == ModelName.CCS) {
            longModel = new CCS((LongitudinalModelInputDataCCS) modelInputData, vehLength);
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
    private static void normalizeFractions(double sumFraction, HashMap<String, VehiclePrototype> prototypes) {
        for (final VehiclePrototype prototype : prototypes.values()) {
            final double fraction = prototype.fraction();
            prototype.setFraction(fraction / sumFraction);
        }
    }

    /**
     * Check for reaction times.
     * 
     * @return true, if successful
     */
    private boolean checkForReactionTimes() {
        for (final VehiclePrototype prototype : defaultPrototypes.values()) {
            if (prototype.hasReactionTime()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the vehicle prototype.
     * 
     * @return the vehicle prototype
     */
    public VehiclePrototype getVehiclePrototype() {
        final double randomNumber = MyRandom.nextDouble();
        double sumFraction = 0;
        for (final VehiclePrototype prototype : defaultPrototypes.values()) {
            sumFraction += prototype.fraction();
            if (sumFraction >= randomNumber) {
                return prototype;
            }
        }
        logger.error("no vehicle prototype found for randomNumber= {}", randomNumber);
        System.exit(-1);
        return null; // not reached after exit
    }
    
    public VehiclePrototype getVehiclePrototype(String roadId) {
        HashMap<String, VehiclePrototype> protos = allRoadPrototypes.get(roadId);
        if (protos == null) {
            System.out.println("default");
            return getVehiclePrototype(); //default
        }
        
        final double randomNumber = MyRandom.nextDouble();
        double sumFraction = 0;
        for (final VehiclePrototype prototype : protos.values()) {
            sumFraction += prototype.fraction();
            if (sumFraction >= randomNumber) {
                return prototype;
            }
        }
        logger.error("no vehicle prototype found for randomNumber= {}", randomNumber);
        System.exit(-1);
        return null; // not reached after exit
    }

    /**
     * Creates the vehicle.
     * 
     * @param prototype
     *            the prototype
     * @return the vehicle
     */
    public Vehicle createVehicle(VehiclePrototype prototype) {
        final VehicleInput vehInput = prototype.getVehicleInput();
        final LongitudinalModelBase longModel = longitudinalModelFactory(vehInput.getAccelerationModelInputData(),
                prototype.length());

        longModel.setRelativeRandomizationV0(prototype.getRelativeRandomizationV0());

        final LaneChangeModel lcModel = new LaneChangeModel(vehInput.getLaneChangeInputData());
        final FuelConsumption fuelModel = fuelConsumptionModels.getFuelConsumptionModel(vehInput
                .getFuelConsumptionLabel());
        final Vehicle veh = new Vehicle(prototype.getLabel(), longModel, vehInput, null, lcModel, fuelModel);
        return veh;
    }

    /**
     * Creates the vehicle.
     * 
     * @return the vehicle
     */
    public Vehicle createVehicle() {
        final VehiclePrototype prototype = getVehiclePrototype();
        return createVehicle(prototype);
    }

    /**
     * Creates the vehicle.
     * 
     * @param typeLabel
     *            the type
     */
    public Vehicle createVehicle(String typeLabel) {
        if (!defaultPrototypes.containsKey(typeLabel)) {
            logger.error("cannot create vehicle. label = {} not defined. exit. ", typeLabel);
            System.exit(-1);
        }
        final VehiclePrototype prototype = defaultPrototypes.get(typeLabel);
        logger.debug("create vehicle with label = {}", typeLabel);
        return createVehicle(prototype);
    }
}
