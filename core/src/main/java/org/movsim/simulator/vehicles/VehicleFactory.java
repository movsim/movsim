package org.movsim.simulator.vehicles;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.core.autogen.VehiclePrototypeConfiguration;
import org.movsim.core.autogen.VehiclePrototypes;
import org.movsim.output.fileoutput.FileFundamentalDiagram;
import org.movsim.simulator.roadnetwork.Route;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// singleton property
public final class VehicleFactory {
    private static final Logger LOG = LoggerFactory.getLogger(VehicleFactory.class);
    
    private final Map<String, VehiclePrototype> vehiclePrototypes = new HashMap<>();

    private Map<String, Route> routes;

    private final boolean writeFundamentalDiagrams;

    public VehicleFactory(double simulationTimestep, VehiclePrototypes prototypes) {
        this.writeFundamentalDiagrams = prototypes.isWriteFundDiagrams();
        initialize(simulationTimestep, prototypes.getVehiclePrototypeConfiguration());
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
        
        // TODO
        // final Consumption fuelModel =
        // fuelConsumptionModels.getFuelConsumptionModel(vehParameter.getConsumptionModelName());

        Vehicle vehicle = new Vehicle(prototype.getLabel(), accelerationModel, prototype.getConfiguration(),
                laneChangeModel, route);

        vehicle.setMemory(prototype.createMemoryModel());
        vehicle.setNoise(prototype.createAccNoiseModel());
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
