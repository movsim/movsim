package org.movsim.simulator.vehicles;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.consumption.model.EnergyFlowModel;
import org.movsim.consumption.model.EnergyFlowModelFactory;
import org.movsim.core.autogen.MovsimScenario;
import org.movsim.core.autogen.VehiclePrototypeConfiguration;
import org.movsim.output.fileoutput.FileFundamentalDiagram;
import org.movsim.simulator.roadnetwork.Route;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

// singleton property
public final class VehicleFactory {
    private static final Logger LOG = LoggerFactory.getLogger(VehicleFactory.class);
    
    private final Map<String, VehiclePrototype> vehiclePrototypes = new HashMap<>();

    private Map<String, Route> routes;

    private final boolean writeFundamentalDiagrams;

    EnergyFlowModelFactory fuelModelFactory = new EnergyFlowModelFactory();

    public VehicleFactory(double simulationTimestep, MovsimScenario inputData) {
        Preconditions.checkNotNull(inputData);
        Preconditions.checkNotNull(inputData.getVehiclePrototypes());

        this.writeFundamentalDiagrams = inputData.getVehiclePrototypes().isWriteFundDiagrams();
        initialize(simulationTimestep, inputData.getVehiclePrototypes().getVehiclePrototypeConfiguration());

        // TODO
        // fuelModelFactory.add(models);
    }
    
    public boolean isWriteFundamentalDiagrams() {
        return writeFundamentalDiagrams;
    }

    public Vehicle create(VehicleType vehicleType) {
        VehiclePrototype prototype = getPrototype(vehicleType.getVehiclePrototypeLabel());
        
        // route
        Route route = vehicleType.hasRouteLabel() ? routes.get(vehicleType.getRouteLabel()) : null;
        
        // acceleration model
        LongitudinalModelBase accelerationModel = prototype.createAccelerationModel();
        accelerationModel.setRelativeRandomizationV0(vehicleType.getRelativeV0Randomization());
        
        LaneChangeModel laneChangeModel = prototype.createLaneChangeModel();
        
        Vehicle vehicle = new Vehicle(prototype.getLabel(), accelerationModel, prototype.getConfiguration(),
                laneChangeModel, route);

        vehicle.setMemory(prototype.createMemoryModel());
        vehicle.setNoise(prototype.createAccNoiseModel());
        EnergyFlowModel energyFlowModel = fuelModelFactory.get(prototype.getLabel());
        if (energyFlowModel != null) {
            vehicle.setFuelModel(energyFlowModel);
        }
        return vehicle;
    }

    private void initialize(double simulationTimestep, List<VehiclePrototypeConfiguration> configurations) {
        for (VehiclePrototypeConfiguration type : configurations) {
            if (vehiclePrototypes.containsKey(type.getLabel())) {
                throw new IllegalArgumentException("ambigous vehicle prototype definition: prototype with label=\""
                        + type.getLabel() + "\" already exists.");
            }
            vehiclePrototypes.put(type.getLabel(), new VehiclePrototype(simulationTimestep, type));
        }
    }

    public VehiclePrototype getPrototype(String label) {
        if (!vehiclePrototypes.containsKey(label)) {
            throw new IllegalArgumentException("cannot create vehicle for unknown label =\""
                    + label);
        }
        return vehiclePrototypes.get(label);
    }

    public Iterable<String> getLabels() {
        return Collections.unmodifiableCollection(vehiclePrototypes.keySet());
    }
    
    public void writeFundamentalDiagrams(double simulationTimestep) {
        if (!isWriteFundamentalDiagrams()) {
            return;
        }
        
        final String ignoreLabel = "Obstacle"; // quick hack TODO remove hack
        
        LOG.info("write fundamental diagrams but ignore label {}.", ignoreLabel);
        for (VehiclePrototype vehiclePrototype : vehiclePrototypes.values()) {
            if (!ignoreLabel.equalsIgnoreCase(vehiclePrototype.getLabel())) {
                FileFundamentalDiagram.writeToFile(simulationTimestep, vehiclePrototype);
            }
        }
    }

}
