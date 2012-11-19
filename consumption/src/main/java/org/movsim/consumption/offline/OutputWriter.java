package org.movsim.consumption.offline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Preconditions;

public class OutputWriter {

    private static final char SEPARATOR_CHARACTER = ',';
    private static final char QUOTE_CHARACTER = CSVWriter.NO_QUOTE_CHARACTER;
    private final File output;
    
    public OutputWriter(File output) {
        Preconditions.checkNotNull(output);
        if (output.exists()) {
            System.out.println("overwrites " + output.getAbsolutePath());
        }
        this.output = output;
    }

    public void write(List<ConsumptionDataRecord> records) {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(output), SEPARATOR_CHARACTER, QUOTE_CHARACTER);
            writer.writeNext(ConsumptionDataRecord.csvHeader(String.valueOf(SEPARATOR_CHARACTER)));
            for(ConsumptionDataRecord record : records){
                // feed in your array (or convert your data to an array)
                writer.writeNext(record.toCsv(String.valueOf(SEPARATOR_CHARACTER)));
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
