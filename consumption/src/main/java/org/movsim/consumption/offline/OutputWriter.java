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

import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Preconditions;

public class OutputWriter {

    private static final Logger LOG = LoggerFactory.getLogger(OutputWriter.class);
    
    private static final char QUOTE_CHARACTER = CSVWriter.NO_QUOTE_CHARACTER;

    private final char separator;
    
    private final File output;
    
    private DateTimeFormatter dateTimeFormatter;

    public OutputWriter(File outputFile, char separator) {
        this.separator = separator;
        this.output = Preconditions.checkNotNull(outputFile);
        if (output.exists()) {
            LOG.info("overwrites {}", output.getAbsolutePath());
        }
        LOG.info("writes output to {}", output.getAbsolutePath());
    }

    public void setTimeFormat(DateTimeFormatter dtFormat) {
        this.dateTimeFormatter = Preconditions.checkNotNull(dtFormat);
    }

    public void write(List<ConsumptionDataRecord> records) {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(output), separator, QUOTE_CHARACTER);
            if (!records.isEmpty()) {
                writer.writeNext(records.get(0).csvHeader(String.valueOf(separator)));
                for (ConsumptionDataRecord record : records) {
                    writer.writeNext(record.toCsv(String.valueOf(separator), dateTimeFormatter));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }

    }

}
