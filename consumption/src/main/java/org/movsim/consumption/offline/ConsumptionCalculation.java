/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
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
        double timestep = 0; // in seconds
        ConsumptionDataRecord previous = null;
        for (ConsumptionDataRecord record : records) {
            double[] minFuelFlow = model.getMinFuelFlow(record.getSpeed(), record.getAcceleration(), record.getGrade(),
                    true);
            double fuelFlow = 1000 * minFuelFlow[0]; // conversion from m^3/s to liter/s
            if (fuelFlow > 0.3) {
                fuelFlow = 0;
                System.out.println("  !!! Ignore unrealistic consumption, set to 0. Inputdata=" + record.toString());
            }
            record.setConsumptionRate(fuelFlow);
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
