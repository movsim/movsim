package org.movsim.consumption.offline;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.movsim.consumption.autogen.Columns;
import org.movsim.consumption.autogen.Conversions;

public class InputDataParser {

    // <CONVERSION time="HH:mm:ss" speed="0.2777777" gradient="0.01" />
    private final String timeInputPattern; // = "HH:mm:ss"; // 10:23:21 AM
    private final double speedConversionFactor;
    private final double accelerationConversionFactor = 1;
    private final double slopeConversionFactor;
    private final double positionConversionFactor;

    private final int timeColumn;
    private final int speedColumn;
    private final int accelerationColum;
    private final int gradeColumn;
    private final int positionColumn;

    final static int MIN_COLUMNS = 4;

    public InputDataParser(Columns columns, Conversions conversions) {
        // consider shift from column count to array index
        this.timeColumn = columns.getTime() - 1;
        this.speedColumn = columns.getSpeed() - 1;
        this.accelerationColum = columns.getAcceleration() - 1;
        this.gradeColumn = columns.getGradient() - 1;
        this.positionColumn = columns.getPosition() - 1;

        this.timeInputPattern = conversions.getTime();
        this.speedConversionFactor = conversions.getSpeed();
        this.slopeConversionFactor = conversions.getGradient();
        this.positionConversionFactor = conversions.getPosition();
    }

    public ConsumptionDataRecord parse(int index, String[] line) throws NumberFormatException, IllegalArgumentException {
        if (line.length <= MIN_COLUMNS) {
            throw new NumberFormatException();
        }
        // System.out.println("parse = " + Arrays.toString(line));
        double speed = isInputQuantity(speedColumn) ? speedConversionFactor * Double.parseDouble(line[speedColumn])
                : Double.NaN;
        double time = convertToSeconds(line[timeColumn]);

        double acceleration = isInputQuantity(accelerationColum) ? accelerationConversionFactor
                * Double.parseDouble(line[accelerationColum]) : Double.NaN;

        double grade = isInputQuantity(gradeColumn) ? slopeConversionFactor * Double.parseDouble(line[gradeColumn]) : 0;
        double position = isInputQuantity(positionColumn) ? positionConversionFactor
                * Double.parseDouble(line[positionColumn]) : Double.NaN;

        return new ConsumptionDataRecord(index, time, position, speed, acceleration, grade);
    }

    private double convertToSeconds(String time) throws NumberFormatException, IllegalArgumentException {
        if (timeInputPattern.isEmpty()) {
            return Double.parseDouble(time);
        }
        // System.out.println("timeInputPattern=" + timeInputPattern + ", time=" + time);
        DateTime dateTime = DateTime.parse(time, DateTimeFormat.forPattern(timeInputPattern));
        // System.out.println(time + "--> dateTime=" + dateTime);
        return dateTime.getSecondOfDay();
    }

    private boolean isInputQuantity(int column) {
        return column >= 0;
    }

}
