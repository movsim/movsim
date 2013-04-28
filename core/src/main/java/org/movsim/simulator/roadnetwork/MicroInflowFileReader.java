package org.movsim.simulator.roadnetwork;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.movsim.autogen.InflowFromFile;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public final class MicroInflowFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(MicroInflowFileReader.class);

    private final TrafficSourceMicro trafficSource;
    private final InflowFromFile config;
    private final int maxLane;
    private final long timeOffsetMillis;
    private final Routing routing;

    public MicroInflowFileReader(InflowFromFile config, int laneCount, long timeOffsetMillis, Routing routing,
            TrafficSourceMicro trafficSource) {
        this.config = Preconditions.checkNotNull(config);
        this.trafficSource = Preconditions.checkNotNull(trafficSource);
        this.routing = Preconditions.checkNotNull(routing);
        this.maxLane = laneCount;
        this.timeOffsetMillis = timeOffsetMillis;
    }

    public void readData() {
        Preconditions.checkNotNull(config);
        File file = FileUtils.lookupFilename(config.getFilename());
        // List<MicroInflowRecord> inflowQueue = new LinkedList<>();
        Preconditions.checkArgument(config.getColumnSeparator().length() == 1,
                "column separator character with length=1 expected but got=" + config.getColumnSeparator());
        List<String[]> inputDataLines = readData(file, config.getColumnSeparator().charAt(0));
        if (inputDataLines == null || inputDataLines.isEmpty()) {
            LOG.warn("no input read from file={}", file.getAbsolutePath());
            return;
        }
        parseInputData(inputDataLines);
    }

    private void parseInputData(List<String[]> input) {
        int maxColumn = determineMaximumColumn();
        int count = 0;
        for (String[] line : input) {
            if (line.length < maxColumn) {
                LOG.info("expected {} columns, cannot parse data. Ignore line={}", maxColumn, Arrays.toString(line));
                continue;
            }
            MicroInflowRecord record = null;
            try {
                record = parse(line);
            } catch (IllegalArgumentException e) {
                LOG.info("cannot parse data or data is invalid. Ignore line={}", Arrays.toString(line));
            }
            if (record != null) {
                trafficSource.addVehicleToQueue(record.getTime(), createVehicle(record));
                count++;
            }
        }
        LOG.info("parsed successfully {} from {} lines in input file.", count, input.size());
        if (count == 0) {
            LOG.error("no valid lines from {} lines in input file parsed!", input.size());
        }
    }

    private Vehicle createVehicle(MicroInflowRecord record) {
        final Vehicle vehicle = trafficSource.vehGenerator.createVehicle(record.getTypeLabel());
        if (record.hasRouteOrDestination()) {
            Route route = routing.hasRoute(record.getRouteOrDestination()) ? routing
                    .get(record.getRouteOrDestination()) : routing.findRoute(trafficSource.roadSegment.userId(),
                    record.getRouteOrDestination());
            LOG.info("overwrites vehicle's default route by route provided by input file: route={}", route.getName());
            vehicle.setRoute(route);
        }
        if (record.hasComment()) {
            vehicle.setInfoComment(record.getComment());
        }
        if (record.hasLength()) {
            vehicle.setLength(record.getLength());
        }
        if (record.hasWeight()) {
            vehicle.setWeight(record.getWeight());
        }
        if (record.hasLength() || record.hasWeight()) {
            LOG.info("and set individual length or weight: length={}, weight={}", vehicle.getLength(),
                    vehicle.getWeight());
        }
        if (record.hasSpeed()) {
            vehicle.setSpeed(record.getSpeed());
        }
        return vehicle;
    }

    private int determineMaximumColumn() {
        int maxColumn = config.getColumnTime();
        maxColumn = Math.max(maxColumn, config.getColumnVehicleType());
        if (config.isSetColumnComment()) {
            maxColumn = Math.max(maxColumn, config.getColumnComment());
        }
        if (config.isSetColumnLane()) {
            maxColumn = Math.max(maxColumn, config.getColumnLane());
        }
        if (config.isSetColumnRouteOrDestination()) {
            maxColumn = Math.max(maxColumn, config.getColumnRouteOrDestination());
        }
        if (config.isSetColumnSpeed()) {
            maxColumn = Math.max(maxColumn, config.getColumnSpeed());
        }
        if (config.isSetColumnLength()) {
            maxColumn = Math.max(maxColumn, config.getColumnLength());
        }
        if (config.isSetColumnWeight()) {
            maxColumn = Math.max(maxColumn, config.getColumnWeight());
        }
        return maxColumn;
    }

    private MicroInflowRecord parse(String[] dataline) throws IllegalArgumentException {
        trim(dataline);
        Preconditions.checkArgument(config.isSetColumnVehicleType() && config.isSetColumnTime());
        long time = convertTimeToSeconds(dataline[config.getColumnTime() - 1]);
        if (time < 0) {
            throw new IllegalArgumentException("negative entry time.");
        }
        String typeLabel = dataline[config.getColumnVehicleType() - 1];
        MicroInflowRecord record = new MicroInflowRecord(time, typeLabel);
        if (config.isSetColumnComment()) {
            record.setComment(dataline[config.getColumnComment() - 1]);
        }
        if (config.isSetColumnLane()) {
            int lane = Integer.parseInt(dataline[config.getColumnLane() - 1]);
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
        if (config.isSetColumnRouteOrDestination()) {
            record.setRouteOrDestination(dataline[config.getColumnRouteOrDestination() - 1]);
        }
        if (config.isSetColumnSpeed()) {
            double formatFactor = config.isSetFormatSpeed() ? config.getFormatSpeed() : 1;
            record.setSpeed(formatFactor * Double.parseDouble(dataline[config.getColumnSpeed() - 1]));
        }
        if (config.isSetColumnLength()) {
            record.setLength(Double.parseDouble(dataline[config.getColumnLength() - 1]));
        }
        if (config.isSetColumnWeight()) {
            record.setWeight(Double.parseDouble(dataline[config.getColumnWeight() - 1]));
        }
        return record;
    }

    private static void trim(String[] data) {
        for (int i = 0, N = data.length; i < N; i++) {
            data[i] = data[i].trim();
        }
    }

    private long convertTimeToSeconds(String time) {
        String timeInputPattern = config.getFormatTime();
        if (timeInputPattern.isEmpty()) {
            return (long) Double.parseDouble(time);
        }
        DateTime dateTime = LocalDateTime.parse(time, DateTimeFormat.forPattern(timeInputPattern)).toDateTime(
                DateTimeZone.UTC);

        long timeInSeconds = (dateTime.getMillis() - timeOffsetMillis) / 1000L;
        LOG.info("time={} --> dateTime={} --> seconds with offset=" + timeInSeconds, time, dateTime);
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

    private final static class MicroInflowRecord {
        private final long time;
        private final String typeLabel;
        private String route = "";
        private double speed = Double.NaN;
        private int lane = Integer.MAX_VALUE;
        private double length = Double.NaN;
        private double weight = Double.NaN;
        private String comment = null;
        private Vehicle vehicle = null;

        MicroInflowRecord(long time, String typeLabel) {
            this.time = time;
            this.typeLabel = typeLabel;
        }

        boolean hasSpeed() {
            return !Double.isNaN(speed);
        }

        boolean hasLane() {
            return lane != Integer.MAX_VALUE;
        }

        long getTime() {
            return time;
        }

        String getTypeLabel() {
            return typeLabel;
        }

        double getSpeed() {
            return speed;
        }

        int getLane() {
            return lane;
        }

        String getComment() {
            return comment;
        }

        boolean hasComment() {
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

        boolean hasRouteOrDestination() {
            return route != null && !route.isEmpty();
        }

        String getRouteOrDestination() {
            return route;
        }

        void setRouteOrDestination(String route) {
            Preconditions.checkArgument(!route.isEmpty());
            this.route = Preconditions.checkNotNull(route);
        }

        double getLength() {
            return length;
        }

        boolean hasLength() {
            return !Double.isNaN(length);
        }

        void setLength(double length) {
            this.length = length;
        }

        double getWeight() {
            return weight;
        }

        void setWeight(double weight) {
            this.weight = weight;
        }

        boolean hasWeight() {
            return !Double.isNaN(weight);
        }

        Vehicle getVehicle() {
            return vehicle;
        }

        void setVehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
        }

        @Override
        public String toString() {
            return "MicroInflowRecord [time=" + time + ", typeLabel=" + typeLabel + ", route=" + route + ", speed="
                    + speed + ", lane=" + lane + ", length=" + length + ", weight=" + weight + ", comment=" + comment
                    + ", vehicle=" + vehicle + "]";
        }

    }
}
