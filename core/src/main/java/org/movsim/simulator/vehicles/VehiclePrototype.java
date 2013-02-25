package org.movsim.simulator.vehicles;

import org.movsim.consumption.model.EnergyFlowModel;
import org.movsim.core.autogen.VehiclePrototypeConfiguration;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.longitudinalmodel.Memory;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.EquilibriumProperties;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.EquilibriumPropertiesFactory;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelFactory;

import com.google.common.base.Preconditions;

public class VehiclePrototype {

    private final VehiclePrototypeConfiguration configuration;

    private final EnergyFlowModel energyFlowModel; // TODO pooling

    private final EquilibriumProperties equiProperties;

    private final double simulationTimestep;
    
    private final TestVehicle testVehicle;

    // TODO simulation timestep handling
    public VehiclePrototype(double simulationTimestep, VehiclePrototypeConfiguration configuration) {
        Preconditions.checkNotNull(configuration);
        this.configuration = configuration;
        this.simulationTimestep = simulationTimestep; // TODO

        testVehicle = null;
        energyFlowModel = null; // TODO
        
        equiProperties = EquilibriumPropertiesFactory.create(getLength(), createAccelerationModel());

    }

    public double getLength() {
        return configuration.getLength();
    }

    public double getWidth() {
        return configuration.getWidth();
    }

    public String getLabel() {
        return configuration.getLabel();
    }

    public double getMaximumDeceleration() {
        return configuration.getMaximumDeceleration();
    }

    public VehiclePrototypeConfiguration getConfiguration() {
        return configuration;
    }

    public LongitudinalModelBase createAccelerationModel() {
        return LongitudinalModelFactory.create(getLength(), configuration.getAccelerationModelType(),
                simulationTimestep);
    }

    public LaneChangeModel createLaneChangeModel() {
        return configuration.isSetLaneChangeModelType()
                && configuration.getLaneChangeModelType().isSetModelParameterMOBIL() ? new LaneChangeModel(
                configuration.getLaneChangeModelType())
                : null;
    }

    public Noise createAccNoiseModel() {
        return configuration.isSetNoiseParameter() ? new Noise(configuration.getNoiseParameter()) : null;
    }

    public Memory createMemoryModel() {
        return configuration.isSetMemoryParameter() ? new Memory(configuration.getMemoryParameter()) : null;
    }

    public EnergyFlowModel getEnergyFlowModel() {
        return energyFlowModel;
    }

    public EquilibriumProperties getEquiProperties() {
        return equiProperties;
    }

    public double getSimulationTimestep() {
        return simulationTimestep;
    }

    public TestVehicle getTestVehicle() {
        return testVehicle;
    }

}
