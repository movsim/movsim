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

import org.movsim.input.InputData;
import org.movsim.input.model.VehiclesInput;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
import org.movsim.input.model.vehicle.VehicleInput;
import org.movsim.simulator.vehicles.consumption.FuelConsumption;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelFactory;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumProperties;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumPropertiesFactory;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class VehicleGenerator.
 */
public class VehicleGenerator {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(VehicleGenerator.class);

    private final double simulationTimestep;

    private final boolean isWithReactionTimes;

    private final HashMap<String, VehiclePrototype> prototypes;

    private final FuelConsumptionModelPool fuelConsumptionModels;

    /**
     * Instantiates a new vehicle generator. And writes fundamental diagram to file system if the param
     * instantaneousFileOutput is true.
     * 
     */
    public VehicleGenerator(double simulationTimestep, VehiclesInput vehiclesInput,
            List<TrafficCompositionInputData> defaultHeterogenInputData, FuelConsumptionModelPool fuelConsumptionModelPool) {
        this.simulationTimestep = simulationTimestep;

        final Map<String, VehicleInput> vehInputMap = InputData.createVehicleInputDataMap(vehiclesInput
                .getVehicleInput());

        prototypes = createPrototypes(vehInputMap, defaultHeterogenInputData);

        isWithReactionTimes = checkForReactionTimes();

        this.fuelConsumptionModels = fuelConsumptionModelPool;
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
            final LongitudinalModelBase longModel = LongitudinalModelFactory.create(vehLength,
                    vehInput.getAccelerationModelInputData(), simulationTimestep);
            final EquilibriumProperties fundDia = EquilibriumPropertiesFactory.create(vehLength, longModel);
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
        return prototypes;
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
        for (final VehiclePrototype prototype : prototypes.values()) {
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
        for (final VehiclePrototype prototype : prototypes.values()) {
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
        final LongitudinalModelBase longModel = LongitudinalModelFactory.create(prototype.length(),
                vehInput.getAccelerationModelInputData(), simulationTimestep);

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
        if (!prototypes.containsKey(typeLabel)) {
            logger.error("cannot create vehicle. label = {} not defined. exit. ", typeLabel);
            System.exit(-1);
        }
        final VehiclePrototype prototype = prototypes.get(typeLabel);
        logger.debug("create vehicle with label = {}", typeLabel);
        return createVehicle(prototype);
    }
}
