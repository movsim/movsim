package org.movsim.simulator.roadnetwork;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.movsim.autogen.InflowFromFile;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public final class MicroInflowQueue {

    static final Logger LOG = LoggerFactory.getLogger(MicroInflowQueue.class);

    private MicroInflowQueue() {
        // private constructor
    }

    public static class MicroInflowRecord {
        private final double time;
        private final String typeLabel;
        private String route = "";
        private double speed = Double.NaN;
        private int lane = Integer.MAX_VALUE;
        private String comment = null;

        public MicroInflowRecord(double time, String typeLabel) {
            this.time = time;
            this.typeLabel = typeLabel;
        }

        public boolean hasSpeed() {
            return !Double.isNaN(speed);
        }

        public boolean hasLane() {
            return lane != Integer.MAX_VALUE;
        }

        public double getTime() {
            return time;
        }

        public String getTypeLabel() {
            return typeLabel;
        }

        public double getSpeed() {
            return speed;
        }

        public int getLane() {
            return lane;
        }

        public String getComment() {
            return comment;
        }

        public boolean hasComment() {
            return comment != null;
        }

        void setSpeed(double speed) {
            this.speed = speed;
        }

        void setComment(String comment) {
            this.comment = comment;
        }

        void setLane(int lane) {
            this.lane = lane;
        }

        public boolean hasRoute() {
            return route != null && !route.isEmpty();
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            Preconditions.checkArgument(!route.isEmpty());
            this.route = route;
        }

        @Override
        public String toString() {
            return "MicroInflowRecord [time=" + time + ", typeLabel=" + typeLabel + ", route=" + route + ", speed="
                    + speed + ", lane=" + lane + ", comment=" + comment + "]";
        }

    }

    public static List<MicroInflowRecord> readData(InflowFromFile config, int maxLane, long timeOffsetMillis) {
        Preconditions.checkNotNull(config);
        File file = FileUtils.lookupFilename(config.getFilename());
        List<MicroInflowRecord> inflowQueue = new LinkedList<>();
        Preconditions.checkArgument(config.getColumnSeparator().length() == 1,
                "column separator character with length=1 expected but got=" + config.getColumnSeparator());
        List<String[]> inputDataLines = readData(file, config.getColumnSeparator().charAt(0));
        if (inputDataLines == null || inputDataLines.isEmpty()) {
            LOG.warn("no input read from file={}", file.getAbsolutePath());
            return inflowQueue;
        }
        parseInputData(inputDataLines, config, maxLane, timeOffsetMillis, inflowQueue);
        Collections.sort(inflowQueue, new Comparator<MicroInflowRecord>() {
            @Override
            public int compare(MicroInflowRecord o1, MicroInflowRecord o2) {
                final Double time1 = new Double((o1).getTime());
                final Double time2 = new Double((o2).getTime());
                return time1.compareTo(time2); // sort with increasing t
            }
        });

        if (LOG.isDebugEnabled()) {
            LOG.debug("n={} micro inflow data records after parsing and sorting:", inflowQueue.size());
            for (MicroInflowRecord record : inflowQueue) {
                LOG.debug(record.toString());
            }
        }
        return inflowQueue;
    }

    private static void parseInputData(List<String[]> input, InflowFromFile config, int maxLane, long timeOffsetMillis,
            List<MicroInflowRecord> inflowQueue) {
        int maxColumn = determineMaximumColumn(config);
        for (String[] line : input) {
            if (line.length < maxColumn) {
                LOG.info("expected {} columns, cannot parse data. Ignore line={}", maxColumn, Arrays.toString(line));
                continue;
            }
            try {
                MicroInflowRecord record = parse(line, config, maxLane, timeOffsetMillis);
                inflowQueue.add(record);
            } catch (IllegalArgumentException e) {
                LOG.info("cannot parse data or data is invalid. Ignore line={}", Arrays.toString(line));
            }
        }
        LOG.info("parsed successfully {} from {} lines in input file.", inflowQueue.size(), input.size());
    }

    private static int determineMaximumColumn(InflowFromFile config) {
        int maxColumn = config.getColumnTime();
        maxColumn = Math.max(maxColumn, config.getColumnVehicleType());
        if (config.isSetColumnComment()) {
            maxColumn = Math.max(maxColumn, config.getColumnComment());
        }
        if (config.isSetColumnLane()) {
            maxColumn = Math.max(maxColumn, config.getColumnLane());
        }
        if (config.isSetColumnRoute()) {
            maxColumn = Math.max(maxColumn, config.getColumnRoute());
        }
        if (config.isSetColumnSpeed()) {
            maxColumn = Math.max(maxColumn, config.getColumnSpeed());
        }
        return maxColumn;
    }

    private static MicroInflowRecord parse(String[] data, InflowFromFile config, int maxLane, long timeOffsetMillis)
            throws IllegalArgumentException {
        trim(data);
        Preconditions.checkArgument(config.isSetColumnVehicleType() && config.isSetColumnTime());
        double time = convertTimeToSeconds(data[config.getColumnTime() - 1], config.getFormatTime(), timeOffsetMillis);
        if (time < 0) {
            throw new IllegalArgumentException("negative entry time.");
        }
        String typeLabel = data[config.getColumnVehicleType() - 1];
        MicroInflowRecord record = new MicroInflowRecord(time, typeLabel);
        if (config.isSetColumnComment()) {
            record.setComment(data[config.getColumnComment() - 1]);
        }
        if (config.isSetColumnLane()) {
            int lane = Integer.parseInt(data[config.getColumnLane() - 1]);
            if (lane > maxLane) {
                LOG.warn("Parsed lane={} not available on road with maxLane={}. ", lane);
                lane = maxLane;
            }
            if (lane < Lanes.MOST_INNER_LANE) {
                LOG.warn("Parsed lane={} not available on road with minLane={}. ", Lanes.MOST_INNER_LANE);
                lane = Lanes.MOST_INNER_LANE;
            }
            record.setLane(lane);
        }
        if (config.isSetColumnRoute()) {
            record.setRoute(data[config.getColumnRoute() - 1]);
        }
        if (config.isSetColumnSpeed()) {
            double formatFactor = config.isSetFormatSpeed() ? config.getFormatSpeed() : 1;
            record.setSpeed(formatFactor * Double.parseDouble(data[config.getColumnSpeed() - 1]));
        }
        return record;
    }

    private static void trim(String[] data) {
        for (int i = 0, N = data.length; i < N; i++) {
            data[i] = data[i].trim();
        }
    }

    private static double convertTimeToSeconds(String time, String timeInputPattern, long timeOffsetMillis) {
        if (timeInputPattern.isEmpty()) {
            return Double.parseDouble(time);
        }
        DateTime dateTime = LocalDateTime.parse(time, DateTimeFormat.forPattern(timeInputPattern)).toDateTime(
                DateTimeZone.UTC);

        double timeInSeconds = (dateTime.getMillis() - timeOffsetMillis) / 1000L;
        LOG.info("time={} --> dateTime={} --> seconds with offset=" + timeInSeconds, time, dateTime);
        if (timeInSeconds < 0) {
            LOG.warn("negative entry time!");
        }
        return timeInSeconds;
    }

    private static List<String[]> readData(File file, char separator) {
        LOG.info("read data from file={}", file.getAbsolutePath());
        List<String[]> myEntries = Lists.newArrayList();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file), separator);
            myEntries = reader.readAll();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
