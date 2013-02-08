package org.movsim.consumption.offline;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.movsim.consumption.input.xml.batch.ColumnInput;
import org.movsim.consumption.input.xml.batch.ConversionInput;

public class InputDataParser {

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

    public InputDataParser(ColumnInput columnData, ConversionInput conversionInput) {
        // consider shift from column count to array index
        this.timeColumn = columnData.getTimeColumn() - 1;
        this.speedColumn = columnData.getSpeedColumn() - 1;
        this.accelerationColum = columnData.getAccelerationColumn() - 1;
        this.gradeColumn = columnData.getGradientColumn() - 1;
        this.positionColumn = columnData.getPositionColumn() - 1;

        this.timeInputPattern = conversionInput.getTimeFormat();
        this.speedConversionFactor = conversionInput.getSpeedConversionFactor();
        this.slopeConversionFactor = conversionInput.getGradientConversionFactor();
        this.positionConversionFactor = conversionInput.getPositionConversionFactor();
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
