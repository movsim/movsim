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
package org.movsim.consumption;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.movsim.consumption.autogen.BatchData;
import org.movsim.consumption.autogen.Model;
import org.movsim.consumption.autogen.MovsimConsumption;
import org.movsim.consumption.input.ConsumptionCommandLine;
import org.movsim.consumption.input.ConsumptionMetadata;
import org.movsim.consumption.input.ConsumptionXmlLoader;
import org.movsim.consumption.logging.ConsumptionLogger;
import org.movsim.consumption.model.EnergyFlowModel;
import org.movsim.consumption.model.EnergyFlowModelFactory;
import org.movsim.consumption.offline.ConsumptionCalculation;
import org.movsim.consumption.offline.ConsumptionDataRecord;
import org.movsim.consumption.offline.InputReader;
import org.movsim.consumption.offline.OutputWriter;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class ConsumptionMain {

    static final Map<String, EnergyFlowModel> consumptionModelPool = new HashMap<String, EnergyFlowModel>();

    // static ConsumptionInputData inputData;

    public static void main(String[] args) throws JAXBException, SAXException {

        Locale.setDefault(Locale.US);

        System.out
                .println("Movsim Energy-Flow Model (Consumption). (c) Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden, see: www.movsim.org");

        ConsumptionLogger.initializeLogger();

        ConsumptionCommandLine.parse(ConsumptionMetadata.getInstance(), args);

        // inputData = new ConsumptionInputData();
        // ConsumptionXmlReader.parse(ConsumptionMetadata.getInstance(), inputData);

        ConsumptionXmlLoader xmlInputLoader = new ConsumptionXmlLoader();
        String consumptionFilename = ConsumptionMetadata.getInstance().getConsumptionFilename();
        MovsimConsumption inputData = xmlInputLoader
                .validateAndLoadOpenConsumptionInput(new File(consumptionFilename));

        createConsumptionModels(inputData);

        // System.out.println("size of batches = " + inputData.getConsumptionInput().getBatchInput().size());
        System.out.println("size of batches = " + inputData.getBatchJobs().getBatchData().size());
        // for (BatchDataInput batch : inputData.getConsumptionInput().getBatchInput()) {
        for (BatchData batch : inputData.getBatchJobs().getBatchData()) {
            InputReader reader = InputReader
                    .create(batch, ConsumptionMetadata.getInstance().getPathToConsumptionFile());
            List<ConsumptionDataRecord> records = reader.getRecords();

            EnergyFlowModel model = consumptionModelPool.get(batch.getModel());
            Preconditions.checkNotNull(model, "model not available with name=" + batch.getModel());
            ConsumptionCalculation calculation = new ConsumptionCalculation(model);

            calculation.process(records);

            OutputWriter writer = OutputWriter.create(batch, ConsumptionMetadata.getInstance().getOutputPath());
            writer.write(records);
        }

        System.out.println(inputData.getBatchJobs().getBatchData().size() + " batches done.");

    }

    // private static EnergyFlowModel getModel(String label, Movsim movsim) {
    // System.out.println("request model with key = " + label);
    // if (!consumptionModelPool.containsKey(label)) {
    // Preconditions.checkArgument(movsim.getConsumption().getModel().contains(label),
    // "cannot find model with label=" + label + " in input.");
    // // ConsumptionModelInput consumptionModelInput = inputData.getConsumptionInput().getConsumptionModelInput()
    // // .get(label);
    //
    // consumptionModelPool.put(label, EnergyFlowModelFactory.create(label, consumptionModelInput));
    // }
    // return consumptionModelPool.get(label);
    // }

    private static void createConsumptionModels(MovsimConsumption movsimInput) {
        for (Model modelInput : movsimInput.getConsumptionModels().getModel()) {
//            final ConsumptionModelInput consModel = new ConsumptionModelInput(modelInput);
//            System.out.println("parse=" + consModel + " and add model=" + consModel.getLabel());
//            consumptionModels.put(consModel.getLabel(), consModel);
            consumptionModelPool.put(modelInput.getLabel(),
                    EnergyFlowModelFactory.create(modelInput.getLabel(), modelInput));
        }
    }

}
