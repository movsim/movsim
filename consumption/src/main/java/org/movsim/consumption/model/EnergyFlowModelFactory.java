package org.movsim.consumption.model;

import org.movsim.consumption.input.xml.model.ConsumptionModelInput;

public class EnergyFlowModelFactory {

    public static EnergyFlowModel create(String keyLabel, ConsumptionModelInput consumptionModelInput) {
        return new EnergyFlowModelImpl(keyLabel, consumptionModelInput);
    }
}
