package org.movsim.simulator.vehicles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.movsim.autogen.TrafficComposition;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class TrafficCompositionGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficCompositionGenerator.class);

    private final org.movsim.autogen.TrafficComposition configuration;

    private final Map<String, VehicleType> vehicleTypes = new HashMap<>();

    private final VehicleFactory vehicleFactory;

    public TrafficCompositionGenerator(TrafficComposition configuration, VehicleFactory vehicleFactory) {
        Preconditions.checkNotNull(configuration);
        this.configuration = configuration;
        this.vehicleFactory = vehicleFactory;
        setUpComposition();
    }

    public Vehicle createVehicle() {
        return vehicleFactory.create(determineVehicleType());
    }

    public Vehicle createVehicle(TestVehicle testVehicle) {
        return vehicleFactory.create(testVehicle.getVehicleType());
    }

    public Vehicle createVehicle(TestVehicle testVehicle, Route route) {
        return vehicleFactory.create(testVehicle.getVehicleType(), route);
    }

    public boolean hasVehicle(String label) {
        return vehicleTypes.containsKey(label);
    }

    public Vehicle createVehicle(String label) {
        if (!vehicleTypes.containsKey(label)) {
            throw new IllegalArgumentException("cannot create vehicle with label=" + label);
        }
        return vehicleFactory.create(vehicleTypes.get(label));
    }

    public TestVehicle getTestVehicle(String label) {
        return vehicleTypes.get(label).getTestVehicle();
    }

    public TestVehicle getTestVehicle() {
        return determineVehicleType().getTestVehicle();
    }

    private VehicleType determineVehicleType() {
        final double randomNumber = MyRandom.nextDouble();
        double sumFraction = 0;
        for (final VehicleType vehicleType : vehicleTypes.values()) {
            sumFraction += vehicleType.getFraction();
            if (sumFraction >= randomNumber) {
                return vehicleType;
            }
        }
        throw new IllegalStateException("no vehicle prototype found for randomNumber=" + randomNumber);
    }

    private void setUpComposition() {
        checkUniqueness();
        normalizeFractions();
        addVehicleTypes();
    }

    private void addVehicleTypes() {
        for (org.movsim.autogen.VehicleType typeConfig : configuration.getVehicleType()) {
            String label = typeConfig.getLabel();
            vehicleTypes.put(label, new VehicleType(typeConfig, vehicleFactory.getPrototype(label)));
        }
    }

    private void normalizeFractions() {
        double sumFractions = 0;
        for (final org.movsim.autogen.VehicleType typeConfig : configuration.getVehicleType()) {
            sumFractions += typeConfig.getFraction();
        }
        Preconditions.checkArgument(sumFractions > 0, "vehicle type fractions sum up to 0. Check configuration.");
        for (org.movsim.autogen.VehicleType typeConfig : configuration.getVehicleType()) {
            typeConfig.setFraction(typeConfig.getFraction() / sumFractions);
        }
    }

    private void checkUniqueness() {
        Set<String> labels = new HashSet<>();
        for (org.movsim.autogen.VehicleType typeConfig : configuration.getVehicleType()) {
            boolean added = labels.add(typeConfig.getLabel());
            if (!added) {
                throw new IllegalArgumentException("ambigous traffic composition input: vehicle type=\""
                        + typeConfig.getLabel() + "\" already exists.");
            }
        }

    }

}
