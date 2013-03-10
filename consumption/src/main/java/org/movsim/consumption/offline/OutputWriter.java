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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.movsim.autogen.BatchData;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Preconditions;

public class OutputWriter {

    private static final char SEPARATOR_CHARACTER = ',';
    private static final char QUOTE_CHARACTER = CSVWriter.NO_QUOTE_CHARACTER;
    private final File output;

    public static OutputWriter create(BatchData batch, String outputPath) {
        File outputFile = new File(outputPath, batch.getOutputfile());
        return new OutputWriter(outputFile);
    }

    public static OutputWriter create(File outputFile) {
        return new OutputWriter(outputFile);
    }

    private OutputWriter(File output) {
        Preconditions.checkNotNull(output);
        if (output.exists()) {
            System.out.println("overwrites " + output.getAbsolutePath());
        }
        System.out.println("writes output to " + output.getAbsolutePath());
        this.output = output;
    }

    public void write(List<ConsumptionDataRecord> records) {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(output), SEPARATOR_CHARACTER, QUOTE_CHARACTER);
            if (!records.isEmpty()) {
                writer.writeNext(records.get(0).csvHeader(String.valueOf(SEPARATOR_CHARACTER)));
                for (ConsumptionDataRecord record : records) {
                    // feed in your array (or convert your data to an array)
                    writer.writeNext(record.toCsv(String.valueOf(SEPARATOR_CHARACTER)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

    }

}
