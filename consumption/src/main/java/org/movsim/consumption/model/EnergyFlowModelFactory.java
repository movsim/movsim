package org.movsim.consumption.model;

import org.movsim.consumption.autogen.Model;

public class EnergyFlowModelFactory {

    public static EnergyFlowModel create(String keyLabel, Model modelInput) {
        return new EnergyFlowModelImpl(keyLabel, modelInput);
    }
}
