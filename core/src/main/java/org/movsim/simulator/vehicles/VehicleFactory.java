package org.movsim.simulator.vehicles;

import com.google.common.base.Preconditions;
import org.movsim.autogen.Consumption;
import org.movsim.autogen.PersonalNavigationDeviceType;
import org.movsim.autogen.VehiclePrototypeConfiguration;
import org.movsim.autogen.VehiclePrototypes;
import org.movsim.consumption.model.EnergyFlowModelFactory;
import org.movsim.simulator.observer.ServiceProvider;
import org.movsim.simulator.observer.ServiceProviders;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// singleton
public final class VehicleFactory {
    private static final Logger LOG = LoggerFactory.getLogger(VehicleFactory.class);

    private final Map<String, VehiclePrototype> vehiclePrototypes = new HashMap<>();

    private final Routing routing;

    private final EnergyFlowModelFactory fuelModelFactory = new EnergyFlowModelFactory();

    private final ServiceProviders serviceProviders;

    public VehicleFactory(double simulationTimestep, VehiclePrototypes vehPrototypes, @Nullable Consumption consumption,
            Routing routing, @Nullable ServiceProviders serviceProviders) {
        Preconditions.checkNotNull(vehPrototypes);
        this.routing = Preconditions.checkNotNull(routing);
        this.serviceProviders = serviceProviders;

        if (consumption != null) {
            fuelModelFactory.add(consumption.getConsumptionModels());
        }

        initialize(simulationTimestep, vehPrototypes.getVehiclePrototypeConfiguration(), consumption);
        if (vehPrototypes.isSetWriteFundDiagrams() && vehPrototypes.isWriteFundDiagrams()) {
            writeFundamentalDiagrams(simulationTimestep);
        }
        if (vehPrototypes.isSetWriteAccFunctions() && vehPrototypes.isWriteAccFunctions()) {
            writeAccelerationFunctions(simulationTimestep);
        }
    }

    // set route explicitly, e.g. in microscopic initial or boundary conditions
    public Vehicle create(VehicleType vehicleType, @Nullable Route route) {
        VehiclePrototype prototype = getPrototype(vehicleType.getVehiclePrototypeLabel());
        LongitudinalModelBase accelerationModel = prototype.createAccelerationModel();
        accelerationModel.setRelativeRandomizationV0(vehicleType.getRelativeV0Randomization(),
                vehicleType.getV0DistributionType());
        LaneChangeModel laneChangeModel = prototype.createLaneChangeModel();

        Vehicle vehicle = new Vehicle(prototype.getLabel(), accelerationModel, prototype.getConfiguration(),
                laneChangeModel);

        vehicle.setRoute(route);
        vehicle.setMemory(prototype.createMemoryModel());
        vehicle.setNoise(prototype.createAccNoiseModel());
        vehicle.getEnergyModel().setModel(prototype.getEnergyFlowModel());

        if (prototype.getConfiguration().isSetPersonalNavigationDevice()) {
            // TODO potential conflicts between prescribed route and dynamic routing decisions...
            setServiceProvider(prototype, vehicle);
        }
        return vehicle;
    }

    private void setServiceProvider(VehiclePrototype prototype, Vehicle vehicle) {
        PersonalNavigationDeviceType personalNavigationDevice = prototype.getConfiguration()
                .getPersonalNavigationDevice();
        String providerName = personalNavigationDevice.getServiceProvider();
        ServiceProvider provider = serviceProviders.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("service provider \"" + providerName + "\" for vehicle not configured.");
        }
        vehicle.routingDecisions().setServiceProvider(provider);
        vehicle.routingDecisions().setUncertainty(personalNavigationDevice.getUncertainty());
        vehicle.routingDecisions().setReroutingThreshold(personalNavigationDevice.getReroutingThreshold());
    }

    // route is determined via the traffic composition
    public Vehicle create(VehicleType vehicleType) {
        Route route = null;
        if (vehicleType.hasRouteLabel()) {
            route = routing.get(vehicleType.getRouteLabel());
        }
        return create(vehicleType, route);
    }

    private void initialize(double simulationTimestep, List<VehiclePrototypeConfiguration> configurations,
            @Nullable Consumption consumption) {
        for (VehiclePrototypeConfiguration typeConfig : configurations) {
            if (vehiclePrototypes.containsKey(typeConfig.getLabel())) {
                throw new IllegalArgumentException(
                        "ambiguous vehicle prototype definition: prototype with label=\"" + typeConfig.getLabel()
                                + "\" already exists.");
            }
            VehiclePrototype vehiclePrototype = new VehiclePrototype(simulationTimestep, typeConfig);
            if (typeConfig.isSetConsumptionModelName()) {
                String consumptionModelName = typeConfig.getConsumptionModelName();
                if (!fuelModelFactory.hasModel(consumptionModelName)) {
                    throw new IllegalArgumentException(
                            "cannot find vehicle's consumption model with label=\"" + consumptionModelName);
                }
                vehiclePrototype.setEnergyFlowModel(fuelModelFactory.get(consumptionModelName));
            }

            vehiclePrototypes.put(typeConfig.getLabel(), vehiclePrototype);

        }
    }

    public VehiclePrototype getPrototype(String label) {
        if (!vehiclePrototypes.containsKey(label)) {
            throw new IllegalArgumentException("cannot create vehicle for unknown label =\"" + label);
        }
        return vehiclePrototypes.get(label);
    }

    /**
     * @return immutable
     */
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

    private void writeAccelerationFunctions(double simulationTimestep) {
        final String ignoreLabel = "Obstacle"; // quick hack TODO remove hack
        LOG.info("write acceleration function but ignore label {}.", ignoreLabel);
        for (VehiclePrototype vehiclePrototype : vehiclePrototypes.values()) {
            if (!ignoreLabel.equalsIgnoreCase(vehiclePrototype.getLabel())) {
                FileAccelerationFunctions.writeToFile(simulationTimestep, vehiclePrototype);
            }
        }

    }

}
