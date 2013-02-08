package org.movsim.consumption.offline;

// http://opencsv.sourceforge.net/#how-to-read
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.movsim.consumption.input.xml.batch.BatchDataInput;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class InputReader {

    private final char separator = ',';

    private List<ConsumptionDataRecord> records;

    private final BatchDataInput batchInput;

    public static InputReader create(BatchDataInput batchInput, String path) {
        File inputFile = new File(path, batchInput.getInputFile());
        System.out.println("read input from " + inputFile.getAbsolutePath());

        return new InputReader(inputFile, batchInput);
    }

    private InputReader(File inputFile, BatchDataInput batchInput) {
        Preconditions.checkNotNull(inputFile);
        Preconditions.checkNotNull(batchInput);
        Preconditions.checkArgument(inputFile.exists() && inputFile.isFile(), "file=" + inputFile.getAbsolutePath()
                + " does not exist!");
        this.batchInput = batchInput;
        records = new LinkedList<ConsumptionDataRecord>();

        process(inputFile);
    }

    public List<ConsumptionDataRecord> getRecords() {
        return records;
    }

    private void process(File inputFile) {
        List<String[]> inputDataLines = readData(inputFile);

        if (inputDataLines == null || inputDataLines.isEmpty()) {
            System.out.println("no input read");
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
            System.out.println("add normalized time with startTime=" + startTime);
            for (ConsumptionDataRecord record : records) {
                record.setNormalizedTime(record.getTime() - startTime);
            }
        }
    }

    private List<ConsumptionDataRecord> calculateSpeeds() {
        Preconditions.checkArgument(hasPosition(), "cannot calculate speeds without positions.");
        System.out.println("calculate speeds numerically.");
        List<ConsumptionDataRecord> newRecords = Lists.newArrayList();
        for (int i = 0, N = records.size() - 1; i <= N; i++) {
            ConsumptionDataRecord record = records.get(i);
            ConsumptionDataRecord recordFwd = records.get(Math.min(i + 1, N));
            ConsumptionDataRecord recordBwd = records.get(Math.max(0, i - 1));
            double speed = calcDerivate(recordFwd.getPosition() - recordBwd.getPosition(),
                    recordFwd.getTime() - recordBwd.getTime());
            newRecords.add(new ConsumptionDataRecord(record.getIndex(), record.getTime(), record.getPosition(), speed,
                    record.getAcceleration(), record.getGrade()));
        }
        return newRecords;
    }

    private List<ConsumptionDataRecord> calculateAccelerations() {
        Preconditions.checkArgument(hasSpeed(), "cannot calculate accelerations without speeds.");
        System.out.println("calculate accelerations numerically.");
        List<ConsumptionDataRecord> newRecords = Lists.newArrayList();
        for (int i = 0, N = records.size() - 1; i <= N; i++) {
            ConsumptionDataRecord record = records.get(i);
            ConsumptionDataRecord recordFwd = records.get(Math.min(i + 1, N));
            ConsumptionDataRecord recordBwd = records.get(Math.max(0, i - 1));
            double acceleration = calcDerivate(recordFwd.getSpeed() - recordBwd.getSpeed(), recordFwd.getTime()
                    - recordBwd.getTime());
            newRecords.add(new ConsumptionDataRecord(record.getIndex(), record.getTime(), record.getPosition(), record
                    .getSpeed(), acceleration, record.getGrade()));
        }
        return newRecords;
    }

    private double calcDerivate(double dx, double dy) {
        return (dy == 0) ? Double.NaN : dx / dy;
    }

    private List<ConsumptionDataRecord> calcAccelerationFromSpeed() {
        System.out.println("no acceleration provided, calculated from speeds.");
        List<ConsumptionDataRecord> newRecords = Lists.newArrayList();
        for (int i = 0, N = records.size() - 1; i <= N; i++) {
            ConsumptionDataRecord recordFwd = records.get(Math.min(i + 1, N));
            ConsumptionDataRecord recordBwd = records.get(Math.max(0, i - 1));
            double speedDiff = recordFwd.getSpeed() - recordBwd.getSpeed();
            double timeDiff = recordFwd.getTime() - recordBwd.getTime();
            ConsumptionDataRecord record = records.get(i);
            double acceleration = speedDiff / timeDiff;
            newRecords.add(new ConsumptionDataRecord(record.getIndex(), record.getTime(), record.getPosition(), record
                    .getSpeed(), acceleration, record.getGrade()));
        }
        return newRecords;
    }


    private void parseInputData(List<String[]> input) {
        InputDataParser parser = new InputDataParser(batchInput.getColumnData(), batchInput.getConversionInput());
        int index = 0;
        for (String[] line : input) {
            try {
                ConsumptionDataRecord record = parser.parse(index, line);
                records.add(record);
                ++index;
            } catch (NumberFormatException e) {
                System.out.println("cannot parse data. Ignore line=" + Arrays.toString(line));
            } catch (IllegalArgumentException e) {
                System.out.println("cannot parse data. Ignore line=" + Arrays.toString(line));
            }
        }

        System.out.println("parsed " + records.size() + "(index=" + index + ")" + " from " + input.size()
                + " input lines");
    }

    private List<String[]> readData(File file) {
        System.out.println("using input file " + file.getAbsolutePath());

        List<String[]> myEntries = Lists.newArrayList();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file), separator);
            myEntries = reader.readAll();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return myEntries;
    }

}
