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
import org.movsim.consumption.model.FuelAndGear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ConsumptionCalculation {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConsumptionCalculation.class);

    private final EnergyFlowModel model;

    public ConsumptionCalculation(EnergyFlowModel model) {
        Preconditions.checkNotNull(model);
        this.model = model;
    }

    public void process(List<ConsumptionDataRecord> records) {
        double timestep = 0; // in seconds
        ConsumptionDataRecord previous = null;
        for (ConsumptionDataRecord record : records) {
            FuelAndGear minFuelFlowResult = model.getMinFuelFlow(record.getSpeed(), record.getAcceleration(),
                    record.getGrade(), true);
            double fuelFlowInLiterPerSecond = minFuelFlowResult.getFuelFlowInLiterPerSecond();
            if (fuelFlowInLiterPerSecond > 0.3) {
                fuelFlowInLiterPerSecond = 0;
                LOG.info("!!! Ignore unrealistic consumption, set to 0. Inputdata={}",  record.toString());
            }
            record.setConsumptionRate(fuelFlowInLiterPerSecond);
            record.setGear(minFuelFlowResult.getGear());
            if (previous != null) {
                // set cumulated value from previous step
                timestep = record.getTime() - previous.getTime();
                double cumulated = previous.getCumulatedConsumption() + timestep * fuelFlowInLiterPerSecond;
                record.setCumulatedConsumption(cumulated);
            }
            previous = record;
        }
    }

}
