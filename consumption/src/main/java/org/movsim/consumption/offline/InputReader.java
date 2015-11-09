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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.movsim.autogen.BatchData;
import org.movsim.io.CsvReaderUtil;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class InputReader {

    private static final Logger LOG = LoggerFactory.getLogger(InputReader.class);

    private final char separator;

    private List<ConsumptionDataRecord> records;

    private final BatchData batchInput;

    public static InputReader create(BatchData batch) {
        File inputFile = FileUtils.lookupFilename(batch.getInputfile());
        LOG.info("inputfile={}", inputFile.getAbsolutePath());
        return new InputReader(inputFile, batch);
    }

    private InputReader(File inputFile, BatchData batch) {
        Preconditions.checkNotNull(inputFile);
        Preconditions.checkNotNull(batch);
        Preconditions.checkArgument(inputFile.exists() && inputFile.isFile(), "file=" + inputFile.getAbsolutePath()
                + " does not exist!");
        this.separator = batch.getSeparator().charAt(0);
        this.batchInput = batch;
        this.records = new LinkedList<ConsumptionDataRecord>();

        process(inputFile);
    }

    public List<ConsumptionDataRecord> getRecords() {
        return records;
    }

    private void process(File inputFile) {
        List<String[]> inputDataLines = CsvReaderUtil.readData(inputFile, separator);

        if (inputDataLines == null || inputDataLines.isEmpty()) {
            LOG.warn("no input read");
            return;
        }

        parseInputData(inputDataLines);

        if (!hasSpeed()) {
            records = calculateSpeeds();
        }
        if (!hasAcceleration()) {
            records = calculateAccelerations();
        }

        // do this after post processing
        addNormalizedTime();
    }

    private boolean hasSpeed() {
        return !records.isEmpty() && records.get(0).hasSpeed();
    }

    private boolean hasAcceleration() {
        return !records.isEmpty() && records.get(0).hasAcceleration();
    }

    private boolean hasPosition() {
        return !records.isEmpty() && records.get(0).hasPosition();
    }

    private void addNormalizedTime() {
        if (!records.isEmpty()) {
            final double startTime = records.get(0).getTime();
            LOG.info("add normalized time with startTime={}", startTime);
            for (ConsumptionDataRecord record : records) {
                record.setNormalizedTime(record.getTime() - startTime);
            }
        }
    }

    private List<ConsumptionDataRecord> calculateSpeeds() {
        Preconditions.checkArgument(hasPosition(), "cannot calculate speeds without positions.");
        LOG.info("calculate speeds numerically.");
        List<ConsumptionDataRecord> newRecords = Lists.newArrayList();
        for (int i = 0, N = records.size() - 1; i <= N; i++) {
            ConsumptionDataRecord record = records.get(i);
            ConsumptionDataRecord recordFwd = records.get(Math.min(i + 1, N));
            ConsumptionDataRecord recordBwd = records.get(Math.max(0, i - 1));
            double speed = calcDerivate(recordFwd.getPosition() - recordBwd.getPosition(), recordFwd.getTime()
                    - recordBwd.getTime());
            newRecords.add(new ConsumptionDataRecord(record.getIndex(), record.getTime(), record.getTimestamp(), record
                    .getPosition(), speed, record.getAcceleration(), record.getGrade()));
        }
        return newRecords;
    }

    private List<ConsumptionDataRecord> calculateAccelerations() {
        Preconditions.checkArgument(hasSpeed(), "cannot calculate accelerations without speeds.");
        LOG.info("calculate accelerations numerically.");
        List<ConsumptionDataRecord> newRecords = Lists.newArrayList();
        for (int i = 0, N = records.size() - 1; i <= N; i++) {
            ConsumptionDataRecord record = records.get(i);
            ConsumptionDataRecord recordFwd = records.get(Math.min(i + 1, N));
            ConsumptionDataRecord recordBwd = records.get(Math.max(0, i - 1));
            double acceleration = calcDerivate(recordFwd.getSpeed() - recordBwd.getSpeed(), recordFwd.getTime()
                    - recordBwd.getTime());
            newRecords.add(new ConsumptionDataRecord(record.getIndex(), record.getTime(), record.getTimestamp(), record
                    .getPosition(), record.getSpeed(), acceleration, record.getGrade()));
        }
        return newRecords;
    }

    private double calcDerivate(double dx, double dy) {
        return (dy == 0) ? Double.NaN : dx / dy;
    }

    private void parseInputData(List<String[]> input) {
        InputDataParser parser = new InputDataParser(batchInput.getColumns(), batchInput.getConversions());
        int index = 0;
        for (String[] line : input) {
            try {
                ConsumptionDataRecord record = parser.parse(index, line);
                records.add(record);
                ++index;
            } catch (NumberFormatException e) {
                LOG.info("cannot parse data. Ignore line={}", Arrays.toString(line));
            } catch (IllegalArgumentException e) {
                LOG.info("cannot parse data. Ignore line={}", Arrays.toString(line));
            }
        }

        LOG.info("parsed={} from={} input lines", records.size(), input.size());
    }


}
