package org.movsim.simulator.vehicles;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.autogen.VehiclePrototypeConfiguration;
import org.movsim.autogen.VehiclePrototypes;
import org.movsim.consumption.model.EnergyFlowModel;
import org.movsim.consumption.model.EnergyFlowModelFactory;
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

    private final EnergyFlowModelFactory fuelModelFactory = new EnergyFlowModelFactory();

    public VehicleFactory(double simulationTimestep, VehiclePrototypes vehPrototypes, Map<String, Route> routes) {
        Preconditions.checkNotNull(vehPrototypes);
        Preconditions.checkNotNull(routes);

        this.routes = routes;
        initialize(simulationTimestep, vehPrototypes.getVehiclePrototypeConfiguration());
        // TODO
        // fuelModelFactory.add(models);

        if (vehPrototypes.isSetWriteFundDiagrams() && vehPrototypes.isWriteFundDiagrams()) {
            writeFundamentalDiagrams(simulationTimestep);
        }
    }
    
    public Vehicle create(VehicleType vehicleType) {
        VehiclePrototype prototype = getPrototype(vehicleType.getVehiclePrototypeLabel());
        
        // route
        Route route = null;
        if(vehicleType.hasRouteLabel()){
            route = routes.get(vehicleType.getRouteLabel());
            if (route == null) {
                throw new IllegalArgumentException("route for label=" + vehicleType.getRouteLabel()
                        + " not defined in input!");
            }
        }
        
        // acceleration model
        LongitudinalModelBase accelerationModel = prototype.createAccelerationModel();
        accelerationModel.setRelativeRandomizationV0(vehicleType.getRelativeV0Randomization(),
                vehicleType.getV0DistributionType());
        
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
    
    private void writeFundamentalDiagrams(double simulationTimestep) {
        final String ignoreLabel = "Obstacle"; // quick hack TODO remove hack
        LOG.info("write fundamental diagrams but ignore label {}.", ignoreLabel);
        for (VehiclePrototype vehiclePrototype : vehiclePrototypes.values()) {
            if (!ignoreLabel.equalsIgnoreCase(vehiclePrototype.getLabel())) {
                FileFundamentalDiagram.writeToFile(simulationTimestep, vehiclePrototype);
            }
        }
    }

}
