package org.movsim.consumption.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.autogen.ConsumptionModel;
import org.movsim.autogen.ConsumptionModels;

import com.google.common.base.Preconditions;

public class EnergyFlowModelFactory {
    
    private final Map<String, EnergyFlowModel> energyFlowModels = new HashMap<>();
    
    public EnergyFlowModelFactory() {
    }

    public EnergyFlowModelFactory(ConsumptionModels models) {
        initialize(models);
    }

    public void add(ConsumptionModels models) {
        initialize(models);
    }

    private void initialize(ConsumptionModels models) {
        Preconditions.checkNotNull(models);
        Preconditions.checkArgument(models.isSetConsumptionModel());
        addModels(models.getConsumptionModel());
    }

    /**
     * Returns a cached {@link EnergyFlowModel} for the given {@code label} name.
     * 
     * @param label
     * @return
     */
    public EnergyFlowModel get(String label) {
        return energyFlowModels.get(label);
    }

    public boolean hasModel(String label) {
        return energyFlowModels.containsKey(label);
    }

    private void addModels(List<ConsumptionModel> models) {
        for (ConsumptionModel model : models) {
            if (energyFlowModels.containsKey(model.getLabel())) {
                throw new IllegalArgumentException("consumption models do not have unique names (labels)!");
            }
            energyFlowModels.put(model.getLabel(), EnergyFlowModels.create(model));
        }
    }

}
