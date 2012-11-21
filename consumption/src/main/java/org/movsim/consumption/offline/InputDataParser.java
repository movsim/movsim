package org.movsim.consumption.offline;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.movsim.utilities.Units;

public class InputDataParser {

    private static final String TIME_PATTERN = "HH:mm:ss"; // 10:23:21 AM

    // consider shift from column count to array index
    final int timeColumn = 3 - 1;
    final int speedColumn = 12 - 1; // km/h
    final int accelerationColum = 26 - 1;
    final int gradeColumn = 24 - 1; // in [%]

    final static int MIN_COLUMNS = 4;

    public ConsumptionDataRecord parse(String[] line) throws NumberFormatException {
        if (line.length <= MIN_COLUMNS) {
            throw new NumberFormatException();
        }

        // System.out.println("parse = " + Arrays.toString(line));
        double speed = Units.KMH_TO_MS * Double.parseDouble(line[speedColumn]);
        double acceleration = Double.parseDouble(line[accelerationColum]);
        double grade = Double.parseDouble(line[gradeColumn]);
        grade = convertFromPercentageToRadians(grade);
        double time = convertToSeconds(line[timeColumn]);
        return new ConsumptionDataRecord(time, speed, acceleration, grade);
    }

    private double convertToSeconds(String time) throws NumberFormatException {
        if (TIME_PATTERN.isEmpty()) {
            return Double.parseDouble(time);
        }
        DateTime dateTime = DateTime.parse(time, DateTimeFormat.forPattern(TIME_PATTERN));
        // System.out.println(time + "--> dateTime=" + dateTime);
        return dateTime.getSecondOfDay();
    }

    private static double convertFromPercentageToRadians(double grade) {
        return grade / 100.;
    }
}
