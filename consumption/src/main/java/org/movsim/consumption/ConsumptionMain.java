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
import org.movsim.consumption.model.EnergyFlowModels;
import org.movsim.consumption.offline.ConsumptionCalculation;
import org.movsim.consumption.offline.ConsumptionDataRecord;
import org.movsim.consumption.offline.InputReader;
import org.movsim.consumption.offline.OutputWriter;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class ConsumptionMain {

    static final Map<String, EnergyFlowModel> consumptionModelPool = new HashMap<String, EnergyFlowModel>();

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);

        System.out
                .println("Movsim Energy-Flow Model (Consumption). (c) Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden, see: www.movsim.org");

        ConsumptionLogger.initializeLogger();

        ConsumptionCommandLine.parse(ConsumptionMetadata.getInstance(), args);

        ConsumptionXmlLoader xmlInputLoader = new ConsumptionXmlLoader();
        String consumptionFilename = ConsumptionMetadata.getInstance().getConsumptionFilename();

        MovsimConsumption inputData = null;
        try {
            inputData = xmlInputLoader.validateAndLoadConsumptionInput(new File(consumptionFilename));
        } catch (JAXBException e) {
            System.err.println(e);
        } catch (SAXException e) {
            System.err.println(e);
        }

        if (inputData == null) {
            System.out.println("input not valid. exit.");
            System.exit(-1);
        }

        createConsumptionModels(inputData);

        System.out.println("size of batches = " + inputData.getBatchJobs().getBatchData().size());
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

    private static void createConsumptionModels(MovsimConsumption movsimInput) {
        for (Model modelInput : movsimInput.getConsumptionModels().getModel()) {
            consumptionModelPool.put(modelInput.getLabel(), EnergyFlowModels.newModel(modelInput.getLabel(), modelInput));
        }
    }

}
