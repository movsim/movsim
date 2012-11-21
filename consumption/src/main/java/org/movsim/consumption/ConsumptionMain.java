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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.movsim.consumption.input.ConsumptionCommandLine;
import org.movsim.consumption.input.ConsumptionMetadata;
import org.movsim.consumption.input.xml.ConsumptionInputData;
import org.movsim.consumption.input.xml.ConsumptionXmlReader;
import org.movsim.consumption.input.xml.model.ConsumptionModelInput;
import org.movsim.consumption.logging.ConsumptionLogger;
import org.movsim.consumption.model.Consumption;
import org.movsim.consumption.offline.ConsumptionCalculation;
import org.movsim.consumption.offline.ConsumptionDataRecord;
import org.movsim.consumption.offline.InputReader;
import org.movsim.consumption.offline.OutputWriter;


public class ConsumptionMain {

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);
        
        System.out.println("Movsim Consumption Model. (c) Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden");
        
        ConsumptionLogger.initializeLogger();
        
        ConsumptionCommandLine.parse(ConsumptionMetadata.getInstance(), args);

        ConsumptionInputData inputData = new ConsumptionInputData();
        ConsumptionXmlReader.parse(ConsumptionMetadata.getInstance(), inputData);
        
        // create model

        Map<String, ConsumptionModelInput> consumptionModelInput = inputData.getConsumptionInput()
                .getConsumptionModelInput();
        
        for (String s : consumptionModelInput.keySet()) {
            System.out.println("parsed models: key = " + s);
            System.out.println(consumptionModelInput.get(s).toString());
        }

        final String label = "car";
        if (!consumptionModelInput.containsKey(label)) {
            System.err.println("xml does not provide model for label="+label);
            System.exit(-1);
        }


        System.out.println("size of model input=" + consumptionModelInput.size());

        Consumption model = new Consumption(label, consumptionModelInput.get(label));
        
        // QUICK HACK: develop parsing of offline trajectory data
        System.out.println("MovismInput parser ");

        // String filename = "Seesen_GPS.csv";
        String filename = "Torfhaus_GPS.csv";
        File input = new File("/home/kesting/workspace/consumption/data", filename);
        // String outFilename = "Seesen_GPS_modeled.csv";
        String outFilename = "Torfhaus_GPS_modeled.csv";
        File output = new File("/home/kesting/tomtom/consumption/testdaten_harz", outFilename);

        InputReader reader = new InputReader(input);
        List<ConsumptionDataRecord> records = reader.getRecords();
        

        ConsumptionCalculation calculation = new ConsumptionCalculation(model);
        calculation.process(records);

        OutputWriter writer = new OutputWriter(output);
        writer.write(records);

        System.out.println("done.");
        
    }
    
}
