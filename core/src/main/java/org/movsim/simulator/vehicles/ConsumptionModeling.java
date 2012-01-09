/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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
package org.movsim.simulator.vehicles;

import java.util.HashMap;
import java.util.Map;

import org.movsim.consumption.FuelConsumption;
import org.movsim.input.model.consumption.ConsumptionModelInput;
import org.movsim.input.model.consumption.FuelConsumptionInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionModeling {

    private static final String DEFAULT_DUMMY_LABEL = "none"; // default from dtd

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ConsumptionModeling.class);

    private Map<String, FuelConsumption> fuelModelsMap;

    public ConsumptionModeling(FuelConsumptionInput input) {
        if (input == null) {
            return;
        }

        fuelModelsMap = new HashMap<String, FuelConsumption>();

        if (input.getConsumptionModelInput() == null) {
            logger.info("no fuel consumption models defined.");
        } else {
            for (final Map.Entry<String, ConsumptionModelInput> entries : input.getConsumptionModelInput().entrySet()) {
                final String key = entries.getKey();
                final ConsumptionModelInput consModelInput = entries.getValue();
                logger.info("create fuel consumption model with key={}", key);
                fuelModelsMap.put(key, new FuelConsumption(consModelInput));
            }
        }
    }

    public FuelConsumption getFuelConsumptionModel(String key) {
        if (key.equals(DEFAULT_DUMMY_LABEL)) {
            logger.debug("no fuel consumption model specified.");
            return null;
        }
        if (!fuelModelsMap.containsKey(key)) {
            logger.error("map does not contain fuel consumption model with key={}. Exit", key);
            System.exit(-1);
        }
        return fuelModelsMap.get(key);
    }
}
