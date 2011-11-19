package org.movsim.simulator.vehicles;

import java.util.HashMap;
import java.util.Map;

import org.movsim.consumption.FuelConsumption;
import org.movsim.consumption.impl.FuelConsumptionImpl;
import org.movsim.input.model.consumption.ConsumptionModelInput;
import org.movsim.input.model.consumption.FuelConsumptionInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionModeling {

    private static final String DEFAULT_DUMMY_LABEL="none";  // default from dtd
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ConsumptionModeling.class);

    private Map<String, FuelConsumption> fuelModelsMap;

    public ConsumptionModeling(FuelConsumptionInput input) {
        if (input == null) {
            return;
        }

        fuelModelsMap = new HashMap<String, FuelConsumption>();

        if(input.getConsumptionModelInput()==null){
            logger.info("no fuel consumption models defined.");
        }
        else{
            for (Map.Entry<String, ConsumptionModelInput> entries : input.getConsumptionModelInput().entrySet()) {
                final String key = entries.getKey();
                final ConsumptionModelInput consModelInput = entries.getValue();
                logger.info("create fuel consumption model with key={}", key);
                fuelModelsMap.put(key, new FuelConsumptionImpl(consModelInput));
            }
        }
    }

    public FuelConsumption getFuelConsumptionModel(String key) {
        if(key.equals(DEFAULT_DUMMY_LABEL)){
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
