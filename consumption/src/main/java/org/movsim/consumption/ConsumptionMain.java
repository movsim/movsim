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
package org.movsim.consumption;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.movsim.consumption.input.ConsumptionCommandLine;
import org.movsim.consumption.input.ConsumptionMetadata;
import org.movsim.consumption.input.xml.ConsumptionInputData;
import org.movsim.consumption.input.xml.ConsumptionXmlReader;
import org.movsim.consumption.input.xml.batch.BatchDataInput;
import org.movsim.consumption.input.xml.model.ConsumptionModelInput;
import org.movsim.consumption.logging.ConsumptionLogger;
import org.movsim.consumption.model.Consumption;
import org.movsim.consumption.offline.ConsumptionCalculation;
import org.movsim.consumption.offline.ConsumptionDataRecord;
import org.movsim.consumption.offline.InputReader;
import org.movsim.consumption.offline.OutputWriter;

import com.google.common.base.Preconditions;


public class ConsumptionMain {

    static final Map<String, Consumption> consumptionModelPool = new HashMap<String, Consumption>();
    static ConsumptionInputData inputData;

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);
        
        System.out.println("Movsim Consumption Model. (c) Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden");
        
        ConsumptionLogger.initializeLogger();
        
        ConsumptionCommandLine.parse(ConsumptionMetadata.getInstance(), args);

        inputData = new ConsumptionInputData();
        ConsumptionXmlReader.parse(ConsumptionMetadata.getInstance(), inputData);
        
        System.out.println("size of batches = " + inputData.getConsumptionInput().getBatchInput().size());
        for (BatchDataInput batch : inputData.getConsumptionInput().getBatchInput()) {
            InputReader reader = InputReader
                    .create(batch, ConsumptionMetadata.getInstance().getPathToConsumptionFile());
            List<ConsumptionDataRecord> records = reader.getRecords();

            Consumption model = getModel(batch.getModelLabel());

            ConsumptionCalculation calculation = new ConsumptionCalculation(model);
            calculation.process(records);

            OutputWriter writer = OutputWriter.create(batch, ConsumptionMetadata.getInstance().getOutputPath());
            writer.write(records);
        }

        System.out.println(inputData.getConsumptionInput().getBatchInput().size() + " batches done.");
        
    }
    
    private static Consumption getModel(String label) {
        System.out.println("request model with key = " + label);
        if (!consumptionModelPool.containsKey(label)) {
            Preconditions.checkArgument(inputData.getConsumptionInput().getConsumptionModelInput().containsKey(label),
                    "cannot find model with label=" + label + " in input.");
            ConsumptionModelInput consumptionModelInput = inputData.getConsumptionInput().getConsumptionModelInput()
                    .get(label);

            consumptionModelPool.put(label, new Consumption(label, consumptionModelInput));
        }
        return consumptionModelPool.get(label);
    }
}
