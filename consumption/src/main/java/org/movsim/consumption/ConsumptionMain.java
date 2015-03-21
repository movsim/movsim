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

import org.movsim.autogen.BatchData;
import org.movsim.autogen.Consumption;
import org.movsim.autogen.ConsumptionModel;
import org.movsim.autogen.Movsim;
import org.movsim.consumption.model.EnergyFlowModel;
import org.movsim.consumption.model.EnergyFlowModels;
import org.movsim.consumption.offline.ConsumptionCalculation;
import org.movsim.consumption.offline.ConsumptionDataRecord;
import org.movsim.consumption.offline.InputReader;
import org.movsim.consumption.offline.OutputWriter;
import org.movsim.input.MovsimCommandLine;
import org.movsim.input.ProjectMetaData;
import org.movsim.logging.Logger;
import org.movsim.xml.InputLoader;

import com.google.common.base.Preconditions;

public class ConsumptionMain {

    private static final Map<String, EnergyFlowModel> consumptionModelPool = new HashMap<>();

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);

        System.out
                .println("Movsim Energy-Flow Model (Consumption). (c) Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden, see: www.movsim.org");

        Logger.initializeLogger();

        MovsimCommandLine.parse(args);

        File xmlInputFile = ProjectMetaData.getInstance().getInputFile();
        Movsim inputData = InputLoader.unmarshallMovsim(xmlInputFile);

        if (!inputData.isSetConsumption()) {
            throw new IllegalArgumentException("no consumption element configured in input file");
        }

        createConsumptionModels(inputData.getConsumption());

        System.out.println("size of batches = " + inputData.getConsumption().getBatchJobs().getBatchData().size());
        for (BatchData batch : inputData.getConsumption().getBatchJobs().getBatchData()) {
            InputReader reader = InputReader.create(batch);
            List<ConsumptionDataRecord> records = reader.getRecords();

            EnergyFlowModel model = consumptionModelPool.get(batch.getModel());
            Preconditions.checkNotNull(model, "model not available with name=" + batch.getModel());
            ConsumptionCalculation calculation = new ConsumptionCalculation(model);

            calculation.process(records);

            OutputWriter writer = OutputWriter.create(batch, ProjectMetaData.getInstance().getOutputPath());
            writer.write(records);
        }

        System.out.println(inputData.getConsumption().getBatchJobs().getBatchData().size() + " batches done.");

    }

    private static void createConsumptionModels(Consumption movsimInput) {
        for (ConsumptionModel modelInput : movsimInput.getConsumptionModels().getConsumptionModel()) {
            EnergyFlowModel energyFlowModel = EnergyFlowModels.create(modelInput);
            consumptionModelPool.put(modelInput.getLabel(), energyFlowModel);
        }
    }

}
