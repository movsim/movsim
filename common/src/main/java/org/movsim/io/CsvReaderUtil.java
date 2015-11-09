package org.movsim.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;

public final class CsvReaderUtil {

    private CsvReaderUtil() {
        throw new IllegalStateException("do not instanciate");
    }

    private static final Logger LOG = LoggerFactory.getLogger(CsvReaderUtil.class);

    public static List<String[]> readData(File file, char separator) {
        LOG.info("using input file={}", file.getAbsolutePath());
        List<String[]> myEntries = Lists.newArrayList();

        // see http://opencsv.sourceforge.net/#how-to-read
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
