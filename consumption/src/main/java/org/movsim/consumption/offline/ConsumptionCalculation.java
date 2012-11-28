package org.movsim.consumption.offline;

import java.util.List;

import org.movsim.consumption.model.EnergyFlowModel;

import com.google.common.base.Preconditions;

public class ConsumptionCalculation {

    private final EnergyFlowModel model;

    public ConsumptionCalculation(EnergyFlowModel model) {
        Preconditions.checkNotNull(model);
        this.model = model;
    }

    public void process(List<ConsumptionDataRecord> records) {
        double timestep = 0; // seconds
        ConsumptionDataRecord previous = null;
        for (ConsumptionDataRecord record : records) {
            double[] minFuelFlow = model.getMinFuelFlow(record.getSpeed(), record.getAcceleration(),
                    record.getGrade(), true);
            double fuelFlow = 1000 * minFuelFlow[0];// conversion from m^3/s to liter/s
            record.setConsumptionRate(1000 * minFuelFlow[0]);
            record.setGear((int) minFuelFlow[1]);
            if (previous != null) {
                // set cumulated value from previous step
                timestep = record.getTime() - previous.getTime();
                double cumulated = previous.getCumulatedConsumption() + timestep * fuelFlow;
                record.setCumulatedConsumption(cumulated);
            }
            previous = record;
        }
    }

}
